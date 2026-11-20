package no.hal.grid.fx;

import javafx.application.Platform;
import javafx.geometry.Dimension2D;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.layout.Region;
import javafx.scene.paint.Color;
import javafx.scene.paint.Paint;
import javafx.scene.shape.Rectangle;
import javafx.util.Callback;
import no.hal.grid.Grid;
import no.hal.grid.util.XYTransformedGrid;

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
		// after the cells, since the position of a cell among the children is its index
		getChildren().add(frame);
	}

	// frame

	/** The frame around the cells: it is outside them, so it hugs the board, wherever the board is placed in the view. */
	private final Rectangle frame = new Rectangle();
	private double frameWidth = 0;

	{
		frame.setManaged(false);
		frame.setMouseTransparent(true);
		frame.setFill(null);
		frame.setVisible(false);
	}

	/**
	 * Sets the frame around the cells, which has the width and the paint. The room for it is reserved around the cells when the width is more than zero,
	 * also if the paint is transparent, so the layout doesn't change when only the paint does. No frame is a width of zero.
	 */
	public void setFrame(Paint paint, double width) {
		boolean relayout = width != frameWidth;
		frameWidth = Math.max(0, width);
		frame.setStroke(paint != null ? paint : Color.TRANSPARENT);
		frame.setStrokeWidth(frameWidth);
		frame.setVisible(frameWidth > 0);
		if (relayout) {
			requestLayout();
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

	private Grid<T> grid;

	public Grid<T> getGrid() {
		return grid;
	}

	public void updateCell(T item, int column, int row) {
		Cell<T> cell = getCell(column, row);
		cell.setGridItem(item, column, row);
	}

	// grid model

	public void updateCell(int column, int row) {
		updateCell(grid.getCell(column, row), column, row);
	}

	public void updateCells(int x, int y, int w, int h) {
		for (int cy = y; cy != y + h; cy += Math.signum(h)) {
			for (int cx = x; cx != x + w; cx += Math.signum(w)) {
				updateCell(cx, cy);
			}
		}
	}

	public void updateCells() {
		updateCells(0, 0, grid.getWidth(), grid.getHeight());
	}

	public void setGrid(Grid<T> grid) {
		if (this.grid == grid) {
			return;
		}
		if (this.grid != null) {
			this.grid.removeGridListener(gridListener);
		}
		this.grid = grid;
		if (this.grid != null) {
			this.grid.addGridListener(gridListener);
			gridListener.gridDimensionsChanged(this.grid, grid.getWidth(), grid.getHeight());
		}
	}

	private Grid.Listener<T> gridListener = new Grid.Listener<T>() {

		@Override
		public void gridDimensionsChanged(Grid<T> grid, int w, int h) {
			if (Platform.isFxApplicationThread()) {
				setDimensions(w, h);
				updateCells();
			} else {
				Platform.runLater(() -> gridDimensionsChanged(grid, w, h));
			}
		}

		@Override
		public void gridContentsChanged(Grid<T> grid, int x, int y, int w, int h) {
			if (Platform.isFxApplicationThread()) {
				updateCells(x, y, w, h);
			} else {
				Platform.runLater(() -> gridContentsChanged(grid, x, y, w, h));
			}
		}
	};

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
		var width = getInsets().getLeft() + 2 * frameWidth + columnCount * cellWidth + getInsets().getRight();
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
		var height = getInsets().getTop() + 2 * frameWidth + rowCount * cellHeight + getInsets().getBottom();
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

	/** Where the cells are: the size of a cell, and the position of the upper left corner of the first one. */
	private record CellLayout(double cellSize, double left, double top) {}

	private CellLayout cellLayout() {
        final double contentWidth = getWidth() - getInsets().getLeft() - getInsets().getRight() - 2 * frameWidth;
        final double contentHeight = getHeight() - getInsets().getTop() - getInsets().getBottom() - 2 * frameWidth;
		final double cellSize = Math.min(contentWidth / columnCount, contentHeight / rowCount);
		final double paddingWidth = contentWidth - cellSize * columnCount;
		final double paddingHeight = contentHeight - cellSize * rowCount;
		final double left = getInsets().getLeft() + frameWidth + switch (alignment.getHpos()) {
			case LEFT -> 0;
			case CENTER -> paddingWidth / 2;
			case RIGHT -> paddingWidth;
		};
		final double top = getInsets().getTop() + frameWidth + switch (alignment.getVpos()) {
			case TOP -> 0;
			case CENTER -> paddingHeight / 2;
			case BOTTOM, BASELINE -> paddingHeight;
		};
		return new CellLayout(cellSize, left, top);
	}

	/**
	 * The location of the cell at the position (in the coordinates of this view), or null if there is none. It is worked out from the layout, not
	 * from the node that is under the position (see {@link #getGridLocation(Node)}), since a cell may have a node that is larger than the cell
	 * (as the circle that shows how often a location has been visited) which hides the cells next to it.
	 */
	public Grid.Location getGridLocation(double x, double y) {
		if (columnCount == 0 || rowCount == 0) {
			return null;
		}
		var layout = cellLayout();
		int column = (int) Math.floor((x - layout.left()) / layout.cellSize());
		int row = (int) Math.floor((y - layout.top()) / layout.cellSize());
		if (! isValidXY(column, row)) {
			return null;
		}
		var location = new Grid.Location(column, row);
		if (grid instanceof XYTransformedGrid xyTransformedGrid) {
			location = xyTransformedGrid.getXYTransformer().untransformed(location);
		}
		return location;
	}

	@Override
	protected void layoutChildren() {
		final var layout = cellLayout();
		final double cellSize = layout.cellSize(), left = layout.left(), top = layout.top();

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
		// the stroke is centered on the edge of the rectangle, so the rectangle is half the width outside the cells
		frame.setX(left - frameWidth / 2);
		frame.setY(top - frameWidth / 2);
		frame.setWidth(cellSize * columnCount + frameWidth);
		frame.setHeight(cellSize * rowCount + frameWidth);
	}


	public Grid.Location getGridLocation(Node child) {
		while (child.getParent() != this) {
			child = child.getParent();
			if (child == null) {
				return null;
			}
		}
		int pos = getChildrenUnmodifiable().indexOf(child);
		var location = new Grid.Location(pos % getColumnCount(), pos / getColumnCount());
		if (grid instanceof XYTransformedGrid xyTransformedGrid) {
			location = xyTransformedGrid.getXYTransformer().untransformed(location);
		}
		return location;
	}
}
