package no.hal.sokoban;

import no.hal.gridgame.Grid;

public interface SokobanGrid extends Grid<SokobanGrid.CellKind> {
	
	/**
	 * @return the player's location
	 */
	Location getPlayerLocation();

	/**
	 * Count the number of cells matching a particular combination of static and dynamic cell value.
	 * @param floor The static part to match, or null if it should be ignored when matching.
	 * @param content The dynamic part to match, or null if it should be ignored when matching.
	 * @return The number of cells of a particular kind, i.e. matching cellStatic and cellDynamic
	 */
	int countCells(FloorKind floor, ContentKind content);

	/**
	 * 
	 */
	public enum FloorKind {
		EMPTY, TARGET, WALL
	}

	public enum ContentKind {
		EMPTY, PLAYER, BOX
	}

	public enum CellKind {
		EMPTY, EMPTY_PLAYER, EMPTY_BOX, TARGET, TARGET_PLAYER, TARGET_BOX, WALL;

		public FloorKind floor() {
			return switch (this) {
				case WALL -> FloorKind.WALL;
				case EMPTY, EMPTY_PLAYER, EMPTY_BOX -> FloorKind.EMPTY;
				case TARGET, TARGET_PLAYER, TARGET_BOX -> FloorKind.TARGET;
			};
		}

		public ContentKind content() {
			return switch (this) {
				case WALL, EMPTY, TARGET -> ContentKind.EMPTY;
				case EMPTY_PLAYER, TARGET_PLAYER -> ContentKind.PLAYER;
				case EMPTY_BOX, TARGET_BOX -> ContentKind.BOX;
			};
		}

		public boolean isOccupied() {
			return switch (this) {
				case EMPTY, TARGET -> false;
				case WALL, EMPTY_PLAYER, TARGET_PLAYER, EMPTY_BOX, TARGET_BOX -> true;
			};
		}

		public static CellKind valueOf(FloorKind floor, ContentKind content) {
			return switch (floor) {
				case WALL -> CellKind.WALL;
				case EMPTY -> switch (content) {
					case EMPTY -> CellKind.EMPTY;
					case PLAYER -> CellKind.EMPTY_PLAYER;
					case BOX -> CellKind.EMPTY_BOX;
				};
				case TARGET -> switch (content) {
					case EMPTY -> CellKind.TARGET;
					case PLAYER -> CellKind.TARGET_PLAYER;
					case BOX -> CellKind.TARGET_BOX;
				};
			};
		}
	}
}
