package com.ungerdesign.ifit;

import java.util.Arrays;
import java.util.stream.Collectors;

public enum Sport {
    RUNNING("run", "Running"),
    BIKING("bike", "Biking"),
    OTHER("??", "Other");

    private final String typeValue;
    private final String ifitName;

    Sport(String ifitName, String typeValue) {
        this.ifitName = ifitName;
        this.typeValue = typeValue;
    }

    public static Sport lookup(String sportStr) {
        for (Sport sport : values()) {
            if (sport.matchesName(sportStr)) {
                return sport;
            }
        }
        throw new RuntimeException("Invalid sport! Allowed values are: " + Arrays.stream(values()).map(Sport::name).map(String::toLowerCase).collect(Collectors.joining(", ")));
    }

    public String getTypeValue() {
        return typeValue;
    }

    public String getIfitName() {
        return ifitName;
    }

    private boolean matchesName(String name) {
        return name().equalsIgnoreCase(name) || ifitName.equalsIgnoreCase(name);
    }
}
