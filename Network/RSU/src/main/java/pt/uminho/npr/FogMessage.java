package pt.uminho.npr;

import org.eclipse.mosaic.lib.objects.v2x.MessageRouting;
import org.eclipse.mosaic.lib.objects.v2x.V2xMessage;

public abstract class FogMessage extends V2xMessage implements Message {
    // The mode field can now be defined as an instance variable
    private Mode mode;

    // Constructor to set the mode
    public FogMessage(final MessageRouting routing, Mode mode) {
        super(routing);
        this.mode = mode;
    }

    // Getter method for mode
    public Mode getMode() {
        return mode;
    }

    // Setter method for mode
    public void setMode(Mode mode) {
        this.mode = mode;
    }

}
