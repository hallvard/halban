package no.hal.sokoban.fx;

import no.hal.grid.fx.GridView;
import no.hal.plugin.Context;
import no.hal.sokoban.SokobanGrid.CellKind;
import no.hal.sokoban.SokobanGrid.ContentKind;
import no.hal.sokoban.SokobanGrid.FloorKind;
import no.hal.sokoban.fx.util.RegionSizeTracker;
import no.hal.sokoban.level.SokobanLevel;
import no.hal.sokoban.level.SokobanLevel.MetaData;
import no.hal.plugin.fx.FxExtensionPoint;
import no.hal.plugin.fx.CompositeLabelAdapter;
import no.hal.plugin.fx.LabelAdapter;
import no.hal.plugin.fx.SimpleFxExtensionPoint;
import no.hal.plugin.impl.ContributionContext;
import no.hal.sokoban.snapshot.SnapshotManager;

import java.util.Collections;
import java.util.List;

import javafx.application.Platform;
import javafx.collections.ObservableList;
import javafx.geometry.Dimension2D;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import javafx.scene.layout.Background;
import javafx.scene.layout.BackgroundFill;
import javafx.scene.layout.Border;
import javafx.scene.layout.BorderStroke;
import javafx.scene.layout.BorderStrokeStyle;
import javafx.scene.layout.BorderWidths;
import javafx.scene.layout.CornerRadii;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.text.Text;

public class SokobanAppController {

	private TabPane tabPane;

	private SnapshotManager snapshotSaver;

	public SokobanAppController(FxExtensionPoint<Parent> extensionPoint) {
		Context context = extensionPoint.getContext();
		this.snapshotSaver = new SnapshotManager();
		SokobanLevel.CollectionProvider snapshotsProvider = () -> snapshotSaver;
		context.registerQualifiedService(SokobanLevel.CollectionProvider.class, snapshotsProvider);
		context.registerQualifiedService(LabelAdapter.class, LabelAdapter.forInstance(snapshotsProvider, "Snapshots"));
		Parent content = createContent(context);
		extensionPoint.extend(content);
	}
	
	public Parent createContent(Context context) {
		ContributionContext.load(LabelAdapter.class, context);
		var sokobanLevelCollectionsController = new SokobanLevelCollectionsController();
		var sokobanGridsPane = new VBox();
		sokobanGridsPane.setAlignment(Pos.CENTER_LEFT);
		sokobanGridsPane.setFillWidth(true);

		ScrollPane rightPane = new ScrollPane(sokobanGridsPane);
		RegionSizeTracker.trackSize(sokobanGridsPane, null);

		RegionSizeTracker.trackSize(rightPane, "right pane");
		tabPane = createLayout(sokobanLevelCollectionsController.createContent(), rightPane);
	
		sokobanLevelCollectionsController.selectedSokobanLevelProperty().addListener((prop, oldValue, newValue) -> {
			if (newValue != null) {
				fillSokobanGridsPane(Collections.singletonList(newValue), sokobanGridsPane.getChildren(), context);
			}
		});
		sokobanLevelCollectionsController.selectedSokobanCollectionProperty().addListener((prop, oldValue, newValue) -> {
			if (newValue != null) {
				fillSokobanGridsPane(newValue.getSokobanLevels(), sokobanGridsPane.getChildren(), context);
			}
		});
		Platform.runLater(() -> sokobanLevelCollectionsController.selectFirst());

		sokobanLevelCollectionsController.setOnOpenSokobanLevel(sokobanLevel -> openSokobanGame(context, sokobanLevel));

		sokobanLevelCollectionsController.setLabelAdapter(CompositeLabelAdapter.fromContext(context));

		var collectionProviders = context.getAllComponents(SokobanLevel.CollectionProvider.class);
		sokobanLevelCollectionsController.setSokobanLevelCollectionProviders(collectionProviders);

		var collectionsProviders = context.getAllComponents(SokobanLevel.CollectionsProvider.class);
		sokobanLevelCollectionsController.setSokobanLevelCollectionsProviders(collectionsProviders);

		RegionSizeTracker.trackSize(tabPane, "tab pane");

		return tabPane;
	}

	private void addMetaDataText(List<Node> children, String property, MetaData metaData, String altProperty) {
		var value = metaData.get(property);
		if (value != null) {
			children.add(new Text((altProperty != null ? altProperty : property) + ": " + value));
		}
	}

	private void fillSokobanGridsPane(Iterable<SokobanLevel> sokobanLevels, ObservableList<Node> children, Context context) {
		children.clear();
		for (var sokobanLevel : sokobanLevels) {

			var sokobanGridView = new SokobanGridView();
	
			GridView<CellKind> gridView = sokobanGridView.getGridView();
			gridView.setCellSize(new Dimension2D(10, 10));
			gridView.setMaxWidth(400);
			gridView.setMaxHeight(400);
			gridView.setPadding(new Insets(10));
	
			sokobanGridView.setSokobanGrid(sokobanLevel.getSokobanGrid());
			sokobanGridView.setOnMouseClicked(mouseEvent -> openSokobanGame(context, sokobanLevel));

			var metaDataPane = new VBox();
			metaDataPane.setPadding(new Insets(15, 10, 15, 0));
			MetaData metaData = sokobanLevel.getMetaData();
			addMetaDataText(metaDataPane.getChildren(), "Title", metaData, null);
			addMetaDataText(metaDataPane.getChildren(), "Description", sokobanLevel.getMetaData(), null);
			addMetaDataText(metaDataPane.getChildren(), "Author", sokobanLevel.getMetaData(), null);

			var snapshot = snapshotSaver.getSnapshot("uri", metaData.get("uri"));
			if (snapshot != null) {
				var paint = snapshot.getSokobanGrid().countCells(FloorKind.TARGET, ContentKind.EMPTY) == 0 ? Color.GREEN : Color.ORANGE;
				gridView.setBorder(new Border(new BorderStroke(paint, BorderStrokeStyle.SOLID, CornerRadii.EMPTY, new BorderWidths(5))));
			}

			var pane = new HBox(sokobanGridView, metaDataPane);

			children.add(pane);
		}
	}

	private void openSokobanGame(Context context, SokobanLevel sokobanLevel) {
		SokobanGameController sokobanGameController = new SokobanGameController(SimpleFxExtensionPoint.forNode(context, node -> {
			var newGameTab = new Tab(sokobanLevel.getMetaData().get("Title"), node);
			tabPane.getTabs().add(newGameTab);
			tabPane.getSelectionModel().select(newGameTab);
		}), sokobanLevel);
		Platform.runLater(() -> {
			var sokobanGame = sokobanGameController.startSokobanGame();
			snapshotSaver.registerSokobanGame(sokobanGame);
		});
	}

	private TabPane createLayout(Region left, Region right) {
		TabPane tabPane = new TabPane();
		var sokobanLevelCollectionsTabContent = new HBox(left, right);
		HBox.setHgrow(right, Priority.ALWAYS);
		RegionSizeTracker.trackSize(sokobanLevelCollectionsTabContent, "tab content");

		left.setMinWidth(250);
		left.setPrefWidth(250);
		left.setMaxWidth(250);
		// right.setPrefWidth(Double.MAX_VALUE);
		sokobanLevelCollectionsTabContent.setFillHeight(true);
		// sokobanLevelCollectionsTabContent.setDividerPositions(0.3f, 0.7f);
		Tab sokobanLevelCollectionsTab = new Tab("Sokoban collections", sokobanLevelCollectionsTabContent);
		sokobanLevelCollectionsTab.setClosable(false);
		tabPane.getTabs().add(sokobanLevelCollectionsTab);
		return tabPane;
	}
}
