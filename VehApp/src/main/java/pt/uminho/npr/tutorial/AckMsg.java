package pt.uminho.npr.tutorial;

import org.eclipse.mosaic.lib.util.SerializationUtils;
import org.eclipse.mosaic.lib.util.objects.Position;

import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;

import org.eclipse.mosaic.lib.objects.v2x.EncodedPayload;
import org.eclipse.mosaic.lib.objects.v2x.MessageRouting;
import org.eclipse.mosaic.lib.objects.v2x.V2xMessage;

public class AckMsg extends V2xMessage {
    private final String originalSenderId;
    private final long originalTimestamp;

    public AckMsg(
        MessageRouting routing,
        long timestamp,
        String senderId,
        Position senderPosition,
        String originalSenderId,
        long originalTimestamp
    ) {
        

        super(routing);
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

    public String getOriginalSenderId() {
        return originalSenderId;
    }

    public long getOriginalTimestamp() {
        return originalTimestamp;
    }

    @Override
    public String toString() {
        return "AckMsg from " + getSenderName() +
               " for msg from " + originalSenderId +
               " at time " + originalTimestamp;
    }
}
