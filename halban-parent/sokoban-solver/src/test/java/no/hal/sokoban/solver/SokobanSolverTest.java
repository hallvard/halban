package no.hal.sokoban.solver;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
import no.hal.sokoban.Move;
import no.hal.sokoban.Moves;
import no.hal.sokoban.SokobanGrid;
import no.hal.sokoban.SokobanGrid.CellKind;
import no.hal.sokoban.solver.SokobanSolver.Status;
import org.junit.jupiter.api.Test;

public class SokobanSolverTest {

  /** Plays the moves on the level, independently of the solver, and checks that each is legal and that the boxes end up on the targets. */
  private static void assertSolution(SokobanGrid grid, Moves moves) {
    int w = grid.getWidth(), h = grid.getHeight();
    boolean[][] box = new boolean[w][h];
    boolean[][] wall = new boolean[w][h];
    boolean[][] target = new boolean[w][h];
    int px = grid.getPlayerLocation().x(), py = grid.getPlayerLocation().y();
    for (int y = 0; y < h; y++) {
      for (int x = 0; x < w; x++) {
        CellKind cell = grid.getCell(x, y);
        box[x][y] = cell.content() == SokobanGrid.ContentKind.BOX;
        wall[x][y] = cell.floor() == SokobanGrid.FloorKind.WALL;
        target[x][y] = cell.floor() == SokobanGrid.FloorKind.TARGET;
      }
    }
    for (Move move : moves) {
      int nx = px + move.direction().dx, ny = py + move.direction().dy;
      assertTrue(! wall[nx][ny], "walked into a wall");
      if (box[nx][ny]) {
        assertEquals(Move.Kind.PUSH, move.moveKind(), "pushed with a move that is not a push");
        int bx = nx + move.direction().dx, by = ny + move.direction().dy;
        assertTrue(! wall[bx][by] && ! box[bx][by], "pushed a box where it can't go");
        box[nx][ny] = false;
        box[bx][by] = true;
      } else {
        assertEquals(Move.Kind.MOVE, move.moveKind(), "a push of nothing");
      }
      px = nx;
      py = ny;
    }
    for (int y = 0; y < h; y++) {
      for (int x = 0; x < w; x++) {
        assertTrue(! box[x][y] || target[x][y], "a box is not on a target at " + x + "," + y);
      }
    }
  }

  @Test
  public void solvedLevelNeedsNoMoves() {
    var result = new SokobanSolver(TextGrid.of("####", "#@*#", "####")).solve();
    assertEquals(Status.SOLVED, result.status());
    assertEquals(0, result.pushes());
    assertTrue(! result.moves().iterator().hasNext());
  }

  @Test
  public void onePush() {
    var grid = TextGrid.of("#####", "#@$.#", "#####");
    var result = new SokobanSolver(grid).solve();
    assertEquals(Status.SOLVED, result.status());
    assertEquals(1, result.pushes());
    assertEquals("R", result.moves().getMoves().stream().map(m -> String.valueOf(m.toChar())).reduce("", String::concat).toUpperCase());
    assertSolution(grid, result.moves());
  }

  @Test
  public void walksAroundTheBoxFirst() {
    var grid = TextGrid.of("#####", "#  .#", "# $ #", "#  @#", "#####");
    var result = new SokobanSolver(grid).solve();
    assertEquals(Status.SOLVED, result.status());
    assertSolution(grid, result.moves());
  }

  @Test
  public void boxInACornerIsUnsolvable() {
    var result = new SokobanSolver(TextGrid.of("######", "#$   #", "#  . #", "#   @#", "######")).solve();
    assertEquals(Status.UNSOLVABLE, result.status());
  }

  @Test
  public void squareOfBoxesIsUnsolvable() {
    var result = new SokobanSolver(TextGrid.of("#######", "#  .. #", "# $$  #", "# $$@ #", "#######")).solve();
    assertEquals(Status.UNSOLVABLE, result.status());
  }

  @Test
  public void givesUpAtTheLimit() throws IOException {
    var level = TextGrid.levelsOf("microban-1-30.txt").get(10);
    var result = new SokobanSolver(level).solve(1, Long.MAX_VALUE, () -> false);
    assertEquals(Status.GAVE_UP, result.status());
  }

  @Test
  public void canBeCancelled() throws IOException {
    var level = TextGrid.levelsOf("microban-1-30.txt").get(10);
    var result = new SokobanSolver(level).solve(SokobanSolver.DEFAULT_MAX_STATES, Long.MAX_VALUE, () -> true);
    assertEquals(Status.GAVE_UP, result.status());
  }

  @Test
  public void solvesTheFirstThirtyOfMicroban() throws IOException {
    var levels = TextGrid.levelsOf("microban-1-30.txt");
    assertEquals(30, levels.size());
    long total = 0;
    for (int i = 0; i < levels.size(); i++) {
      var level = levels.get(i);
      long start = System.nanoTime();
      var result = new SokobanSolver(level).solve(SokobanSolver.DEFAULT_MAX_STATES, 20_000, () -> false);
      long millis = (System.nanoTime() - start) / 1_000_000;
      total += millis;
      System.out.printf("Microban %d: %s, %d pushes, %d moves, %d states, %d ms%n", i + 1, result.status(), result.pushes(),
          result.moves() != null ? result.moves().getMoves().size() : 0, result.expanded(), millis);
      assertEquals(Status.SOLVED, result.status(), "Microban " + (i + 1));
      assertNotNull(result.moves());
      assertSolution(level, result.moves());
    }
    System.out.println("Microban 1-30 in " + total + " ms");
  }
}
