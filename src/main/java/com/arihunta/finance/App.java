package com.arihunta.finance;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;

import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.text.PDFTextStripper;

public class App
{

	private static final String OLD_PASSWORD = ""; // insert password here
	private static final String NEW_PASSWORD = ""; // insert password here

	final static String[] oldPassword = { //
			"2017-02-20.pdf", //
			"2017-03-20.pdf", //
			"2017-05-20.pdf", //
			"2017-06-20.pdf", //
			"2017-07-20.pdf", //
			"2017-08-19.pdf", //
			"2017-09-20.pdf", //
	};

	final static String[] newPassword = { //
			"2017-10-20.pdf", //
			"2017-11-20.pdf", //
			"2017-12-20.pdf", //
			"2018-01-20.pdf", //
			"2018-02-20.pdf", //
			"2018-03-17.pdf", //
			"2018-04-16.pdf", //
			"2018-04-23.pdf", //
			"2018-05-22.pdf", //
			"2018-06-22.pdf", //
			"2018-07-23.pdf", //
			"2018-08-22.pdf", //
			"2018-09-21.pdf", //
			"2018-10-20.pdf", //
			"2018-11-21.pdf", //
			"2018-12-21.pdf", //
			"2019-01-21.pdf", //
			"2019-02-21.pdf", //
			"2019-03-20.pdf", //
			"2019-04-20.pdf", //
			"2019-05-21.pdf", //
			"2019-06-21.pdf", //
			"2019-07-20.pdf", //
			"2019-08-21.pdf", //
			"2019-09-21.pdf", //
			"2019-10-21.pdf", //
			"2019-11-21.pdf", //
			"2019-12-21.pdf", //
			"2020-01-21.pdf", //
			"2020-02-21.pdf", //
			"2020-03-20.pdf", //
			"2020-04-21.pdf", //
	};

	public static void main(String[] args) {

		for (final var fileName : newPassword) {

			System.out.println("Loading " + fileName + "...");

			try (final PDDocument document = PDDocument
					.load(App.class.getResourceAsStream("/statements/" + fileName), NEW_PASSWORD)) {

				document.setAllSecurityToBeRemoved(true);

				final PDFTextStripper reader = new PDFTextStripper();
				final String pageText = reader.getText(document);

				Files.writeString(Paths
						.get("./src/main/resources/statements/" + fileName.replace(".pdf", ".csv")),
						pageText);

			}
			catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}

		}

	}

}
