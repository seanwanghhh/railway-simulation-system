package unsw.trains;

public class Load {
    private String loadId;
    private String type;
    private int weight;
    private String destination;
    private String location;
    private int minsTillPerish;
    private boolean perishable;

    // normal cargo
    public Load(String loadId, String type, int weight, String destination, String location) {
        this.loadId = loadId;
        this.type = type;
        this.weight = weight;
        this.destination = destination;
        this.location = location;
        this.perishable = false;
        this.minsTillPerish = -1;
    }

    // perishable cargo
    public Load(String loadId, String type, int weight, String destination, String startStation, int minsTillPerish) {
        this.loadId = loadId;
        this.type = type;
        this.weight = weight;
        this.destination = destination;
        this.location = startStation;
        this.perishable = true;
        this.minsTillPerish = minsTillPerish;
    }

    public boolean isPerishable() {
        return perishable;
    }

    public boolean isExpired() {
        return perishable && minsTillPerish <= 0;
    }

    public void tick() {
        if (perishable)
            minsTillPerish--;
    }

    public int getMinsTillPerish() {
        return minsTillPerish;
    }

    public String getLoadId() {
        return loadId;
    }

    public String getType() {
        return type;
    }

    public int getWeight() {
        return weight;
    }

    public String getDestination() {
        return destination;
    }

    public String getLocation() {
        return location;
    }

    public void setLocation(String location) {
        this.location = location;
    }

    public boolean isPassengerType() {
        return type.equals("Passenger") || type.equals("Mechanic");
    }

    public boolean isCargoType() {
        return type.equals("Cargo") || type.equals("PerishableCargo");
    }

}
