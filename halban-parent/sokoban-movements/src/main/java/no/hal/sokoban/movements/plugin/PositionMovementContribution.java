package no.hal.sokoban.movements.plugin;

import com.gluonhq.attach.position.PositionService;

import javafx.scene.Node;
import no.hal.plugin.Contribution;
import no.hal.plugin.InstanceRegistry;
import no.hal.plugin.fx.ContentProvider;
import no.hal.plugin.fx.xp.FxExtensionPoint;
import no.hal.sokoban.SokobanGame;
import no.hal.sokoban.movements.fx.PositionMovementController;

public class PositionMovementContribution implements Contribution {
    
    @Override
    public void activate(InstanceRegistry instanceRegistry) {
        instanceRegistry.addListener((clazz, qualifier, oldValue, newValue) -> {
            if (FxExtensionPoint.class == clazz && newValue instanceof FxExtensionPoint extensionPoint && qualifier instanceof SokobanGame.Provider sokobanGameProvider) {
                // scope added, check to see if it contains an FxExtensionPoint
                if (extensionPoint != null && ContentProvider.Child.class == extensionPoint.forClass()) {
                    PositionService.create().ifPresent(service ->
                        new PositionMovementController((FxExtensionPoint<ContentProvider.Child, Node>) extensionPoint, sokobanGameProvider, service)
                    );
                }
            }
        });
    }
}
