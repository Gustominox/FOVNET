package pt.uminho.npr.tutorial;

import org.eclipse.mosaic.lib.util.objects.Position;

public class NeighborInfo {
    public String id;
    public Position position;
    public double heading;
    public double speed;
    public int lane;
    public long lastSeen;

    public NeighborInfo(String id, Position position, double heading, double speed, int lane, long lastSeen) {
        this.id = id;
        this.position = position;
        this.heading = heading;
        this.speed = speed;
        this.lane = lane;
        this.lastSeen = lastSeen;
    }
}
