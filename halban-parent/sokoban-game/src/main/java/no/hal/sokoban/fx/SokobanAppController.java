package no.hal.sokoban.fx;

import no.hal.plugin.InstanceRegistry;
import no.hal.sokoban.SokobanGame;
import no.hal.sokoban.fx.util.RegionSizeTracker;
import no.hal.sokoban.level.SokobanLevel;
import no.hal.plugin.fx.CompositeLabelAdapter;
import no.hal.plugin.fx.ContentProvider;
import no.hal.plugin.fx.LabelAdapter;
import no.hal.plugin.fx.xp.FxExtensionPoint;
import no.hal.plugin.fx.xp.LabeledChildExtender;
import no.hal.plugin.fx.xp.SimpleFxExtensionPoint;
import no.hal.plugin.impl.InstanceRegistryImpl;
import no.hal.settings.Settings;
import no.hal.sokoban.snapshot.SnapshotManager;

import java.util.ServiceLoader;

import javafx.application.Platform;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import javafx.scene.control.skin.TabPaneSkin;
import javafx.scene.layout.Region;

public class SokobanAppController {

	private TabPane tabPane;

	private SnapshotManager snapshotSaver;

 	FxExtensionPoint<LabeledChildExtender, Node> sokobanGamesExtensionPoint;

	public SokobanAppController(FxExtensionPoint<ContentProvider.Container, Parent> extensionPoint) {
		InstanceRegistry instanceRegistry = extensionPoint.getInstanceRegistry();
		this.snapshotSaver = new SnapshotManager();
		SokobanLevel.CollectionProvider snapshotsProvider = () -> snapshotSaver;
		instanceRegistry.registerQualifiedInstance(snapshotsProvider, SokobanLevel.CollectionProvider.class);
		var settings = instanceRegistry.getComponent(Settings.class);
		instanceRegistry.registerQualifiedInstance(LabelAdapter.forInstance(snapshotsProvider, settings.getValue("snapshots.label").asString()), LabelAdapter.class);
		extensionPoint.extend(() -> createContent(instanceRegistry));

		sokobanGamesExtensionPoint = new SimpleFxExtensionPoint<LabeledChildExtender, Node>(instanceRegistry, LabeledChildExtender.class, extender -> {
			var newGameTab = new Tab(extender.getText(), extender.getContent());
			newGameTab.setClosable(false);
			tabPane.getTabs().add(newGameTab);
			tabPane.getSelectionModel().select(newGameTab);
			return () -> tabPane.getTabs().remove(newGameTab);
		});
	}
	
	public Parent createContent(InstanceRegistry instanceRegistry) {
		InstanceRegistryImpl.loadServices(instanceRegistry, LabelAdapter.class, () -> ServiceLoader.load(LabelAdapter.class));
		var sokobanLevelCollectionsController = new SokobanLevelCollectionsTreeViewer(instanceRegistry);
		/*
		var sokobanGridsPane = new VBox();
		sokobanGridsPane.setAlignment(Pos.CENTER_LEFT);
		sokobanGridsPane.setFillWidth(true);

		ScrollPane rightPane = new ScrollPane(sokobanGridsPane);
		RegionSizeTracker.trackSize(sokobanGridsPane, null);

		RegionSizeTracker.trackSize(rightPane, "right pane");
		 */
		tabPane = createLayout(sokobanLevelCollectionsController.createContent());

		sokobanLevelCollectionsController.setLabelAdapter(CompositeLabelAdapter.fromInstanceRegistry(instanceRegistry));
//		sokobanLevelCollectionsController.setChildrenAdapter(CompositeChildrenAdapter.fromInstanceRegistry(instanceRegistry));

/*
		sokobanLevelCollectionsController.selectedSokobanLevelProperty().addListener((prop, oldValue, newValue) -> {
			if (newValue != null) {
				fillSokobanGridsPane(Collections.singletonList(newValue), sokobanGridsPane, instanceRegistry);
			}
		});
		sokobanLevelCollectionsController.selectedSokobanCollectionProperty().addListener((prop, oldValue, newValue) -> {
			if (newValue != null) {
				fillSokobanGridsPane(newValue.getSokobanLevels(), sokobanGridsPane, instanceRegistry);
			}
		});
*/
		Platform.runLater(() -> sokobanLevelCollectionsController.selectFirst(true));

		sokobanLevelCollectionsController.setOnOpenAction(sokobanLevel -> openSokobanGame(instanceRegistry, sokobanLevel));

		var collectionProviders = instanceRegistry.getAllComponents(SokobanLevel.CollectionProvider.class);
		sokobanLevelCollectionsController.setSokobanLevelCollectionProviders(collectionProviders);

		var collectionsProviders = instanceRegistry.getAllComponents(SokobanLevel.CollectionsProvider.class);
		sokobanLevelCollectionsController.setSokobanLevelCollectionsProviders(collectionsProviders);

		RegionSizeTracker.trackSize(tabPane, "tab pane");

		return tabPane;
	}

/*
	private void fillSokobanGridsPane(Iterable<SokobanLevel> sokobanLevels, Pane parent, InstanceRegistry instanceRegistry) {
		parent.getChildren().clear();
		for (var sokobanLevel : sokobanLevels) {
			SnapshotManager.SnapshotState snapshotState = null;
			var metaData = sokobanLevel.getMetaData();
			var snapshot = snapshotSaver.getSnapshot("uri", metaData.get("uri"));
			if (snapshot != null) {
				snapshotState = snapshotSaver.getSnapshotState(snapshot);
			}
			var sokobanLevelPane = new HBox();
			var sokobanLevelView = new SokobanLevelViewer(sokobanLevelPane);
			sokobanLevelView.setOnSokobanLevelSelected(sl -> openSokobanGame(instanceRegistry, sl));
			sokobanLevelView.updateView(sokobanLevel, snapshotState);
			parent.getChildren().add(sokobanLevelPane);
		}
	}
 */

	private void openSokobanGame(InstanceRegistry instanceRegistry, SokobanLevel sokobanLevel) {
		SokobanGameController sokobanGameController = new SokobanGameController(sokobanGamesExtensionPoint, sokobanLevel);
		Platform.runLater(() -> {
			var sokobanGame = sokobanGameController.startSokobanGame();
			snapshotSaver.registerSokobanGame(sokobanGame);
		});
		TabPaneSkin skin;
	}

	private TabPane createLayout(Region content) {
		TabPane tabPane = new TabPane();
		tabPane.setStyle("-fx-close-button-height: 50");
		RegionSizeTracker.trackSize(content, "tab content");
		Tab sokobanLevelCollectionsTab = new Tab("Sokoban collections", content);
		sokobanLevelCollectionsTab.setClosable(false);
		tabPane.getTabs().add(sokobanLevelCollectionsTab);
		return tabPane;
	}
}
