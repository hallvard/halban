package no.hal.sokoban;

import java.util.Collection;
import java.util.Iterator;
import java.util.List;

public interface Moves extends Iterable<Move> {
	
	public List<Move> getMoves();

	@Override
	default Iterator<Move> iterator() {
		return getMoves().iterator();
	}

	public static Moves of(Collection<Move> moves) {
		return () -> List.copyOf(moves);
	}
}
