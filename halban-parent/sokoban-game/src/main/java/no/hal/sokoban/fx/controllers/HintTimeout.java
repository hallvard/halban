package no.hal.sokoban.fx.controllers;

import java.nio.file.Path;
import javafx.beans.property.IntegerProperty;

/**
 * The time in seconds that the search for a hint may use before it gives up, chosen in the settings and read each time a hint is asked for.
 * A longer time finds a hint for more levels (a level with more boxes needs more), but the player waits longer. The search keeps its states in memory, about 50 bytes each,
 * so the number of states it may keep follows the time, {@link #maxStates()}, at a rate that a phone can keep up with, and that is why the range stops at a minute.
 */
public class HintTimeout extends IntSetting {

  public static final int DEFAULT_SECONDS = 15;
  public static final int MIN_SECONDS = 5;
  public static final int MAX_SECONDS = 60;
  /** The states the search may keep for each second of the time, which is a little less than what a desktop makes in a second. */
  private static final int STATES_PER_SECOND = 100_000;

  public HintTimeout(Path file) {
    super(file, "seconds", "The time in seconds that the search for a hint may use", DEFAULT_SECONDS, MIN_SECONDS, MAX_SECONDS);
  }

  /** The timeout, kept in the file in the home directory. */
  public static HintTimeout inHomeDirectory() {
    return new HintTimeout(inHomeDirectory("hint-timeout.properties"));
  }

  public IntegerProperty secondsProperty() {
    return valueProperty();
  }

  public int getSeconds() {
    return value();
  }

  public long timeoutMillis() {
    return getSeconds() * 1000L;
  }

  /** The number of states that the search may keep, at most 6 million (about 300 MB). */
  public int maxStates() {
    return getSeconds() * STATES_PER_SECOND;
  }
}
