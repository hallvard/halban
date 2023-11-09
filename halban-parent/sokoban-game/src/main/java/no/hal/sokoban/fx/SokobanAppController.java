package no.hal.sokoban.fx;

import java.util.ServiceLoader;

import jakarta.inject.Named;
import javafx.application.Platform;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import no.hal.plugin.InstanceRegistry;
import no.hal.plugin.di.annotation.Component;
import no.hal.plugin.di.annotation.Reference;
import no.hal.plugin.fx.ContentProvider;
import no.hal.plugin.fx.LabelAdapter;
import no.hal.plugin.fx.xp.FxExtensionPoint;
import no.hal.plugin.fx.xp.LabeledChildExtender;
import no.hal.plugin.fx.xp.SimpleFxExtensionPoint;
import no.hal.settings.Setting.Value;
import no.hal.sokoban.level.SokobanLevel;
import no.hal.sokoban.snapshot.SnapshotManager;

@Component
public class SokobanAppController implements ContentProvider.Container {

	@Reference
	InstanceRegistry instanceRegistry;

	@Reference
	SnapshotManager snapshotManager;

	private String snapshotsLabel;

	@Reference
	public void setSnapshotsLabel(@Named("snapshots.label") Value snapshotsLabel) {
		this.snapshotsLabel = snapshotsLabel.asString();
	}

	public SokobanAppController() {
	}

	@Override	
	public Parent getContent() {
		SokobanLevel.CollectionProvider snapshotsProvider = () -> snapshotManager;
		instanceRegistry.registerQualifiedInstance(snapshotsProvider, SokobanLevel.CollectionProvider.class);
		instanceRegistry.registerQualifiedInstance(LabelAdapter.forInstance(snapshotsProvider, snapshotsLabel), LabelAdapter.class);

		InstanceRegistry.loadServices(instanceRegistry, LabelAdapter.class, () -> ServiceLoader.load(LabelAdapter.class));
		var sokobanCollectionsBrowser = SokobanCollectionsBrowser.Layouts.TREE_AND_LIST_LAYOUT.createSokobanCollectionsBrowser(instanceRegistry);
		Node content = sokobanCollectionsBrowser.getContent();

		TabPane tabPane = new TabPane();
		tabPane.setStyle("-fx-close-button-height: 50");
		Tab sokobanLevelCollectionsTab = new Tab("Sokoban collections", content);
		sokobanLevelCollectionsTab.setClosable(false);
		tabPane.getTabs().add(sokobanLevelCollectionsTab);

		FxExtensionPoint<LabeledChildExtender, Node> sokobanGamesExtensionPoint = new SimpleFxExtensionPoint<>(instanceRegistry, LabeledChildExtender.class, extender -> {
			var newGameTab = new Tab(extender.getText(), extender.getContent());
			newGameTab.setClosable(false);
			tabPane.getTabs().add(newGameTab);
			tabPane.getSelectionModel().select(newGameTab);
			return () -> tabPane.getTabs().remove(newGameTab);
		});

		sokobanCollectionsBrowser.setOnOpenAction(sokobanLevel -> {
			SokobanGameController sokobanGameController = new SokobanGameController(sokobanGamesExtensionPoint, sokobanLevel);
			Platform.runLater(() -> {
				var sokobanGame = sokobanGameController.startSokobanGame();
				snapshotManager.registerSokobanGame(sokobanGame);
			});
		});
		return tabPane;
	}
}
