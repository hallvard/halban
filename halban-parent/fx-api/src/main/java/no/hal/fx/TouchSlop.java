package no.hal.fx;

import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.IndexedCell;
import javafx.scene.input.MouseEvent;
import javafx.stage.Screen;

/**
 * Makes a tap on an item of a list, tree or table select it on a touch screen. A cell selects its item when the finger is lifted, but only if there
 * was no drag event in between (see {@code CellBehaviorBase}), and JavaFX makes a drag event of every movement of the finger, however small,
 * and a finger moves a little during any tap: the logs from a phone have taps that moved 1 to 3 units and had 1 to 4 drag events,
 * and none of those selected anything, while all the taps without a drag event did. Android avoids this with a touch slop, a distance that the finger
 * must move before it is more than a tap, and this is that: the drag events of a touch that started on a cell are swallowed until
 * the finger is more than the slop from where it started, and after that, the touch is a drag, which scrolls, as before.
 * <p>
 * The filter is on the scene, so it covers all cells, and only touches the touch events (the synthesized ones) that start on a cell,
 * so other uses of the mouse are as they were. Install it before {@link TouchLog}, which counts the swallowed events.
 */
public final class TouchSlop {

  /** The slop in density independent pixels, 8 as Android has it. The unit of a scene is not the same, so it is scaled by the density of the screen. */
  static final double SLOP_DP = 8;

  private TouchSlop() {
  }

  /** The slop in units of the scene, for a screen with this density in dots per inch. Never less than the units of a dp on the most common screens. */
  static double slopFor(double dpi) {
    return SLOP_DP * Math.max(dpi, 160) / 160;
  }

  /** Swallows the drag events of a touch that has moved less than the slop, for the scene. */
  public static void install(Scene scene) {
    var tap = new Tap(slopFor(Screen.getPrimary().getDpi()));
    scene.addEventFilter(MouseEvent.MOUSE_PRESSED, event -> tap.press(event.isSynthesized() && cellOf(event.getTarget()), event.getSceneX(), event.getSceneY()));
    scene.addEventFilter(MouseEvent.MOUSE_DRAGGED, event -> {
      if (tap.swallows(event.getSceneX(), event.getSceneY())) {
        event.consume();
      }
    });
    scene.addEventFilter(MouseEvent.MOUSE_RELEASED, event -> tap.release());
  }

  private static boolean cellOf(Object target) {
    for (Node node = target instanceof Node n ? n : null; node != null; node = node.getParent()) {
      if (node instanceof IndexedCell<?>) {
        return true;
      }
    }
    return false;
  }

  /** The state of one touch: where it started, and if it has moved further than the slop yet. Without anything of JavaFX, so it can be tested. */
  static final class Tap {

    private final double slop;
    private boolean watching, moved;
    private double startX, startY;

    Tap(double slop) {
      this.slop = slop;
    }

    /** A touch starts, which is only watched if it is one that should be a tap, as on a cell. */
    void press(boolean watched, double x, double y) {
      watching = watched;
      moved = false;
      startX = x;
      startY = y;
    }

    /** If the drag event, at this position, is still within the slop, and is to be swallowed. Once the touch has moved out, no more are. */
    boolean swallows(double x, double y) {
      if (! watching || moved) {
        return false;
      }
      if (Math.hypot(x - startX, y - startY) > slop) {
        moved = true;
        return false;
      }
      return true;
    }

    void release() {
      watching = false;
      moved = false;
    }
  }
}
