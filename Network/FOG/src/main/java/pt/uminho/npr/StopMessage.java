package pt.uminho.npr;

import org.eclipse.mosaic.lib.objects.v2x.EncodedPayload;
import org.eclipse.mosaic.lib.objects.v2x.MessageRouting;
import org.eclipse.mosaic.lib.objects.v2x.V2xMessage;

import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;

import javax.annotation.Nonnull;

public class StopMessage extends V2xMessage {

    private final String stopCommand;

    private final EncodedPayload payload;
    private final long timeStamp;
    private final String senderName;

    public StopMessage(
            final MessageRouting routing,
            final long time,
            final String name) {

        super(routing);
        this.timeStamp = time;
        this.senderName = name;
        this.stopCommand = "STOP"; // Fixed stop command

        try (final ByteArrayOutputStream baos = new ByteArrayOutputStream();
                final DataOutputStream dos = new DataOutputStream(baos)) {
            dos.writeLong(timeStamp);
            dos.writeUTF(senderName);

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
