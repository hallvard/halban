package no.hal.fx;

import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.ComboBox;
import javafx.scene.control.IndexedCell;
import javafx.scene.control.ListCell;
import javafx.scene.input.MouseEvent;
import javafx.stage.Screen;

/**
 * Makes a tap on an item of a list, tree or table select it on a touch screen, with a touch slop: a distance that the finger must move
 * before the touch is more than a tap. This is written so that it can be used as it is in an issue for OpenJFX.
 *
 * <h2>The problem</h2>
 * On a touch screen (seen with JavaFX 24.0.2 in a Gluon native app on Android, but the code is the same on other platforms), a tap on an item of a
 * {@code ListView} or {@code TreeView} often selects nothing. Only a tap where the finger is perfectly still does, and no finger is.
 *
 * <h2>The cause</h2>
 * A touch is delivered as synthesized mouse events. {@code CellBehaviorBase.mousePressed} sees that the press is synthesized, and does not select
 * then, but sets {@code latePress} to be handled in {@code mouseReleased}. {@code mouseDragged} sets {@code latePress} to false, to
 * leave the touch to scrolling. But a {@code MOUSE_DRAGGED} event is sent for every movement of the finger, however small, since there
 * is no slop (the distance that must be moved before it is a drag) for a touch, as Android and iOS have, so the selection is lost.
 * The events of a mouse are not a problem, since a mouse has no such movement between the press and the release of a click.
 *
 * <h2>The evidence</h2>
 * The press, drag and release events were logged on a phone (the numbers are in scene units, which are about physical pixels there). Of 9 taps on
 * items in a tree and a list, the 3 that did not produce a drag event selected the item, and the 6 that produced 1 to 4 drag events did not,
 * though the finger had moved only 1.0 to 3.2 units at most, and the first drag event came 12 to 70 ms after the press. A deliberate scroll starts the same
 * way: its first drag event comes after 12 to 46 ms and 1 to 3 units, but it goes on to move 110 to 225 units in 150 to 260 ms. So it is
 * the distance that tells a tap from a scroll, and not the time, and the movement of a tap is much smaller than the slop of Android (8 dp).
 *
 * <h2>The workaround</h2>
 * A filter on the scene swallows the drag events of a touch that started on a cell, until the finger is more than the slop from where it started,
 * and after that, the touch is a drag, which scrolls, as before. With it, every tap selected the item, and every scroll scrolled.
 * The filter is on the scene, so it covers all cells, and only touches the touch events (the synthesized ones) that start on a cell,
 * so other uses of the mouse are as they were.
 *
 * <h2>A fix in JavaFX</h2>
 * The same could be done in {@code CellBehaviorBase}, where {@code mouseDragged} would not set {@code latePress} to false for a
 * synthesized event as long as the position is within the slop of the position of the press, and the slop could be the one of
 * {@code ScrollPane} and {@code VirtualFlow} for touch, if they have one, or a distance in the density independent pixels
 * of the screen, as here.
 */
public final class TouchSlop {

  /** The slop in density independent pixels, 8 as Android has it. The unit of a scene is not the same, so it is scaled by the density of the screen. */
  static final double SLOP_DP = 8;

  private TouchSlop() {
  }

  /** The slop in units of the scene for the primary screen, for code that tells a tap from a drag itself. */
  public static double slop() {
    return slopFor(Screen.getPrimary().getDpi());
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

  /**
   * The same for the list of a dropdown, which is in a window of its own (a popup) with a scene of its own, so the filter of the scene of the app
   * doesn't see it, and a tap on an item doesn't always select it. The scene is known when the first cell of the list is shown, so the
   * filter is installed then, once for each scene. The cells show the items as text, as the default of a dropdown does.
   */
  public static <T> void install(ComboBox<T> comboBox) {
    comboBox.setCellFactory(list -> {
      var cell = new ListCell<T>() {
        @Override
        protected void updateItem(T item, boolean empty) {
          super.updateItem(item, empty);
          setText(empty || item == null ? null : item.toString());
        }
      };
      cell.sceneProperty().addListener((obs, oldScene, scene) -> {
        if (scene != null && scene.getProperties().putIfAbsent(TouchSlop.class, true) == null) {
          install(scene);
        }
      });
      return cell;
    });
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
