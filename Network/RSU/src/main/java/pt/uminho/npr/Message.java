package pt.uminho.npr;

import org.eclipse.mosaic.lib.objects.v2x.MessageRouting;

public interface Message {
    Message clone(final MessageRouting routing);
}
