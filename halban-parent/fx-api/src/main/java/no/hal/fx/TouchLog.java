package no.hal.fx;

import java.util.ArrayDeque;
import java.util.Locale;
import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.IndexedCell;
import javafx.scene.control.Label;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.stage.Screen;

/**
 * A temporary tool for finding out why a tap on an item of a list or tree often selects nothing on a phone: a cell selects the item when
 * the finger is lifted, but not if there was a drag event in between, and a finger always moves a little. This logs one line for each press-drag-release,
 * on the screen (the last lines, over the application, which doesn't take the touches) and on the standard output: how long it took (dt),
 * how far the finger was from where it started (dx, dy) when it was lifted, the number of drag events, when the first one came and how far
 * the finger had moved then, how far it got at most, and, for a cell, if the item got selected.
 */
public final class TouchLog {

  private static final int LINES = 9;

  private final Label label = new Label();
  private final ArrayDeque<String> lines = new ArrayDeque<>();

  private long pressNanos;
  private double pressX, pressY;
  private boolean synthesized;
  private String target;
  private IndexedCell<?> cell;
  private int drags;
  private double firstDragMillis, firstDx, firstDy, maxDistance;

  private TouchLog() {
  }

  /** Starts logging the mouse (and touch) events of the scene, and shows the log over its content, which must be set already. */
  public static void install(Scene scene) {
    var log = new TouchLog();
    log.label.setFont(Font.font("monospace", 10));
    log.label.setTextFill(Color.WHITE);
    log.label.setStyle("-fx-background-color: rgba(0, 0, 0, 0.7);");
    log.label.setPadding(new Insets(3));
    log.label.setMouseTransparent(true);
    log.label.setWrapText(true);
    var overlay = new StackPane(scene.getRoot(), log.label);
    StackPane.setAlignment(log.label, Pos.BOTTOM_LEFT);
    scene.setRoot(overlay);
    var screen = Screen.getPrimary();
    log.add(String.format(Locale.ROOT, "dpi %.0f, scale %.2f, scene %.0f x %.0f", screen.getDpi(), screen.getOutputScaleX(), scene.getWidth(), scene.getHeight()));
    scene.addEventFilter(MouseEvent.MOUSE_PRESSED, log::pressed);
    scene.addEventFilter(MouseEvent.MOUSE_DRAGGED, log::dragged);
    scene.addEventFilter(MouseEvent.MOUSE_RELEASED, log::released);
  }

  private static IndexedCell<?> cellOf(Object target) {
    for (Node node = target instanceof Node n ? n : null; node != null; node = node.getParent()) {
      if (node instanceof IndexedCell<?> cell) {
        return cell;
      }
    }
    return null;
  }

  /** The simple name of the class, or of the first superclass that has one, since an anonymous class has none. */
  private static String nameOf(Class<?> type) {
    while (type.getSimpleName().isEmpty()) {
      type = type.getSuperclass();
    }
    return type.getSimpleName();
  }

  private void pressed(MouseEvent event) {
    pressNanos = System.nanoTime();
    pressX = event.getSceneX();
    pressY = event.getSceneY();
    synthesized = event.isSynthesized();
    target = nameOf(event.getTarget().getClass());
    cell = cellOf(event.getTarget());
    drags = 0;
    maxDistance = 0;
  }

  private double millis() {
    return (System.nanoTime() - pressNanos) / 1_000_000.0;
  }

  private void dragged(MouseEvent event) {
    double dx = event.getSceneX() - pressX, dy = event.getSceneY() - pressY;
    if (drags++ == 0) {
      firstDragMillis = millis();
      firstDx = dx;
      firstDy = dy;
    }
    maxDistance = Math.max(maxDistance, Math.hypot(dx, dy));
  }

  private void released(MouseEvent event) {
    double dt = millis(), dx = event.getSceneX() - pressX, dy = event.getSceneY() - pressY;
    var movement = drags == 0 ? "no drags"
        : String.format(Locale.ROOT, "%d drags, first at %.0f ms (%.1f, %.1f), max %.1f", drags, firstDragMillis, firstDx, firstDy, maxDistance);
    var description = String.format(Locale.ROOT, "%s%s dt %.0f ms d (%.1f, %.1f), %s", synthesized ? "touch " : "mouse ", target, dt, dx, dy, movement);
    var tapped = cell;
    // the cell selects its item when the event has been handled
    Platform.runLater(() -> add(description + (tapped != null ? (tapped.isSelected() ? ", selected" : ", NOT selected") : "")));
  }

  private void add(String line) {
    System.out.println("TouchLog: " + line);
    lines.addLast(line);
    while (lines.size() > LINES) {
      lines.removeFirst();
    }
    label.setText(String.join("\n", lines));
  }
}
