package com.arihunta.finance;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.StandardOpenOption;
import java.util.regex.Pattern;

import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.text.PDFTextStripper;

import com.google.common.collect.Streams;

import lombok.extern.log4j.Log4j2;

@Log4j2
public class PdfProcessing
{

    private static final String TABLE_END_TAG = "##TABLE_END";

    private static final String TABLE_START_TAG = "##TABLE_START";

    public static void extractPdfTxt() {
        Data.PDF_FILES_OLD_PWD.stream().forEach(it -> extractPdfTxt(it, Data.OLD_PASSWORD));
        Data.PDF_FILES_NEW_PWD.stream().forEach(it -> extractPdfTxt(it, Data.NEW_PASSWORD));
        Data.PDF_FILES_NEWER_PWD.stream().forEach(it -> extractPdfTxt(it, Data.NEWER_PASSWORD));
    }

    public static void extractPdfTxt(final String fileName, final String password) {

        log.info("Converting " + fileName + "...");

        try (final var stream = Files.newInputStream(Data.PDF_DIR.resolve(fileName));
                final PDDocument document = PDDocument.load(stream, password)) {

            document.setAllSecurityToBeRemoved(true);

            final PDFTextStripper reader = new PDFTextStripper();
            final String pageText = transformToIntermediate(reader.getText(document));

            Files.writeString(Data.PDFTXT_DIR.resolve(fileName.replace(".pdf", ".csv")), pageText,
                    StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING);

        }
        catch (IOException e) {
            log.error(e);
        }

    }

    public static void extractTransactions() {
        Streams.concat(Data.PDF_FILES_OLD_PWD.stream(), Data.PDF_FILES_NEW_PWD.stream(), Data.PDF_FILES_NEWER_PWD.stream()) //
                .map(it -> it.replace(".pdf", ".csv")) //
                .forEach(fileName -> extractTransactions(fileName)) //
        ;
    }

    public static void extractTransactions(final String fileName) {
        final var pat = Pattern.compile(
                Pattern.quote(TABLE_START_TAG) + "([\\s\\S]*?)" + Pattern.quote(TABLE_END_TAG));
        try {
            final String contents = Files.readString(Data.PDFTXT_DIR.resolve(fileName));
            final var mch = pat.matcher(contents);
            final StringBuilder sb = new StringBuilder();
            log.info("searching " + fileName);
            while (mch.find()) {
                sb.append(mch.group(1).trim());
                sb.append("\n");
            }
            var str = sb.toString();
            Files.writeString(Data.TRANSACTIONS_DIR.resolve(fileName), str,
                    StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING);
        }
        catch (IOException e) {
            log.error(e);
        }
    }

    private static String transformToIntermediate(final String input) {
        final var intermediate = input //
                .replaceAll("BALANCE BROUGHT FORWARD .*?\r?\n", "\n" + TABLE_START_TAG + "\n") //
                .replaceAll("## These fees are inclusive of VAT .*?\r?\n",
                        "\n" + TABLE_END_TAG + "\n") //
                .replaceAll("## These fees include VAT .*?\r?\n", "\n" + TABLE_END_TAG + "\n") //
                .replaceAll("RONDEBOSCH\r?\nPO BOX 61342 MARSHALLTOWN 2107\r?\nRONDEBOSCH",
                        "\n" + TABLE_END_TAG + "\n") //
                .replaceAll("REVERSAL: BANK FEE SERVICE\r?\nCHARGE\r?\n",
                        "REVERSAL: BANK FEE SERVICE CHARGE ") //
        ;
        return intermediate.substring(intermediate.indexOf(TABLE_START_TAG));
    }

}
