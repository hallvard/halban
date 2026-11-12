package no.hal.sokoban;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.HexFormat;
import no.hal.grid.Direction;
import no.hal.grid.util.XYTransform;
import no.hal.sokoban.parser.SokobanSerializer;

/**
 * Computes the hash that identifies a level, so a saved game can be matched with the level it started from.
 * The hash is a string that starts with the name of the algorithm and a colon, e.g. {@code d8v1:0123456789abcdef},
 * so that a better algorithm can be introduced later, and hashes of both can be told apart (and rehashed).
 * A hash without a name is the one of {@link Legacy}.
 */
public interface SokobanHasher {

    /** The name of the algorithm, which is the prefix of the hashes it computes (empty if they have none). */
    String name();

    String hash(SokobanGrid grid);

    /**
     * Gives the same hash for a grid and all grids that are rotated and/or mirrored versions of it, and (apart from chance) different
     * hashes for all others. The name, d8, is of the group of the eight such transformations.
     * <p>
     * It is the hash of the canonical form of the grid: of the eight transformed versions, written as text with the characters of
     * {@link SokobanSerializer#toChar}, the smallest. The text includes the dimensions, so the same level with a border of empty cells is another level.
     * The text is hashed with SHA-256, cut to 64 bits. The characters, not the ordinals of the cell kinds, are used, since the hash is
     * stored in files, and must not change if the enum does.
     */
    public class Impl implements SokobanHasher {

        public static final String NAME = "d8v1";

        private final SokobanSerializer serializer = new SokobanSerializer();

        @Override
        public String name() {
            return NAME;
        }

        @Override
        public String hash(SokobanGrid grid) {
            String canonical = null;
            for (var transform : transforms()) {
                var text = text(grid, transform);
                if (canonical == null || text.compareTo(canonical) < 0) {
                    canonical = text;
                }
            }
            return NAME + ":" + digest(canonical);
        }

        private static XYTransform[] transforms() {
            var transforms = new XYTransform[8];
            for (int i = 0; i < transforms.length; i++) {
                transforms[i] = XYTransform.NONE.flipX((i & 1) != 0).flipY((i & 2) != 0).rotate((i & 4) != 0);
            }
            return transforms;
        }

        /** The grid as seen after the transform: its dimensions, and the rows of characters. */
        private String text(SokobanGrid grid, XYTransform transform) {
            int w = grid.getWidth(), h = grid.getHeight();
            int tw = transform.transformedWidth(w, h), th = transform.transformedHeight(w, h);
            var builder = new StringBuilder().append(tw).append('x').append(th);
            for (int y = 0; y < th; y++) {
                builder.append('\n');
                for (int x = 0; x < tw; x++) {
                    var cell = grid.getCell(transform.untransformedX(x, y, w, h), transform.untransformedY(x, y, w, h));
                    builder.append(serializer.toChar(cell));
                }
            }
            return builder.toString();
        }

        private static String digest(String text) {
            try {
                var bytes = MessageDigest.getInstance("SHA-256").digest(text.getBytes(StandardCharsets.UTF_8));
                return HexFormat.of().formatHex(bytes, 0, 8);
            } catch (NoSuchAlgorithmException e) {
                throw new IllegalStateException(e);
            }
        }
    }

    /**
     * The first algorithm, which gave the hashes that are saved in snapshots from before {@link Impl}: hex digits, without a name.
     * It also gives the same hash for rotated and mirrored grids, but has no memory of which side of the grid something is on,
     * so different levels may get the same hash, and it depends on the order of the cell kinds. It is kept (as it was) to
     * find the snapshots that have such hashes, and should not be used for anything new.
     */
    public class Legacy implements SokobanHasher {

        @Override
        public String name() {
            return "";
        }

        @Override
        public String hash(SokobanGrid grid) {
            int w = grid.getWidth(), h = grid.getHeight();
            return Long.toHexString(grid.reduceCells(0L, (hash, cellKind, x, y) -> hash ^ cellHash(grid, cellKind, x, y, w, h)));
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
