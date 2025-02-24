package no.hal.sokoban.fx.views;

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
import javafx.scene.paint.Paint;
import javafx.scene.text.Text;
import no.hal.config.Configuration;
import no.hal.grid.fx.GridView;
import no.hal.sokoban.SokobanGrid.CellKind;
import no.hal.sokoban.fx.util.XYTransformStrategy;
import no.hal.sokoban.level.SokobanLevel;
import no.hal.sokoban.level.SokobanLevel.MetaData;
import no.hal.sokoban.snapshot.SnapshotManager;

public class SokobanLevelView {

    private Consumer<SokobanLevel> onSokobanLevelSelected = null;

    private Pane metaDataPane;
    private SokobanGridView sokobanGridView;

    private SokobanLevel sokobanLevel;

    public SokobanLevelView(Configuration config, Pane parent) {
        this.sokobanGridView = new SokobanGridView(config);
        var sokobanPane = sokobanGridView.getGridView();
        this.sokobanGridView.setXYTransformStrategy(XYTransformStrategy.PREFER_HEIGHT);

        GridView<CellKind> gridView = sokobanGridView.getGridView();
        gridView.setCellSize(new Dimension2D(10, 10));
        gridView.setMaxWidth(400);
        gridView.setMaxHeight(400);

        sokobanGridView.getGridView().setOnMouseClicked(ev -> {
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
        this.sokobanGridView.setSokobanGrid(sokobanLevel.getSokobanGrid());
        var metaData = sokobanLevel.getMetaData();
        metaDataPane.getChildren().clear();
        ObservableList<Node> metaDataChildren = metaDataPane.getChildren();
        addMetaDataText(metaDataChildren, "Title", metaData, null);
        addMetaDataText(metaDataChildren, "Description", sokobanLevel.getMetaData(), null);
        addMetaDataText(metaDataChildren, "Author", sokobanLevel.getMetaData(), null);
        addMetaDataText(metaDataChildren, "hash", sokobanLevel.getMetaData(), "#");

        Paint paint = null;
        if (snapshotState != null) {
            paint = switch (snapshotState) {
                case FINISHED -> Color.GREEN;
                case STARTED -> Color.ORANGE;
                default -> null;
            };
        }
        var border = (paint != null ? new Border(new BorderStroke(paint, BorderStrokeStyle.SOLID, CornerRadii.EMPTY, new BorderWidths(5))) : new Border((BorderStroke[]) null));
        sokobanGridView.getGridView().setBorder(border);
    }

    private void addMetaDataText(List<Node> children, String property, MetaData metaData, String altProperty) {
		var value = metaData.get(property);
		if (value != null) {
			children.add(new Text((altProperty != null ? altProperty : property) + ": " + value));
		}
	}
}
