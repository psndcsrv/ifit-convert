package com.ungerdesign.ifit;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Duration;
import java.time.Instant;

public class Point {
    private static final BigDecimal METERS_PER_SECOND_PER_MPH = BigDecimal.valueOf(0.44704);
    private static final BigDecimal MILES_PER_METER = BigDecimal.valueOf(0.00062137);

    private final Duration time;
    private final BigDecimal miles;
    private final BigDecimal mph;

    public Point(Duration time, BigDecimal miles, BigDecimal mph) {
        this.time = time;
        this.miles = miles;
        this.mph = mph;
    }

    public Instant getRelativeTime(Instant reference) {
        return reference.plus(time);
    }

    public BigDecimal getMeters() {
        return milesToMeters(miles);
    }

    public BigDecimal getMetersForDuration(Duration duration) {
        return mph.multiply(METERS_PER_SECOND_PER_MPH)
                .multiply(BigDecimal.valueOf(duration.getSeconds()))
                .setScale(2, RoundingMode.HALF_UP);
    }

    private BigDecimal milesToMeters(BigDecimal mi) {
        return mi.divide(MILES_PER_METER, 2, RoundingMode.HALF_UP);
    }
}
