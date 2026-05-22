package trains;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;
import java.util.concurrent.TimeUnit;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Timeout;

import unsw.response.models.LoadInfoResponse;
import unsw.trains.TrainsController;
import unsw.utils.Position;

@Timeout(value = 5, unit = TimeUnit.SECONDS, threadMode = Timeout.ThreadMode.SEPARATE_THREAD)
public class MyTests {
    @Test
    public void testArriveAtStation() {
        TrainsController controller = new TrainsController();
        controller.createStation("s1", "CentralStation", 0.0, 0.0);
        controller.createStation("s2", "PassengerStation", 2.0, 0.0);
        controller.createTrack("t1-2", "s1", "s2");

        assertDoesNotThrow(() -> {
            controller.createTrain("train1", "PassengerTrain", "s1", List.of("s1", "s2"));
        });

        controller.simulate();

        assertEquals(new Position(2.0, 0.0), controller.getTrainInfo("train1").getPosition());
        assertEquals("s2", controller.getTrainInfo("train1").getLocation());
    }

    @Test
    public void testLinearRouteBounceBack() {
        TrainsController controller = new TrainsController();
        controller.createStation("s1", "CentralStation", 0.0, 0.0);
        controller.createStation("s2", "PassengerStation", 2.0, 0.0);
        controller.createTrack("t1-2", "s1", "s2");

        assertDoesNotThrow(() -> {
            controller.createTrain("train1", "PassengerTrain", "s1", List.of("s1", "s2"));
        });

        controller.simulate();
        controller.simulate();

        assertEquals(new Position(0.0, 0.0), controller.getTrainInfo("train1").getPosition());
        assertEquals("s1", controller.getTrainInfo("train1").getLocation());
    }

    @Test
    public void testPassengerEmbarkAndDisembark() {
        TrainsController controller = new TrainsController();

        controller.createStation("s1", "CentralStation", 0.0, 0.0);
        controller.createStation("s2", "PassengerStation", 2.0, 0.0);
        controller.createTrack("t1", "s1", "s2");

        assertDoesNotThrow(() -> {
            controller.createTrain("train1", "PassengerTrain", "s1", List.of("s1", "s2"));
        });

        controller.createPassenger("s1", "s2", "p1");
        assertEquals(
                List.of(new LoadInfoResponse("p1", "Passenger")),
                controller.getStationInfo("s1").getLoads());

        controller.simulate();

        assertEquals(List.of(), controller.getTrainInfo("train1").getLoads());
        assertEquals(List.of(), controller.getStationInfo("s1").getLoads());
        assertEquals(List.of(), controller.getStationInfo("s2").getLoads());
    }

    @Test
    public void testStationFullBlocksLaterTrain() {
        TrainsController controller = new TrainsController();

        controller.createStation("s1", "CentralStation", 0.0, 0.0);
        controller.createStation("s2", "DepotStation", 2.0, 0.0); // small capacity
        controller.createStation("s3", "CentralStation", 4.0, 0.0);

        controller.createTrack("t1-2", "s1", "s2");
        controller.createTrack("t2-3", "s2", "s3");

        assertDoesNotThrow(() -> {
            controller.createTrain("aTrain", "PassengerTrain", "s1", List.of("s1", "s2"));
            controller.createTrain("bTrain", "PassengerTrain", "s3", List.of("s3", "s2"));
        });

        controller.simulate();

        assertEquals("s2", controller.getTrainInfo("aTrain").getLocation());
    }

    @Test
    public void testPassengerEmbarkStaysOnTrainMidJourney() {
        TrainsController controller = new TrainsController();

        controller.createStation("s1", "CentralStation", 0.0, 0.0);
        controller.createStation("s2", "PassengerStation", 10.0, 0.0);
        controller.createTrack("t1", "s1", "s2");

        assertDoesNotThrow(() -> {
            controller.createTrain("train1", "PassengerTrain", "s1", List.of("s1", "s2"));
        });

        controller.createPassenger("s1", "s2", "p1");

        controller.simulate();

        assertEquals(
                List.of(new LoadInfoResponse("p1", "Passenger")),
                controller.getTrainInfo("train1").getLoads());
        assertEquals(List.of(), controller.getStationInfo("s1").getLoads());
        assertEquals("t1", controller.getTrainInfo("train1").getLocation());
    }

    @Test
    public void testCargoDoesNotEmbarkWrongTrain() {
        TrainsController controller = new TrainsController();

        controller.createStation("s1", "CentralStation", 0.0, 0.0);
        controller.createStation("s2", "CargoStation", 10.0, 0.0);
        controller.createStation("s3", "CargoStation", 20.0, 0.0);

        controller.createTrack("t1", "s1", "s2");
        controller.createTrack("t2", "s2", "s3");

        assertDoesNotThrow(() -> {
            controller.createTrain("cargo1", "CargoTrain", "s1", List.of("s1", "s2"));
        });

        controller.createCargo("s1", "s3", "c1", 500);

        controller.simulate();

        assertEquals(List.of(), controller.getTrainInfo("cargo1").getLoads());
        assertEquals(
                List.of(new LoadInfoResponse("c1", "Cargo")),
                controller.getStationInfo("s1").getLoads());
    }

    @Test
    public void testCargoSlowsTrain() {
        TrainsController controller = new TrainsController();

        controller.createStation("s1", "CentralStation", 0.0, 0.0);
        controller.createStation("s2", "CargoStation", 10.0, 0.0);
        controller.createTrack("t1", "s1", "s2");

        assertDoesNotThrow(() -> {
            controller.createTrain("cargo1", "CargoTrain", "s1", List.of("s1", "s2"));
        });

        controller.createCargo("s1", "s2", "c1", 500);
        controller.simulate();

        assertEquals(new Position(2.85, 0.0), controller.getTrainInfo("cargo1").getPosition());
    }

    @Test
    public void testLoadPriorityLexicographicalOrder() {
        TrainsController controller = new TrainsController();

        controller.createStation("s1", "CentralStation", 0.0, 0.0);
        controller.createStation("s2", "PassengerStation", 10.0, 0.0);
        controller.createTrack("t1", "s1", "s2");

        assertDoesNotThrow(() -> {
            controller.createTrain("abc", "PassengerTrain", "s1", List.of("s1", "s2"));
            controller.createTrain("ABC", "PassengerTrain", "s1", List.of("s1", "s2"));
        });

        controller.createPassenger("s1", "s2", "passenger2");
        controller.createPassenger("s1", "s2", "passenger1");

        controller.simulate();

        assertTrue(
                controller.getTrainInfo("ABC").getLoads()
                        .contains(new LoadInfoResponse("passenger1", "Passenger")));
    }

    @Test
    public void testPerishableCargoExpiresAtStation() {
        TrainsController controller = new TrainsController();

        controller.createStation("s1", "CentralStation", 0.0, 0.0);
        controller.createStation("s2", "CargoStation", 10.0, 0.0);

        controller.createPerishableCargo("s1", "s2", "pc1", 100, 1);

        assertEquals(
                List.of(new LoadInfoResponse("pc1", "PerishableCargo")),
                controller.getStationInfo("s1").getLoads());

        controller.simulate();

        assertEquals(List.of(), controller.getStationInfo("s1").getLoads());
    }

    @Test
    public void testPerishableCargoNotEmbarkedIfCannotArriveInTime() {
        TrainsController controller = new TrainsController();

        controller.createStation("s1", "CentralStation", 0.0, 0.0);
        controller.createStation("s2", "CargoStation", 100.0, 0.0);
        controller.createTrack("t1", "s1", "s2");

        assertDoesNotThrow(() -> {
            controller.createTrain("cargo1", "CargoTrain", "s1", List.of("s1", "s2"));
        });

        controller.createPerishableCargo("s1", "s2", "pc1", 100, 1);

        controller.simulate();

        assertEquals(List.of(), controller.getTrainInfo("cargo1").getLoads());
        assertEquals(List.of(), controller.getStationInfo("s1").getLoads());
    }
}
