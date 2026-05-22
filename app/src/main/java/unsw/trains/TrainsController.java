package unsw.trains;

import java.util.ArrayList;
import java.util.List;
import java.util.Collections;

import unsw.exceptions.InvalidRouteException;
import unsw.response.models.*;
import unsw.utils.Position;

/**
 * The controller for the Trains system.
 *
 * The method signatures here are provided for you. Do NOT change the method signatures.
 */
public class TrainsController {
    private List<Station> stations = new ArrayList<>();
    private List<Track> tracks = new ArrayList<>();
    private List<Train> trains = new ArrayList<>();
    private List<Load> loads = new ArrayList<>();

    private Station findStation(String stationId) {
        for (Station station : stations) {
            if (station.getStationId().equals(stationId)) {
                return station;
            }
        }
        return null;
    }

    private Track findTrack(String trackId) {
        for (Track track : tracks) {
            if (track.getTrackId().equals(trackId)) {
                return track;
            }
        }
        return null;
    }

    private boolean hasTrackBetween(String stationA, String stationB) {
        for (Track track : tracks) {
            if (track.connects(stationA, stationB)) {
                return true;
            }
        }
        return false;
    }

    private Train findTrain(String trainId) {
        for (Train train : trains) {
            if (train.getTrainId().equals(trainId)) {
                return train;
            }
        }
        return null;
    }

    public void createStation(String stationId, String type, double x, double y) {
        Station station = new Station(stationId, type, x, y);
        stations.add(station);
    }

    public void createTrack(String trackId, String fromStationId, String toStationId) {
        Track track = new Track(trackId, fromStationId, toStationId);
        tracks.add(track);
    }

    private boolean isLinearRoute(List<String> route) {
        if (route.size() < 2)
            return false;

        for (int i = 0; i < route.size() - 1; i++) {
            if (!hasTrackBetween(route.get(i), route.get(i + 1)))
                return false;
        }

        if (route.size() >= 3 && hasTrackBetween(route.get(0), route.get(route.size() - 1)))
            return false;
        return true;
    }

    private boolean isCyclicalRoute(List<String> route) {
        if (route.size() < 3) {
            return false;
        }

        for (int i = 0; i < route.size() - 1; i++) {
            if (!hasTrackBetween(route.get(i), route.get(i + 1)))
                return false;
        }

        return hasTrackBetween(route.get(0), route.get(route.size() - 1));
    }

    private Track findTrackBetween(String stationA, String stationB) {
        for (Track track : tracks) {
            if (track.connects(stationA, stationB))
                return track;
        }
        return null;
    }

    public void createTrain(String trainId, String type, String stationId, List<String> route)
            throws InvalidRouteException {
        boolean linear = isLinearRoute(route);
        boolean cyclical = isCyclicalRoute(route);

        if (!route.contains(stationId)) {
            throw new InvalidRouteException("Invalid route");
        }

        if (type.equals("PassengerTrain") || type.equals("CargoTrain")) {
            if (!linear) {
                throw new InvalidRouteException("Invalid route");
            }
        }

        if (type.equals("BulletTrain")) {
            if (!linear && !cyclical) {
                throw new InvalidRouteException("Invalid route");
            }
        }

        Station station = findStation(stationId);
        Position p = station.getPosition();

        Train train = new Train(trainId, type, new Position(p.getX(), p.getY()), stationId, route, cyclical);
        trains.add(train);
        station.addTrain();
    }

    public List<String> listStationIds() {
        List<String> ids = new ArrayList<>();
        for (Station station : stations) {
            ids.add(station.getStationId());
        }

        Collections.sort(ids);
        return ids;
    }

    public List<String> listTrackIds() {
        List<String> ids = new ArrayList<>();
        for (Track track : tracks) {
            ids.add(track.getTrackId());
        }

        Collections.sort(ids);
        return ids;
    }

    public List<String> listTrainIds() {
        List<String> ids = new ArrayList<>();
        for (Train train : trains) {
            ids.add(train.getTrainId());
        }

        Collections.sort(ids);
        return ids;
    }

    public TrainInfoResponse getTrainInfo(String trainId) {
        Train train = findTrain(trainId);

        return new TrainInfoResponse(
                train.getTrainId(),
                train.getLocation(),
                train.getType(),
                train.getPosition(),
                makeLoadResponses(train.getLoads()));
    }

    private List<TrainInfoResponse> makeStationTrainResponses(String stationId) {
        List<Train> stationTrains = new ArrayList<>();

        for (Train train : trains) {
            if (train.getLocation().equals(stationId)) {
                stationTrains.add(train);
            }
        }

        Collections.sort(stationTrains, (a, b) -> a.getTrainId().compareTo(b.getTrainId()));

        List<TrainInfoResponse> responses = new ArrayList<>();
        for (Train train : stationTrains) {
            responses.add(new TrainInfoResponse(
                    train.getTrainId(),
                    train.getLocation(),
                    train.getType(),
                    train.getPosition(),
                    makeLoadResponses(train.getLoads())));
        }

        return responses;
    }

    public StationInfoResponse getStationInfo(String stationId) {
        Station station = findStation(stationId);

        return new StationInfoResponse(
                station.getStationId(),
                station.getType(),
                station.getPosition(),
                makeLoadResponses(station.getLoads()),
                makeStationTrainResponses(stationId));
    }

    public TrackInfoResponse getTrackInfo(String trackId) {
        Track track = findTrack(trackId);

        return new TrackInfoResponse(
                track.getTrackId(),
                track.getFromStationId(),
                track.getToStationId(),
                track.getType(),
                track.getDurability());
    }

    private List<LoadInfoResponse> makeLoadResponses(List<Load> loads) {

        List<Load> sortedLoads = new ArrayList<>(loads);

        Collections.sort(sortedLoads, (a, b) -> a.getLoadId().compareTo(b.getLoadId()));

        List<LoadInfoResponse> responses = new ArrayList<>();

        for (Load load : sortedLoads) {
            responses.add(
                    new LoadInfoResponse(
                            load.getLoadId(),
                            load.getType()));
        }

        return responses;
    }

    public void simulate() {
        Collections.sort(trains, (a, b) -> a.getTrainId().compareTo(b.getTrainId()));
        for (Train train : trains) {
            Station currStation = findStation(train.getLocation());
            if (currStation != null) {
                embarkLoads(train, currStation);
            }
        }

        for (Train train : trains) {
            String nextStationId = train.getNextStationId();
            Station nextStation = findStation(nextStationId);
            if (nextStation == null)
                continue;

            Position curr = train.getPosition();
            Position dest = nextStation.getPosition();
            double speed = train.getSpeed();

            if (curr.isInBound(dest, speed)) {
                if (nextStation.isFull())
                    continue;

                Station currStation = findStation(train.getLocation());
                if (currStation != null) {
                    currStation.removeTrain();
                }

                train.setPosition(new Position(dest.getX(), dest.getY()));
                train.setLocation(nextStation.getStationId());
                train.arriveAtStation();
                nextStation.addTrain();

                disembarkLoads(train, nextStation);

            } else {
                Station currStation = findStation(train.getLocation());
                if (currStation != null) {
                    currStation.removeTrain();
                }

                Position newPosition = curr.calculateNewPosition(dest, speed);
                train.setPosition(newPosition);

                Track track = findTrackBetween(train.getCurrentStationId(), nextStationId);
                if (track != null) {
                    train.setLocation(track.getTrackId());
                }
            }
        }

        for (Load load : new ArrayList<>(loads)) {
            load.tick();
            if (load.isExpired()) {
                loads.remove(load);
                for (Station station : stations) {
                    station.removeLoad(load);
                }
                for (Train train : trains) {
                    train.removeLoad(load);
                }
            }
        }
    }

    /**
     * Simulate for the specified number of minutes. You should NOT modify
     * this function.
     */
    public void simulate(int numberOfMinutes) {
        for (int i = 0; i < numberOfMinutes; i++) {
            simulate();
        }
    }

    public void createPassenger(String startStationId, String destStationId, String passengerId) {
        Station start = findStation(startStationId);
        Station dest = findStation(destStationId);

        if (!start.canStorePassenger() || !dest.canStorePassenger()) {
            return;
        }

        Load passenger = new Load(passengerId, "Passenger", 70, destStationId, startStationId);
        loads.add(passenger);
        start.addLoad(passenger);
    }

    public void createCargo(String startStationId, String destStationId, String cargoId, int weight) {
        Station start = findStation(startStationId);
        Station dest = findStation(destStationId);

        if (!start.canStoreCargo() || !dest.canStoreCargo()) {
            return;
        }

        Load cargo = new Load(cargoId, "Cargo", weight, destStationId, startStationId);
        loads.add(cargo);
        start.addLoad(cargo);
    }

    /**
     * helper function that disembarks loads
     * @param train
     * @param station
     */
    private void disembarkLoads(Train train, Station station) {
        List<Load> trainLoads = new ArrayList<>(train.getLoads());

        for (Load load : trainLoads) {
            if (load.getDestination().equals(station.getStationId())) {
                train.removeLoad(load);
                loads.remove(load);
            }
        }
    }

    /**
     * helper function that embarks loads
     * @param train
     * @param station
     */
    private void embarkLoads(Train train, Station station) {
        List<Load> stationLoads = new ArrayList<>(station.getLoads());
        Collections.sort(stationLoads, (a, b) -> a.getLoadId().compareTo(b.getLoadId()));

        for (Load load : stationLoads) {
            if (!train.canEmbark(load)) {
                continue;
            }

            if (!train.willReachDestination(load.getDestination())) {
                continue;
            }
            if (load.isPerishable()) {
                int travelTime = calculateTravelTime(train, load.getDestination());
                if (travelTime > load.getMinsTillPerish()) {
                    continue;
                }
            }
            station.removeLoad(load);
            train.addLoad(load);
            load.setLocation(train.getTrainId());
        }
    }

    public void createPerishableCargo(String startStationId, String destStationId, String cargoId, int weight,
            int minsTillPerish) {
        Station start = findStation(startStationId);
        Station dest = findStation(destStationId);

        if (!start.canStoreCargo() || !dest.canStoreCargo()) {
            return;
        }
        Load cargo = new Load(cargoId, "PerishableCargo", weight, destStationId, startStationId, minsTillPerish);
        loads.add(cargo);
        start.addLoad(cargo);
    }

    private int calculateTravelTime(Train train, String destStationId) {
        int totalTime = 0;
        Position curr = train.getPosition();
        double speed = train.getSpeed();

        List<String> route = train.getRoute();
        int idx = train.getRouteIndex();
        int dir = train.getDirection();
        boolean cyclical = train.isCyclical();

        int maxSteps = route.size() * 2 + 1;
        int steps = 0;

        while (steps < maxSteps) {
            steps++;

            int nextIdx;
            if (cyclical) {
                nextIdx = (idx + 1) % route.size();
            } else {
                nextIdx = idx + dir;
                if (nextIdx < 0 || nextIdx >= route.size())
                    break;
            }

            String nextStationId = route.get(nextIdx);
            Station nextStation = findStation(nextStationId);
            Position nextPos = nextStation.getPosition();

            double distance = curr.distance(nextPos);
            int ticks = (int) Math.ceil(distance / speed);
            totalTime += ticks;

            if (nextStationId.equals(destStationId)) {
                break;
            }

            totalTime += 1;
            curr = nextPos;
            idx = nextIdx;

            if (!cyclical) {
                if (idx == route.size() - 1)
                    dir = -1;
                else if (idx == 0)
                    dir = 1;
            }
        }

        return totalTime;
    }

    public void createTrack(String trackId, String fromStationId, String toStationId, boolean isBreakable) {
        throw new UnsupportedOperationException();
    }

    public void createPassenger(String startStationId, String destStationId, String passengerId, boolean isMechanic) {
        throw new UnsupportedOperationException();
    }
}
