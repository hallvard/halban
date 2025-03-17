package no.hal.sokoban.fx;

import javafx.beans.property.ReadOnlyProperty;
import no.hal.grid.util.XYTransformer;
import no.hal.sokoban.SokobanGame;

public interface SokobanGameController extends SokobanGame.Provider {
	// void performMoves(Moves moves);
	ReadOnlyProperty<XYTransformer> xyTransformerProperty();
	void updateMovement(Movement movement);
}
