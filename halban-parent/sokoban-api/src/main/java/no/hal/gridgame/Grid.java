package no.hal.gridgame;

public interface Grid<T> {

	/**
	 * @return The width of the grid
	 */
	int getWidth();
	/**
	 * @return The height of the grid
	 */
	int getHeight();
	
	default boolean isLegalLocation(int x, int y) {
		return x >= 0 && y >= 0 && x < getWidth() && y < getHeight();
	}

	default boolean isLegal(Location location) {
		return isLegalLocation(location.x(), location.y());
	}

	default Location locationFor(int x, int y) {
		return isLegalLocation(x, y) ? new Location(x, y) : null;
	}

	/**
	 * Gets the cell type.
	 * @param x The x-coordinate of the cell
	 * @param y The y-coordinate of the cell
	 * @return The cell type
	 */
	T getCell(int x, int y);

	default T getCell(Location location) {
		return getCell(location.x(), location.y());
	}

	public interface CellConsumer<T> {
		void accept(T t, int x, int y);
	}
	
	/**
	 * Applies consumer to each cell in the rectangle @ x,y with dimensions w x h 
	 * @param fun
	 * @param x
	 * @param y
	 * @param w
	 * @param h
	 */
	default void forEachCell(CellConsumer<T> consumer, int x, int y, int w, int h) {
		for (int dy = 0; dy < h; dy++) {
            for (int dx = 0; dx < w; dx++) {
                consumer.accept(getCell(x + dx, y + dy), x + dx, y + dy);
            }
        }
	}

	default void forEachCell(CellConsumer<T> consumer) {
		forEachCell(consumer, 0, 0, getWidth(), getHeight());
	}

	public interface CellFunction<T, R> {
		R reduce(R r, T t, int x, int y);
	}

	/**
	 * Applies reducer to each cell in the rectangle @ x,y with dimensions w x h
	 * @param fun
	 * @param x
	 * @param y
	 * @param w
	 * @param h
	 * @param r
	 */
	default <R> R reduceCells(R r, CellFunction<T, R> reducer, int x, int y, int w, int h) {
		for (int dy = 0; dy < h; dy++) {
            for (int dx = 0; dx < w; dx++) {
                r = reducer.reduce(r, getCell(x + dx, y + dy), x + dx, y + dy);
            }
        }
        return r;
	}

	default <R> R reduceCells(R r, CellFunction<T, R> reducer) {
		return reduceCells(r, reducer, 0, 0, getWidth(), getHeight());
	}
	
	public interface Listener<T> {
		/**
		 * Notifies the listener that the grid dimensions (and contents) has changed.
		 * @param grid the grid that has changed
		 * @param w the width of the new dimensions
		 * @param h the height of the new dimensions
		 */
		public void gridDimensionsChanged(Grid<T> grid, int w, int h);
		/**
		 * Notifies the listener that the grid contents has changed. The changed region is a rectangle at x,y with dimensions w,h.
		 * @param grid the grid that has changed
		 * @param x the x coordinate of the changed rectangle
		 * @param y the y coordinate of the changed rectangle
		 * @param w the width of the changed rectangle
		 * @param h the height of the changed rectangle
		 */
		public void gridContentsChanged(Grid<T> grid, int x, int y, int w, int h);
	}
	
		/**
	 * Adds the listener, so it will be notified when the grid changes
	 * @param gridListener the listener to add
	 */
	public void addGridListener(Listener<T> gridListener);

	/**
	 * Removes the listener, so it no longer will be notified when the grid changes
	 * @param gridListener the listener to remove
	 */
	public void removeGridListener(Listener<T> gridListener);

	/**
	 * Location within the grid
	 * @param x the x-coordinate
	 * @param y the y-coordinate
	 */
	public record Location(int x, int y) {

		@Override
		public String toString() {
			return "@" + x + "," + y;
		}

		public Location from(Direction direction) {
			return new Location(x - direction.dx, y - direction.dy);
		}
		public Location to(Direction direction) {
			return new Location(x + direction.dx, y + direction.dy);
		}
	}
}
