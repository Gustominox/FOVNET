package pt.uminho.npr.TP;

/**
 * Hello world!
 *
 */
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

public class VehApp extends AbstractApplication<VehicleOperatingSystem> implements VehicleApplication, CommunicationApplication
{
    private final long MsgDelay = 200 * TIME.MILLI_SECOND;
    private final int Power = 50;
    private final double Distance = 140.0;

    private int setVal;

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
        setVal = 0;
        getOs().getEventManager().addEvent(getOs().getSimulationTime() + MsgDelay, this);
    }

    @Override
    public void processEvent(Event arg0) throws Exception {
        getLog().infoSimTime(this, "processEvent");
        if(setVal == 1)
            sendVehInfoMsg();
            sendMessageToRSU();
        getOs().getEventManager().addEvent(getOs().getSimulationTime() + MsgDelay, this);
    }

    @Override
    public void onMessageReceived(ReceivedV2xMessage receivedMsg) {
        if (receivedMsg.getMessage() instanceof VehInfoMsg) {
            VehInfoMsg msg = (VehInfoMsg) receivedMsg.getMessage();
            getLog().infoSimTime(this, "Received Message" + msg.toString());

        }
    }

    @Override
    public void onMessageTransmitted(V2xMessageTransmission transmission) {
        String logEntry = String.format(
        "Msg transmitida | Tipo: %s | ID: %s | Tamanho: %d bytes",
        transmission.getMessage().getClass().getSimpleName(),
        transmission.getMessage().toString().substring(0, 8),
        transmission.getMessage().getPayload().getActualLength()
    );
    getLog().infoSimTime(this, logEntry);


    }

    @Override
    public void onVehicleUpdated(@Nullable VehicleData previousVehicleData, @Nonnull VehicleData updatedVehicleData) {            
        getLog().infoSimTime(this, "onVehicleUpdated");
        if(setVal == 0)
            setVal = 1;
        this.vehHeading = updatedVehicleData.getHeading().doubleValue();
        this.vehSpeed = updatedVehicleData.getSpeed();
        this.vehLane = updatedVehicleData.getRoadPosition().getLaneIndex();
    }

    @Override
    public void onAcknowledgementReceived(ReceivedAcknowledgement ack) {
        getLog().infoSimTime(this, "onAcknowledgementReceived");
    }

    private void sendMessageToRSU() {
        // Definir o roteamento da mensagem como Unicast para o RSU mais próximo
        
        MessageRouting routing = getOs().getAdHocModule()
                                       .createMessageRouting()
                                       .viaChannel(AdHocChannel.CCH) //Canal de comunicação
                                       .topoBroadCast();         

        long time = getOs().getSimulationTime();  // Tempo da simulação
    
        VehInfoMsg message = new VehInfoMsg(routing, time, getOs().getId(), getOs().getPosition(), this.vehHeading, this.vehSpeed, this.vehLane);
    
        getOs().getAdHocModule().sendV2xMessage(message);
        getLog().infoSimTime(this, "Sent VehInfoMsg to RSU: " + message.toString());
    }

    @Override
    public void onCamBuilding(CamBuilder arg0) {
        getLog().infoSimTime(this, "onCamBuilding");
    }

    private void sendVehInfoMsg(){
        MessageRouting routing = getOs().getAdHocModule().createMessageRouting().viaChannel(AdHocChannel.CCH).topoBroadCast();
        long time = getOs().getSimulationTime();
        VehInfoMsg message = new VehInfoMsg(routing, time, getOs().getId(), getOs().getPosition(), this.vehHeading, this.vehSpeed, this.vehLane);
        getOs().getAdHocModule().sendV2xMessage(message);
        getLog().infoSimTime(this, "Sent VehInfoMsg: " + message.toString());
    }

}
