package no.hal.sokoban.impl;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Queue;

import no.hal.gridgame.Direction;
import no.hal.sokoban.Move;
import no.hal.sokoban.Moves;
import no.hal.sokoban.SokobanGameState;
import no.hal.sokoban.SokobanGrid;
import no.hal.sokoban.SokobanGrid.CellKind;
import no.hal.sokoban.SokobanGrid.ContentKind;
import no.hal.gridgame.Grid.Location;

public class MovesComputer {
    
    public static Moves computeMovesTo(SokobanGameState sokobanGameState, int x, int y) {
        var sokobanGrid = sokobanGameState.getSokobanGrid();
        var playerLocation = sokobanGameState.getPlayerLocation();
		CellKind goalValue = sokobanGrid.getCell(x, y);
		if (goalValue.isOccupied()) {
			return null;
		}
		Map<SokobanGrid.Location, Direction> directions = new HashMap<>();
		// we extend a boundary, like riples in the water, from the starting point
		Queue<Location> boundary = new LinkedList<Location>();
		boundary.add(sokobanGameState.getPlayerLocation());
		// as long as there are more cells to consider
		List<Move> moves = null;
		while (boundary.size() > 0) {
			// remove current position
			Location location = boundary.poll();
			for (Direction direction : Direction.values()) {
				int nx = location.x() + direction.dx, ny = location.y() + direction.dy;
				Location nextLocation = sokobanGrid.locationFor(nx, ny);
				// if this is a legal, new and unoccupied cell
				if (nextLocation != null && directions.get(nextLocation) == null && ! sokobanGrid.getCell(nx, ny).isOccupied()) {
					// note the direction we came from
					directions.put(nextLocation, direction);
					// if this is the goal, walk backwards (the opposite direction) and collect moves
					if (nx == x && ny == y) {
						moves = new ArrayList<>();
						while (nx != playerLocation.x() || ny != playerLocation.y()) {
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

    public static Move.Kind canMove(SokobanGameState sokobanGameState, int x, int y, Direction direction, Move.Kind allowedMoveKind) {
        var sokobanGrid = sokobanGameState.getSokobanGrid();
		int dx = direction.dx, dy = direction.dy;
		CellKind forward1 = sokobanGrid.isLegalLocation(x + dx, y + dy) ? sokobanGrid.getCell(x + dx, y + dy) : null;
		CellKind forward2 = sokobanGrid.isLegalLocation(x + dx + dx, y + dy + dy) ? sokobanGrid.getCell(x + dx + dx, y + dy + dy) : null;
		// assume move
		Move.Kind moveKind = Move.Kind.MOVE;
		if (forward1 != null && ! forward1.isOccupied()) {
			// yes, move
		} else if (forward1 != null && forward1.content() == ContentKind.BOX && forward2 != null && ! forward2.isOccupied()) {
			// no, is push
			moveKind = Move.Kind.PUSH;
		} else {
			// neither move nor push
			return null;
		}
		return (allowedMoveKind != null && moveKind != allowedMoveKind ? null : moveKind);
	}

	public static Move.Kind canMove(SokobanGameState sokobanGameState, Direction direction, Move.Kind allowedMoveKind) {
        var playerLocation = sokobanGameState.getPlayerLocation();
		return canMove(sokobanGameState, playerLocation.x(), playerLocation.y(), direction, allowedMoveKind);
	}

	public static Moves computeMovesAlong(SokobanGameState sokobanGameState, Direction direction) {
        var playerLocation = sokobanGameState.getPlayerLocation();
		int x = playerLocation.x(), y = playerLocation.y();
		List<Move> moves = new ArrayList<>();
		if (canMove(sokobanGameState, direction, Move.Kind.MOVE) != null) {
			moves.add(new Move(direction));
			while (true) {
				x += direction.dx;
				y += direction.dy;
				Direction moveDirection = null;
				for (Direction dir : Direction.values()) {
					// don't go backwards
					if (dir != direction.opposite()) {
						Move.Kind moveKind = canMove(sokobanGameState, x, y, dir, null);
						if (moveKind != null) {
							// if move is push or not only choice, stop
							if (moveKind == Move.Kind.PUSH || moveDirection != null) {
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
				moves.add(new Move(moveDirection));
				direction = moveDirection;
			}
		}
		return Moves.of(moves);
	}

    public static Moves computeBoxMoves(SokobanGameState sokobanGameState, int x, int y, Direction direction) {
        var sokobanGrid = sokobanGameState.getSokobanGrid();
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
			Moves moves = computeMovesTo(sokobanGameState, x - dx, y - dy);
			if (moves == null) {
				return null;
			}
			allMoves.addAll(moves.getMoves());
		}
        allMoves.add(new Move(direction, Move.Kind.PUSH));
		return Moves.of(allMoves);
    }
}
