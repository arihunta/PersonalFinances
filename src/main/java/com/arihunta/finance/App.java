package com.arihunta.finance;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.file.Files;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import javax.swing.JFrame;
import javax.swing.UIManager;
import javax.swing.UnsupportedLookAndFeelException;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import com.arihunta.finance.model.Transaction;
import com.arihunta.finance.ui.TransactionsPanel;

import lombok.extern.log4j.Log4j2;

@Log4j2
@SuppressWarnings("unused")
public class App {

	public static void main(String[] args) throws ClassNotFoundException, InstantiationException,
			IllegalAccessException, UnsupportedLookAndFeelException, IOException, ParseException {

		log.info("Hello app");

		final Map<String, Set<String>> tags = loadTags();

		log.info("---");
		log.info("TAGS: " + tags);

		// \[\]
//        PdfProcessing.extractPdfTxt();
//        PdfProcessing.extractTransactions();

		final List<Transaction> transactions = Stream
				.concat(TxtProcessing.parseOldFiles().stream(), TxtProcessing.parseNewFiles().stream())
				.peek(it -> {
					System.out.println(it);
					tags.forEach((key, value) -> {
						if (value.stream().anyMatch(matchStr -> it.getDescription().contains(matchStr))) {
							it.getTags().add(key);
						}
					});
				}) //
				.collect(Collectors.toList()) //
		;

		log.info("---");
		log.info("Loaded " + transactions.size() + " transactions.");
		log.info("---");

		showTransactions(transactions);

	}

	private static void showTransactions(final List<Transaction> transactions) throws ClassNotFoundException,
			InstantiationException, IllegalAccessException, UnsupportedLookAndFeelException {
		UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
		final JFrame frame = new JFrame();

		frame.setLayout(new BorderLayout());
		frame.getContentPane().add(new TransactionsPanel(transactions), BorderLayout.CENTER);

		frame.setSize(new Dimension(1024, 768));
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

		frame.setVisible(true);
	}

	@SuppressWarnings("unchecked")
	private static Map<String, Set<String>> loadTags() throws IOException, ParseException {
		try (final var stream = Files.newInputStream(Data.DATA_DIR.resolve("tags.json"));
				var reader = new InputStreamReader(stream)){
			final JSONObject parse = (JSONObject) new JSONParser().parse(reader);
			return (Map<String, Set<String>>) parse.keySet().stream()
					.map(it -> it.toString())
					.collect(Collectors.toMap(
							key -> key,
							key -> ((JSONArray) parse.get(key)).stream().map(it -> it.toString()).collect(Collectors.toSet())
					));
		}
	}

}
