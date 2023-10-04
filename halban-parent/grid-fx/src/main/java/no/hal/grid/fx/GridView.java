package no.hal.grid.fx;

import javafx.geometry.Dimension2D;
import javafx.geometry.Pos;
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
		
	public int getRowCount() {
		return rowCount;
	}

	public int getColumnCount() {
		return columnCount;
	}

	public boolean isValidXY(int x, int y) {
		return x >= 0 && x < getColumnCount() && y >= 0 && y < getRowCount();
	}

	public Cell<T> getCell(int column, int row) {
		return cells[row * columnCount + column];
	}

	public void updateCell(T item, int column, int row) {
		Cell<T> cell = getCell(column, row);
		cell.setGridItem(item, column, row);
	}

	// layout
	
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

	private Pos alignment = Pos.CENTER;

	public Pos getAlignment() {
		return alignment;
	}

	public void setAlignment(Pos alignment) {
		this.alignment = alignment;
	}

	@Override
	protected void layoutChildren() {
        final double contentWidth = getWidth() - getInsets().getLeft() - getInsets().getRight();
        final double contentHeight = getHeight() - getInsets().getTop() - getInsets().getBottom();
		final double cellSize = Math.min(contentWidth / columnCount, contentHeight / rowCount);
		final double paddingWidth = contentWidth - cellSize * columnCount;
		final double paddingHeight = contentHeight - cellSize * rowCount;
		final double left = getInsets().getLeft() + switch (alignment.getHpos()) {
			case LEFT -> 0;
			case CENTER -> paddingWidth / 2;
			case RIGHT -> paddingWidth;
		};
		final double top = getInsets().getTop() + switch (alignment.getVpos()) {
			case TOP -> 0;
			case CENTER -> paddingHeight / 2;
			case BOTTOM, BASELINE -> paddingHeight;
		};

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
}
