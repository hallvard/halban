package no.hal.sokoban.solver;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import no.hal.grid.impl.GridImpl;
import no.hal.sokoban.SokobanGrid;
import no.hal.sokoban.SokobanGrid.CellKind;

/** A level as text, for the tests, in the characters of the level files, which is all the solver needs of a level. */
class TextGrid extends GridImpl<CellKind> implements SokobanGrid {

  private final Location player;

  private TextGrid(List<String> lines, int width) {
    super(width, lines.size());
    Location found = null;
    for (int y = 0; y < lines.size(); y++) {
      for (int x = 0; x < width; x++) {
        char c = x < lines.get(y).length() ? lines.get(y).charAt(x) : ' ';
        var kind = switch (c) {
          case '#' -> CellKind.WALL;
          case '.' -> CellKind.TARGET;
          case '@' -> CellKind.EMPTY_PLAYER;
          case '$' -> CellKind.EMPTY_BOX;
          case '+' -> CellKind.TARGET_PLAYER;
          case '*' -> CellKind.TARGET_BOX;
          default -> CellKind.EMPTY;
        };
        setCell(x, y, kind);
        if (kind.content() == ContentKind.PLAYER) {
          found = new Location(x, y);
        }
      }
    }
    this.player = found;
  }

  static TextGrid of(String... lines) {
    return of(List.of(lines));
  }

  static TextGrid of(List<String> lines) {
    return new TextGrid(lines, lines.stream().mapToInt(String::length).max().orElse(0));
  }

  /** The levels of a file of the usual format: blocks of lines, with comment lines (starting with ;) and blank lines between. */
  static List<TextGrid> levelsOf(String resource) throws IOException {
    var levels = new ArrayList<TextGrid>();
    var block = new ArrayList<String>();
    try (var reader = new java.io.BufferedReader(new java.io.InputStreamReader(TextGrid.class.getResourceAsStream(resource)))) {
      for (var line = reader.readLine(); ; line = reader.readLine()) {
        if (line == null || line.isBlank() || line.startsWith(";")) {
          if (! block.isEmpty()) {
            levels.add(of(block));
            block = new ArrayList<>();
          }
          if (line == null) {
            return levels;
          }
        } else {
          block.add(line);
        }
      }
    }
  }

  @Override
  public Location getPlayerLocation() {
    return player;
  }
}
