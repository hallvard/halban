package no.hal.sokoban;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import no.hal.grid.Grid;

public class LocationMovesCounters {

    private MovesCounter totalMoves = null;
    private Map<SokobanGrid.Location, MovesCounter> locationMoves = new HashMap<>();

    public LocationMovesCounters() {
        clear();
    }

    public LocationMovesCounters(Grid.Location currentLocation, List<Move> initialMoves) {
        clear(currentLocation, initialMoves);
    }

    @Override
    public String toString() {
        return totalMoves + ":" + locationMoves;
    }

    public void clear() {
        totalMoves = MovesCounter.NO_MOVES;
        locationMoves.clear();
    }
    public void clear(Grid.Location currentLocation, List<Move> initialMoves) {
        clear();
        for (int i = initialMoves.size() - 1; i >= 0; i--) {
            var move = initialMoves.get(i);
            plus(currentLocation, move);
            currentLocation = currentLocation.from(move.direction());
        }
    }

    public MovesCounter getCounter() {
        return totalMoves;
    }
    public MovesCounter getCounter(Grid.Location location) {
        return locationMoves.getOrDefault(location, MovesCounter.NO_MOVES);
    }
    public MovesCounter getCounter(int x, int y) {
        return getCounter(new Grid.Location(x, y));
    }

    public void plus(Grid.Location location, Move move) {
        totalMoves =  totalMoves.plus(move);
        locationMoves.put(location, getCounter(location).plus(move));
    }

    public void minus(Grid.Location location, Move move) {
        totalMoves = totalMoves.minus(move);
        MovesCounter counter = getCounter(location).minus(move);
        if (counter.moves() == 0 && counter.pushes() == 0) {
            locationMoves.remove(location, counter);
        } else {
            locationMoves.put(location, counter);
        }
    }
}
