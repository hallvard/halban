package no.hal.sokoban.app;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ServiceLoader;
import java.util.function.Function;

import javafx.application.Application;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.layout.Pane;
import javafx.stage.Stage;
import no.hal.plugin.InstanceRegistry;
import no.hal.plugin.Plugin;
import no.hal.sokoban.fx.SokobanAppController;
import no.hal.plugin.impl.InstanceRegistryImpl;
import no.hal.settings.CompositeSettings;
import no.hal.settings.Settings;
import no.hal.settings.SettingsProvider;
import no.hal.plugin.fx.FxInstanceRegistryImpl;
import no.hal.plugin.fx.xp.SimpleFxExtensionPoint;

public class SokobanApp extends Application {
    
    @Override
	public void start(Stage primaryStage) throws Exception {
        Scene scene = new Scene(new Pane(), 800, 800);
        scene.getStylesheets().add(getClass().getResource("app.css").toExternalForm());
        InstanceRegistry instanceRegistry = setupInstanceRegistry(scene);
        new SokobanAppController(SimpleFxExtensionPoint.forContainer(instanceRegistry, containerProvider -> {
            scene.setRoot(containerProvider.getContent());
            return () -> scene.setRoot(new Pane());
        }));

        //ChangeListener<? super Number> sizeListener = (prop, oldValue, newValue) -> {
        //    System.out.println("scene size: " + scene.getWidth() + " x " + scene.getHeight());
        //};
        //scene.widthProperty().addListener(sizeListener);
        //scene.heightProperty().addListener(sizeListener);

		primaryStage.setScene(scene);
        primaryStage.show();
	}

	private InstanceRegistryImpl setupInstanceRegistry(Scene root) {
		FxInstanceRegistryImpl instanceRegistry = new FxInstanceRegistryImpl(root);
		var settingsProviders = InstanceRegistryImpl.loadServices(instanceRegistry, SettingsProvider.class, () -> ServiceLoader.load(SettingsProvider.class));
        var settings = new CompositeSettings(
            SettingsProvider.loadSettings(settingsProviders, userSettingsOf("preferences")),
            SettingsProvider.loadSettings(settingsProviders, userSettingsOf("settings")),
            SettingsProvider.loadSettings(settingsProviders, resourceSettingsOf("application"))
        );
        instanceRegistry.registerInstance(settings, Settings.class);
		InstanceRegistryImpl.loadServices(instanceRegistry, Plugin.class, () -> ServiceLoader.load(Plugin.class));
		return instanceRegistry;
	}
	
    private Function<String, InputStream> resourceSettingsOf(String baseName) {
        return fileExtension -> SokobanApp.class.getResourceAsStream(baseName + "." + fileExtension);
    }

    private final static String userHome = System.getProperty("user.home", System.getenv("HOME"));

    private Function<String, InputStream> userSettingsOf(String baseName) {
        return fileExtension -> {
            var settingsFile = new File(userHome, baseName + "." + fileExtension);
            try {
                if (settingsFile.exists()) {
                    return new FileInputStream(settingsFile);
                }
            } catch (IOException ioex) {
                // ignore
            }
            return null;
        };
    }

    public static void main(String[] args) {
        launch(SokobanApp.class, args);
    }
}
