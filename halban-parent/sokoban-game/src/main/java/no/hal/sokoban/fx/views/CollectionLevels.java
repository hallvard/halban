package no.hal.sokoban.fx.views;

import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.WeakHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import javafx.application.Platform;
import no.hal.sokoban.level.SokobanLevel;
import no.hal.sokoban.snapshot.SnapshotManager;

/**
 * The levels of the collections that are loaded, kept so that a collection can show how many of its levels are solved, s/n.
 * Loading a collection may read a file or download it, so it isn't done for the collections that are only seen in a list. Whoever loads the levels of
 * a collection tells it with {@link #loaded}, whether that is the tree when a collection is expanded or the list when it is selected, and
 * {@link #load} loads them on a thread of its own, for the collection that is selected. The listeners are told on the FX thread when levels have arrived, and redraw the labels.
 * <p>
 * A collection is the item of the tree, which is a {@link SokobanLevel.Collection} or a {@link SokobanLevel.CollectionProvider}. The ongoing and the solved
 * games are left out, as they have their number in the label already, and s/n is not meaningful for them.
 */
public class CollectionLevels {

  private final SnapshotManager snapshotManager;
  private final Map<Object, List<SokobanLevel>> levels = Collections.synchronizedMap(new WeakHashMap<>());
  private final Set<Object> loading = new HashSet<>();
  private final List<Runnable> listeners = new CopyOnWriteArrayList<>();

  public CollectionLevels(SnapshotManager snapshotManager) {
    this.snapshotManager = snapshotManager;
  }

  /** Called on the FX thread when the levels of a collection have been loaded. */
  public void addListener(Runnable listener) {
    listeners.add(listener);
  }

  /** The levels of the collection, or null if they aren't loaded, or the collection doesn't show a count. */
  public List<SokobanLevel> getLevels(Object collection) {
    return levels.get(collection);
  }

  /** How many levels of the collection that are solved, or -1 if it isn't known. */
  public int solvedCount(Object collection) {
    var collectionLevels = levels.get(collection);
    if (collectionLevels == null) {
      return -1;
    }
    return (int) collectionLevels.stream()
        .filter(level -> snapshotManager.getSokobanLevelSnapshotState(level) == SnapshotManager.SnapshotState.SOLVED)
        .count();
  }

  /** Tells that the levels of the collection are loaded, so labels that have s/n are updated. May be called on any thread. */
  public void loaded(Object collection, List<?> collectionLevels) {
    if (! isCounted(collection) || levels.containsKey(collection)) {
      return;
    }
    levels.put(collection, collectionLevels.stream().filter(SokobanLevel.class::isInstance).map(SokobanLevel.class::cast).toList());
    Platform.runLater(() -> listeners.forEach(Runnable::run));
  }

  /** Loads the levels of the collection, if that isn't done or going on already. */
  public void load(Object collection) {
    if (! isCounted(collection) || levels.containsKey(collection)) {
      return;
    }
    synchronized (loading) {
      if (! loading.add(collection)) {
        return;
      }
    }
    var thread = new Thread(() -> {
      try {
        var levelCollection = collectionOf(collection);
        if (levelCollection != null) {
          loaded(collection, levelCollection.getSokobanLevels());
        }
      } finally {
        synchronized (loading) {
          loading.remove(collection);
        }
      }
    }, "collection-levels");
    thread.setDaemon(true);
    thread.start();
  }

  private static SokobanLevel.Collection collectionOf(Object item) {
    if (item instanceof SokobanLevel.CollectionProvider provider) {
      return provider.getSokobanLevelCollection();
    }
    return item instanceof SokobanLevel.Collection collection ? collection : null;
  }

  /** Tells if the item is a collection that shows a count. */
  public boolean isCounted(Object item) {
    if (item instanceof SokobanLevel.Collection || item instanceof SokobanLevel.CollectionProvider) {
      var collection = collectionOf(item);
      return collection == null || ! snapshotManager.isGamesCollection(collection);
    }
    return false;
  }
}
