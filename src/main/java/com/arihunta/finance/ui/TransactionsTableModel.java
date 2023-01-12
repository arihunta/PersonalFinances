package com.arihunta.finance.ui;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.swing.table.AbstractTableModel;

import com.arihunta.finance.model.Transaction;
import com.google.common.base.Function;

@SuppressWarnings("serial")
public class TransactionsTableModel extends AbstractTableModel {

	private final List<Transaction> transactions = new ArrayList<>();

	private final Map<Integer, String> columnNames = new HashMap<>();
	private final Map<Integer, Function<Transaction, Object>> columnAccessors = new HashMap<>();
	private final FilterPanel filterPanel;


	public TransactionsTableModel(final List<Transaction> transactions, final FilterPanel filterPanel) {
		this.transactions.addAll(transactions);
		this.filterPanel = filterPanel;

		newColumn("Date", t -> t.getActualDate());
		newColumn("Statement Date", t -> t.getStatementDate());
		newColumn("Description", t -> t.getDescription());
		newColumn("Amount", t -> t.getAmount());
		newColumn("Type", t -> t.getType());
		newColumn("Tags", t -> t.getTags());
		newColumn("Credit/debit", t -> t.getAmount() > 0 ? "credit" : "debit");
	}

	private void newColumn(final String name, final Function<Transaction, Object> accessor) {
		int colIdx = columnNames.size();
		columnNames.put(colIdx, name);
		columnAccessors.put(colIdx, accessor);
		filterPanel.addFilter();
	}

	@Override
	public int getRowCount() {
		return transactions.size();
	}

	@Override
	public int getColumnCount() {
		return columnNames.size();
	}

	@Override
	public String getColumnName(int column) {
		return columnNames.get(column);
	}

	@Override
	public Object getValueAt(int rowIndex, int columnIndex) {
		return columnAccessors.get(columnIndex).apply(transactions.get(rowIndex));
	}

}
