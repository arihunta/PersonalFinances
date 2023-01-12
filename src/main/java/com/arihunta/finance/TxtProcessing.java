package com.arihunta.finance;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDate;
import java.util.AbstractMap;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Map.Entry;
import java.util.Set;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;

import com.arihunta.finance.model.Transaction;
import com.google.common.collect.Lists;

import lombok.extern.log4j.Log4j2;

@Log4j2
public class TxtProcessing
{

    private static final String TYPES = Stream.of( //
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
    ).collect(Collectors.joining("|"));

    public static final Set<String> descriptions = new HashSet<>();

    private static final String DATE = "(\\d{2}) (JAN|FEB|MAR|APR|MAY|JUN|JUL|AUG|SEP|OCT|NOV|DEC)";

    private static final String TYPES_REGEX = "(" + TYPES + ")( \\d{2,4})?";
    private static final String DETAILS_REGEX = "(#{1,2} )?(\\d+.\\d{2})(-?) (\\d{2}) (\\d{2}) (\\d+.\\d{2})(-?)";
    private static final String DESCRIPTIONS_REGEX = "(.+?)(?:" + DATE + ")?";

    /** TYPE,TYPENUM */
    private static final Pattern TYPE = Pattern.compile(TYPES_REGEX);

    /** FOOTNOTE,QUANTITY,QUANTITY_SIGN,DAY,MONTH,BALANCE,BALANCE_SIGN */
    private static final Pattern DETAILS = Pattern.compile(DETAILS_REGEX);

    /** DESCRIPTION,DAY,MONTH */
    private static final Pattern DESCRIPTION = Pattern.compile(DESCRIPTIONS_REGEX);

    /** TYPE,TYPENUM,FOOTNOTE,QUANTITY,QUANTITY_SIGN,MONTH,DAY,BALANCE,BALANCE_SIGN */
    private static final Pattern ONE_LINER = Pattern.compile(TYPES_REGEX + " " + DETAILS_REGEX);

    static final List<Transaction> parseOldFiles() {
        return streamFiles(Data.CSV_FILES_OLD_FORMAT) //
                .map(dateAndFileData -> (Entry<LocalDate, List<String>>) new AbstractMap.SimpleEntry<>(
                        dateAndFileData.getKey(), dateAndFileData.getValue().stream() //
                                .map(line -> line.trim().replace(".", "").replace(",", ".")) //
                                .filter(line -> !line.isEmpty()) //
                                .collect(Collectors.toList())))
                .flatMap(dateAndFileData -> {
                    var fileData = dateAndFileData.getValue();
                    int idx = 0;
                    final List<Transaction> transactions = Lists.newArrayList();
                    while (idx < fileData.size()) {
                        if (ONE_LINER.matcher(fileData.get(idx)).matches()) {
                            transactions.add(transactionFromOneLiner(fileData.get(idx),
                                    dateAndFileData.getKey()));
                            idx++;
                        }
                        else if (TYPE.matcher(fileData.get(idx)).matches()
                                && DESCRIPTION.matcher(fileData.get(idx + 1)).matches()
                                && DETAILS.matcher(fileData.get(idx + 2)).matches()) {
                            transactions.add(transactionFromThreeLiner(fileData.get(idx),
                                    fileData.get(idx + 1), fileData.get(idx + 2),
                                    dateAndFileData.getKey()));
                            idx += 3;
                        }
                        else {
                            log.error("FAILED LINE " + (fileData.get(idx)));
                            idx++;
                        }
                    }
                    return transactions.stream();
                }) //
                .filter(transaction -> transaction != null) //
                .collect(Collectors.toList()) //
        ;
    }

    static final List<Transaction> parseNewFiles() {
        return streamFiles(Data.CSV_FILES_NEW_FORMAT) //
                .map(dateAndFileData -> (Entry<LocalDate, List<String>>) new AbstractMap.SimpleEntry<>(
                        dateAndFileData.getKey(), dateAndFileData.getValue().stream() //
                                .map(line -> line.trim().replace(",", "")) //
                                .filter(line -> !line.isEmpty()) //
                                .collect(Collectors.toList())))
                .flatMap(dateAndFileData -> {
                    var fileData = dateAndFileData.getValue();
                    int idx = 0;
                    final List<Transaction> transactions = Lists.newArrayList();
                    while (idx < fileData.size()) {
                        if (ONE_LINER.matcher(fileData.get(idx)).matches()) {
                            if (idx + 1 < fileData.size()
                                    && !ONE_LINER.matcher(fileData.get(idx + 1)).matches()) {
                                transactions.add(transactionFromTwoLiner(fileData.get(idx),
                                        fileData.get(idx + 1), dateAndFileData.getKey()));
                                idx++;
                            }
                            else {
                                transactions.add(transactionFromOneLiner(fileData.get(idx),
                                        dateAndFileData.getKey()));
                            }
                        }
                        else {
                            log.error("FAILED LINE " + (fileData.get(idx)));
                        }
                        idx++;
                    }
                    return transactions.stream();
                }) //
                .filter(transaction -> transaction != null) //
                .collect(Collectors.toList()) //
        ;
    }

    private static Stream<Entry<LocalDate, List<String>>> streamFiles(List<String> fileNames) {
        return fileNames.stream() //
                .map(fileName -> (Entry<LocalDate, Path>) new AbstractMap.SimpleEntry<>(
                        LocalDate.parse(fileName.replace(".csv", "")),
                        Data.TRANSACTIONS_DIR.resolve(fileName))) //
                .map(entry -> {
                    try {
                        return (Entry<LocalDate, List<String>>) new AbstractMap.SimpleEntry<>(
                                entry.getKey(),
                                Files.readAllLines(entry.getValue(), StandardCharsets.UTF_8));
                    }
                    catch (IOException e) {
                        log.error("Error reading " + entry.getValue(), e);
                        return (Entry<LocalDate, List<String>>) new AbstractMap.SimpleEntry<>(
                                entry.getKey(), Arrays.asList(new String[] {}));
                    }
                }) //
                .filter(fileContents -> !fileContents.getValue().isEmpty()) //
        ;
    }

    private static Transaction transactionFromOneLiner(final String details,
            final LocalDate statementDate) {
        final String csvData = matcherToCsv(details, ONE_LINER) + ",,,";
        return csvToTransaction(csvData, statementDate);
    }

    private static Transaction transactionFromTwoLiner(final String details,
            final String description, final LocalDate statementDate) {
        final String csvData = matcherToCsv(details, ONE_LINER) + ","
                + matcherToCsv(description, DESCRIPTION);
        return csvToTransaction(csvData, statementDate);
    }

    private static Transaction transactionFromThreeLiner(final String type,
            final String description, final String details, final LocalDate statementDate) {
        final String csvData = matcherToCsv(type, TYPE) + "," + matcherToCsv(details, DETAILS) + ","
                + matcherToCsv(description, DESCRIPTION);
        return csvToTransaction(csvData, statementDate);
    }

    private static String matcherToCsv(final String input, final Pattern pattern) {
        final var matcher = pattern.matcher(input);
        matcher.matches();
        return IntStream.range(1, matcher.groupCount() + 1) //
                .boxed() //
                .map(i -> matcher.group(i) != null ? matcher.group(i).trim() : "") //
                .collect(Collectors.joining(",")) //
        ;
    }

    /**
     * Generate transactionfrom CSV line of the form:
     * 0----1-------2--------3--------4-------------5-----6---7-------8------------9-----------10--11---
     * TYPE,TYPENUM,FOOTNOTE,QUANTITY,QUANTITY_SIGN,MONTH,DAY,BALANCE,BALANCE_SIGN,DESCRIPTION,DAY,MONTH
     *
     * @param csvLine The CSV data.
     * @param stmtDate
     * @return The transaction.
     */
    private static Transaction csvToTransaction(final String csvLine, final LocalDate stmtDate) {

        final String[] data = csvLine.split(",", -1);

        if (data.length != 12) {
            throw new IllegalStateException("INVALID CSV LINE: " + csvLine + "/" + data.length);
        }

        final int month = Integer.parseInt(data[5]);
        final int day = Integer.parseInt(data[6]);
        final LocalDate statementDate = LocalDate
                .of(month == 12 && stmtDate.getMonthValue() == 1 ? stmtDate.getYear() - 1
                        : stmtDate.getYear(), month, day);
        final LocalDate actualDate = statementDate;
        final double amount = Double.parseDouble(data[3]) * (data[4].equals("-") ? -1 : 1);
        final String type = data[0];
        final String description = data[9];
        final double accountBalance = Double.parseDouble(data[7]) * (data[8].equals("-") ? -1 : 1);
        final List<String> tags = new ArrayList<>();

        final Transaction transaction = new Transaction(statementDate, actualDate, amount, type,
                description, accountBalance, tags, csvLine);

        return transaction;
    }

}