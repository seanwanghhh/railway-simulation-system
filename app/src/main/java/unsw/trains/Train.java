package unsw.trains;

import java.util.*;
import unsw.utils.Position;

public class Train {
    private String trainId;
    private String type;
    private Position position;
    private String location;
    private List<String> route;

    private int routeIndex;
    private int direction;
    private boolean cyclical;
    private List<Load> loads = new ArrayList<>();

    public Train(String trainId, String type, Position position, String location, List<String> route,
            boolean cyclical) {
        this.trainId = trainId;
        this.type = type;
        this.position = position;
        this.location = location;
        this.route = new ArrayList<>(route);
        this.routeIndex = route.indexOf(location);
        this.direction = 1;
        this.cyclical = cyclical;
    }

    public String getTrainId() {
        return trainId;
    }

    public String getType() {
        return type;
    }

    public Position getPosition() {
        return position;
    }

    public String getLocation() {
        return location;
    }

    public List<String> getRoute() {
        return route;
    }

    public void setPosition(Position position) {
        this.position = position;
    }

    public void setLocation(String location) {
        this.location = location;
    }

    public int getCapacity() {
        if (type.equals("PassengerTrain")) {
            return 3500;
        }
        if (type.equals("CargoTrain")) {
            return 5000;
        }
        if (type.equals("BulletTrain")) {
            return 5000;
        }
        return 0;
    }

    public int getCurrentWeight() {
        int total = 0;
        for (Load load : loads) {
            total += load.getWeight();
        }
        return total;
    }

    public double getOriginalSpeed() {
        if (type.equals("PassengerTrain")) {
            return 2.0;
        }
        if (type.equals("CargoTrain")) {
            return 3.0;
        }
        if (type.equals("BulletTrain")) {
            return 5.0;
        }
        return 0.0;
    }

    public double getSpeed() {
        double speed = getOriginalSpeed();

        if (!type.equals("CargoTrain") && !type.equals("BulletTrain")) {
            return speed;
        }
        int totalCargoWeight = 0;
        for (Load load : loads) {
            if (load.getType().equals("Cargo") || load.getType().equals("PerishableCargo")) {
                totalCargoWeight += load.getWeight();
            }
        }

        double reduction = totalCargoWeight * 0.01;
        return speed * (100.0 - reduction) / 100.0;
    }

    public String getNextStationId() {
        if (cyclical) {
            int nextIndex = (routeIndex + 1) % route.size();
            return route.get(nextIndex);
        }
        return route.get(routeIndex + direction);
    }

    public void arriveAtStation() {
        if (cyclical) {
            routeIndex = (routeIndex + 1) % route.size();
            return;
        }

        if (routeIndex == route.size() - 1) {
            direction = -1;
        } else if (routeIndex == 0) {
            direction = 1;
        }

        routeIndex += direction;

        if (routeIndex == route.size() - 1) {
            direction = -1;
        } else if (routeIndex == 0) {
            direction = 1;
        }
    }

    public String getCurrentStationId() {
        return route.get(routeIndex);
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

    public boolean canCarryPassenger() {
        return type.equals("PassengerTrain") || type.equals("BulletTrain");
    }

    public boolean canCarryCargo() {
        return type.equals("CargoTrain") || type.equals("BulletTrain");
    }

    public boolean canEmbark(Load load) {
        if (load.isPassengerType() && !canCarryPassenger()) {
            return false;
        }
        if (load.isCargoType() && !canCarryCargo()) {
            return false;
        }
        return getCurrentWeight() + load.getWeight() <= getCapacity();
    }

    public boolean willReachDestination(String dest) {
        return route.contains(dest);
    }

    public int getDirection() {
        return direction;
    }

    public int getRouteIndex() {
        return routeIndex;
    }

    public boolean isCyclical() {
        return cyclical;
    }
}
