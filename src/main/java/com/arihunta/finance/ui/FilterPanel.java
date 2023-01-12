package com.arihunta.finance.ui;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import javax.swing.BoxLayout;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;

import lombok.Setter;

@SuppressWarnings("serial")
public class FilterPanel extends JPanel {

	@Setter
	private TransactionsPanel parentPanel;

	private List<JTextField> textFields = new ArrayList<>();

	public FilterPanel() {
		super();
		setLayout(new BoxLayout(this, BoxLayout.X_AXIS));
	}

	public void addFilter() {
		final JTextField textField = new JTextField();
		textFields.add(textField);
		this.add(textField);
		textField.getDocument().addDocumentListener(new FilterUpdateListener());
	}

	private Map<Integer, String> getFilters() {
		return IntStream.range(0, textFields.size())
			.filter(i -> !textFields.get(i).getText().isBlank())
			.boxed()
			.collect(Collectors.toMap(i -> i, i -> textFields.get(i).getText()))
		;
	}

	private final class FilterUpdateListener implements DocumentListener {
		@Override
		public void removeUpdate(DocumentEvent e) {
			if (parentPanel != null) {
				parentPanel.filtersUpdated(getFilters());
			}
		}

		@Override
		public void insertUpdate(DocumentEvent e) {
			if (parentPanel != null) {
				parentPanel.filtersUpdated(getFilters());
			}
		}

		@Override
		public void changedUpdate(DocumentEvent e) {
			if (parentPanel != null) {
				parentPanel.filtersUpdated(getFilters());
			}
		}
	}

}
