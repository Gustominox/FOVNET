package pt.uminho.npr;

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
        // getLog().infoSimTime(this, "processEvent");
        cleanupOldNeighbors();

        // Example: Send a Awareness message every 1 second
        if (getOs().getSimulationTime() % (1 * TIME.SECOND) == 0) {
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

            getOs().changeSpeedWithForcedAcceleration(3.0d, 5d); // not implemented in SUMO
            getLog().infoSimTime(this, "Vehicle is stopping due to received STOP command.");

        } else if (receivedMessage.getMessage() instanceof BeaconMsg) {

            BeaconMsg beaconMsg = (BeaconMsg) receivedMessage.getMessage();
            String senderId = beaconMsg.getSenderName();

            getLog().infoSimTime(this, "Received Beacon message from " + beaconMsg.getSenderName());

            NeighborInfo neighbor = new NeighborInfo(
                    senderId,
                    beaconMsg.getSenderPosition(),
                    0.0,
                    0,
                    0.0,
                    0.0,
                    -1,
                    currentTime);

            getLog().infoSimTime(this, "Adding RSU: " + neighbor.toString() + " To Network Map");

            knownRsuNeighbors.put(senderId, neighbor);

        } else if (receivedMessage.getMessage() instanceof VehInfoMsg) {

            // Receive Info message
            VehInfoMsg fwdMsg = (VehInfoMsg) receivedMessage.getMessage();
            String senderId = fwdMsg.getSenderName();

            // Atualizar vizinhança
            NeighborInfo neighbor = new NeighborInfo(
                    senderId,
                    fwdMsg.getSenderPosition(),
                    fwdMsg.getDistanceToRsu(),
                    fwdMsg.getNumberOfHops(),
                    fwdMsg.getHeading(),
                    fwdMsg.getSpeed(),
                    fwdMsg.getLaneId(),
                    currentTime);

            getLog().infoSimTime(this, "Adding Veh: " + neighbor.toString() + " To Network Map");

            knownVehicleNeighbors.put(senderId, neighbor);
            //
            // Forward Info message if my id is equal to fwrdID of msg
            if (fwdMsg.getForwarderId() == getOs().getId()) {
                getLog().infoSimTime(this, "Forwarding Message ");

                /**
                 * Default values if no RSU info exists,
                 * this means that OBU as no info about Network map
                 */
                String forwarderId = "BROADCAST";

                // Check closest RSU
                Position myPosition = new Position(getOs().getPosition()); // Vehicle's current position
                NeighborInfo receiver = findClosestRsu(myPosition);

                if (receiver != null) {
                    forwarderId = receiver.getId();
                } else { // search for neiborh Closest to RSU
                    receiver = findClosestNeighborToRsu();
                    if (receiver != null) {
                        forwarderId = receiver.getId();
                    }
                }
                // change the Id of forwarder to next best neighbor (be RSU or Vehicle)

                MessageRouting routing = getOs().getAdHocModule()
                        .createMessageRouting()
                        .channel(AdHocChannel.CCH) // Send on Control Channel
                        .topological().broadcast()
                        .build(); // Broadcast to nearby vehicles

                // Create the VehInfoMsg that is a copy of the message received, only changing
                // the forwarder Id
                VehInfoMsg message = new VehInfoMsg(
                        routing,
                        fwdMsg.getMessageId(),
                        fwdMsg.getTime(),
                        fwdMsg.getSenderName(),
                        fwdMsg.getSenderPosition(),
                        fwdMsg.getHeading(),
                        fwdMsg.getSpeed(),
                        fwdMsg.getLaneId(),
                        fwdMsg.getDistanceToRsu(),
                        fwdMsg.getNumberOfHops(),
                        forwarderId);

                getOs().getAdHocModule().sendV2xMessage(message);
                getLog().infoSimTime(this, "Sent VehInfoMsg: " + message.toString());

                // TODO
                /**
                 * If msgId is Broadcast, the sender doesnt know about any RSU's
                 * I should Apply the forwarding logic here
                 * 1. try to find RSU
                 * 2. if not find veh with closest
                 * 3. if not BROADCAST
                 */
            } else if (fwdMsg.getForwarderId() == "BROADCAST") {

            }

        }

    }

    private void cleanupOldNeighbors() {
        long currentTime = getOs().getSimulationTime();
        long threshold = 10 * TIME.SECOND;

        knownVehicleNeighbors.values().removeIf(n -> (currentTime - n.lastSeen) > threshold);
        knownRsuNeighbors.values().removeIf(n -> (currentTime - n.lastSeen) > threshold);
    }

    @Override
    public void onMessageTransmitted(V2xMessageTransmission arg0) {
        getLog().infoSimTime(this, "onMessageTransmitted");
    }

    @Override
    public void onVehicleUpdated(@Nullable VehicleData previousVehicleData, @Nonnull VehicleData updatedVehicleData) {
        getLog().infoSimTime(this, "onVehicleUpdated");

        this.vehHeading = updatedVehicleData.getHeading().doubleValue();
        double speedKmh = updatedVehicleData.getSpeed() * 3.6;
        this.vehSpeed = Double.parseDouble(String.format("%.2f", speedKmh));
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
                .channel(AdHocChannel.CCH) // Send on Control Channel
                .topological().broadcast()
                .build();
        long time = getOs().getSimulationTime();

        StopMessage message = new StopMessage(routing, time, getOs().getId(), getOs().getPosition(), this.vehHeading,
                this.vehSpeed, this.vehLane);

        getOs().getAdHocModule().sendV2xMessage(message);
        getLog().infoSimTime(this, "Sent StopMessage: " + message.toString());
    }

    private void sendVehInfoMsg() {
        MessageRouting routing = getOs().getAdHocModule()
                .createMessageRouting()
                .channel(AdHocChannel.CCH) // Send on Control Channel
                .topological().broadcast()
                .build();

        long time = getOs().getSimulationTime();
        String carId = getOs().getId();
        String messageId = carId + "_" + msgIdCounter++;

        /**
         * Default values if no RSU info exists,
         * this would mean that OBU as no info about Network map
         */

        double distanceToRsu = Double.MAX_VALUE;
        int numberOfHops = -1;
        String forwarderId = "BROADCAST";

        // Check closest RSU
        Position myPosition = new Position(getOs().getPosition()); // Vehicle's current position
        NeighborInfo receiver = findClosestRsu(myPosition);

        if (receiver != null) {
            distanceToRsu = myPosition.distanceTo(receiver.position); // Calculate distance to the closest RSU
            numberOfHops = 0;
            forwarderId = receiver.getId();
        } else { // search for neiborh Closest to RSU
            receiver = findClosestNeighborToRsu();
            if (receiver != null) {
                double distanceToVeh = myPosition.distanceTo(receiver.position); // Calculate distance to the vehicle
                distanceToRsu = distanceToVeh + receiver.getDistanceToRsu();
                numberOfHops = receiver.getNumberOfHops() + 1;
                forwarderId = receiver.getId();
            }
        }

        // Create the VehInfoMsg with the closest RSU details
        VehInfoMsg message = new VehInfoMsg(
                routing,
                messageId,
                time,
                carId,
                new Position(getOs().getPosition()),
                this.vehHeading,
                this.vehSpeed,
                this.vehLane,
                distanceToRsu,
                numberOfHops,
                forwarderId);

        getOs().getAdHocModule().sendV2xMessage(message);
        getLog().infoSimTime(this, "Sent VehInfoMsg: " + message.toString());
    }

    private NeighborInfo findClosestRsu(Position myPosition) {
        if (knownRsuNeighbors.isEmpty()) {
            getLog().infoSimTime(this, "No RSUs known.");
            return null;
        }

        NeighborInfo closestRsu = null;
        double minDistance = Double.MAX_VALUE;

        for (NeighborInfo rsu : knownRsuNeighbors.values()) {
            double distance = myPosition.distanceTo(rsu.position);
            if (distance < minDistance) {
                minDistance = distance;
                closestRsu = rsu;
            }
        }
        return closestRsu;
    }

    private NeighborInfo findClosestNeighborToRsu() {
        // If there are no neighbors, return null
        if (knownVehicleNeighbors.isEmpty()) {
            getLog().infoSimTime(this, "No Vehicles known.");
            return null;
        }

        NeighborInfo closestNeighbor = null;
        double minDistance = Double.MAX_VALUE; // Initially set to maximum possible value
        int minHops = Integer.MAX_VALUE; // Initially set to maximum possible value

        // Loop through all neighbors
        for (NeighborInfo neighbor : knownVehicleNeighbors.values()) {
            // Check if the neighbor is closer and has fewer hops
            if (neighbor.distanceToRsu < minDistance ||
                    (neighbor.distanceToRsu == minDistance && neighbor.numberOfHops < minHops)) {
                minDistance = neighbor.distanceToRsu;
                minHops = neighbor.numberOfHops;
                if (minHops < 0)
                    closestNeighbor = null; // If the closest neihbor doesnt know of any RSU then chose no neighbor
                else
                    closestNeighbor = neighbor; // Update the closest neighbor
            }
        }

        return closestNeighbor; // Return the closest neighbor with fewer hops
    }

}
