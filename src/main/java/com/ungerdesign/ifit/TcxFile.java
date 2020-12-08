package com.ungerdesign.ifit;

import org.dom4j.Document;
import org.dom4j.DocumentException;
import org.dom4j.Element;
import org.dom4j.Node;
import org.dom4j.io.SAXReader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Duration;
import java.time.Instant;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class TcxFile {
    public static final Logger LOG = LoggerFactory.getLogger(TcxFile.class);

    private boolean parsed = false;
    private final File originalFile;

    private Document document;

    public TcxFile(File tcxFile) {
        originalFile = tcxFile;

        parse();
    }

    public synchronized void parse() {
        if (!parsed) {
            try {
                SAXReader reader = new SAXReader();
                document = reader.read(originalFile);

                parsed = true;
            } catch (DocumentException e) {
                LOG.error("Failed to parse TCX document", e);
                throw new RuntimeException("Failed to parse TCX document", e);
            }
        }
    }

    public Instant getStartTimestamp() {
        // Find the first trackpoint and return the timestamp as an Instant
        return getTrackpointTimestamp(getTrackpoints().get(0));
    }

    List<Node> getLaps() {
        return getLaps(document);
    }

    List<Node> getLaps(Node node) {
        return node.selectNodes(String.format("//%s", xpathFor("Lap")));
    }

    List<Node> getTrackpoints() {
        return getTrackpoints(document);
    }

    List<Node> getTrackpoints(Node node) {
        return node.selectNodes(String.format("//%s", xpathFor("Trackpoint")));
    }

    public void setActivitySport(String sport) {
        Element activity = (Element) document.selectSingleNode(String.format("//%s", xpathFor("Activity")));

        LOG.info("Setting {} Sport: {} -> {}", activity.getPath(), activity.attributeValue("Sport"), sport);

        activity.addAttribute("Sport", sport);
    }

    public void fixIntegerValues() {
        getLaps().forEach(node -> {
            // Change AverageHeartRateBpm value to an integer
            roundValue(node.selectSingleNode(xpathFor("AverageHeartRateBpm", "Value")));
            // Change Calories value to an integer
            roundValue(node.selectSingleNode(xpathFor("Calories")));

        });
    }

    public void fixLapDistances() {
        getLaps().forEach(node -> {
            List<Node> trackPoints = getTrackpoints(node);

            Node lastTrackpoint = trackPoints.get(trackPoints.size()-1);
            String furthestDistance = lastTrackpoint.selectSingleNode(xpathFor("DistanceMeters")).getText();

            Node lapDistanceMeters = node.selectSingleNode(xpathFor("DistanceMeters"));

            LOG.info("Setting {}: {} -> {}", lapDistanceMeters.getPath(), lapDistanceMeters.getText(), furthestDistance);

            lapDistanceMeters.setText(furthestDistance);
        });
    }

    public String render() {
        return document.asXML();
    }

    @Override
    public String toString() {
        return "TcxFile{" +
                "originalFile=" + originalFile +
                '}';
    }

    private Instant getTrackpointTimestamp(Node trackpoint) {
        return Instant.parse(trackpoint
                .selectSingleNode(xpathFor("Time"))
                .getStringValue());
    }

    private void roundValue(Node node) {
        BigDecimal value = new BigDecimal(node.getStringValue());
        value = value.setScale(0, RoundingMode.HALF_UP);
        String newValue = value.toBigInteger().toString();

        LOG.info("Rounded {}: {} -? {}", node.getPath(), node.getText(), newValue);

        node.setText(newValue);
    }

    String xpathFor(String... parts) {
        return Arrays.stream(parts)
                .map(part -> String.format("*[local-name() = '%s']", part))
                .collect(Collectors.joining("/"));
    }

    public void smoothTrackpoints(Map<Instant, Point> distancesByTime) {
        getLaps().forEach(node -> {
            List<Node> trackPoints = getTrackpoints(node);

            Instant timestamp;
            Instant prevTimestamp = null;
            Node distance;
            Duration durationSincePreviousPoint = Duration.ofSeconds(0);
            BigDecimal totalDistance = BigDecimal.ZERO;
            for (Node trackPoint : trackPoints) {
                timestamp = getTrackpointTimestamp(trackPoint);

                Point point = distancesByTime.get(timestamp);

                if (point == null) {
                    LOG.error("Failed to find real point for timestamp: {}", timestamp);
                } else {
                    if (prevTimestamp != null) {
                        durationSincePreviousPoint = Duration.between(prevTimestamp, timestamp);
                    }

                    distance = trackPoint.selectSingleNode(xpathFor("DistanceMeters"));
                    BigDecimal metersForDuration = point.getMetersForDuration(durationSincePreviousPoint);
                    totalDistance = totalDistance.add(metersForDuration);

                    LOG.info("Setting trackpoint distance {}: {} -> {} (+{} = {})",
                            timestamp, distance.getText(), point.getMeters().toString(),
                            metersForDuration, totalDistance);

                    distance.setText(totalDistance.toString());

                    prevTimestamp = timestamp;
                }
            }
        });
    }
}
