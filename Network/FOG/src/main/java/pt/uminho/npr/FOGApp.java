package pt.uminho.npr;

import org.eclipse.mosaic.fed.application.ambassador.simulation.communication.CamBuilder;
import org.eclipse.mosaic.fed.application.ambassador.simulation.communication.ReceivedAcknowledgement;
import org.eclipse.mosaic.fed.application.ambassador.simulation.communication.ReceivedV2xMessage;
import org.eclipse.mosaic.fed.application.app.AbstractApplication;
import org.eclipse.mosaic.fed.application.app.api.CommunicationApplication;
import org.eclipse.mosaic.fed.application.app.api.os.ServerOperatingSystem;
import org.eclipse.mosaic.interactions.communication.V2xMessageTransmission;
import org.eclipse.mosaic.lib.objects.road.IConnection;
import org.eclipse.mosaic.lib.objects.road.INode;
import org.eclipse.mosaic.lib.objects.v2x.GenericV2xMessage;
import org.eclipse.mosaic.lib.objects.v2x.MessageRouting;
import org.eclipse.mosaic.lib.util.scheduling.Event;
import org.eclipse.mosaic.lib.util.scheduling.EventProcessor;
import org.eclipse.mosaic.rti.TIME;

import java.util.List;
import java.util.stream.Collectors;

public class FOGApp extends AbstractApplication<ServerOperatingSystem> implements CommunicationApplication {

    private final long MsgDelay = 200 * TIME.MILLI_SECOND;
    private final int Power = 50;
    private final double Distance = 140.0;

    @Override
    public void onStartup() {
        getOs().getCellModule().enable();

        getLog().infoSimTime(this, "Setup FOG server {} at time {}", getOs().getId(), getOs().getSimulationTime());

        // getOs().getEventManager().addEvent(getOs().getSimulationTime() + MsgDelay,
        // this);
    }

    @Override
    public void processEvent(Event arg0) throws Exception {
        getLog().infoSimTime(this, "processEvent");

        getOs().getEventManager().addEvent(getOs().getSimulationTime() + MsgDelay, this);
    }

    @Override
    public void onShutdown() {
        getLog().infoSimTime(this, "Shutdown application");
    }

    @Override
    public void onMessageTransmitted(V2xMessageTransmission v2xMessageTransmission) {

    }

    @Override
    public void onAcknowledgementReceived(ReceivedAcknowledgement acknowledgement) {

    }

    @Override
    public void onCamBuilding(CamBuilder camBuilder) {
    }

    @Override
    public void onMessageReceived(ReceivedV2xMessage receivedMsg) {
        if (receivedMsg.getMessage() instanceof VehInfoMessage) {
            VehInfoMessage msg = (VehInfoMessage) receivedMsg.getMessage();
            getLog().infoSimTime(this, "Received msg: " + msg.toString());

            // TODO : need to have a sense of old messages, if a message is received
            // that has an older timestamp than a message i already have from that vehicle
            // need to treat it differently, ignore it for instance
            //
            // maybe mensagens com um delay superior a x ignorar, tipo aging factor

            // // IConnection connection = getOs().getRoutingModule()
            // // .getClosestRoadPosition(msg.getSenderPosition().toGeoPoint())
            // // .getConnection();

            // // List<String> ids = connection.getOutgoingConnections()
            // // .stream()
            // // .map(IConnection::getId)
            // // .collect(Collectors.toList());
            // getLog().infoSimTime(this, "Connection Id: " + connection.getId());
            // getLog().infoSimTime(this, "Outgoing Connections: " + ids);
            // getLog().infoSimTime(this, "Incoming Connections: " +
            // connection.getIncomingConnections());

            double speedLimit = 40;
            if (msg.getSpeed() > speedLimit + 5) {// 5km de respiro
                Mode mode;
                if (msg.getNumberOfHops() == 0)
                    mode = Mode.DIRECT;
                else
                    mode = Mode.SEARCH;

                sendSlowMessage(msg.getFwrdId(), mode, msg.getSenderName(), (float) speedLimit);
            }

        }
    }

    private void sendStopMessage(String rsu, Mode mode, String destination) {
        long time = getOs().getSimulationTime();

        MessageRouting routing = getOs().getCellModule().createMessageRouting()
                .tcp()
                .destination(rsu)
                .topological()
                .build();

        StopMessage message = new StopMessage(routing, mode, time, getOs().getId(), "BROADCAST", destination);

        getOs().getCellModule().sendV2xMessage(message);

        getLog().infoSimTime(this, "Sent StopMessage: " + message.toString());
    }

    private void sendSlowMessage(String rsu, Mode mode, String destination, float targetSpeed) {
        long time = getOs().getSimulationTime();

        MessageRouting routing = getOs().getCellModule().createMessageRouting()
                .tcp()
                .destination(rsu)
                .topological()
                .build();

        SlowMessage message = new SlowMessage(routing, mode, time, getOs().getId(), "BROADCAST", destination,
                targetSpeed);

        getOs().getCellModule().sendV2xMessage(message);

        getLog().infoSimTime(this, "Sent SlowMessage: " + message.toString());
    }

}