package pt.uminho.npr.tutorial;

import org.eclipse.mosaic.lib.util.SerializationUtils;
import org.eclipse.mosaic.lib.util.objects.Position;

import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;

import javax.annotation.Nonnull;

import org.eclipse.mosaic.lib.geo.GeoPoint;
import org.eclipse.mosaic.lib.objects.v2x.EncodedPayload;
import org.eclipse.mosaic.lib.objects.v2x.MessageRouting;
import org.eclipse.mosaic.lib.objects.v2x.V2xMessage;

public class AckMsg extends V2xMessage {

    private final EncodedPayload payload;

    private final String messageId;

    private final long timeStamp;
    private final String senderName;
    private final GeoPoint senderPos;
    private final double senderHeading;
    private final double senderSpeed;
    private final int senderLaneId;

    public AckMsg(
            final MessageRouting routing,
            final String messageId,
            final long time,
            final String name,
            final GeoPoint pos,
            final double heading,
            final double speed,
            final int laneId) {

        super(routing);
        this.messageId = messageId;
        this.timeStamp = time;
        this.senderName = name;
        this.senderPos = pos;
        this.senderHeading = heading;
        this.senderSpeed = speed;
        this.senderLaneId = laneId;
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

    public String getMessageId() {
        return messageId;
    }

    @Override
    public String toString() {
        return "AckMsg for message: " + getMessageId();
    }
}
