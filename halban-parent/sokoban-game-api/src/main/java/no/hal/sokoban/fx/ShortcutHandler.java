package no.hal.sokoban.fx;

import java.util.Map;
import java.util.function.Supplier;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.input.KeyCodeCombination;
import javafx.scene.input.Mnemonic;

public class ShortcutHandler {

  private final Supplier<Scene> sceneSupplier;

  public ShortcutHandler(Supplier<Scene> sceneSupplier) {
    this.sceneSupplier = sceneSupplier;
  }

  public void registerShortcuts(Map<KeyCodeCombination, Button> keyButtons) {
    Scene scene = sceneSupplier.get();
    for (var keyButton : keyButtons.entrySet()) {
      scene.addMnemonic(new Mnemonic(keyButton.getValue(), keyButton.getKey()));
    }
  }
}
