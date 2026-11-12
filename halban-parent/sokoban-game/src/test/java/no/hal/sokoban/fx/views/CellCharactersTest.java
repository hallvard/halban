package no.hal.sokoban.fx.views;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import no.hal.sokoban.SokobanGrid.CellKind;
import org.junit.jupiter.api.Test;

public class CellCharactersTest {

  private static Path newFile(String name) throws IOException {
    return Files.createTempDirectory("chars").resolve(name);
  }

  @Test
  void startsWithASampleSetAndTheStandard() throws IOException {
    var characters = new CellCharacters(newFile("none.properties"));
    assertNull(characters.getActive(), "the standard, the images");
    assertEquals(1, characters.getSets().size());
    var sample = characters.getSets().get(0);
    assertEquals("Shapes", sample.getName());
    assertEquals("▓", sample.getCharacter(CellKind.WALL));
    assertEquals("☺", sample.getCharacter(CellKind.EMPTY_PLAYER));
  }

  @Test
  void aNewSetHasTheCharactersOfTheLevelFiles() {
    var set = new CharacterSet("Plain");
    assertEquals("#", set.getCharacter(CellKind.WALL));
    assertEquals(" ", set.getCharacter(CellKind.EMPTY));
    assertEquals("@", set.getCharacter(CellKind.EMPTY_PLAYER));
    assertEquals("*", set.getCharacter(CellKind.TARGET_BOX));
    var copy = new CharacterSet(set, "Plain copy");
    set.characterProperty(CellKind.WALL).set("X");
    assertEquals("#", copy.getCharacter(CellKind.WALL), "a copy is not changed with the original");
    assertEquals("Plain copy", copy.getName());
  }

  @Test
  void setsAndTheChoiceAreKeptInTheFile() throws IOException {
    var file = newFile("sub/chars.properties");
    var characters = new CellCharacters(file);
    var changes = characters.changesProperty().get();
    var mine = new CharacterSet(characters.uniqueName("Mine"));
    mine.fontFamilyProperty().set("Menlo");
    characters.getSets().add(mine);
    characters.activeProperty().set(mine);
    mine.characterProperty(CellKind.WALL).set("█");
    mine.characterProperty(CellKind.EMPTY).set("");
    mine.characterProperty(CellKind.EMPTY_BOX).set("📦");   // outside the first plane of Unicode
    assertTrue(characters.changesProperty().get() >= changes + 5);
    assertTrue(Files.exists(file));

    var again = new CellCharacters(file);
    assertEquals(2, again.getSets().size());
    assertEquals("Shapes", again.getSets().get(0).getName());
    var loaded = again.getActive();
    assertSame(again.getSets().get(1), loaded);
    assertEquals("Mine", loaded.getName());
    assertEquals("Menlo", loaded.getFontFamily());
    assertEquals("█", loaded.getCharacter(CellKind.WALL));
    assertEquals("", loaded.getCharacter(CellKind.EMPTY));
    assertEquals("📦", loaded.getCharacter(CellKind.EMPTY_BOX));
    assertEquals("@", loaded.getCharacter(CellKind.EMPTY_PLAYER), "what was not changed is as before");
  }

  @Test
  void removingTheActiveSetGivesTheStandard() throws IOException {
    var file = newFile("remove.properties");
    var characters = new CellCharacters(file);
    characters.activeProperty().set(characters.getSets().get(0));
    characters.getSets().remove(characters.getActive());
    assertNull(characters.getActive());
    var again = new CellCharacters(file);
    assertTrue(again.getSets().isEmpty(), "the sample is not made again when there is a file");
    assertNull(again.getActive());
  }

  @Test
  void namesAreMadeUnique() throws IOException {
    var characters = new CellCharacters(newFile("names.properties"));
    assertEquals("Other", characters.uniqueName("Other"));
    assertEquals("Shapes 2", characters.uniqueName("Shapes"));
    characters.getSets().add(new CharacterSet("Shapes 2"));
    assertEquals("Shapes 3", characters.uniqueName("Shapes"));
  }
}
