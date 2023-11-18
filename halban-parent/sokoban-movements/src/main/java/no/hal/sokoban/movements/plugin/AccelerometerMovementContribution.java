package no.hal.sokoban.movements.plugin;

import java.util.function.Consumer;

import com.gluonhq.attach.accelerometer.AccelerometerService;

import javafx.scene.Node;
import no.hal.plugin.Contribution;
import no.hal.plugin.InstanceRegistry;
import no.hal.plugin.fx.ContentProvider;
import no.hal.plugin.fx.xp.FxExtensionPoint;
import no.hal.plugin.fx.xp.LabeledChildExtender;
import no.hal.sokoban.SokobanGame;
import no.hal.sokoban.movements.fx.AccelerometerConfigurationController;
import no.hal.sokoban.movements.fx.AccelerometerMovementController;

public class AccelerometerMovementContribution implements Contribution {

    private AccelerometerService accelerometerService = null;

    private AccelerometerService getAccelerometerService() {
        if (accelerometerService == null) {
            AccelerometerService.create().ifPresent(service -> this.accelerometerService = service);
        }
        return accelerometerService;
    }

    private void ifAccelerometerServicePresent(Consumer<AccelerometerService> consumer) {
        var service = getAccelerometerService();
        if (service != null) {
            consumer.accept(service);
        }
    }

    @Override
    public void activate(InstanceRegistry instanceRegistry) {
        instanceRegistry.addListener((clazz, qualifier, oldValue, newValue) -> {
            if (FxExtensionPoint.class == clazz && newValue instanceof FxExtensionPoint extensionPoint && extensionPoint != null) {
                // scope added, check to see if it contains an FxExtensionPoint
                if (ContentProvider.Child.class == extensionPoint.forClass() && qualifier instanceof SokobanGame.Provider sokobanGameProvider) {
                    ifAccelerometerServicePresent(service ->
                        new AccelerometerMovementController((FxExtensionPoint<ContentProvider.Child, Node>) extensionPoint, sokobanGameProvider, service)
                    );
                } else if (LabeledChildExtender.class == extensionPoint.forClass() && qualifier == null) {
                    new AccelerometerConfigurationController((FxExtensionPoint<LabeledChildExtender, Node>)extensionPoint, getAccelerometerService());
                }
            }
        });
    }
}
