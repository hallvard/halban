module sokoban.movements {

    requires config.api;
    requires grid.api;
    requires grid.fx;
    requires sokoban.game;

    requires com.gluonhq.attach.position;

    provides no.hal.config.ext.ExtConfigurationProvider with no.hal.sokoban.movements.fx.ext.PositionMovementExtConfigurationProvider;
}
