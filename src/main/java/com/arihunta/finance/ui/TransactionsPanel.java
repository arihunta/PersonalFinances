package com.arihunta.finance.ui;

import java.awt.BorderLayout;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.image.BufferedImage;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import javax.swing.BoxLayout;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.RowFilter;
import javax.swing.table.TableRowSorter;

import com.arihunta.finance.model.Transaction;

import lombok.extern.log4j.Log4j2;

@SuppressWarnings("serial")
@Log4j2
public class TransactionsPanel extends JPanel {

	private final TransactionsTableModel dataModel;
	private final TableRowSorter<TransactionsTableModel> tableRowSorter;
	private final FilterPanel filterPanel = new FilterPanel();
	private final JLabel statusLabel = new JLabel();

	public TransactionsPanel(List<Transaction> transactions) {
		super(new BorderLayout());

		dataModel = new TransactionsTableModel(transactions, filterPanel);

		tableRowSorter = new TableRowSorter<TransactionsTableModel>();
		tableRowSorter.setModel(dataModel);
		tableRowSorter.setRowFilter(RowFilter.regexFilter("credit", 6));

		final JTable transactionsTable = new JTable();
		transactionsTable.setModel(dataModel);
		transactionsTable.setRowSorter(tableRowSorter);

//		transactionsTable.setFont(getNewFont());

		final JPanel statusPanel = new JPanel();
		statusPanel.setLayout(new BoxLayout(statusPanel, BoxLayout.X_AXIS));
		statusPanel.add(statusLabel);

		add(new JScrollPane(transactionsTable), BorderLayout.CENTER);
		add(filterPanel, BorderLayout.NORTH);
		add(statusPanel, BorderLayout.SOUTH);

		filterPanel.setParentPanel(this);
	}

	private final Font getNewFont()
    {
        Graphics g = new BufferedImage(1, 1, BufferedImage.TYPE_INT_RGB).getGraphics();
        Font font = new Font(g.getFont().toString(), 0, 16);
        g.dispose();

        return font;
    }

	public void filtersUpdated(Map<Integer, String> filters) {
		tableRowSorter.setRowFilter(RowFilter.andFilter(filters.keySet().stream().map(it -> {
			try {
				return RowFilter.regexFilter("(?i)" + filters.get(it), it);
			} catch (Exception e) {
				log.error("Error creating regex filter for '" + it + "': " + e.getMessage(), e);
				return null;
			}
		}).filter(Objects::nonNull).collect(Collectors.toList())));
		final String rowcount = "'" + tableRowSorter.getViewRowCount() + "' / '" + tableRowSorter.getModelRowCount() + "' rows";
		final String price = "'" +
		IntStream.range(0, tableRowSorter.getViewRowCount())
			.map(tableRowSorter::convertRowIndexToModel)
			.mapToDouble(idx -> (Double) dataModel.getValueAt(idx, 3))
			.sum() + "'R";
		statusLabel.setText(rowcount + ", " + price);
	}

}
