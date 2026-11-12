package no.hal.sokoban.fx.controllers;

import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import javafx.scene.layout.StackPane;
import no.hal.config.ext.ExtConfiguration;
import no.hal.fx.ContentProvider;
import no.hal.fx.LabelAdapter;
import no.hal.sokoban.fx.ShortcutHandler;
import no.hal.sokoban.fx.views.CellCharacters;
import no.hal.sokoban.fx.views.SokobanCollectionsBrowser;
import no.hal.sokoban.level.SokobanLevel;
import no.hal.sokoban.snapshot.SnapshotManager;
import org.kordamp.ikonli.javafx.FontIcon;

public class SokobanAppController implements ContentProvider.Container {

  private final ExtConfiguration config;
  private SnapshotManager snapshotManager;
  private String snapshotsLabel;
    
  public SokobanAppController(ExtConfiguration config) {
    this.config = config;
    this.snapshotManager = new SnapshotManager(config);
    this.snapshotsLabel = config.getString("snapshots.label", "Ongoing");

    config.registerInstance(snapshotManager, SnapshotManager.class);
    // the instances are registered by class, and the adapters are all of the same class, so each is given a qualifier or the last one replaces the others
    config.registerInstance(LabelAdapter.forInstance(snapshotManager, s -> snapshotsLabel + " (" + snapshotManager.getOngoingCount() + ")"), LabelAdapter.class, snapshotManager);
    
    SokobanLevel.CollectionProvider snapshotManagerSupplier = () -> snapshotManager;
    config.registerInstance(snapshotManagerSupplier, SokobanLevel.CollectionProvider.class);
    config.registerInstance(LabelAdapter.forInstance(snapshotManagerSupplier, s -> snapshotsLabel + " (" + snapshotManager.getOngoingCount() + ")"), LabelAdapter.class, snapshotManagerSupplier);

    var solvedLabel = config.getString("solved.label", "Solved");
    SokobanLevel.CollectionProvider solvedSupplier = snapshotManager::getSolvedCollection;
    config.registerInstance(solvedSupplier, SokobanLevel.CollectionProvider.class);
    config.registerInstance(LabelAdapter.forInstance(solvedSupplier, s -> solvedLabel + " (" + snapshotManager.getSolvedCount() + ")"), LabelAdapter.class, solvedSupplier);

    // read by the boards, and changed by the settings
    config.registerInstance(CellCharacters.inHomeDirectory(), CellCharacters.class);

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
      Tab gameTab = new Tab(sokobanLevel.getMetaData("Title"));
      tabPane.getTabs().add(gameTab);
      tabPane.getSelectionModel().select(gameTab);
			SokobanGameController sokobanGameController = new SokobanGameController(config, sokobanLevel, (sokobanGame) -> {
        snapshotManager.unregisterSokobanGame(sokobanGame);
        tabPane.getTabs().remove(gameTab);
      });
      gameTab.setContent(sokobanGameController.createLayout());
			sokobanGameController.setOnGameStartedEnded(snapshotManager::registerSokobanGame, snapshotManager::unregisterSokobanGame);
			Platform.runLater(sokobanGameController::startSokobanGame);
		});
    // tabPane.getTabs().add(createFontTestTab());

    // a cog in the upper right corner opens the settings, in a tab of its own that is made when it is needed
    var settingsButton = new Button(null, new FontIcon("mdi2c-cog:22"));
    settingsButton.setStyle("-fx-background-color: transparent;");
    settingsButton.setOnAction(ev -> showSettings(tabPane));
    StackPane.setAlignment(settingsButton, Pos.TOP_RIGHT);
    StackPane.setMargin(settingsButton, new Insets(2, 6, 0, 0));
    return new StackPane(tabPane, settingsButton);
  }

  private Tab settingsTab;

  /** Selects the settings tab, which is made first if there is none (it is closable, and is made again if it was closed). */
  private void showSettings(TabPane tabPane) {
    if (settingsTab == null || ! tabPane.getTabs().contains(settingsTab)) {
      settingsTab = new Tab("Settings", new SokobanSettingsController(config).getContent());
      tabPane.getTabs().add(settingsTab);
    }
    tabPane.getSelectionModel().select(settingsTab);
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
