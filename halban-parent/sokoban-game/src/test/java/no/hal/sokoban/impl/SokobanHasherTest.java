package no.hal.sokoban.impl;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.ArrayList;
import java.util.List;
import no.hal.sokoban.SokobanGrid;
import no.hal.sokoban.SokobanHasher;
import no.hal.sokoban.parser.SokobanFactory;
import org.junit.jupiter.api.Test;

public class SokobanHasherTest {

  private final SokobanFactory factory = new SokobanFactoryImpl();
  private final SokobanHasher hasher = new SokobanHasher.Impl();

  private SokobanGrid grid(String... rows) {
    return factory.createSokobanGrid(rows);
  }

  private String hash(String... rows) {
    return hasher.hash(grid(rows));
  }

  /** The grid turned a quarter, as rows of text. */
  private static String[] rotated(String... rows) {
    int w = rows[0].length();
    var result = new String[w];
    for (int x = 0; x < w; x++) {
      var row = new StringBuilder();
      for (int y = rows.length - 1; y >= 0; y--) {
        row.append(rows[y].charAt(x));
      }
      result[x] = row.toString();
    }
    return result;
  }

  private static String[] mirrored(String... rows) {
    return java.util.Arrays.stream(rows).map(row -> new StringBuilder(row).reverse().toString()).toArray(String[]::new);
  }

  private static final String[] LEVEL = {
      "#########",
      "#.#     #",
      "#   $   #",
      "# @   $.#",
      "#########"
  };

  @Test
  void hasNameAsPrefix() {
    assertTrue(hash(LEVEL).matches("d8v1:[0-9a-f]{16}"), hash(LEVEL));
  }

  @Test
  void sameForRotationsAndMirrorings() {
    var expected = hash(LEVEL);
    var variants = new ArrayList<String[]>();
    var rows = LEVEL;
    for (int i = 0; i < 4; i++) {
      variants.add(rows);
      variants.add(mirrored(rows));
      rows = rotated(rows);
    }
    for (var variant : variants) {
      assertEquals(expected, hash(variant), String.join("/", variant));
    }
  }

  @Test
  void differentWhenOnlyPartOfTheLevelIsMirrored() {
    // the legacy hash can't tell these apart, a box on either side of a level that isn't symmetric
    var left = hash("###########", "#.#       #", "#         #", "#  $      #", "#         #", "#@        #", "###########");
    var right = hash("###########", "#.#       #", "#         #", "#      $  #", "#         #", "#@        #", "###########");
    assertNotEquals(left, right);
    var legacy = new SokobanHasher.Legacy();
    assertEquals(
        legacy.hash(grid("###########", "#.#       #", "#         #", "#  $      #", "#         #", "#@        #", "###########")),
        legacy.hash(grid("###########", "#.#       #", "#         #", "#      $  #", "#         #", "#@        #", "###########")));
  }

  @Test
  void differentForOtherLevels() {
    assertNotEquals(hash(LEVEL), hash("#########", "#.#     #", "#   $   #", "# @   $ #", "####.####"));
    // a border of empty cells makes it another level
    assertNotEquals(hash("###", "#@#", "###"), hash("####", "#@ #", "####"));
  }

  @Test
  void legacyHashHasNoName() {
    var legacy = new SokobanHasher.Legacy();
    assertEquals("", legacy.name());
    assertTrue(legacy.hash(grid(LEVEL)).matches("[0-9a-f]+"));
  }

  @Test
  void hashIsStable() {
    // saved in files, so this must not change (and not with the order of the cell kinds)
    assertEquals("d8v1:2259bf068002d15a", hash(LEVEL));
  }
}
