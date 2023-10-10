package no.hal.sokoban.fx;

import no.hal.plugin.InstanceRegistry;
import no.hal.sokoban.level.SokobanLevel;
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

public class SokobanAppController {

	private TabPane tabPane;

	private SnapshotManager snapshotSaver;

 	FxExtensionPoint<LabeledChildExtender, Node> sokobanGamesExtensionPoint;

	public SokobanAppController(FxExtensionPoint<ContentProvider.Container, Parent> extensionPoint) {
		InstanceRegistry instanceRegistry = extensionPoint.getInstanceRegistry();
		this.snapshotSaver = new SnapshotManager();
		instanceRegistry.registerInstance(snapshotSaver, SnapshotManager.class);
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
		var sokobanCollectionsBrowser = SokobanCollectionsBrowser.Layouts.TREE_AND_LIST_LAYOUT.createSokobanCollectionsBrowser(instanceRegistry);
		sokobanCollectionsBrowser.setOnOpenAction(sokobanLevel -> openSokobanGame(instanceRegistry, sokobanLevel));
		Node content = sokobanCollectionsBrowser.getContent();
		this.tabPane = createLayout(content);
		return this.tabPane;
	}

	private void openSokobanGame(InstanceRegistry instanceRegistry, SokobanLevel sokobanLevel) {
		SokobanGameController sokobanGameController = new SokobanGameController(sokobanGamesExtensionPoint, sokobanLevel);
		Platform.runLater(() -> {
			var sokobanGame = sokobanGameController.startSokobanGame();
			snapshotSaver.registerSokobanGame(sokobanGame);
		});
	}

	private TabPane createLayout(Node content) {
		TabPane tabPane = new TabPane();
		tabPane.setStyle("-fx-close-button-height: 50");
		Tab sokobanLevelCollectionsTab = new Tab("Sokoban collections", content);
		sokobanLevelCollectionsTab.setClosable(false);
		tabPane.getTabs().add(sokobanLevelCollectionsTab);
		return tabPane;
	}
}
