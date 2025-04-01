package pt.uminho.npr.tutorial;

import org.eclipse.mosaic.fed.application.ambassador.simulation.communication.AdHocModuleConfiguration;
import org.eclipse.mosaic.fed.application.ambassador.simulation.communication.CamBuilder;
import org.eclipse.mosaic.fed.application.ambassador.simulation.communication.ReceivedAcknowledgement;
import org.eclipse.mosaic.fed.application.ambassador.simulation.communication.ReceivedV2xMessage;
import org.eclipse.mosaic.fed.application.app.AbstractApplication;
import org.eclipse.mosaic.fed.application.app.api.CommunicationApplication;
import org.eclipse.mosaic.fed.application.app.api.VehicleApplication;
import org.eclipse.mosaic.fed.application.app.api.os.VehicleOperatingSystem;
import org.eclipse.mosaic.interactions.communication.V2xMessageTransmission;
import org.eclipse.mosaic.lib.enums.AdHocChannel;
import org.eclipse.mosaic.lib.objects.v2x.MessageRouting;
import org.eclipse.mosaic.lib.objects.vehicle.VehicleData;
import org.eclipse.mosaic.lib.util.scheduling.Event;
import org.eclipse.mosaic.rti.TIME;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.HashSet;

public class VehApp extends AbstractApplication<VehicleOperatingSystem>
        implements VehicleApplication, CommunicationApplication {
    private final long MsgDelay = 200 * TIME.MILLI_SECOND;
    private final int Power = 50;
    private final double Distance = 140.0;

    private final Map<String, NeighborInfo> knownVehicleNeighbors = new HashMap<>();
    private final Map<String, NeighborInfo> knownRsuNeighbors = new HashMap<>();

    private final Set<String> receivedForwardMsgIds = new HashSet<>();
    private final Map<String, ForwardMsg> pendingForwardMessages = new HashMap<>();

    private int msgIdCounter = 0;
    private double vehHeading;
    private double vehSpeed;
    private int vehLane;

    @Override
    public void onShutdown() {
        getLog().infoSimTime(this, "onShutdown");
        getOs().getAdHocModule().disable();
    }

    @Override
    public void onStartup() {
        getOs().getAdHocModule().enable(new AdHocModuleConfiguration()
                .addRadio()
                .channel(AdHocChannel.CCH)
                .power(Power)
                .distance(Distance)
                .create());

        getLog().infoSimTime(this, "onStartup: Set up");
        getOs().getEventManager().addEvent(getOs().getSimulationTime() + MsgDelay, this);
    }

    @Override
    public void processEvent(Event arg0) throws Exception {
        getLog().infoSimTime(this, "processEvent");
        cleanupOldNeighbors();

        // Example: Send a Awareness message every 5 seconds
        if (getOs().getSimulationTime() % (5 * TIME.SECOND) == 0) {
            sendVehInfoMsg();
        }

        getOs().getEventManager().addEvent(getOs().getSimulationTime() + MsgDelay, this);

    }

    @Override
    public void onMessageReceived(ReceivedV2xMessage receivedMessage) {
        getLog().infoSimTime(this, "onMessageReceived");

        long currentTime = getOs().getSimulationTime();

        if (receivedMessage.getMessage() instanceof StopMessage) {

            StopMessage stopMsg = (StopMessage) receivedMessage.getMessage();
            getLog().infoSimTime(this, "Received STOP message from " + stopMsg.getSenderName());

            getOs().changeSpeedWithForcedAcceleration(3.0d, 5d);
            getLog().infoSimTime(this, "Vehicle is stopping due to received STOP command.");

            // UPDATE MESSAGE
        } else if (receivedMessage.getMessage() instanceof ForwardMsg) {
            ForwardMsg fwdMsg = (ForwardMsg) receivedMessage.getMessage();
            String senderId = fwdMsg.getSenderName();
            String msgKey = fwdMsg.getMessageId();

            if (!receivedForwardMsgIds.contains(msgKey)) {
                receivedForwardMsgIds.add(msgKey);
                pendingForwardMessages.put(msgKey, fwdMsg);
                getLog().infoSimTime(this, "Stored ForwardMsg from " + senderId);
            }

            // Atualizar vizinhança
            NeighborInfo neighbor = new NeighborInfo(
                    senderId,
                    fwdMsg.getSenderPosition(),
                    fwdMsg.getHeading(),
                    fwdMsg.getSpeed(),
                    fwdMsg.getLaneId(),
                    currentTime);

            if (senderId.startsWith("rsu")) {
                knownRsuNeighbors.put(senderId, neighbor);
            } else {
                knownVehicleNeighbors.put(senderId, neighbor);
            }
        } else if (receivedMessage.getMessage() instanceof AckMsg) {
            AckMsg ackMsg = (AckMsg) receivedMessage.getMessage();
            String key = ackMsg.getMessageId();

            if (pendingForwardMessages.containsKey(key)) {
                pendingForwardMessages.remove(key);
                getLog().infoSimTime(this,
                        "Received AckMsg — removed message with key " + key + " from pendingForwardMessages");
            }
        }

    }

    private void cleanupOldNeighbors() {
        long currentTime = getOs().getSimulationTime();
        long threshold = 10 * TIME.SECOND;

        knownVehicleNeighbors.values().removeIf(n -> (currentTime - n.lastSeen) > threshold);
        knownRsuNeighbors.values().removeIf(n -> (currentTime - n.lastSeen) > threshold);
    }

    private void tryForwardToNearestRsu() {
        if (knownRsuNeighbors.isEmpty()) {
            getLog().infoSimTime(this, "No RSUs in range to forward messages.");
            return;
        }

        // Determinar RSU mais próximo
        Position myPos = new Position(getOs().getPosition());
        NeighborInfo closestRsu = null;
        double minDistance = Double.MAX_VALUE;

        for (NeighborInfo rsu : knownRsuNeighbors.values()) {
            double distance = myPos.distanceTo(rsu.position);
            if (distance < minDistance) {
                minDistance = distance;
                closestRsu = rsu;
            }
        }

        if (closestRsu == null)
            return;

        // Enviar todas as mensagens pendentes para o RSU mais próximo
        for (ForwardMsg originalMsg : pendingForwardMessages.values()) {

            MessageRouting routing = getOs().getAdHocModule()
                    .createMessageRouting()
                    .viaChannel(AdHocChannel.CCH)
                    .topoBroadCast();

            ForwardMsg msgCopy = new ForwardMsg(
                    routing,
                    originalMsg.getMessageId(), // ID original
                    getOs().getSimulationTime(),
                    originalMsg.getSenderName(), // remetente original
                    originalMsg.getSenderPosition(), // posição original
                    originalMsg.getHeading(),
                    originalMsg.getSpeed(),
                    originalMsg.getLaneId());

            getOs().getAdHocModule().sendV2xMessage(msgCopy);
            getLog().infoSimTime(this, "Forwarded message " + msgCopy.getMessageId() +
                    " to RSU: " + closestRsu.id);
        }

    }

    @Override
    public void onMessageTransmitted(V2xMessageTransmission arg0) {
        getLog().infoSimTime(this, "onMessageTransmitted");
    }

    @Override
    public void onVehicleUpdated(@Nullable VehicleData previousVehicleData, @Nonnull VehicleData updatedVehicleData) {
        getLog().infoSimTime(this, "onVehicleUpdated");

        this.vehHeading = updatedVehicleData.getHeading().doubleValue();
        this.vehSpeed = updatedVehicleData.getSpeed();
        this.vehLane = updatedVehicleData.getRoadPosition().getLaneIndex();
    }

    @Override
    public void onAcknowledgementReceived(ReceivedAcknowledgement arg0) {
        getLog().infoSimTime(this, "onAcknowledgementReceived");

    }

    @Override
    public void onCamBuilding(CamBuilder arg0) {
        getLog().infoSimTime(this, "onCamBuilding");
    }

    private void sendStopMessage() {
        MessageRouting routing = getOs().getAdHocModule()
                .createMessageRouting()
                .viaChannel(AdHocChannel.CCH) // Send on Control Channel
                .topoBroadCast(); // Broadcast to nearby vehicles

        long time = getOs().getSimulationTime();

        StopMessage message = new StopMessage(routing, time, getOs().getId(), getOs().getPosition(), this.vehHeading,
                this.vehSpeed, this.vehLane);

        getOs().getAdHocModule().sendV2xMessage(message);
        getLog().infoSimTime(this, "Sent StopMessage: " + message.toString());
    }

    private void sendVehInfoMsg() {
        MessageRouting routing = getOs().getAdHocModule()
                .createMessageRouting()
                .viaChannel(AdHocChannel.CCH)
                .topoBroadCast();

        long time = getOs().getSimulationTime();
        String carId = getOs().getId();
        String messageId = carId + "_" + msgIdCounter++;

        ForwardMsg message = new ForwardMsg(
                routing,
                messageId,
                time,
                carId,
                new Position(getOs().getPosition()),
                this.vehHeading,
                this.vehSpeed,
                this.vehLane);

        getOs().getAdHocModule().sendV2xMessage(message);
        getLog().infoSimTime(this, "Sent VehInfoMsg: " + message.toString());
    }

}
