package no.hal.sokoban.movements.fx.ext;

import com.gluonhq.attach.position.PositionService;
import no.hal.config.ext.ExtConfiguration;
import no.hal.config.ext.ExtConfigurationProvider;
import no.hal.sokoban.fx.SokobanGameController;
import no.hal.sokoban.fx.SokobanGameSubController;
import no.hal.sokoban.movements.fx.PositionMovementController;

public class PositionMovementExtConfigurationProvider implements ExtConfigurationProvider {

  @Override
  public void registerInstances(ExtConfiguration config) {
    PositionService.create().ifPresent(positionService -> {
      config.registerInstance(new SokobanGameSubController.Provider() {
        @Override
        public SokobanGameSubController createSokobanGameSubController(SokobanGameController sokobanGameController) {
          return new PositionMovementController(sokobanGameController, positionService);
        }
      }, SokobanGameSubController.Provider.class);
      config.registerInstance(positionService, PositionService.class);
    });
  }
}
