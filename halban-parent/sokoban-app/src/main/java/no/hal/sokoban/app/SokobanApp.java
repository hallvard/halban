package no.hal.sokoban.app;

import java.util.ServiceLoader;
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
        ConfigurationProvider.loadSettings(configProviders, new UserSettingsProvider("preferences")),
        ConfigurationProvider.loadSettings(configProviders, new UserSettingsProvider("settings")),
        ConfigurationProvider.loadSettings(configProviders, new ConfigurationResourceProvider("application", SokobanApp.class))
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

    primaryStage.setScene(scene);
    primaryStage.show();
  }

  public static void main(String[] args) {
    launch(SokobanApp.class, args);
  }
}
