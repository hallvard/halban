package no.hal.sokoban.impl;

import java.util.ArrayList;
import java.util.List;
import no.hal.grid.Direction;
import no.hal.grid.Grid.Location;
import no.hal.sokoban.Move;
import no.hal.sokoban.Moves;
import no.hal.sokoban.SokobanGame;
import no.hal.sokoban.SokobanGrid;
import no.hal.sokoban.SokobanGrid.ContentKind;
import no.hal.sokoban.level.SokobanLevel;
import no.hal.sokoban.parser.SokobanSerializer;

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

	public SokobanGameImpl(SokobanGrid grid) {
		this(new SokobanGridImpl(grid), null);
	}

	public SokobanGameImpl(SokobanLevel sokobanLevel) {
		this(sokobanLevel, new SokobanGridImpl(sokobanLevel.getSokobanGrid()), sokobanLevel.getMetaData().get("moves"));
	}

	private final SokobanSerializer sokobanSerializer = new SokobanSerializer();

	@Override
	public String toString() {
		return sokobanSerializer.toString(this);
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

	// moves

	private void setGridContents(int x, int y, int w, int h, ContentKind... contents) {
		sokobanGrid.setContents(x, y, w == 0 ? 1 : w, h == 0 ? 1 : h, contents);
	}

	private Move.Kind doMove(Direction direction, Move.Kind expectedMoveKind) {
		var moveKind = MovesComputer.canMove(this, direction, expectedMoveKind);
		if (moveKind != null) {
			final int x = getPlayerLocation().x(), y = getPlayerLocation().y();
			int dx = direction.dx, dy = direction.dy;
				if (moveKind == Move.Kind.PUSH) {
				setGridContents(x, y, dx * 3, dy * 3, ContentKind.EMPTY, ContentKind.PLAYER, ContentKind.BOX);
			} else {
				setGridContents(x, y, dx * 2, dy * 2, ContentKind.EMPTY, ContentKind.PLAYER);
			}
		}
		return moveKind;
	}

	private void undoMove(Direction direction, Move.Kind moveKind) {
		int dx = direction.dx, dy = direction.dy;
		final int x = getPlayerLocation().x() - dx, y = getPlayerLocation().y() - dy;
		if (moveKind == Move.Kind.PUSH) {
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
		for (var i = 0; i < undoPos; i++) {
			copy.addAll(moves.get(i).getMoves());
		}
		return copy;
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
	public Move.Kind movePlayer(Direction direction) {
		Move.Kind moveKind = doMove(direction, null);
		if (moveKind != null) {
			pushMoves(new Move(direction, moveKind));
		}
		return moveKind;
	}

	@Override
	public Moves movePlayer(Moves moves) {
		List<Move> actualMoves = new ArrayList<>();
		for (var move : moves) {
			var moveKind = doMove(move.direction(), move.moveKind());
			if (moveKind == null) {
				break;
			}
			actualMoves.add(move);
		}
		var result = Moves.of(actualMoves);
		pushMoves(result);
		return result;
	}

	@Override
	public int undoCount() {
		return undoPos;
	}
	
	@Override
	public int undo(int count) {
    int undone = 0;
		while (canUndo() && undone < count) {
			undoPos--;
			var moves = this.moves.get(undoPos).getMoves();
			for (int i = moves.size() - 1; i >= 0; i--) {
				var move = moves.get(i);
				undoMove(move.direction(), move.moveKind());
				fireMoveUndone(move);
			}
      undone++;
		}
    return undone;
	}

	@Override
	public int redoCount() {
		return moves.size() - undoPos;
	}
	
	@Override
	public int redo(int count) {
    int redone = 0;
		while (canRedo() && redone < count) {
			Moves moves = this.moves.get(undoPos);
			undoPos++;
			for (var move : moves) {
				doMove(move.direction(), move.moveKind());
				fireMoveDone(move);
			}
      redone++;
		}
    return redone;
	}
}
