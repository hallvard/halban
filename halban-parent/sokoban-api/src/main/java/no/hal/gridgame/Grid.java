package no.hal.gridgame;

public interface Grid<T> extends ObservableGrid {

	/**
	 * @return The width of the grid
	 */
	int getWidth();
	/**
	 * @return The height of the grid
	 */
	int getHeight();
	
	/**
	 * Gets the cell type.
	 * @param x The x-coordinate of the cell
	 * @param y The y-coordinate of the cell
	 * @return The cell type
	 */
	T getCell(int x, int y);

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
	void forEachCell(CellConsumer<T> consumer, int x, int y, int w, int h);

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
	<R> R reduceCells(R r, CellFunction<T, R> reducer, int x, int y, int w, int h);

	default <R> R reduceCells(R r, CellFunction<T, R> reducer) {
		return reduceCells(r, reducer, 0, 0, getWidth(), getHeight());
	}

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
