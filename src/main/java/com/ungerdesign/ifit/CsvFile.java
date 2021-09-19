package com.ungerdesign.ifit;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVRecord;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.math.BigDecimal;
import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

import static com.ungerdesign.ifit.CsvFile.Headers.*;

public class CsvFile {
    public static final Logger LOG = LoggerFactory.getLogger(CsvFile.class);

    private final File originalFile;
    private final InputStream originalStream;
    private boolean parsed = false;

    private final List<Point> points = new ArrayList<>();

    public CsvFile(File csvFile) {
        originalFile = csvFile;
        originalStream = null;

        parse();
    }

    public CsvFile(InputStream csvStream) {
        originalStream = csvStream;
        originalFile = null;

        parse();
    }

    public enum Headers {
        TIME, MILES, MPH, WATTS, HR, RPM, RESISTANCE, REL_RESISTANCE, INCLINE
    }

    public synchronized void parse() {
        if (!parsed) {
            try {
                points.clear();

                Reader in;
                if (Objects.nonNull(originalFile)) {
                    in = new BufferedReader(new FileReader(originalFile));
                } else {
                    in = new InputStreamReader(originalStream);
                }

                Iterable<CSVRecord> records = CSVFormat.DEFAULT
                        .withHeader(Headers.class)
                        .parse(in);

                int skipped = 0;
                for (CSVRecord record : records) {
                    if (skipped < 3) {
                        skipped++;
                        continue;
                    }

                    Duration time = toDuration(record.get(TIME));
                    BigDecimal miles = new BigDecimal(record.get(MILES));
                    BigDecimal mph = new BigDecimal(record.get(MPH));

                    points.add(new Point(time, miles, mph));
                }

                parsed = true;
            } catch (IOException e) {
                LOG.error("Failed to parse TCX document", e);
                throw new RuntimeException("Failed to parse TCX document", e);
            }
        }
    }

    private Duration toDuration(String hms) {
        String[] parts = hms.split(":");
        String[] suffixes = new String[] { "", "S", "M", "H" };

        String durationStr = "";
        for (int i = 1; i <= parts.length; i++) {
            String part = parts[parts.length - i];
            durationStr = String.format("%s%s%s", part, suffixes[i], durationStr);
        }
        return Duration.parse("PT" + durationStr);
    }

    public Map<Instant, Point> getPointsByTime(Instant tZero) {
        return points.stream()
                .collect(Collectors.toMap(
                        point -> point.getRelativeTime(tZero),
                        p -> p));
    }

    @Override
    public String toString() {
        return "CsvFile{" +
                "originalFile=" + originalFile +
                '}';
    }
}
