package no.hal.sokoban.impl;

import no.hal.sokoban.Move;

public record MovesCounter(int moves, int pushes) {

    private MovesCounter plus(int moves, int pushes) {
        return new MovesCounter(this.moves + moves, this.pushes + pushes);
    }
    
    public MovesCounter plus(MovesCounter counter) {
        return plus(counter.moves, counter.pushes);
    }
    public MovesCounter minus(MovesCounter counter) {
        return plus(-counter.moves, -counter.pushes);
    }
    
    public MovesCounter of(Move move) {
        int dm = move.isPush() ? 0 : 1;
        return new MovesCounter(dm, 1 - dm);
    }
    public MovesCounter plus(Move move) {
        return plus(of(move));
    }
    public MovesCounter minus(Move move) {
        return minus(of(move));
    }
}
