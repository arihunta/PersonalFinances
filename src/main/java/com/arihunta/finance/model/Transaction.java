package com.arihunta.finance.model;

import java.time.LocalDate;
import java.util.List;

import lombok.Data;

@Data
public class Transaction
{

    private final LocalDate statementDate;
    private final LocalDate actualDate;
    private final double amount;
    private final String type;
    private final String description;
    private final double accountBalance;
    private final List<String> tags;

    private final String rawText;

}
