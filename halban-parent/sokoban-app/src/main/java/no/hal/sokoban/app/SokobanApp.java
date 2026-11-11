package no.hal.sokoban.app;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ServiceLoader;
import java.util.function.Function;
import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.layout.Pane;
import javafx.stage.Stage;
import no.hal.config.Configuration;
import no.hal.config.ConfigurationProvider;
import no.hal.config.ext.ExtConfigurationProvider;
import no.hal.fx.IconFonts;
import no.hal.fx.TouchSlop;
import no.hal.sokoban.fx.controllers.SokobanAppController;

public class SokobanApp extends Application {

  @Override
  public void start(Stage primaryStage) throws Exception {
    // before the first icon is made
    IconFonts.load();
    Scene scene = new Scene(new Pane(), 800, 800);
    scene.getStylesheets().add(getClass().getResource("app.css").toExternalForm());

    // load configuration providers, first
    var configProviders = ExtConfigurationProvider.loadServices(ConfigurationProvider.class, ServiceLoader::load);

    // then load the application configurations
    var configs = Configuration.of(
        ConfigurationProvider.loadSettings(configProviders, userSettingsOf("preferences")),
        ConfigurationProvider.loadSettings(configProviders, userSettingsOf("settings")),
        ConfigurationProvider.loadSettings(configProviders, "application", SokobanApp.class)
    );
    configs.registerInstance(scene, Scene.class);

    // load extensions
    var extConfigurations = ExtConfigurationProvider.loadServices(ExtConfigurationProvider.class, ServiceLoader::load);
    // and add their configurations
    for (var extConfiguration : extConfigurations) {
      var config = extConfiguration.getConfiguration();
      if (config != null) {
        configs.addConfiguration(config);
      }
    }
    // and finally register their instances
    for (var extConfiguration : extConfigurations) {
      extConfiguration.registerInstances(configs);
    }
  
    SokobanAppController appController = new SokobanAppController(configs);
    scene.setRoot(appController.getContent());
    // a tap on a list item selects it on a touch screen, even if the finger moves a little
    TouchSlop.install(scene);
    // ChangeListener<? super Number> sizeListener = (prop, oldValue, newValue) -> {

    //   System.out.println("scene size: " + scene.getWidth() + " x " +
    //       scene.getHeight());
    // };
    // scene.widthProperty().addListener(sizeListener);
    // scene.heightProperty().addListener(sizeListener);

    primaryStage.setScene(scene);
    primaryStage.show();
  }

  private final static String USER_HOME = System.getProperty("user.home", System.getenv("HOME"));

  private Function<String, InputStream> userSettingsOf(String baseName) {
    return fileExtension -> {
      var settingsFile = new File(USER_HOME, baseName + "." + fileExtension);
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
