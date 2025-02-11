package no.hal.sokoban.fx.controllers;

import java.util.List;
import javafx.application.Platform;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import no.hal.config.ext.ExtConfiguration;
import no.hal.fx.ContentProvider;
import no.hal.fx.LabelAdapter;
import no.hal.sokoban.fx.ShortcutHandler;
import no.hal.sokoban.fx.views.SokobanCollectionsBrowser;
import no.hal.sokoban.level.SokobanLevel;
import no.hal.sokoban.level.SokobanLevel.MetaData;
import no.hal.sokoban.snapshot.SnapshotManager;

public class SokobanAppController implements ContentProvider.Container {

  private final ExtConfiguration config;
  private SnapshotManager snapshotManager;
  private String snapshotsLabel;
    
  public SokobanAppController(ExtConfiguration config) {
    this.config = config;
    this.snapshotManager = new SnapshotManager(config);
    this.snapshotsLabel = config.getString("snapshots.label", "Snapshots");

    config.registerInstance(snapshotManager, SnapshotManager.class);
    config.registerInstance(LabelAdapter.forInstance(snapshotManager, snapshotsLabel), LabelAdapter.class);
    
    SokobanLevel.CollectionProvider snapshotManagerSupplier = () -> snapshotManager;
    config.registerInstance(snapshotManagerSupplier, SokobanLevel.CollectionProvider.class);
    config.registerInstance(LabelAdapter.forInstance(snapshotManagerSupplier, snapshotsLabel), LabelAdapter.class);

    config.registerInstance(new ShortcutHandler(() -> config.getInstance(Scene.class)), ShortcutHandler.class);
  }

	@Override	
	public Parent getContent() {
		var sokobanCollectionsBrowser = SokobanCollectionsBrowser.Layouts.TREE_AND_LIST_LAYOUT.createSokobanCollectionsBrowser(config);
		Node content = sokobanCollectionsBrowser.getContent();

		TabPane tabPane = new TabPane();
		tabPane.setStyle("-fx-close-button-height: 50");
		Tab sokobanLevelCollectionsTab = new Tab("Sokoban collections", content);
		sokobanLevelCollectionsTab.setClosable(false);
		tabPane.getTabs().add(sokobanLevelCollectionsTab);

		sokobanCollectionsBrowser.setOnOpenAction(sokobanLevel -> {
      Tab gameTab = new Tab(sokobanLevel.getMetaData().get("Title"));
      tabPane.getTabs().add(gameTab);
      tabPane.getSelectionModel().select(gameTab);
			SokobanGameController sokobanGameController = new SokobanGameController(config, sokobanLevel, (sokobanGame) -> {
        snapshotManager.unregisterSokobanGame(sokobanGame);
        tabPane.getTabs().remove(gameTab);
      });
      gameTab.setContent(sokobanGameController.createLayout());
			Platform.runLater(() -> {
				var sokobanGame = sokobanGameController.startSokobanGame();
				snapshotManager.registerSokobanGame(sokobanGame);
			});
		});
		return tabPane;
	}
}
