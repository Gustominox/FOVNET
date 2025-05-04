package pt.uminho.npr;

import org.eclipse.mosaic.lib.objects.v2x.EncodedPayload;
import org.eclipse.mosaic.lib.objects.v2x.MessageRouting;
import org.eclipse.mosaic.lib.objects.v2x.V2xMessage;
import org.eclipse.mosaic.lib.util.SerializationUtils;
import org.eclipse.mosaic.lib.geo.GeoPoint;

import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;

import javax.annotation.Nonnull;

public class StopMessage extends V2xMessage implements Message {

    private final String stopCommand;

    private final EncodedPayload payload;
    private final long timeStamp;
    private final String senderName;
    private final String receiverName;

    public StopMessage(
            final MessageRouting routing,
            final long time,
            final String senderName,
            final String receiverName) {

        super(routing);
        this.timeStamp = time;
        this.senderName = senderName;
        this.receiverName = receiverName;
        this.stopCommand = "STOP"; // Fixed stop command

        try (final ByteArrayOutputStream baos = new ByteArrayOutputStream();
                final DataOutputStream dos = new DataOutputStream(baos)) {
            dos.writeLong(timeStamp);
            dos.writeUTF(senderName);
            dos.writeUTF(receiverName);

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

    public StopMessage clone(final MessageRouting routing) {
        return new StopMessage(
                routing,
                timeStamp,
                senderName,
                receiverName);
    }

    @Override
    public String toString() {
        return "StopMessage from " + senderName + " with command: " + stopCommand;
    }
}
