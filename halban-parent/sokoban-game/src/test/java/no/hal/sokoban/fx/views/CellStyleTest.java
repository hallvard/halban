package no.hal.sokoban.fx.views;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.nio.file.Path;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

public class CellStyleTest {

  @TempDir
  Path dir;

  @Test
  public void standardWhenThereIsNoFile() {
    assertEquals(CellStyle.Style.STANDARD, new CellStyle(dir.resolve("style.properties")).getStyle());
  }

  @Test
  public void theChoiceIsKept() {
    var file = dir.resolve("sub").resolve("style.properties");
    new CellStyle(file).styleProperty().set(CellStyle.Style.BORGAR);
    assertEquals(CellStyle.Style.BORGAR, new CellStyle(file).getStyle());
  }
}
