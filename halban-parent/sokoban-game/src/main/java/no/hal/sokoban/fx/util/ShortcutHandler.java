package no.hal.sokoban.fx.util;

import java.util.Map;
import java.util.function.Supplier;

import javafx.application.Platform;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.input.KeyCodeCombination;
import javafx.scene.input.Mnemonic;

public class ShortcutHandler {

    private Supplier<Scene> scene;

    public ShortcutHandler(Supplier<Scene> scene) {
        this.scene = scene;
    }

    public void registerShortcuts(Map<KeyCodeCombination, Button> keyButtons ) {
		Platform.runLater(() -> {
            Scene scene = this.scene.get();
            for (var keyButton : keyButtons.entrySet()) {
                scene.addMnemonic(new Mnemonic(keyButton.getValue(), keyButton.getKey()));
            }
		});
    }
}
