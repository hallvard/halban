package no.hal.fx;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

public class TouchSlopTest {

  @Test
  public void jitterOfATapIsSwallowed() {
    // the drags of the taps in the log from the phone: 1.0 to 3.2 units from the start
    var tap = new TouchSlop.Tap(TouchSlop.slopFor(400));
    tap.press(true, 100, 200);
    assertTrue(tap.swallows(101, 200));
    assertTrue(tap.swallows(103, 201));
    assertTrue(tap.swallows(100, 201));
    tap.release();
  }

  @Test
  public void aDragIsNotSwallowedOnceItIsOutsideTheSlop() {
    var tap = new TouchSlop.Tap(10);
    tap.press(true, 0, 0);
    assertTrue(tap.swallows(3, 4));
    assertFalse(tap.swallows(0, 12));
    // and not when it comes back, since it has started to drag, and scrolls
    assertFalse(tap.swallows(1, 1));
    assertFalse(tap.swallows(0, 100));
  }

  @Test
  public void aTouchThatIsNotWatchedIsNeverSwallowed() {
    var tap = new TouchSlop.Tap(10);
    tap.press(false, 0, 0);
    assertFalse(tap.swallows(1, 1));
  }

  @Test
  public void eachTouchStartsOver() {
    var tap = new TouchSlop.Tap(10);
    tap.press(true, 0, 0);
    assertFalse(tap.swallows(50, 0));
    tap.release();
    tap.press(true, 50, 0);
    assertTrue(tap.swallows(51, 0));
  }

  @Test
  public void theSlopGrowsWithTheDensity() {
    assertEquals(8, TouchSlop.slopFor(96), 0.001);
    assertEquals(8, TouchSlop.slopFor(160), 0.001);
    assertEquals(20, TouchSlop.slopFor(400), 0.001);
  }
}
