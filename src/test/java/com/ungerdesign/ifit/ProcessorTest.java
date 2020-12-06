package com.ungerdesign.ifit;

import org.apache.commons.io.FileUtils;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.nio.charset.StandardCharsets;

import static org.assertj.core.api.Assertions.assertThat;

public class ProcessorTest {

    @Test
    public void process() throws Exception {
        File tcxFile = new File(getClass().getResource("/test.tcx").getFile());
        File csvFile = new File(getClass().getResource("/test.csv").getFile());

        String expectedOutput = FileUtils.readFileToString(new File(getClass().getResource("/expected.tcx").getFile()), StandardCharsets.UTF_8);

        String processedOutput = new Processor(tcxFile, csvFile).process();

        assertThat(processedOutput).isEqualTo(expectedOutput);
    }
}