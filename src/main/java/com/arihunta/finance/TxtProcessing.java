package com.arihunta.finance;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.google.common.collect.Lists;

public class TxtProcessing {

    private static final Logger logger = LogManager.getLogger(TxtProcessing.class);

    private static final String DATE = "\\d{2} (JAN|FEB|MAR|APR|MAY|JUN|JUL|AUG|SEP|OCT|NOV|DEC)";

    private static final List<String> TYPES = Lists.newArrayList( //
            "CHEQUE CARD PURCHASE", //
            "DEBIT CARD PURCHASE FROM", //
            "AUTOBANK CASH WITHDRAWAL AT", //
            "OTHER BANK ATM CASH WITHD AT", //
            "OTHER BANK ATM CASH WITHD. AT", //
            "IB PAYMENT TO", //
            "IB PAYMENT FROM", //
            "IB TRANSFER TO", //
            "IB TRANSFER FROM", //
            "IB FUTURE-DATED PAYMENT TO", //
            "ELECTRONIC BANKING PAYMENT FR", //
            "TELETRANSMISSION INWARD", //
            "PAYMENT OF TRAFFIC FINE TO", //
            "PRE-PAID PAYMENT TO", //
            "PRE-PAID ELECTRICITY", //
            "SERVICE AGREEMENT", //
            "SERVICE FEE", //
            "SERVICE CHARGE", //
            "MEDICAL AID CONTRIBUTION", //
            "ACCOUNT PAYMENT", //
            "INSURANCE PREMIUM", //
            "TELEPHONE ACCOUNT", //
            "CASH WITHDRAWAL FEE", //
            "BANK CHARGES", //
            "MAGTAPE CREDIT", //
            "REFUND/GARAGE CARD", //
            "ELECTRONIC TRF - CREDIT CARD", //
            "ELECTRONIC TRF-CREDIT CARD", //
            "CREDIT TRANSFER", //
            "FIXED MONTHLY FEE", //
            "REVERSAL: BANK FEE SERVICE CHARGE", //
            "FEE-PIN RESET", //
            "FEE:90 DAY STATEMENT-AUTOPLUS", //
            "FEE- POS DECLINED INSUFF FUNDS", //
            "FEE: PAYMENT CONFIRM - EMAIL", //
            "FEE - OTHER BANK ATM", //
            "FEE - PRE-PAID TOP UP", //
            "FEE PRE PAID ELECTRICITY", //
            "FEE TELETRANSMISSION INWARD", //
            "FEE-TELETRANSMISSION INWARD", //
            "FEE-POS DECLINED INSUFF FUNDS", //
            "FEE: INTERIM/30 DAY STATEMENT" //
    );

    public static final Set<String> descriptions = new HashSet<>();

    private static final String TYPES_REGEX = "(" + TYPES.stream().collect(Collectors.joining("|")) + ")( \\d{2,4})?";
    private static final String DETAILS_REGEX = "(#{1,2} )?\\d+.\\d{2}-? \\d{2} \\d{2} \\d+.\\d{2}";

    private static final Pattern TYPE = Pattern.compile(TYPES_REGEX);
    private static final Pattern DETAILS = Pattern.compile(DETAILS_REGEX);
    private static final Pattern DESCRIPTION = Pattern.compile(".*");
    private static final Pattern ONE_LINER = Pattern.compile(TYPES_REGEX + " " + DETAILS_REGEX);

    static final void parseOldFiles() {
        streamFiles(Data.CSV_FILES_OLD_FORMAT) //
                .map(fileData -> fileData.stream() //
                        .map(line -> line.trim().replace(".", "").replace(",", ".")) //
                        .filter(line -> !line.isEmpty()) //
                        .collect(Collectors.toList()))
                .forEach(fileData -> {
                    int idx = 0;
                    while (idx < fileData.size()) {
                        if (ONE_LINER.matcher(fileData.get(idx)).matches()) {
                            idx++;
                        } else if (TYPE.matcher(fileData.get(idx)).matches()
                                && DESCRIPTION.matcher(fileData.get(idx + 1)).matches()
                                && DETAILS.matcher(fileData.get(idx + 2)).matches()) {
                            logger.info(fileData.get(idx + 1));
                            idx += 3;
                        } else {
                            logger.error("FAILED LINE " + (fileData.get(idx)));
                            idx++;
                        }
                    }
                }) //
        ;
    }

    static final void parseNewFiles() {
        streamFiles(Data.CSV_FILES_NEW_FORMAT) //
                .map(fileData -> fileData.stream() //
                        .map(line -> line.trim().replace(",", "")) //
                        .filter(line -> !line.isEmpty()) //
                        .collect(Collectors.toList()))
                .forEach(fileData -> {
                    int idx = 0;
                    while (idx < fileData.size()) {
                        if (ONE_LINER.matcher(fileData.get(idx)).matches()) {
                            if (idx + 1 < fileData.size() && !ONE_LINER.matcher(fileData.get(idx + 1)).matches()) {
                                idx++;
                            }
                        } else {
                            logger.error("FAILED LINE " + (fileData.get(idx)));
                        }
                        idx++;
                    }
                }) //
        ;
    }

    private static Stream<List<String>> streamFiles(List<String> fileNames) {
        return fileNames.stream() //
                .map(fileName -> Data.TRANSACTIONS_DIR.resolve(fileName)) //
                .map(resource -> {
                    try {
                        return Files.readAllLines(resource, StandardCharsets.UTF_8);
                    } catch (IOException e) {
                        logger.error("Error reading " + resource, e);
                        return new ArrayList<String>();
                    }
                }) //
                .filter(fileContents -> !fileContents.isEmpty()) //
        ;
    }

}