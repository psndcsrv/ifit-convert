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
import java.time.Instant;
import java.util.Arrays;
import java.util.List;
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
        ((Element) document.selectSingleNode(String.format("//%s", xpathFor("Activity"))))
                .addAttribute("Sport", sport);
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

            node.selectSingleNode(xpathFor("DistanceMeters")).setText(furthestDistance);
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
        node.setText(value.toBigInteger().toString());
    }

    String xpathFor(String... parts) {
        return Arrays.stream(parts)
                .map(part -> String.format("*[local-name() = '%s']", part))
                .collect(Collectors.joining("/"));
    }
}
