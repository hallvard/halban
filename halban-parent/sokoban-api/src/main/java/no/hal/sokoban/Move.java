package no.hal.sokoban;

import java.util.Collections;
import java.util.List;

import no.hal.gridgame.Direction;

public record Move(Direction direction, boolean isPush) implements Moves {

	public Move(Direction direction) {
		this(direction, false);
	}

	public Move(char c) {
		this(Direction.valueOf(c), isPush(c));
	}

	public static boolean isPush(char c) {
		return Character.isUpperCase(c);
	}

	public char toChar() {
		char c = direction.toChar();
		return isPush ? toPush(c) : c;
	}

	public static char toPush(char c) {
		return Character.toUpperCase(c);
	}
	
	@Override
	public List<Move> getMoves() {
		return Collections.singletonList(this);
	}
}
