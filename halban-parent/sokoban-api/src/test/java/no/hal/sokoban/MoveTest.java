package no.hal.sokoban;

import static org.junit.jupiter.api.Assertions.assertEquals;

import no.hal.grid.Direction;
import org.junit.jupiter.api.Test;

public class MoveTest {
  
  @Test
  void testValueOf() {
    assertEquals(new Move(Direction.UP, Move.Kind.MOVE), Move.valueOf('u'));
    assertEquals(new Move(Direction.DOWN, Move.Kind.PUSH), Move.valueOf('D'));
  }

  @Test
  void testToChar() {
    assertEquals('u', new Move(Direction.UP, Move.Kind.MOVE).toChar());
    assertEquals('D', new Move(Direction.DOWN, Move.Kind.PUSH).toChar());
  }
}
