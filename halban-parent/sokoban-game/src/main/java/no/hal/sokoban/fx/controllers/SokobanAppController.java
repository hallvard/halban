package no.hal.sokoban.fx.controllers;

import javafx.application.Platform;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.text.Font;
import javafx.scene.text.Text;
import no.hal.config.ext.ExtConfiguration;
import no.hal.fx.ContentProvider;
import no.hal.fx.LabelAdapter;
import no.hal.sokoban.fx.ShortcutHandler;
import no.hal.sokoban.fx.views.SokobanCollectionsBrowser;
import no.hal.sokoban.level.SokobanLevel;
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
    // tabPane.getTabs().add(createFontTestTab());
    return tabPane;
  }

  // private Tab createFontTestTab() {
  //   VBox pane = new VBox();
  //   String fontResource = "/META-INF/resources/materialdesignicons2/5.8.55/fonts/materialdesignicons-webfont.ttf";
  //   String fastForward = new String(Character.toChars(Integer.parseInt("F0211", 16)));
  //   try {
  //     Text textIcons = new Text();
  //     Label labelIcons = new Label();
  //     String ttfUrl = getClass().getResource(fontResource).toExternalForm();
  //     Font font = Font.loadFont(ttfUrl, 16);
  //     textIcons.setFont(font);
  //     labelIcons.setFont(font);
  //     textIcons.setText(fastForward);
  //     labelIcons.setText(fastForward);
  //     pane.getChildren().addAll(
  //       new Text(ttfUrl.substring(ttfUrl.lastIndexOf("/"))),
  //       new HBox(new Text("Text: "), textIcons),
  //       new HBox(new Text("Label: "), labelIcons)
  //     );
  //   } catch (Exception e) {
  //     pane.getChildren().add(new Text(e.getMessage()));
  //   }
  //   Tab iconTab = new Tab("Font icon test");
  //   iconTab.setContent(pane);
  //   return iconTab;
  // }
}
