package com.ungerdesign.ifit;

import java.util.Arrays;
import java.util.stream.Collectors;

public enum Sport {
    RUNNING("Running"),
    BIKING("Biking"),
    OTHER("Other");

    private final String typeValue;

    Sport(String typeValue) {
        this.typeValue = typeValue;
    }

    public static Sport lookup(String sportStr) {
        for (Sport sport : values()) {
            if (sport.name().toLowerCase().equals(sportStr)) {
                return sport;
            }
        }
        throw new RuntimeException("Invalid sport! Allowed values are: " + Arrays.asList(values()).stream().map(Sport::name).map(String::toLowerCase).collect(Collectors.joining(", ")));
    }

    public String getTypeValue() {
        return typeValue;
    }
}
