package pt.uminho.npr.tutorial;

import org.eclipse.mosaic.lib.objects.v2x.EncodedPayload;
import org.eclipse.mosaic.lib.objects.v2x.MessageRouting;
import org.eclipse.mosaic.lib.objects.v2x.V2xMessage;
import org.eclipse.mosaic.lib.util.SerializationUtils;
import org.eclipse.mosaic.lib.geo.GeoPoint;

import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;

import javax.annotation.Nonnull;

public class StopMessage extends V2xMessage {

    private final String stopCommand;

    private final EncodedPayload payload;
    private final long timeStamp;
    private final String senderName;
    private final GeoPoint senderPos;
    private final double senderHeading;
    private final double senderSpeed;
    private final int senderLaneId;

   public StopMessage (
        final MessageRouting routing,
        final long time,
        final String name,
        final GeoPoint pos,
        final double heading,
        final double speed,
        final int laneId) {

        super(routing);
        this.timeStamp = time;
        this.senderName = name;
        this.senderPos = pos;
        this.senderHeading = heading;
        this.senderSpeed = speed;
        this.senderLaneId = laneId;
        this.stopCommand = "STOP"; // Fixed stop command

        try (final ByteArrayOutputStream baos = new ByteArrayOutputStream();
             final DataOutputStream dos = new DataOutputStream(baos)) {
            dos.writeLong(timeStamp);
            dos.writeUTF(senderName);
            SerializationUtils.encodeGeoPoint(dos, senderPos);
            dos.writeDouble(senderHeading);
            dos.writeDouble(senderSpeed);
            dos.writeInt(senderLaneId);

            payload = new EncodedPayload(baos.toByteArray(), baos.size());
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
    @Nonnull
    @Override
    public EncodedPayload getPayload() {
        return payload;
    }

    public String getSenderName() {
        return senderName;
    }

    public String getStopCommand() {
        return stopCommand;
    }

    @Override
    public String toString() {
        return "StopMessage from " + senderName + " with command: " + stopCommand;
    }
}
