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
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.List;

public class RSUApp extends AbstractApplication<RoadSideUnitOperatingSystem>
        implements CommunicationApplication

{

    private static final double COMMUNICATION_RANGE = 140.0; // 140 metros
    private final List<VehInfoMsg> receivedMessages = new CopyOnWriteArrayList<>();
    private final static AdHocChannel vehSendChannel = AdHocChannel.SCH1;
    private final long MsgDelay = 100 * TIME.MILLI_SECOND;

    @Override
    public void onStartup() {
        getLog().infoSimTime(this, "Initialize application");
        getOs().getAdHocModule().enable(new AdHocModuleConfiguration()
                .addRadio()
                .channel(vehSendChannel)
                .power(50)
                .create());

        getLog().infoSimTime(this, "Activated WLAN Module");
        // sample();
    }

    @Override
    public void onShutdown() {
        getLog().infoSimTime(this, "Shutdown application");

    }

    @Override
    public void processEvent(Event event) throws Exception {
        getOs().getEventManager().addEvent(
                getOs().getSimulationTime() + MsgDelay, this);
        getLog().infoSimTime(this, "Sending out AdHoc broadcast");
        // sendBroadcast();
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
        if (receivedMsg.getMessage() instanceof VehInfoMsg) {
            VehInfoMsg msg = (VehInfoMsg) receivedMsg.getMessage();

            // Verifica distância usando posição da RSU (implementação real usaria GPS)
            Position RsuPos = new Position(getOs().getPosition());
            Position MsgPos = msg.getSenderPosition();
            double distance = MsgPos.distanceTo(RsuPos);

            if (distance <= COMMUNICATION_RANGE) {
                receivedMessages.add(msg);
                getLog().info("Mensagem recebida de " + msg.getSenderName() +
                        " a " + String.format("%.2f", distance) + " metros");

            } else {
                getLog().warn("Veículo fora de alcance: " +
                        String.format("%.2f", distance) + "m (ID: " +
                        msg.getSenderName() + ")");
            }
        }
    }

    private double calculateDistance(GeoPoint a, GeoPoint b) {
        return Math.sqrt(Math.pow(a.getAltitude() - b.getLatitude(), 2) +
                Math.pow(a.getLongitude() - b.getLongitude(), 2));
    }

}