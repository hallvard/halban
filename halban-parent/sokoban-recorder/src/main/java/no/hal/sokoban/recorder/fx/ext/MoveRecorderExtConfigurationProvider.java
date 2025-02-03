package no.hal.sokoban.recorder.fx.ext;

import no.hal.config.ext.ExtConfiguration;
import no.hal.config.ext.ExtConfigurationProvider;
import no.hal.sokoban.fx.ShortcutHandler;
import no.hal.sokoban.fx.SokobanGameController;
import no.hal.sokoban.fx.SokobanGameSubController;
import no.hal.sokoban.recorder.fx.MoveRecorderController;

public class MoveRecorderExtConfigurationProvider implements ExtConfigurationProvider {

  @Override
  public void registerInstances(ExtConfiguration config) {
    config.registerInstance(new SokobanGameSubController.Provider() {
      @Override
      public SokobanGameSubController createSokobanGameSubController(SokobanGameController sokobanGameController) {
        return new MoveRecorderController(sokobanGameController, config.getInstance(ShortcutHandler.class));
      }     
    }, SokobanGameSubController.Provider.class);
  }
}
