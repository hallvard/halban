module sokoban.movements {

    requires grid.api;
    requires grid.fx;
    requires sokoban.game;

    requires com.gluonhq.attach.accelerometer;
    requires com.gluonhq.attach.position;

    provides no.hal.plugin.Plugin with no.hal.sokoban.movements.plugin.MovementPlugin;
}
