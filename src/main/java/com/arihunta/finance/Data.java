package com.arihunta.finance;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.google.common.io.Resources;

public class Data {

    private static final Logger logger = LogManager.getLogger(Data.class);

    static final Path DATA_DIR = Paths.get("./data/");
    static final Path PDF_DIR = DATA_DIR.resolve("pdf");
    static final Path PDFTXT_DIR = DATA_DIR.resolve("pdftxt");
    static final Path TRANSACTIONS_DIR = DATA_DIR.resolve("transactions");
    static final Path CSV_DIR = DATA_DIR.resolve("csv");

    static final String OLD_PASSWORD;
    static final String NEW_PASSWORD;

    static final List<String> PDF_FILES_OLD_PWD = new ArrayList<>();
    static final List<String> PDF_FILES_NEW_PWD = new ArrayList<>();
    static final List<String> CSV_FILES_OLD_FORMAT = new ArrayList<>();
    static final List<String> CSV_FILES_NEW_FORMAT = new ArrayList<>();

    static {
        String oldPassword = "";
        String newPassword = "";
        try {
            oldPassword = Files.readString(DATA_DIR.resolve("password-old.txt"), StandardCharsets.UTF_8);
            newPassword = Files.readString(DATA_DIR.resolve("password-new.txt"), StandardCharsets.UTF_8);
            PDF_FILES_OLD_PWD.addAll(
                    Resources.readLines(Resources.getResource("pdf-filenames-old.txt"), StandardCharsets.UTF_8));
            PDF_FILES_NEW_PWD.addAll(
                    Resources.readLines(Resources.getResource("pdf-filenames-new.txt"), StandardCharsets.UTF_8));
            CSV_FILES_OLD_FORMAT.addAll(
                    Resources.readLines(Resources.getResource("csv-filenames-old.txt"), StandardCharsets.UTF_8));
            CSV_FILES_NEW_FORMAT.addAll(
                    Resources.readLines(Resources.getResource("csv-filenames-new.txt"), StandardCharsets.UTF_8));
        } catch (IOException e) {
            logger.error("Error loading data", e);
        }
        OLD_PASSWORD = oldPassword;
        NEW_PASSWORD = newPassword;
    }

}
