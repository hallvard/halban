module sokoban.movements {

    requires config.api;
    requires grid.api;
    requires grid.fx;
    requires sokoban.game;

    requires com.gluonhq.attach.position;
    requires com.gluonhq.attach.compass;

    provides no.hal.config.ext.ExtConfigurationProvider with no.hal.sokoban.movements.fx.ext.PositionMovementExtConfigurationProvider;
}
