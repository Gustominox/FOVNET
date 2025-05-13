package pt.uminho.npr;

import org.eclipse.mosaic.lib.objects.v2x.EncodedPayload;
import org.eclipse.mosaic.lib.objects.v2x.MessageRouting;
import org.eclipse.mosaic.lib.objects.v2x.V2xMessage;

import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;

import javax.annotation.Nonnull;

public class SlowMessage extends FogMessage {

    private final EncodedPayload payload;
    private final long timeStamp;
    private final String senderName;
    private final String receiverName;
    private final float targetSpeed;

    public SlowMessage(
            final MessageRouting routing,
            final Mode mode,
            final long time,
            final String senderName,
            final String receiverName,
            final float targetSpeed) {

        super(routing, mode);
        this.timeStamp = time;
        this.senderName = senderName;
        this.receiverName = receiverName;
        this.targetSpeed = targetSpeed;

        try (final ByteArrayOutputStream baos = new ByteArrayOutputStream();
                final DataOutputStream dos = new DataOutputStream(baos)) {
            dos.writeLong(timeStamp);
            dos.writeUTF(senderName);
            dos.writeUTF(receiverName);
            dos.writeFloat(targetSpeed);

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

    public String getReceiverName() {
        return receiverName;
    }

    public Long getTime() {
        return timeStamp;
    }

    /**
     * 
     * @return targetSpeed in Km/h
     */
    public float getTargetSpeed() {
        return targetSpeed;
    }

    public SlowMessage clone(final MessageRouting routing) {
        return new SlowMessage(
                routing,
                super.getMode(),
                timeStamp,
                senderName,
                receiverName,
                targetSpeed);
    }

    @Override
    public String toString() {
        return "SlowMessage from " + senderName;
    }
}
