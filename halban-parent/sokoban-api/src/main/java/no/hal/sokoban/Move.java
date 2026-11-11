package no.hal.sokoban;

import java.util.Collections;
import java.util.List;

import no.hal.grid.Direction;

public record Move(Direction direction, Kind moveKind) implements Moves {

	public enum Kind {
		MOVE, PUSH
	}

	public Move(Direction direction) {
		this(direction, Kind.MOVE);
	}

  public static Move valueOf(char c) {
    return new Move(Direction.valueOf(c), Move.moveKind(c));
  }

	public static Kind moveKind(char c) {
		return Character.isUpperCase(c) ? Kind.PUSH : Kind.MOVE;
	}

	public char toChar() {
		char c = direction.toChar();
		return moveKind() == Kind.PUSH ? Character.toUpperCase(c) : c;
	}
	
	@Override
	public List<Move> getMoves() {
		return Collections.singletonList(this);
	}
}
