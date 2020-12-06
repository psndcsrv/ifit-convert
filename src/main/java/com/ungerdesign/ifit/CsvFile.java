package com.ungerdesign.ifit;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.Collections;
import java.util.Map;

public class CsvFile {
    public static final Logger LOG = LoggerFactory.getLogger(CsvFile.class);

    private final File originalFile;

    public CsvFile(File csvFile) {
        this.originalFile = csvFile;
    }

    public Map<Instant, BigDecimal> getDistancesInMetersByTime(Instant tZero) {
        // TODO
        return Collections.emptyMap();
    }

    @Override
    public String toString() {
        return "CsvFile{" +
                "originalFile=" + originalFile +
                '}';
    }
}
