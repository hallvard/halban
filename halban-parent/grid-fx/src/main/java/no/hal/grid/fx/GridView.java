package no.hal.grid.fx;

import javafx.geometry.Dimension2D;
import javafx.scene.Node;
import javafx.scene.layout.Region;
import javafx.util.Callback;

public class GridView<T> extends Region {

	public interface Cell<T> {
		Node getNode();
		void setNodeSize(double width, double height);
		void setGridItem(T item, int x, int y);
	}

	Callback<GridView<T>, Cell<T>> cellFactory;

	public GridView() {
		super();
		setFocusTraversable(true);
	}

	public Callback<GridView<T>, Cell<T>> getCellFactory() {
		return cellFactory;
	}

	public void setCellFactory(Callback<GridView<T>, Cell<T>> cellFactory) {
		this.cellFactory = cellFactory;
		refreshCells();
	}

	//
	
	private int columnCount = 0, rowCount = 0;

	private Cell<T>[] cells = null;

	public void setDimensions(int columnCount, int rowCount) {
		this.columnCount = columnCount;
		this.rowCount = rowCount;
		refreshCells();
	}

	private void refreshCells() {
		getChildren().clear();
		cells = new Cell[columnCount * rowCount];
		for (var pos = 0; pos < cells.length; pos++) {
			cells[pos] = cellFactory.call(this);
			getChildren().add(cells[pos].getNode());
		}
	}

	public void setRowCount(int rows) {
		setDimensions(columnCount, rows);
	}

	public void setColumnCount(int columns) {
		setDimensions(columns, rowCount);
	}
		
	public int getRowCount() {
		return rowCount;
	}

	public int getColumnCount() {
		return columnCount;
	}

	public boolean isValidXY(int x, int y) {
		return x >= 0 && x < getColumnCount() && y >= 0 && y < getRowCount();
	}

	private final static Dimension2D DEFAULT_CELL_SIZE = new Dimension2D(20, 20);
    
    private Dimension2D minCellSize = DEFAULT_CELL_SIZE;
    private Dimension2D prefCellSize = DEFAULT_CELL_SIZE;
    private Dimension2D maxCellSize = DEFAULT_CELL_SIZE;

    public void setMinCellSize(Dimension2D minCellSize) {
        this.minCellSize = minCellSize;
    }
    public void setPrefCellSize(Dimension2D prefCellSize) {
        this.prefCellSize = prefCellSize;
    }
    public void setMaxCellSize(Dimension2D maxCellSize) {
        this.maxCellSize = maxCellSize;
    }

    public void setCellSize(Dimension2D cellSize) {
        setMinCellSize(cellSize);
		setPrefCellSize(cellSize);
		setMaxCellSize(cellSize);
    }

	private double computeWidth(double cellWidth, double prefWidth, double height) {
		var width = getInsets().getLeft() + columnCount * cellWidth + getInsets().getRight();
		return prefWidth > 0 ? Math.min(width, prefWidth) : width;
	}
	@Override
	protected double computeMinWidth(double height) {
		return computeWidth(minCellSize.getWidth(), getMinWidth(), height);
	}
	@Override
	protected double computePrefWidth(double height) {
		return computeWidth(prefCellSize.getWidth(), getPrefWidth(), height);
	}
	@Override
	protected double computeMaxWidth(double height) {
		return computeWidth(maxCellSize.getWidth(), getMaxWidth(), height);
	}

	private double computeHeight(double cellHeight, double prefHeight, double width) {
		var height = getInsets().getTop() + rowCount * cellHeight + getInsets().getBottom();
		return prefHeight > 0 ? Math.min(height, prefHeight) : height;
	}
	@Override
	protected double computeMinHeight(double width) {
		return computeHeight(minCellSize.getHeight(), getMinHeight(), width);
	}
	@Override
	protected double computePrefHeight(double width) {
		return computeHeight(prefCellSize.getHeight(), getPrefHeight(), width);
	}
	@Override
	protected double computeMaxHeight(double width) {
		return computeHeight(maxCellSize.getHeight(), getMaxHeight(), width);
	}

	@Override
	protected void layoutChildren() {
        final double left = getInsets().getLeft(), top = getInsets().getTop();
        final double computedCellWidth = (getWidth() - left - getInsets().getRight()) / columnCount;
		final double computedCellHeight = (getHeight() - top - getInsets().getBottom()) / rowCount;
		final double cellSize = Math.min(computedCellWidth, computedCellHeight);

		if (cells != null) {
			int row = 0, column = 0;
			for (var cell : cells) {
				cell.setNodeSize(cellSize, cellSize);
				cell.getNode().relocate(left + column * cellSize, top + row * cellSize);
				column++;
				if (column >= columnCount) {
					column = 0;
					row++;
				}
			}
		}
	}

	public Cell<T> getCell(int column, int row) {
		return cells[row * columnCount + column];
	}

	public void updateCell(T item, int column, int row) {
		Cell<T> cell = getCell(column, row);
		cell.setGridItem(item, column, row);
	}
}
