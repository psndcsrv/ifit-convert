package com.ungerdesign.ifit;

import org.apache.commons.io.FileUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;

public class Application {
    private final static Logger LOG = LoggerFactory.getLogger(Application.class);

    public static void main(String[] args) {
        LOG.info("Got arguments: {}", (Object) args);

        if (args.length > 2) {
            throw new RuntimeException("Only two arguments allowed -- the sport, and the tcx or csv filename to be converted");
        } else if (args.length < 2) {
            throw new RuntimeException("Two arguments required -- the sport, and the tcx or csv filename to be converted");
        }

        Sport sport = Sport.lookup(args[0]);
        List<String> filenames = Arrays.asList(args[1].split(" "));

        LOG.info("Got files ({}): {}", filenames.size(), filenames);

        boolean hasErrors = false;

        for (String filename : filenames) {
            try {
                handleFile(sport, filename);
            } catch (RuntimeException e) {
                LOG.error("Failed to process file: {}", filename, e);
                hasErrors = true;
            }
        }

        if (hasErrors) {
            System.exit(1);
        }
    }

    private static void handleFile(Sport sport, String filename) {
        String lowercaseFilename = filename.toLowerCase(Locale.ROOT);

        File csvFile;
        File tcxFile;
        String tcxFilename;

        if (lowercaseFilename.endsWith(".csv")) {
            csvFile = FileUtils.getFile(filename);
            tcxFilename = filename.replace(".csv", ".tcx");
            tcxFile = FileUtils.getFile(tcxFilename);
        } else if (lowercaseFilename.endsWith(".tcx")) {
            tcxFile = FileUtils.getFile(filename);
            tcxFilename = filename;
            csvFile = FileUtils.getFile(filename.replace(".tcx", ".csv"));
        } else {
            LOG.error("The passed filename must be a TCX or CSV file");
            throw new RuntimeException("The passed filename must be a TCX or CSV file");
        }

        if (!(tcxFile.exists() && tcxFile.canRead() && tcxFile.canWrite())) {
            LOG.error("The TCX file needs to exist, and be readable and writable");
            throw new RuntimeException("The TCX file needs to exist, and be readable and writable");
        } else if (!(csvFile.exists() && csvFile.canRead())) {
            LOG.error("The CSV file needs to exist, and be readable");
            throw new RuntimeException("The CSV file needs to exist, and be readable");
        }

        try {
            Processor p = new Processor(tcxFile, csvFile, sport);
            String output = p.process();
            FileUtils.writeStringToFile(FileUtils.getFile(tcxFilename.replace(".tcx", "-new.tcx")), output, StandardCharsets.UTF_8);
        } catch (Throwable t) {
            LOG.error("Failed to process files", t);
        }
    }
}
