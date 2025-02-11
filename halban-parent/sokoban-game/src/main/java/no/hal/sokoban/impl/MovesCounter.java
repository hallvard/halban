package no.hal.sokoban.impl;

import no.hal.sokoban.Move;

/**
 * Data class for counting moves and pushes.
 */
public record MovesCounter(int moves, int pushes) {

  private MovesCounter plus(int moves, int pushes) {
    return new MovesCounter(this.moves + moves, this.pushes + pushes);
  }

  /**
   * Returns a new MovesCounter with the sum of the moves and pushes of this and the argument.
   *
   * @param counter the MovesCounter to add
   * @return a new MovesCounter with the sum of the moves and pushes of this and the argument
   */
  public MovesCounter plus(MovesCounter counter) {
    return plus(counter.moves, counter.pushes);
  }

  /**
   * Returns a new MovesCounter with the difference of the moves and pushes of this and the argument.
   *
   * @param counter the MovesCounter to subtract
   * @return a new MovesCounter with the difference of the moves and pushes of this and the argument
   */
  public MovesCounter minus(MovesCounter counter) {
    return plus(-counter.moves, -counter.pushes);
  }

  /**
   * Returns a new MovesCounter with one move or push depending on the Move argument.
   *
   * @param move
   * @return
   */
  public MovesCounter of(Move move) {
    int dm = switch (move.moveKind()) {
      case MOVE -> 1;
      case PUSH -> 0;
    };
    return new MovesCounter(dm, 1 - dm);
  }

  /**
   * Returns a new MovesCounter with the sum of the moves and pushes of this and the Move argument.
   *
   * @param move the Move to add
   * @return a new MovesCounter with the sum of the moves and pushes of this and the Move argument
   */
  public MovesCounter plus(Move move) {
    return plus(of(move));
  }

  /**
   * Returns a new MovesCounter with the difference of the moves and pushes of this and the Move argument.
   *
   * @param move the Move to subtract
   * @return a new MovesCounter with the difference of the moves and pushes of this and the Move argument
   */
  public MovesCounter minus(Move move) {
    return minus(of(move));
  }
}
