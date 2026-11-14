package no.hal.sokoban.solver;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

/** A development tool, not a test: solves the levels of a file with a time limit for each and prints how it went. */
public class CollectionRun {
  public static void main(String[] args) throws Exception {
    var lines = Files.readAllLines(Path.of(args[0]));
    long limit = Long.parseLong(args[1]);
    var levels = new ArrayList<TextGrid>();
    var block = new ArrayList<String>();
    for (var line : lines) {
      if (line.isBlank() || line.startsWith(";")) {
        if (! block.isEmpty()) { levels.add(TextGrid.of(block)); block = new ArrayList<>(); }
      } else {
        block.add(line);
      }
    }
    if (! block.isEmpty()) levels.add(TextGrid.of(block));
    int solved = 0, unsolvable = 0, gaveUp = 0;
    long total = 0;
    var slow = new ArrayList<String>();
    for (int i = 0; i < levels.size(); i++) {
      long start = System.nanoTime();
      var r = new SokobanSolver(levels.get(i)).solve(3_000_000, limit, () -> false);
      long ms = (System.nanoTime() - start) / 1_000_000;
      total += ms;
      switch (r.status()) { case SOLVED -> solved++; case UNSOLVABLE -> unsolvable++; default -> gaveUp++; }
      if (r.status() != SokobanSolver.Status.SOLVED || ms > 500) slow.add((i + 1) + ":" + r.status() + " " + ms + "ms " + r.expanded() + " states " + r.pushes() + " pushes");
    }
    System.out.println(args[0] + ": " + levels.size() + " levels, solved " + solved + ", unsolvable " + unsolvable + ", gave up " + gaveUp + ", total " + total + " ms");
    slow.forEach(s -> System.out.println("  " + s));
  }
}
