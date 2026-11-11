package no.hal.sokoban.level;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

import java.util.List;
import java.util.Map;
import no.hal.sokoban.SokobanGrid;
import org.junit.jupiter.api.Test;

public class SokobanLevelTest {

  private SokobanLevel levelWith(SokobanLevel.MetaData metaData) {
    return new SokobanLevel() {
      @Override
      public MetaData getMetaData() {
        return metaData;
      }
      @Override
      public SokobanGrid getSokobanGrid() {
        return null;
      }
    };
  }

  @Test
  void testMetaDataProperty() {
    var level = levelWith(SokobanLevel.metaDataOf(Map.of("Title", "Intro")));
    assertEquals("Intro", level.getMetaData("Title"));
    assertNull(level.getMetaData("Author"));
  }

  @Test
  void testMetaDataPropertyWithoutMetaData() {
    assertNull(levelWith(null).getMetaData("Title"));
  }

  @Test
  void testCollectionMetaDataPropertyWithoutMetaData() {
    var collection = new SokobanLevel.Collection() {
      @Override
      public SokobanLevel.MetaData getMetaData() {
        return null;
      }
      @Override
      public List<SokobanLevel> getSokobanLevels() {
        return List.of();
      }
    };
    assertNull(collection.getMetaData("Title"));
  }
}
