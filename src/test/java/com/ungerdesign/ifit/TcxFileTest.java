package com.ungerdesign.ifit;

import org.dom4j.Node;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.time.Instant;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

public class TcxFileTest {

    @Test
    public void testGetTrackpoints() throws Exception {
        TcxFile file = new TcxFile(new File(getClass().getResource("/test.tcx").getFile()));

        List<Node> elements = file.getTrackpoints();
        assertThat(elements).hasSize(2458);
    }

    @Test
    public void testGetStartTimestamp() throws Exception {
        TcxFile file = new TcxFile(new File(getClass().getResource("/test.tcx").getFile()));

        Instant startTimestamp = file.getStartTimestamp();
        assertThat(startTimestamp).isEqualTo("2020-12-05T18:43:31.207Z");
    }

    @Test
    public void testXpathFor() {
        TcxFile file = new TcxFile(new File(getClass().getResource("/test.tcx").getFile()));

        assertThat(file.xpathFor("Test")).isEqualTo("*[local-name() = 'Test']");
        assertThat(file.xpathFor("Part1", "Part2", "Part3"))
                .isEqualTo("*[local-name() = 'Part1']/*[local-name() = 'Part2']/*[local-name() = 'Part3']");
    }
}
