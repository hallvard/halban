package no.hal.sokoban.fx;

import java.util.List;
import no.hal.fx.ContentProvider;
import no.hal.grid.fx.GridCellFactory;

public interface SokobanGameSubController extends ContentProvider.Child{
  
  List<GridCellFactory> getGridCellFactories();

  public interface Provider {
    SokobanGameSubController createSokobanGameSubController(SokobanGameController sokobanGameController);
  }
}
