package no.hal.sokoban.fx;

import java.util.List;
import java.util.function.Consumer;

import javafx.collections.ObservableList;
import javafx.geometry.Dimension2D;
import javafx.geometry.Insets;
import javafx.scene.Node;
import javafx.scene.layout.Border;
import javafx.scene.layout.BorderStroke;
import javafx.scene.layout.BorderStrokeStyle;
import javafx.scene.layout.BorderWidths;
import javafx.scene.layout.CornerRadii;
import javafx.scene.layout.Pane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.text.Text;
import no.hal.grid.fx.GridView;
import no.hal.plugin.InstanceRegistry;
import no.hal.settings.Settings;
import no.hal.sokoban.SokobanGrid.CellKind;
import no.hal.sokoban.fx.util.XYTransformStrategy;
import no.hal.sokoban.level.SokobanLevel;
import no.hal.sokoban.level.SokobanLevel.MetaData;
import no.hal.sokoban.snapshot.SnapshotManager;

public class SokobanLevelViewer {

    private Consumer<SokobanLevel> onSokobanLevelSelected = null;

    private Pane metaDataPane;
    private SokobanGridViewer sokobanGridViewer;

    private SokobanLevel sokobanLevel;

    public SokobanLevelViewer(Pane parent, InstanceRegistry instanceRegistry) {
        this.sokobanGridViewer = new SokobanGridViewer(instanceRegistry.getComponent(Settings.class));
        var sokobanPane = sokobanGridViewer.getGridView();
        this.sokobanGridViewer.setXYTransformStrategy(XYTransformStrategy.PREFER_HEIGHT);

        GridView<CellKind> gridView = sokobanGridViewer.getGridView();
        gridView.setCellSize(new Dimension2D(10, 10));
        gridView.setMaxWidth(400);
        gridView.setMaxHeight(400);

        sokobanGridViewer.getGridView().setOnMouseClicked(mouseEvent -> {
            if (onSokobanLevelSelected != null) {
                onSokobanLevelSelected.accept(this.sokobanLevel);
            }
        });
        this.metaDataPane = new VBox();
        this.metaDataPane.setPadding(new Insets(5, 0, 5, 10));

        parent.getChildren().addAll(sokobanPane, metaDataPane);
    }

    public void setOnSokobanLevelSelected(Consumer<SokobanLevel> sokobanLevelConsumer) {
        this.onSokobanLevelSelected = sokobanLevelConsumer;
    }

    public void updateView(SokobanLevel sokobanLevel, SnapshotManager.SnapshotState snapshotState) {
        this.sokobanLevel = sokobanLevel;
        this.sokobanGridViewer.setSokobanGrid(sokobanLevel.getSokobanGrid());
        var metaData = sokobanLevel.getMetaData();
        metaDataPane.getChildren().clear();
        ObservableList<Node> metaDataChildren = metaDataPane.getChildren();
        addMetaDataText(metaDataChildren, "Title", metaData, null);
        addMetaDataText(metaDataChildren, "Description", sokobanLevel.getMetaData(), null);
        addMetaDataText(metaDataChildren, "Author", sokobanLevel.getMetaData(), null);
        addMetaDataText(metaDataChildren, "hash", sokobanLevel.getMetaData(), "#");

        if (snapshotState != null) {
            var paint = switch (snapshotState) {
                 case FINISHED -> Color.GREEN;
                 case STARTED -> Color.ORANGE;
                 default -> null;
            };
            if (paint != null) {
                sokobanGridViewer.getGridView().setBorder(new Border(new BorderStroke(paint, BorderStrokeStyle.SOLID, CornerRadii.EMPTY, new BorderWidths(5))));
            }
        }
    }

    private void addMetaDataText(List<Node> children, String property, MetaData metaData, String altProperty) {
		var value = metaData.get(property);
		if (value != null) {
			children.add(new Text((altProperty != null ? altProperty : property) + ": " + value));
		}
	}
}
