package pt.uminho.npr;

public class NeighborInfo {
    public String id;
    public Position position;
    final double distanceToRsu;
    final int numberOfHops;
    public double heading;
    public double speed;
    public int lane;
    public long lastSeen;

    public NeighborInfo(String id, Position position, double distanceToRsu, int numberOfHops, double heading,
            double speed, int lane, long lastSeen) {
        this.id = id;
        this.position = position;
        this.distanceToRsu = distanceToRsu;
        this.numberOfHops = numberOfHops;
        this.heading = heading;
        this.speed = speed;
        this.lane = lane;
        this.lastSeen = lastSeen;
    }

    public String getId() {
        return id;
    }

    public double getDistanceToRsu() {
        return distanceToRsu;
    }

    public int getNumberOfHops() {
        return numberOfHops;
    }
}
