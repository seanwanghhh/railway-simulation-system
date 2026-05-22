package unsw.trains;

import unsw.utils.TrackType;

public class Track {
    private String trackId;
    private String fromStationId;
    private String toStationId;
    private TrackType type;
    private int durability;

    public Track(String trackId, String fromStationId, String toStationId) {
        this.trackId = trackId;
        this.fromStationId = fromStationId;
        this.toStationId = toStationId;
        this.type = TrackType.NORMAL;
        this.durability = 10;
    }

    public String getTrackId() {
        return trackId;
    }

    public String getFromStationId() {
        return fromStationId;
    }

    public String getToStationId() {
        return toStationId;
    }

    public TrackType getType() {
        return type;
    }

    public int getDurability() {
        return durability;
    }

    public boolean connects(String stationA, String stationB) {
        return (fromStationId.equals(stationA) && toStationId.equals(stationB))
                || (fromStationId.equals(stationB) && toStationId.equals(stationA));
    }
}
