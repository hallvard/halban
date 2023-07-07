package no.hal.sokoban.recorder.plugin;

import javafx.scene.Node;
import no.hal.plugin.Context;
import no.hal.plugin.Contribution;
import no.hal.sokoban.SokobanGame;
import no.hal.plugin.fx.FxExtensionPoint;
import no.hal.sokoban.recorder.fx.MoveRecorderController;

public class MoveRecorderContribution implements Contribution {
    
    @Override
    public void activate(Context context) {
        context.addListener((clazz, qualifier, oldValue, newValue) -> {
            if (FxExtensionPoint.class == clazz && newValue instanceof FxExtensionPoint extensionPoint && qualifier instanceof SokobanGame.Provider sokobanGameProvider) {
                // scope context added, check to see if it contains an FxExtensionPoint
                if (extensionPoint != null && Node.class == extensionPoint.forClass()) {
                    new MoveRecorderController((FxExtensionPoint<Node>) extensionPoint, sokobanGameProvider);
                }
            }
        });
    }
}
