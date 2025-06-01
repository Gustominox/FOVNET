package pt.uminho.npr;

import org.eclipse.mosaic.rti.api.Interaction;

public class VehInfoMsgReception extends Interaction {

    private String vehicleId;
    private String vehicleType;
    private String vehicleStatus;

    // Constructor
    public VehInfoMsgReception(long time, String vehicleId, String vehicleType, String vehicleStatus) {
        super(time);
        this.vehicleId = vehicleId;
        this.vehicleType = vehicleType;
        this.vehicleStatus = vehicleStatus;
    }

    // Getters and Setters
    public String getVehicleId() {
        return vehicleId;
    }

    public void setVehicleId(String vehicleId) {
        this.vehicleId = vehicleId;
    }

    public String getVehicleType() {
        return vehicleType;
    }

    public void setVehicleType(String vehicleType) {
        this.vehicleType = vehicleType;
    }

    public String getVehicleStatus() {
        return vehicleStatus;
    }

    public void setVehicleStatus(String vehicleStatus) {
        this.vehicleStatus = vehicleStatus;
    }

    @Override
    public String getTypeId() {
        return "VehInfoMsgReception";
    }

    @Override
    public String toString() {
        return "VehInfoMsgReception { vehicleId=" + vehicleId + ", vehicleType=" + vehicleType + ", vehicleStatus="
                + vehicleStatus + ", time=" + getTime() + " }";
    }

    // Implement compareTo method for comparison based on time
    @Override
    public int compareTo(Interaction other) {
        return Long.compare(this.getTime(), other.getTime());
    }
}
