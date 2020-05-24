package com.arihunta.finance;

import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import com.arihunta.finance.model.Transaction;

import lombok.extern.log4j.Log4j2;

@Log4j2
public class App
{

    public static void main(String[] args) {

        log.info("Hello app");

        final List<Transaction> transactions = Stream
                .concat(TxtProcessing.parseOldFiles().stream(),
                        TxtProcessing.parseNewFiles().stream())
                .peek(it -> System.out.println(it)) //
                .collect(Collectors.toList()) //
        ;

        log.info("---");
        log.info("Loaded " + transactions.size() + " transactions.");
        log.info("---");

// books(transactions);

    }

    private static void books(final List<Transaction> transactions) {
        final var amount = transactions.stream() //
                .filter(it -> it.getDescription().contains("AMZN DIGITAL")
                        || it.getDescription().contains("EXCLUSIVE BOOK")
                        || it.getDescription().contains("HELP THE RU")
                        || it.getDescription().contains("THE BOOK SHOP")) //
                .peek(it -> System.out.println(it)) //
                .mapToDouble(it -> it.getAmount()) //
                .sum() //
        ;
        log.info("Books: " + amount);
    }

    private static void woolworths(final List<Transaction> transactions) {
        final var amount = transactions.stream() //
                .filter(it -> it.getDescription().contains("WOOLWORTHS")) //
                .peek(it -> System.out.println(it)) //
                .mapToDouble(it -> it.getAmount()) //
                .sum() //
        ;
        log.info("WOOLWORTHS: " + amount);
    }

    private static void checkers(final List<Transaction> transactions) {
        final var amount = transactions.stream() //
                .filter(it -> it.getDescription().contains("CHECKERS")) //
                .peek(it -> System.out.println(it)) //
                .mapToDouble(it -> it.getAmount()) //
                .sum() //
        ;
        log.info("CHECKERS: " + amount);
    }

    private static void picknpay(final List<Transaction> transactions) {
        final var amount = transactions.stream() //
                .filter(it -> it.getDescription().contains("PNP")) //
                .peek(it -> System.out.println(it)) //
                .mapToDouble(it -> it.getAmount()) //
                .sum() //
        ;
        log.info("PNP: " + amount);
    }

    private static void mobile(final List<Transaction> transactions) {
        final var amount = transactions.stream() //
                .filter(it -> it.getDescription().contains("VOD PREPAID")
                        || it.getDescription().contains("RAIN")) //
                .peek(it -> System.out.println(it)) //
                .mapToDouble(it -> it.getAmount()) //
                .sum() //
        ;
        log.info("Mobile: " + amount);
    }

    private static void rent(final List<Transaction> transactions) {
        final var amount = transactions.stream() //
                .filter(it -> it.getDescription().contains("RAWSON")
                        && it.getType().contains("IB PAYMENT TO")) //
                .peek(it -> System.out.println(it)) //
                .mapToDouble(it -> it.getAmount()) //
                .sum() //
        ;
        log.info("Rent: " + amount);
    }

    private static void liquor(final List<Transaction> transactions) {
        final var amount = transactions.stream() //
                .filter(it -> it.getDescription().contains("LIQOUR")
                        || it.getDescription().contains("LIQUOR")
                        || it.getDescription().contains("TOPS")) //
                .peek(it -> System.out.println(it)) //
                .mapToDouble(it -> it.getAmount()) //
                .sum() //
        ;
        log.info("Liquor: " + amount);
    }

    private static void salary(final List<Transaction> transactions) {
        final var earnings = transactions.stream() //
                .filter(it -> it.getDescription().contains("PERALEX")
                        || it.getDescription().contains("AMAZON DEVAMZN")) //
                .peek(it -> System.out.println(it)) //
                .mapToDouble(it -> it.getAmount()) //
                .sum() //
        ;
        log.info("Earnings: " + earnings);
    }

}
