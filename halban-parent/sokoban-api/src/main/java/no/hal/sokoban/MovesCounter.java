package no.hal.sokoban;

public record MovesCounter(int moves, int pushes) {

    public final static MovesCounter NO_MOVES = new MovesCounter();

    public MovesCounter() {
        this(0, 0);
    }

    @Override
    public String toString() {
        return "#" + moves + "/" + pushes;
    }

    public int sum() {
        return moves + pushes;
    }

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
        int dm = switch (move.moveKind()) {
            case MOVE -> 1;
            case PUSH -> 0;
        };
        return new MovesCounter(dm, 1 - dm);
    }
    public MovesCounter plus(Move move) {
        return plus(of(move));
    }
    public MovesCounter minus(Move move) {
        return minus(of(move));
    }
}
