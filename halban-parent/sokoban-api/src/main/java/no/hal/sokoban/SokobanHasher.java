package no.hal.sokoban;

import no.hal.grid.Direction;

public interface SokobanHasher {

    Long hash(SokobanGrid grid);

    public class Impl implements SokobanHasher {

        public Long hash(SokobanGrid grid) {
            int w = grid.getWidth(), h = grid.getHeight();
            return grid.reduceCells(0L, (hash, cellKind, x, y) -> {
                long cellHash = cellHash(grid, cellKind, x, y, w, h);
                return hash ^ cellHash;
            });
        }

        private long cellHash(SokobanGrid grid, SokobanGrid.CellKind cellKind, int x, int y, int w, int h) {
            int dx = Math.min(w - x, x + 1), dy = Math.min(h - y, y + 1);
            int distance = dx * dx + dy * dy;
            long cell = cellKindHash(cellKind);
            long left = cellKindHash(grid, x + Direction.LEFT.dx, y + Direction.LEFT.dy, 1);
            long right = cellKindHash(grid, x + Direction.RIGHT.dx, y + Direction.RIGHT.dy, 1);
            long up = cellKindHash(grid, x + Direction.UP.dx, y + Direction.UP.dy, 1);
            long down = cellKindHash(grid, x + Direction.DOWN.dx, y + Direction.DOWN.dy, 1);
            return cell * left * right * up * down * distance;
        }

        private long cellKindHash(SokobanGrid grid, int x, int y, int defaultHash) {
            return (grid.isLegalLocation(x, y) ? cellKindHash(grid.getCell(x, y)) : defaultHash);
        }

        private long cellKindHash(SokobanGrid.CellKind cellKind) {
            return (cellKind.ordinal() + 7) * 31;
        }
    }
}