package pt.uminho.npr;

import org.eclipse.mosaic.fed.application.app.api.CommunicationApplication;
import org.eclipse.mosaic.fed.application.app.AbstractApplication;
import org.eclipse.mosaic.fed.application.ambassador.simulation.communication.ReceivedV2xMessage;
import org.eclipse.mosaic.fed.application.ambassador.simulation.communication.ReceivedAcknowledgement;
import org.eclipse.mosaic.fed.application.ambassador.simulation.communication.CamBuilder;
import org.eclipse.mosaic.interactions.communication.V2xMessageTransmission;
import org.eclipse.mosaic.lib.enums.AdHocChannel;
import org.eclipse.mosaic.fed.application.ambassador.simulation.communication.AdHocModuleConfiguration;
import org.eclipse.mosaic.lib.util.scheduling.Event;
import org.eclipse.mosaic.fed.application.app.api.os.RoadSideUnitOperatingSystem;
import org.eclipse.mosaic.rti.TIME;
import org.eclipse.mosaic.lib.geo.GeoPoint;
import org.eclipse.mosaic.lib.objects.v2x.MessageRouting;
import org.eclipse.mosaic.lib.objects.v2x.V2xMessage;

import pt.uminho.npr.VehInfoMessage;

import java.util.concurrent.CopyOnWriteArrayList;
import java.util.List;

public class RSUApp extends AbstractApplication<RoadSideUnitOperatingSystem>
        implements CommunicationApplication

{
    private final long MsgDelay = 200 * TIME.MILLI_SECOND;
    private final int Power = 50;
    private final double Distance = 140.0;

    @Override
    public void onStartup() {
        getOs().getAdHocModule().enable(new AdHocModuleConfiguration()
                .addRadio()
                .channel(AdHocChannel.CCH)
                .power(Power)
                .distance(Distance)
                .create());

        getOs().getCellModule().enable();

        getLog().infoSimTime(this, "onStartup: Set up");
        getOs().getEventManager().addEvent(getOs().getSimulationTime() + MsgDelay, this);
    }

    @Override
    public void processEvent(Event arg0) throws Exception {
        getLog().infoSimTime(this, "processEvent");

        // Send a Beacon message every 1 second
        if (getOs().getSimulationTime() % (1 * TIME.SECOND) == 0) {
            sendBeaconMsg();
            getLog().infoSimTime(this, "Sent Beacon Message: ");

        }

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

    private boolean isFogMessage(V2xMessage msg) {
        return msg instanceof SlowMessage; // || msg instanceof FastMessage;
    }

    private boolean isNetworkMessage(V2xMessage msg) {
        return msg instanceof VehInfoMessage; // || msg instanceof FastMessage;
    }

    @Override
    public void onMessageReceived(ReceivedV2xMessage receivedMsg) {
        if (isNetworkMessage(receivedMsg.getMessage())) {

            Message msg = (Message) receivedMsg.getMessage();

            getLog().infoSimTime(this, "Received msg: " + msg.toString());

            MessageRouting routing = getOs().getCellModule().createMessageRouting()
                    .tcp()
                    .destination("server_0")
                    .topological()
                    .build();

            getOs().getCellModule().sendV2xMessage((V2xMessage) msg.clone(routing));

        } else if (isFogMessage(receivedMsg.getMessage())) {

            Message msg = (Message) receivedMsg.getMessage();

            getLog().infoSimTime(this, "Received msg: " + msg.toString());

            MessageRouting routing = getOs().getAdHocModule()
                    .createMessageRouting()
                    .channel(AdHocChannel.CCH)
                    .topological().broadcast()
                    .build();

            getOs().getAdHocModule().sendV2xMessage((V2xMessage) msg.clone(routing));

        } else {
            Message msg = (Message) receivedMsg.getMessage();

            getLog().infoSimTime(this, "Undefined behavior for " + msg.toString());

        }
    }

    private void sendBeaconMsg() {
        MessageRouting routing = getOs().getAdHocModule()
                .createMessageRouting()
                .channel(AdHocChannel.CCH)
                .topological().broadcast()
                .build();

        long time = getOs().getSimulationTime();
        String rsuId = getOs().getId();

        BeaconMsg message = new BeaconMsg(
                routing,
                time,
                rsuId,
                new Position(getOs().getPosition()));

        getOs().getAdHocModule().sendV2xMessage(message);
        getLog().infoSimTime(this, "Sent BeaconMsg " + message.toString());
    }

}