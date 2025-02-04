package no.hal.sokoban.levels;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;

import java.util.List;
import no.hal.sokoban.level.SokobanLevel;
import no.hal.sokoban.levels.borgarnet.BorgarNetLevelCollectionsProvider;
import no.hal.sokoban.levels.omerkelsokoban3rdparty.OmerkelSokoban3rdpartyCollectionsProvider;
import no.hal.sokoban.levels.sourcecodese.SourcecodeSeLevelCollectionsProvider;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

public class SokobanCollectionsProvidersTest {

  private void testSokobanLevelCollections(SokobanLevel.CollectionsProvider provider, int expectedSize) {
    List<SokobanLevel.Collection> collections = provider.getSokobanLevelCollections();
    assertEquals(expectedSize, collections.size());
    int num = 1;
    for (var collection : collections) {
      try {
        var levels = collection.getSokobanLevels();
        assertTrue(! levels.isEmpty(), "Collection #" + num + " " + collection.getMetaData().get("Title") + " is empty");
      } catch (Exception e) {
        fail("Exception when loading collection #" + num + " " + collection.getMetaData().get("Title") + " failed: " + e.getMessage());
      }
      num++;
    }
  }

  @Test
  @Disabled
  public void testBorgarNetLevelCollections() {
    testSokobanLevelCollections(new BorgarNetLevelCollectionsProvider(), 24);
  }

  @Test
  @Disabled
  public void testSourcecodeSeLevelCollections() {
    testSokobanLevelCollections(new SourcecodeSeLevelCollectionsProvider(), 546);
  }

  @Test
  public void testOmerkelSokoban3rdpartyLevelCollections() {
    testSokobanLevelCollections(new OmerkelSokoban3rdpartyCollectionsProvider(), 16);
  }
}
