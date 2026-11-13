package no.hal.sokoban.fx.controllers;

import javafx.geometry.Dimension2D;
import javafx.geometry.Insets;
import javafx.scene.Parent;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.layout.VBox;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import no.hal.config.ext.ExtConfiguration;
import no.hal.fx.ContentProvider;
import no.hal.grid.fx.GridView;
import no.hal.grid.impl.GridImpl;
import no.hal.sokoban.SokobanGrid.CellKind;
import no.hal.sokoban.fx.views.CellStyle;
import no.hal.sokoban.fx.views.SokobanCellFactory;
import no.hal.sokoban.impl.SokobanFactoryImpl;

/**
 * The settings page: a dropdown that chooses how the cells of the board are drawn (see {@link CellStyle}), and below it a preview, a small
 * board with all the kinds of cells, drawn in the chosen style. The boards that are open follow the choice at once.
 */
public class SokobanSettingsController implements ContentProvider<Parent> {

  private static final String[] PREVIEW = {
      "##########",
      "#@ $ .   #",
      "#  * $   #",
      "#   .    #",
      "##########" };

  private final ExtConfiguration config;
  private final CellStyle cellStyle;

  public SokobanSettingsController(ExtConfiguration config) {
    this.config = config;
    this.cellStyle = config.getInstance(CellStyle.class);
  }

  @Override
  public Parent getContent() {
    var title = new Label("Settings");
    title.setFont(Font.font(null, FontWeight.BOLD, 18));
    var styleBox = new ComboBox<CellStyle.Style>();
    styleBox.getItems().setAll(cellStyle.getStyles());
    styleBox.valueProperty().bindBidirectional(cellStyle.styleProperty());
    var preview = createPreview();
    var content = new VBox(10, title, new Label("Draw the cells with"), styleBox, new Label("Preview"), preview);
    content.setPadding(new Insets(12));

    var scroll = new ScrollPane(content);
    scroll.setFitToWidth(true);
    // the preview has the width that is there, a part of it, and at most 24, so that it fits on a phone
    scroll.viewportBoundsProperty().addListener((obs, oldBounds, bounds) -> {
      double cell = Math.max(8, Math.min(24, Math.floor((bounds.getWidth() - 2 * 12) / PREVIEW[0].length() * 0.6)));
      preview.setCellSize(new Dimension2D(cell, cell));
    });
    return scroll;
  }

  private GridView<CellKind> createPreview() {
    var factory = new SokobanFactoryImpl();
    var grid = new GridImpl<CellKind>(PREVIEW[0].length(), PREVIEW.length) {
      {
        for (int y = 0; y < PREVIEW.length; y++) {
          for (int x = 0; x < PREVIEW[y].length(); x++) {
            setCell(x, y, factory.toCellKind(PREVIEW[y].charAt(x)));
          }
        }
      }
    };
    var gridView = new GridView<CellKind>();
    gridView.setCellSize(new Dimension2D(24, 24));
    var cellFactory = new SokobanCellFactory(config, cellStyle);
    cellFactory.setUpdateCallback((topLeft, bottomRight) -> gridView.updateCells());
    gridView.setCellFactory(cellFactory);
    gridView.setGrid(grid);
    return gridView;
  }
}
