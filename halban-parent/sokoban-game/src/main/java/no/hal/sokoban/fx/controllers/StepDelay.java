package no.hal.sokoban.fx.controllers;

import java.nio.file.Path;
import javafx.beans.property.IntegerProperty;

/**
 * The delay in milliseconds after each move that is made when moves are (re)done or undone, so that the player and the boxes are seen to move.
 * Zero means no delay. It is one value for now, but may be split into one for do, one for undo and one for redo.
 * It is chosen in the settings and read by {@link MovesSlowdownController} each time moves are made, so a change is used at once.
 */
public class StepDelay extends IntSetting {

  public static final int DEFAULT_MILLIS = 10;
  public static final int MAX_MILLIS = 2000;

  public StepDelay(Path file) {
    super(file, "millis", "The delay in milliseconds after each move that is made, zero is none", DEFAULT_MILLIS, 0, MAX_MILLIS);
  }

  /** The delay, kept in the file in the home directory. */
  public static StepDelay inHomeDirectory() {
    return new StepDelay(inHomeDirectory("step-delay.properties"));
  }

  public IntegerProperty millisProperty() {
    return valueProperty();
  }

  public int getMillis() {
    return value();
  }
}
