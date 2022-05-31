package io.kontur.disasterninja.dto;

public enum EventType {

    FLOOD("Flood"),
    TSUNAMI("Tsunami"),
    WILDFIRE("Wildfire"),
    THERMAL_ANOMALY("Thermal Anomaly"),
    INDUSTRIAL_HEAT("Industrial Heat"),
    TORNADO("Tornado"),
    WINTER_STORM("Winter Storm"),
    EARTHQUAKE("Earthquake"),
    STORM("Storm"),
    CYCLONE("Cyclone"),
    DROUGHT("Drought"),
    VOLCANO("Volcano"),
    OTHER("Other");

    private final String name;

    EventType(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }
}
