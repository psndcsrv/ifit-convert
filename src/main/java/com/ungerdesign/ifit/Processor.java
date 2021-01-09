package com.ungerdesign.ifit;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.time.Instant;
import java.util.Map;

public class Processor {
    public static final Logger LOG = LoggerFactory.getLogger(Processor.class);

    private final TcxFile tcxFile;
    private final CsvFile csvFile;
    private final Sport sport;

    public Processor(File tcxFile, File csvFile, Sport sport) {
        this.tcxFile = new TcxFile(tcxFile);
        this.csvFile = new CsvFile(csvFile);
        this.sport = sport;
    }

    public String process() {
        LOG.info("Processing files:\nTCX: {}\nCSV: {}", tcxFile, csvFile);

        // Do the trackpoint distances first, so that the final distance for each lap can match the summary value
        fixTrackpointDistances();
        fixSummaryFields();

        return tcxFile.render();
    }

    public void fixSummaryFields() {
        tcxFile.setActivitySport(sport.getTypeValue());
        tcxFile.fixIntegerValues();
        tcxFile.fixLapDistances();
    }

    public void fixTrackpointDistances() {
        Instant startTimestamp = tcxFile.getStartTimestamp();
        LOG.info("First trackpoint timestamp: {}", startTimestamp);

        Map<Instant, Point> distancesByTime = csvFile.getPointsByTime(startTimestamp);

        tcxFile.smoothTrackpoints(distancesByTime);
    }
}
