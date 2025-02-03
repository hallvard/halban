package no.hal.sokoban.fx;

import javafx.beans.property.ReadOnlyProperty;
import no.hal.grid.util.XYTransformer;
import no.hal.sokoban.Moves;
import no.hal.sokoban.SokobanGameState;

public interface SokobanGameController extends SokobanGameState.Provider {
	void performMoves(Moves moves);
	ReadOnlyProperty<XYTransformer> xyTransformerProperty();
	void updateMovement(Movement movement);
}
