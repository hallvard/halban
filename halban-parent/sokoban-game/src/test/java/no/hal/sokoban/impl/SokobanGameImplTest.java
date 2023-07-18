package no.hal.sokoban.impl;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
import java.util.Iterator;
import java.util.List;

import no.hal.gridgame.Direction;
import no.hal.gridgame.Grid;
import no.hal.sokoban.Move;
import no.hal.sokoban.Moves;
import no.hal.sokoban.SokobanGame;
import no.hal.sokoban.SokobanGrid.CellKind;
import no.hal.sokoban.SokobanGrid.ContentKind;
import no.hal.sokoban.SokobanGrid.FloorKind;
import no.hal.sokoban.parser.SokobanParser;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public class SokobanGameImplTest {
    
    private String levelString = """
        ######
        #    #
        #@$ .#
        #    #
        ######
        """;

    private SokobanGame sokobanGame;
    
    @BeforeEach
    public void setUp() throws IOException {
        sokobanGame = new SokobanGameImpl(SokobanParser.getSokobanGrid(levelString));
    }

    private void checkCellKind(CellKind cellKind, int x, int y) {
        assertEquals(cellKind, sokobanGame.getSokobanGrid().getCell(x, y));
    }
    private void checkCellContentKind(ContentKind contentKind, int x, int y) {
        assertEquals(contentKind, sokobanGame.getSokobanGrid().getCell(x, y).content());
    }
    private void checkCellFloorKind(FloorKind floorKind, int x, int y) {
        assertEquals(floorKind, sokobanGame.getSokobanGrid().getCell(x, y).floor());
    }

    private void checkPlayerLocation(int x, int y) {
        assertEquals(new Grid.Location(x, y), sokobanGame.getPlayerLocation());
    }

    private void checkMoves(List<Move> expected, Moves moves) {
        var movesList = moves.getMoves();
        assertEquals(expected.size(), movesList.size(), "Lengths of moves differ");
        Iterator<Move> it1 = expected.iterator(), it2 = moves.getMoves().iterator();
        while (it1.hasNext()) {
            assertTrue(it2.hasNext());
            assertEquals(it1.next(), it2.next());
        }
        assertFalse(it2.hasNext());
    }
    private void checkMoveDirections(List<Direction> expected, Moves moves) {
        checkMoves(expected.stream().map(direction -> new Move(direction)).toList(), moves);
    }

    @Test
    public void testSetUp() {
        checkCellKind(CellKind.EMPTY_PLAYER, 1, 2);
        checkPlayerLocation(1, 2);
        checkCellKind(CellKind.EMPTY_BOX, 2, 2);
        checkCellKind(CellKind.TARGET, 4, 2);
    }

    @Test
    public void testCanMove() {
        assertEquals(Move.Kind.MOVE, MovesComputer.canMove(sokobanGame, Direction.UP, null));
        assertEquals(Move.Kind.PUSH, MovesComputer.canMove(sokobanGame, Direction.RIGHT, null));
        assertEquals(Move.Kind.MOVE, MovesComputer.canMove(sokobanGame, Direction.DOWN, null));
        assertNull(MovesComputer.canMove(sokobanGame, Direction.LEFT, null));
    }

    @Test
    public void testMovePlayerUp() {
        assertEquals(Move.Kind.MOVE, sokobanGame.movePlayer(Direction.UP));
        checkCellKind(CellKind.EMPTY, 1, 2);
        checkCellKind(CellKind.EMPTY_PLAYER, 1, 1);
        checkPlayerLocation(1, 1);
    }

    @Test
    public void testMovePlayerRight() {
        assertEquals(Move.Kind.PUSH, sokobanGame.movePlayer(Direction.RIGHT));
        checkCellKind(CellKind.EMPTY, 1, 2);
        checkCellKind(CellKind.EMPTY_PLAYER, 2, 2);
        checkPlayerLocation(2, 2);
    }

    @Test
    public void testMovePlayerDown() {
        assertEquals(Move.Kind.MOVE, sokobanGame.movePlayer(Direction.DOWN));
        checkCellKind(CellKind.EMPTY, 1, 2);
        checkCellKind(CellKind.EMPTY_PLAYER, 1, 3);
        checkPlayerLocation(1, 3);
    }

    @Test
    public void testMovePlayerLeft() {
        assertNull(sokobanGame.movePlayer(Direction.LEFT));
        checkCellKind(CellKind.EMPTY_PLAYER, 1, 2);
        checkPlayerLocation(1, 2);
    }

    @Test
    public void testComputeMoveTo() {
        var moves = MovesComputer.computeMovesTo(sokobanGame, 2, 1);
        checkMoveDirections(List.of(Direction.UP, Direction.RIGHT), moves);
    }

    @Test
    public void testComputeMovesAlong() {
        var moves = MovesComputer.computeMovesAlong(sokobanGame, Direction.UP);
        checkMoveDirections(List.of(Direction.UP, Direction.RIGHT), moves);
    }

    @Test
    public void testComputeBoxMoves() {
        var moves = MovesComputer.computeBoxMoves(sokobanGame, 2, 2, Direction.DOWN);
        checkMoves(List.of(new Move(Direction.UP, Move.Kind.MOVE), new Move(Direction.RIGHT, Move.Kind.MOVE), new Move(Direction.DOWN, Move.Kind.PUSH)), moves);
    }

    @Test
    public void testMovePlayer_Moves() {
        var moves = MovesComputer.computeBoxMoves(sokobanGame, 2, 2, Direction.DOWN);
        sokobanGame.movePlayer(moves);
        checkBoxMoves();
    }

    private void checkBoxMoves() {
        checkCellKind(CellKind.EMPTY_PLAYER, 2, 2);
        checkPlayerLocation(2, 2);
        checkCellKind(CellKind.EMPTY_BOX, 2, 3);
    }

    @Test
    public void testUndo() {
        var moves = MovesComputer.computeBoxMoves(sokobanGame, 2, 2, Direction.DOWN);
        sokobanGame.movePlayer(moves);
        sokobanGame.undo();
        testSetUp();
    }

    @Test
    public void testRedo() {
        var moves = MovesComputer.computeBoxMoves(sokobanGame, 2, 2, Direction.DOWN);
        sokobanGame.movePlayer(moves);
        sokobanGame.undo();
        sokobanGame.redo();
        checkBoxMoves();
    }
}
