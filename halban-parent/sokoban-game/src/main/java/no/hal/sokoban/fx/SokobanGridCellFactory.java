package no.hal.sokoban.fx;

import no.hal.settings.Settings;
import no.hal.sokoban.SokobanGrid.CellKind;

class SokobanGridCellFactory extends

	no.hal.grid.fx.ImageGridCellFactory<CellKind> {

    public SokobanGridCellFactory(Settings settings) {
        super(cellKind -> SokobanGridCellFactory.class.getResource("/no/hal/sokoban/fx/images/default/%s.png".formatted(
				switch (cellKind) {
					case WALL 			-> settings.getString("sokoban.grid.wall", "wall16x16");
					case EMPTY 			-> settings.getString("sokoban.grid.empty", "empty16x16");
					case TARGET 		-> settings.getString("sokoban.grid.target", "target16x16");
					case EMPTY_PLAYER 	-> settings.getString("sokoban.grid.player", "player16x16");
					case TARGET_PLAYER 	-> settings.getString("sokoban.grid.playerOnTarget", "player_on_target16x16");
					case EMPTY_BOX 		-> settings.getString("sokoban.grid.box", "box16x16");
					case TARGET_BOX 	-> settings.getString("sokoban.grid.boxOnTarget", "box_on_target16x16");
				})).toExternalForm());
			}
/* 
	no.hal.grid.fx.FontIconGridCellFactory<CellKind> {

	public SokobanGridCellFactory(Settings settings) {
        super(cellKind -> switch (cellKind) {
					case WALL 			-> settings.getString("sokoban.grid.wall", "mdi2v-view-grid-outline:20:gray");
					case EMPTY 			-> settings.getString("sokoban.grid.empty", null);
					case TARGET 		-> settings.getString("sokoban.grid.target", "mdi2b-border-all-variant:20:orange");
					case EMPTY_PLAYER 	-> settings.getString("sokoban.grid.player", "mdi2f-face:20:blue");
					case TARGET_PLAYER 	-> settings.getString("sokoban.grid.playerOnTarget", "mdi2f-face-recognition:20:blue");
					case EMPTY_BOX 		-> settings.getString("sokoban.grid.box", "mdi2c-close:20:orange");
					case TARGET_BOX 	-> settings.getString("sokoban.grid.boxOnTarget", "mdi2c-close-box-outline:20:green");
				});
			}
*/
}
