package no.hal.sokoban.movements.plugin;

import com.gluonhq.attach.accelerometer.AccelerometerService;

import javafx.scene.Node;
import no.hal.plugin.Contribution;
import no.hal.plugin.InstanceRegistry;
import no.hal.plugin.fx.ContentProvider;
import no.hal.plugin.fx.xp.FxExtensionPoint;
import no.hal.sokoban.SokobanGame;
import no.hal.sokoban.movements.fx.AccelerometerMovementController;

public class AccelerometerMovementContribution implements Contribution {
    
    private AccelerometerService accelerometerService;

    public AccelerometerMovementContribution() {
        AccelerometerService.create().ifPresent(service -> this.accelerometerService = service);
    }

    @Override
    public void activate(InstanceRegistry instanceRegistry) {
        System.out.println("AccelerometerService: " + accelerometerService);
        instanceRegistry.addListener((clazz, qualifier, oldValue, newValue) -> {
            if (FxExtensionPoint.class == clazz && newValue instanceof FxExtensionPoint extensionPoint && qualifier instanceof SokobanGame.Provider sokobanGameProvider) {
                // scope added, check to see if it contains an FxExtensionPoint
                if (extensionPoint != null && ContentProvider.Child.class == extensionPoint.forClass()) {
                    new AccelerometerMovementController((FxExtensionPoint<ContentProvider.Child, Node>) extensionPoint, sokobanGameProvider, accelerometerService);
                }
            }
        });
    }
}
