package pt.uminho.npr.tutorial;

import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;

import javax.annotation.Nonnull;

import org.eclipse.mosaic.lib.geo.GeoPoint;
import org.eclipse.mosaic.lib.objects.v2x.EncodedPayload;
import org.eclipse.mosaic.lib.objects.v2x.MessageRouting;
import org.eclipse.mosaic.lib.objects.v2x.V2xMessage;
import org.eclipse.mosaic.lib.util.SerializationUtils;

/*
 * timestamp
 * sendeID
 * sender position
 * sender heading
 * sender speed
 * sender lane
 */

public class ForwardMsg extends V2xMessage {

    private final EncodedPayload payload;
    private final String messageId;
    private final long timeStamp;
    private final String senderName;
    private final Position senderPos;
    private final double senderHeading;
    private final double senderSpeed;
    private final int senderLaneId;

    public ForwardMsg(
            final MessageRouting routing,
            final String messageId,
            final long time,
            final String name,
            final Position pos,
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
            dos.writeUTF(messageId);
            dos.writeLong(timeStamp);
            dos.writeUTF(senderName);
            senderPos.encode(dos); // substitui o encodeGeoPoint
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

    public long getTime() {
        return timeStamp;
    }

    public String getSenderName() {
        return senderName;
    }

    public Position getSenderPosition() {
        return new Position(senderPos);
    }

    public double getHeading() {
        return senderHeading;
    }

    public double getSpeed() {
        return senderSpeed;
    }

    public int getLaneId() {
        return senderLaneId;
    }

    @Override
    public String toString() {
        return "VehInfoMessage{" +
                "timeStamp=" + timeStamp +
                ", senderName=" + senderName +
                ", senderPosition=" + senderPos +
                ", senderHeading=" + senderHeading +
                ", senderSpeed=" + senderSpeed +
                ", senderLaneId=" + senderLaneId + '}';
    }
}
