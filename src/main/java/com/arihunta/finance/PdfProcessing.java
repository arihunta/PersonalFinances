package com.arihunta.finance;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.StandardOpenOption;
import java.util.regex.Pattern;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.text.PDFTextStripper;

import com.google.common.collect.Streams;

public class PdfProcessing {

    private static final Logger logger = LogManager.getLogger(PdfProcessing.class);

    private static final String TABLE_END_TAG = "##TABLE_END";

    private static final String TABLE_START_TAG = "##TABLE_START";

    public static void convertToCsv() {
        Data.PDF_FILES_OLD_PWD.stream().forEach(it -> extractPdfTxt(it, Data.OLD_PASSWORD));
        Data.PDF_FILES_NEW_PWD.stream().forEach(it -> extractPdfTxt(it, Data.NEW_PASSWORD));
    }

    public static void extractTransactions() {

        final var pat = Pattern.compile(Pattern.quote(TABLE_START_TAG) + "([\\s\\S]*?)" + Pattern.quote(TABLE_END_TAG));

        Streams.concat(Data.PDF_FILES_OLD_PWD.stream(), Data.PDF_FILES_NEW_PWD.stream()) //
                .map(it -> it.replace(".pdf", ".csv")) //
                .forEach(fileName -> {
                    try {
                        final String contents = Files.readString(Data.PDFTXT_DIR.resolve(fileName));
                        final var mch = pat.matcher(contents);
                        final StringBuilder sb = new StringBuilder();
                        logger.info("searching " + fileName);
                        while (mch.find()) {
                            sb.append(mch.group(1).trim());
                            sb.append("\n");
                        }
                        var str = sb.toString();
                        Files.writeString(Data.TRANSACTIONS_DIR.resolve(fileName), str, StandardOpenOption.CREATE,
                                StandardOpenOption.TRUNCATE_EXISTING);
                    } catch (IOException e) {
                        logger.error(e);
                    }
                }) //
        ;
    }

    static void extractPdfTxt(final String fileName, final String password) {

        logger.info("Converting " + fileName + "...");

        try (final var stream = Files.newInputStream(Data.PDF_DIR.resolve(fileName));
                final PDDocument document = PDDocument.load(stream, password)) {

            document.setAllSecurityToBeRemoved(true);

            final PDFTextStripper reader = new PDFTextStripper();
            final String pageText = transformToIntermediate(reader.getText(document));

            Files.writeString(Data.PDFTXT_DIR.resolve(fileName.replace(".pdf", ".csv")), pageText,
                    StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING);

        } catch (IOException e) {
            logger.error(e);
        }

    }

    private static String transformToIntermediate(final String input) {
        final var intermediate = input //
                .replaceAll("BALANCE BROUGHT FORWARD .*?\\n", "\n" + TABLE_START_TAG + "\n") //
                .replaceAll("## These fees are inclusive of VAT .*?\\n", "\n" + TABLE_END_TAG + "\n") //
                .replaceAll("## These fees include VAT .*?\\n", "\n" + TABLE_END_TAG + "\n") //
                .replaceAll("RONDEBOSCH\nPO BOX 61342 MARSHALLTOWN 2107\nRONDEBOSCH", "\n" + TABLE_END_TAG + "\n") //
                .replaceAll("REVERSAL: BANK FEE SERVICE\nCHARGE\n", "REVERSAL: BANK FEE SERVICE CHARGE ") //
        ;
        return intermediate.substring(intermediate.indexOf(TABLE_START_TAG));
    }

}
