package no.hal.sokoban.impl;

import no.hal.gridgame.Direction;
import no.hal.sokoban.Move;
import no.hal.sokoban.Moves;
import no.hal.sokoban.SokobanGame;
import no.hal.sokoban.SokobanGrid;
import no.hal.sokoban.SokobanGrid.CellKind;
import no.hal.sokoban.SokobanGrid.ContentKind;
import no.hal.sokoban.level.SokobanLevel;
import no.hal.gridgame.Grid.Location;
import no.hal.sokoban.parser.SokobanParser;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Queue;

public class SokobanGameImpl extends AbstractSokobanGameProvider implements SokobanGame {

	private final SokobanLevel sokobanLevel;
	private SokobanGridImpl sokobanGrid;

	private SokobanGameImpl(SokobanLevel sokobanLevel, SokobanGridImpl grid, String moves) {
		this.sokobanLevel = sokobanLevel;
		this.sokobanGrid = grid;
		this.moves = new ArrayList<Moves>();
		undoPos = 0;
		if (moves != null) {
			for (int i = 0; i < moves.length(); i++) {
				this.moves.add(new Move(moves.charAt(i)));
			}
			undoPos = moves.length();
		}
	}
	public SokobanGameImpl(SokobanGridImpl grid, String moves) {
		this(null, grid, moves);
	}

	public SokobanGameImpl(SokobanLevel sokobanLevel) {
		this(sokobanLevel, new SokobanGridImpl(sokobanLevel.getSokobanGrid()), sokobanLevel.getMetaData().get("moves"));
	}

	@Override
	public String toString() {
		return SokobanParser.toString(this);
	}

	@Override
	public SokobanLevel getSokobanLevel() {
		return sokobanLevel;
	}
	
	@Override
	public SokobanGame getSokobanGame() {
		return this;
	}

	@Override
	public SokobanGrid getSokobanGrid() {
		return sokobanGrid;
	}

	@Override
	public Location getPlayerLocation() {
		return getSokobanGrid().getPlayerLocation();
	}

	private Boolean doMove(Direction direction, boolean performMove) {
		final int x = getPlayerLocation().x(), y = getPlayerLocation().y();
		int dx = direction.dx, dy = direction.dy;
		boolean isPush = false;
		CellKind forward1 = sokobanGrid.getCell(x + dx, y + dy);
		CellKind forward2 = sokobanGrid.getCell(x + dx + dx, y + dy + dy);
		if (forward1 != null && ! forward1.isOccupied()) {
			// move
		} else if (forward1 != null && forward1.content() == ContentKind.BOX && forward2 != null && ! forward2.isOccupied()) {
			// push
			isPush = true;
		} else {
			// no move or push
			return null;
		}
		if (performMove) {				
			if (isPush) {
				setGridContents(x, y, dx * 3, dy * 3, ContentKind.EMPTY, ContentKind.PLAYER, ContentKind.BOX);
			} else {
				setGridContents(x, y, dx * 2, dy * 2, ContentKind.EMPTY, ContentKind.PLAYER);
			}
		}
		return isPush;
	}

	private void setGridContents(int x, int y, int w, int h, ContentKind... contents) {
		sokobanGrid.setContents(x, y, w == 0 ? 1 : w, h == 0 ? 1 : h, contents);
	}

	private Boolean canMove(Direction direction, Boolean allowedMove) {
		var isPush = doMove(direction, false);
		return isPush != null && (allowedMove == null || isPush == allowedMove) ? isPush : null;
	}

	private Boolean doMove(Direction direction) {
		return doMove(direction, true);
	}

	private void undoMove(Direction direction, boolean wasPush) {
		int dx = direction.dx, dy = direction.dy;
		final int x = getPlayerLocation().x() - dx, y = getPlayerLocation().y() - dy;
		if (wasPush) {
			setGridContents(x, y, dx * 3, dy * 3, ContentKind.PLAYER, ContentKind.BOX, ContentKind.EMPTY);
		} else {
			setGridContents(x, y, dx * 2, dy * 2, ContentKind.PLAYER, ContentKind.EMPTY);
		}
	}
			
	private List<Moves> moves;
	private int undoPos = 0;
	
	@Override
	public List<Move> getMoves() {
		List<Move> copy = new ArrayList<>();
		for (var moves : this.moves) {
			copy.addAll(moves.getMoves());
			if (copy.size() >= undoPos) {
				break;
			}
		}
		return copy;
	}
	
	@Override
	public Boolean movePlayer(Direction direction) {
		Boolean isPush = (direction != null ? doMove(direction) : null);
		if (isPush != null) {
			pushMoves(new Move(direction, isPush));
		}
		return isPush;
	}

	@Override
	public boolean movePlayer(Moves moves) {
		for (var move: moves) {
			var isPush = movePlayer(move.direction());
			if (isPush == null || ! isPush.booleanValue() == move.isPush()) {
				return false;
			}
		}
		return true;
	}

	private void pushMoves(Moves moves) {
		while (this.moves.size() > undoPos) {
			this.moves.remove(this.moves.size() - 1);
		}
		this.moves.add(moves);
		undoPos++;
		for (var move: moves) {
			fireMoveDone(move);
		}
	}

	@Override
	public boolean canUndo() {
		return undoPos > 0;
	}
	
	private void doMoves(Moves moves, Boolean pushEach) {
		for (var move : moves) {
			Direction direction = move.direction();
			if (Boolean.TRUE.equals(pushEach)) {
				movePlayer(direction);
			} else {
				doMove(direction);
			}
		}
		if (Boolean.FALSE.equals(pushEach)) {
			pushMoves(moves);
		}
	}
	
	@Override
	public void undo() {
		if (canUndo()) {
			undoPos--;
			var moves = this.moves.get(undoPos).getMoves();
			for (int i = moves.size() - 1; i >= 0; i--) {
				var move = moves.get(i);
				undoMove(move.direction(), move.isPush());
				fireMoveUndone(move);
			}
		}
	}

	@Override
	public boolean canRedo() {
		return undoPos < moves.size();
	}
	
	@Override
	public void redo() {
		if (canRedo()) {
			Moves moveCommand = moves.get(undoPos);
			undoPos++;
			doMoves(moveCommand, null);
		}
	}

	//
	
	private Moves computeMovesToGoal(int goalX, int goalY) {
		CellKind goalValue = sokobanGrid.getCell(goalX, goalY);
		if (goalValue.isOccupied()) {
			return null;
		}
		Map<SokobanGrid.Location, Direction> directions = new HashMap<>();
		// we extend a boundary, like riples in the water, from the starting point
		Queue<Location> boundary = new LinkedList<Location>();
		boundary.add(getPlayerLocation());
		// as long as there are more cells to consider
		List<Move> moves = null;
		while (boundary.size() > 0) {
			// remove current position
			Location location = boundary.poll();
			for (Direction direction : Direction.values()) {
				int nx = location.x() + direction.dx, ny = location.y() + direction.dy;
				CellKind cellKind = sokobanGrid.getCell(nx, ny);
				Location nextLocation = new Location(nx, ny);
				// if this is a new and unoccupied cell
				if (directions.get(nextLocation) == null && !cellKind.isOccupied()) {
					// note the direction we came from
					directions.put(nextLocation, direction);
					// if this is goal, walk backwards (the opposite direction) and collect moves
					if (nx == goalX && ny == goalY) {
						moves = new ArrayList<>();
						while (nx != getPlayerLocation().x() || ny != getPlayerLocation().y()) {
							Direction moveDirection = directions.get(new Location(nx, ny));
							moves.add(new Move(moveDirection));
							nx -= moveDirection.dx;
							ny -= moveDirection.dy;
						}
						Collections.reverse(moves);
						break;
					}
					// enqueue this position, so we can consider it later
					boundary.offer(nextLocation);
				}
			}
		}
		return moves != null ? Moves.of(moves) : null;
	}
	
	@Override
	public Moves movePlayerTo(int x, int y) {
		Moves moves = computeMovesToGoal(x, y);
		if (moves == null) {
			return null;
		}
		doMoves(moves, true);
		return moves;
	}

	@Override
	public Moves movePlayerAlong(Direction direction) {
		List<Move> moves = new ArrayList<>();
		while (true) {
			Direction moveDirection = null;
			for (Direction dir : Direction.values()) {
				// don't go backwards
				if (dir != direction.opposite()) {
					var isPush = canMove(dir, null);
					if (isPush != null) {
						// if move is push or not only choice, stop
						if (isPush || moveDirection != null) {
							moveDirection = null;
							break;
						} else {
							moveDirection = dir;
						}
					}
				}
			}
			if (moveDirection == null) {
				break;
			}
			// only one choice that isn't a push
			movePlayer(moveDirection);
			moves.add(new Move(moveDirection));
			direction = moveDirection;
		}
		return Moves.of(moves);
	}
	
	@Override
	public Moves moveBox(int x, int y, Direction direction) {
		CellKind box = sokobanGrid.getCell(x, y);
		if (box.content() != ContentKind.BOX || direction == null) {
			return null;
		}
		int dx = direction.dx, dy = direction.dy;
		CellKind goal = sokobanGrid.getCell(x + dx, y + dy);
		if (goal == null || (goal.isOccupied() && goal.content() != ContentKind.PLAYER)) {
			return null;
		}
		CellKind player = sokobanGrid.getCell(x - dx, y - dy);
		if (player == null || (player.isOccupied() && player.content() != ContentKind.PLAYER)) {
			return null;
		}
		List<Move> allMoves = new ArrayList<>();
		if (player.content() != ContentKind.PLAYER) {
			Moves moves = movePlayerTo(x - dx, y - dy);
			if (moves == null) {
				return null;
			}
			allMoves.addAll(moves.getMoves());
		}
		var isPush = movePlayer(direction);
		if (isPush != null) {
			allMoves.add(new Move(direction, isPush));
		}
		return Moves.of(allMoves);
	}
}
