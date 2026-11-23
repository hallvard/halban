package no.hal.sokoban.fx.controllers;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.List;
import no.hal.grid.Direction;
import no.hal.sokoban.Move;
import no.hal.sokoban.Moves;
import org.junit.jupiter.api.Test;

public class HintStepsTest {

  private static Move walk(Direction d) {
    return new Move(d, Move.Kind.MOVE);
  }

  private static Move push(Direction d) {
    return new Move(d, Move.Kind.PUSH);
  }

  private static List<List<Move>> sizes(Moves moves) {
    return SokobanGameController.stepsOf(moves).stream().map(Moves::getMoves).toList();
  }

  @Test
  public void walksAndPushesAreSeparateSteps() {
    var moves = Moves.of(List.of(walk(Direction.LEFT), walk(Direction.UP), push(Direction.UP), push(Direction.UP), walk(Direction.RIGHT), push(Direction.RIGHT)));
    assertEquals(List.of(
        List.of(walk(Direction.LEFT), walk(Direction.UP)),
        List.of(push(Direction.UP)),
        List.of(push(Direction.UP)),
        List.of(walk(Direction.RIGHT)),
        List.of(push(Direction.RIGHT))), sizes(moves));
  }

  @Test
  public void aPushWithoutAWalkIsAStepOfItsOwn() {
    assertEquals(List.of(List.of(push(Direction.DOWN))), sizes(Moves.of(List.of(push(Direction.DOWN)))));
  }

  @Test
  public void noMovesGiveNoSteps() {
    assertEquals(List.of(), sizes(Moves.of(List.of())));
  }
}
