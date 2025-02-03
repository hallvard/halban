package no.hal.gridgame;

public interface UndoActions {

	/**
	 * Gets the size of the undo stack.
   *
	 * @return the size of the undo stack
	 */
	int undoCount();

  /**
   * Tells if the previous action can be undone.
   * @return if undo can be performed
   */
  default boolean canUndo() {
    return undoCount() > 0;
  }
	
	/**
	 * Undoes n previous moves.
   *
   * Returns the number of moves actually undone.
	 */
	int undo(int n);

  /**
	 * Undoes a previous move.
   *
   * @return if a move was undone
	 */
	default boolean undo() {
    return undo(1) == 1;
  }

	/**
	 * Gets the size of the redo stack.
   *
	 * @return the size of the redo stack
	 */
  int redoCount();

	/**
	 * Tells if there is previously undone action that can be redone.
	 * @return if redo can be performed
	 */
	default boolean canRedo() {
    return redoCount() > 0;
  }

	/**
	 * Redoes n previously undone moves.
   *
   * Returns the number of moves actually redone.
	 */
	int redo(int n);

	/**
	 * Redoes a previously undone move.
	 */
	default boolean redo() {
    return redo(1) == 1;
  }
}
