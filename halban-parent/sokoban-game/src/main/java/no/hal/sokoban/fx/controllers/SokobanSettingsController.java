package no.hal.sokoban.fx.controllers;

import java.util.ArrayList;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import javafx.beans.binding.Bindings;
import javafx.beans.property.StringProperty;
import javafx.collections.ListChangeListener;
import javafx.geometry.Dimension2D;
import javafx.geometry.Insets;
import javafx.scene.Parent;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TextField;
import javafx.scene.control.TextFormatter;
import javafx.scene.control.ToggleButton;
import javafx.scene.control.ToggleGroup;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import no.hal.config.ext.ExtConfiguration;
import no.hal.fx.ContentProvider;
import no.hal.grid.fx.GridView;
import no.hal.grid.impl.GridImpl;
import no.hal.sokoban.SokobanGrid.CellKind;
import no.hal.sokoban.fx.views.CellCharacters;
import no.hal.sokoban.fx.views.CharacterSet;
import no.hal.sokoban.fx.views.SokobanCellFactory;
import no.hal.sokoban.impl.SokobanFactoryImpl;

/**
 * The settings page. A dropdown chooses how the cells of the board are drawn: the standard, which is the images of the game, or one of the
 * named sets of characters (see {@link CellCharacters}). When a set is chosen it can be edited: its name, the font, and a character of the font for
 * each kind of cell. Sets can be made (new, or a copy of the one that is chosen) and removed. A preview shows a small board with all the kinds
 * of cells, drawn as the choice says, and it follows the changes.
 * <p>
 * The characters are picked from a block of the Unicode characters, which are shown in the font: the font can't tell which characters it has,
 * so a block may have some that it doesn't, which are drawn as boxes (or with another font), and they are then not good choices.
 * A character can also be typed (or pasted).
 */
public class SokobanSettingsController implements ContentProvider<Parent> {

  private record Block(String name, int from, int to) {
    @Override
    public String toString() {
      return name;
    }
  }

  private static final List<Block> BLOCKS = List.of(
      new Block("Letters, digits and signs", 0x21, 0x7E),
      new Block("Arrows", 0x2190, 0x21FF),
      new Block("Technical", 0x2300, 0x23FF),
      new Block("Box drawing", 0x2500, 0x257F),
      new Block("Block elements", 0x2580, 0x259F),
      new Block("Geometric shapes", 0x25A0, 0x25FF),
      new Block("Symbols", 0x2600, 0x26FF),
      new Block("Dingbats", 0x2700, 0x27BF),
      new Block("Pictographs", 0x1F300, 0x1F5FF),
      new Block("Emoticons", 0x1F600, 0x1F64F));

  private static final String[] PREVIEW = {
      "##########",
      "#@ $ .   #",
      "#  * $ + #",
      "#   .    #",
      "##########" };

  private static final Map<CellKind, String> NAMES = new EnumMap<>(Map.of(
      CellKind.WALL, "Wall",
      CellKind.EMPTY, "Floor",
      CellKind.TARGET, "Target",
      CellKind.EMPTY_PLAYER, "Player",
      CellKind.TARGET_PLAYER, "Player on target",
      CellKind.EMPTY_BOX, "Box",
      CellKind.TARGET_BOX, "Box on target"));

  private static final String STANDARD = "Standard (images)";

  private final ExtConfiguration config;
  private final CellCharacters characters;
  private final ComboBox<String> choiceBox = new ComboBox<>();
  private final ToggleGroup kindGroup = new ToggleGroup();
  private final FlowPane palette = new FlowPane(4, 4);
  private final ComboBox<Block> blockBox = new ComboBox<>();
  private final ComboBox<String> fontBox = new ComboBox<>();
  private final TextField nameField = new TextField();
  private final Map<CellKind, TextField> characterFields = new EnumMap<>(CellKind.class);
  /** Set while the fields are filled from the set that is edited, so that is not taken as the player changing it. */
  private boolean filling = false;

  public SokobanSettingsController(ExtConfiguration config) {
    this.config = config;
    this.characters = config.getInstance(CellCharacters.class);
  }

  @Override
  public Parent getContent() {
    // the choice, with the buttons that make and remove sets
    choiceBox.setVisibleRowCount(10);
    var newButton = new Button("New");
    newButton.setOnAction(ev -> {
      var set = new CharacterSet(characters.uniqueName("New set"));
      characters.getSets().add(set);
      characters.activeProperty().set(set);
    });
    var copyButton = new Button("Copy");
    copyButton.setOnAction(ev -> {
      var set = new CharacterSet(characters.getActive(), characters.uniqueName(characters.getActive().getName() + " copy"));
      characters.getSets().add(set);
      characters.activeProperty().set(set);
    });
    var deleteButton = new Button("Delete");
    deleteButton.setOnAction(ev -> characters.getSets().remove(characters.getActive()));
    copyButton.disableProperty().bind(characters.activeProperty().isNull());
    deleteButton.disableProperty().bind(characters.activeProperty().isNull());

    var editor = createEditor();
    var standardHint = new Label("The images of the game are used. Choose a set, or make a new one, to draw the cells with characters.");
    standardHint.setWrapText(true);
    editor.visibleProperty().bind(characters.activeProperty().isNotNull());
    editor.managedProperty().bind(editor.visibleProperty());
    standardHint.visibleProperty().bind(characters.activeProperty().isNull());
    standardHint.managedProperty().bind(standardHint.visibleProperty());

    var title = new Label("Settings");
    title.setFont(Font.font(null, FontWeight.BOLD, 18));
    var content = new VBox(10,
        title,
        new HBox(8, new Label("Draw the cells with"), choiceBox, newButton, copyButton, deleteButton),
        standardHint,
        editor,
        new Label("Preview"),
        createPreview());
    content.setPadding(new Insets(12));

    // the choice lists the standard and the sets, and the one that is chosen is the one that is edited
    characters.getSets().addListener((ListChangeListener<CharacterSet>) change -> fillChoice());
    characters.activeProperty().addListener(obs -> {
      fillChoice();
      fillEditor();
    });
    choiceBox.valueProperty().addListener(obs -> {
      if (! filling) {
        int index = choiceBox.getItems().indexOf(choiceBox.getValue());
        characters.activeProperty().set(index > 0 ? characters.getSets().get(index - 1) : null);
      }
    });
    fillChoice();
    fillEditor();

    var scroll = new ScrollPane(content);
    scroll.setFitToWidth(true);
    return scroll;
  }

  /** The names of the standard and the sets, with the active one chosen. */
  private void fillChoice() {
    var wasFilling = filling;
    filling = true;
    var names = new ArrayList<String>();
    names.add(STANDARD);
    characters.getSets().forEach(set -> names.add(set.getName()));
    choiceBox.getItems().setAll(names);
    int index = characters.getSets().indexOf(characters.getActive());
    choiceBox.getSelectionModel().select(index + 1);
    filling = wasFilling;
  }

  /** The part for editing the set that is chosen. */
  private VBox createEditor() {
    nameField.textProperty().addListener(obs -> edit(CharacterSet::nameProperty, nameField.getText()));

    fontBox.getItems().setAll(Font.getFamilies());
    fontBox.setVisibleRowCount(12);
    fontBox.valueProperty().addListener(obs -> {
      edit(CharacterSet::fontFamilyProperty, fontBox.getValue());
      fillPalette();
    });

    var kinds = new GridPane();
    kinds.setHgap(8);
    kinds.setVgap(4);
    int row = 0;
    for (var kind : CellKind.values()) {
      var kindButton = new ToggleButton(NAMES.get(kind));
      kindButton.setUserData(kind);
      kindButton.setToggleGroup(kindGroup);
      kindButton.setMaxWidth(Double.MAX_VALUE);
      var field = new TextField();
      field.setPrefColumnCount(2);
      // one character, which may be two chars of Java (an emoji, for instance)
      field.setTextFormatter(new TextFormatter<String>(change ->
          change.getControlNewText().codePointCount(0, change.getControlNewText().length()) <= 1 ? change : null));
      field.textProperty().addListener(obs -> edit(set -> set.characterProperty(kind), field.getText()));
      field.fontProperty().bind(Bindings.createObjectBinding(() -> Font.font(fontBox.getValue(), 20), fontBox.valueProperty()));
      characterFields.put(kind, field);
      kinds.addRow(row++, kindButton, field);
    }
    kindGroup.selectToggle(kindGroup.getToggles().get(0));
    // a toggle can't be left off, a click on the selected one would otherwise leave no kind to give a character to
    kindGroup.selectedToggleProperty().addListener((obs, oldToggle, newToggle) -> {
      if (newToggle == null) {
        kindGroup.selectToggle(oldToggle);
      }
    });

    blockBox.getItems().setAll(BLOCKS);
    blockBox.getSelectionModel().select(5);
    blockBox.valueProperty().addListener(obs -> fillPalette());
    var paletteScroll = new ScrollPane(palette);
    paletteScroll.setFitToWidth(true);
    paletteScroll.setPrefHeight(220);
    palette.setPadding(new Insets(4));
    HBox.setHgrow(paletteScroll, Priority.ALWAYS);

    return new VBox(10,
        new HBox(8, new Label("Name"), nameField, new Label("Font"), fontBox),
        new Label("Choose a kind of cell, and a character for it from the list, or type one."),
        new HBox(16, kinds, new VBox(6, new HBox(8, new Label("Characters from"), blockBox), paletteScroll)));
  }

  /** Gives the set that is edited the value, unless the fields are being filled from it. */
  private void edit(Function<CharacterSet, StringProperty> property, String value) {
    if (! filling && characters.getActive() != null) {
      property.apply(characters.getActive()).set(value);
    }
  }

  /** Fills the fields from the set that is edited, which is the one that is chosen. */
  private void fillEditor() {
    var set = characters.getActive();
    if (set == null) {
      return;
    }
    var wasFilling = filling;
    filling = true;
    nameField.setText(set.getName());
    fontBox.setValue(set.getFontFamily());
    characterFields.forEach((kind, field) -> field.setText(set.getCharacter(kind)));
    filling = wasFilling;
    fillPalette();
  }

  /** The characters of the block, as buttons, in the font. A button gives its character to the selected kind. */
  private void fillPalette() {
    palette.getChildren().clear();
    var block = blockBox.getValue();
    var set = characters.getActive();
    if (block == null || set == null) {
      return;
    }
    var font = Font.font(set.getFontFamily(), 22);
    for (int codePoint = block.from(); codePoint <= block.to(); codePoint++) {
      if (! Character.isDefined(codePoint) || Character.isISOControl(codePoint)) {
        continue;
      }
      var character = Character.toString(codePoint);
      var button = new Button(character);
      button.setFont(font);
      button.setMinSize(40, 40);
      button.setOnAction(ev -> {
        if (kindGroup.getSelectedToggle() != null && characters.getActive() != null) {
          characters.getActive().characterProperty((CellKind) kindGroup.getSelectedToggle().getUserData()).set(character);
        }
      });
      palette.getChildren().add(button);
    }
  }

  /** A small board with all the kinds of cells, drawn as the boards are, so it shows the images when the standard is chosen. */
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
    gridView.setCellSize(new Dimension2D(40, 40));
    var cellFactory = new SokobanCellFactory(config, characters);
    cellFactory.setUpdateCallback((topLeft, bottomRight) -> gridView.updateCells());
    gridView.setCellFactory(cellFactory);
    gridView.setGrid(grid);
    return gridView;
  }
}
