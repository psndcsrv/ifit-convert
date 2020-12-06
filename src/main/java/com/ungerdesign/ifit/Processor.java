package com.ungerdesign.ifit;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.Map;

public class Processor {
    public static final Logger LOG = LoggerFactory.getLogger(Processor.class);

    private final TcxFile tcxFile;
    private final CsvFile csvFile;

    public Processor(File tcxFile, File csvFile) {
        this.tcxFile = new TcxFile(tcxFile);
        this.csvFile = new CsvFile(csvFile);
    }

    public String process() {
        LOG.info("Processing files:\nTCX: {}\nCSV: {}", tcxFile, csvFile);

        // Do the trackpoint distances first, so that the final distance for each lap can match the summary value
        fixTrackpointDistances();
        fixSummaryFields();

        return tcxFile.render();
    }

    public void fixSummaryFields() {
        tcxFile.setActivitySport("Running");
        tcxFile.fixIntegerValues();
        tcxFile.fixLapDistances();
    }

    public void fixTrackpointDistances() {
        Instant startTimestamp = tcxFile.getStartTimestamp();
        LOG.info("First trackpoint timestamp: {}", startTimestamp);

        Map<Instant, BigDecimal> distancesByTime = csvFile.getDistancesInMetersByTime(startTimestamp);

        // TODO
    }
}
