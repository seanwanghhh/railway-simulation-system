package unsw.trains;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import unsw.utils.Position;

public class Station {
    private String stationId;
    private String type;
    private Position position;

    private int capacity;
    private int trainCount;
    private List<Load> loads = new ArrayList<>();

    public Station(String stationId, String type, double x, double y) {
        this.stationId = stationId;
        this.type = type;
        this.position = new Position(x, y);

        if (type.equals("CentralStation")) {
            capacity = 8;
        } else if (type.equals("CargoStation")) {
            capacity = 4;
        } else if (type.equals("DepotStation")) {
            capacity = 8;
        } else {
            capacity = 2;
        }

        trainCount = 0;
    }

    public String getStationId() {
        return stationId;
    }

    public String getType() {
        return type;
    }

    public Position getPosition() {
        return position;
    }

    public boolean isFull() {
        return trainCount >= capacity;
    }

    public void addTrain() {
        trainCount++;
    }

    public void removeTrain() {
        if (trainCount > 0) {
            trainCount--;
        }
    }

    public void addLoad(Load load) {
        loads.add(load);
    }

    public void removeLoad(Load load) {
        loads.remove(load);
    }

    public List<Load> getLoads() {
        List<Load> copy = new ArrayList<>(loads);
        Collections.sort(copy, Comparator.comparing(Load::getLoadId));
        return copy;
    }

    public boolean canStorePassenger() {
        return type.equals("PassengerStation") || type.equals("CentralStation");
    }

    public boolean canStoreCargo() {
        return type.equals("CargoStation") || type.equals("CentralStation");
    }
}
