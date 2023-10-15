package no.hal.sokoban.fx.util;

import java.util.function.BiConsumer;
import java.util.function.BiFunction;
import java.util.function.Consumer;

import javafx.event.EventHandler;
import javafx.scene.input.MouseEvent;
import no.hal.grid.Direction;
import no.hal.sokoban.SokobanGrid;

public class GridItemDragController {

	private final BiFunction<Double, Double, SokobanGrid.Location> locationProvider;
	private final BiConsumer<SokobanGrid.Location, Direction> dragConsumer;
	private final Consumer<SokobanGrid.Location> clickConsumer;

	public GridItemDragController(
		BiFunction<Double, Double, SokobanGrid.Location> locationProvider,
		BiConsumer<SokobanGrid.Location, Direction> dragConsumer,
		Consumer<SokobanGrid.Location> clickConsumer
	) {
		this.locationProvider = locationProvider;
		this.dragConsumer = dragConsumer;
		this.clickConsumer = clickConsumer;
	}

	private SokobanGrid.Location pressedLocation;
	private SokobanGrid.Location lastLocation;

	public void mousePressed(double x, double y) {
		var location = locationProvider.apply(x, y);
		if (location != null) {
			mousePressed(location);
		}
	}

	protected void mousePressed(SokobanGrid.Location location) {
		lastLocation = pressedLocation = location;
	}

	public void mouseDragged(double x, double y) {
		var location = locationProvider.apply(x, y);
		if (location != null) {
			mouseDragged(location);
		}
	}

	protected void mouseDragged(SokobanGrid.Location location) {
		if (lastLocation != null && location != null && (! location.equals(lastLocation))) {
		int dx = (int) Math.signum(location.x() - lastLocation.x());
			int dy = (int) Math.signum(location.y() - lastLocation.y());
			// one and only one can be non-zero
			if (dx * dy == 0 && dx + dy != 0) {
				var direction = Direction.valueOf(dx, dy);
				if (dragConsumer != null) {
					dragConsumer.accept(pressedLocation, direction);
				}
			}
			lastLocation = location;
		}
	}

	public void mouseReleased(double x, double y) {
		var location = locationProvider.apply(x, y);
		if (location != null) {
			mouseDragged(location);
		}
	}

	protected void mouseReleased(SokobanGrid.Location location) {
		if (pressedLocation != null && location != null && location.equals(pressedLocation) && clickConsumer != null) {
			clickConsumer.accept(pressedLocation);
		}
		pressedLocation = null;
		lastLocation = null;
	}

	public static class MouseEventHandler extends GridItemDragController implements EventHandler<MouseEvent> {
		public MouseEventHandler(
			BiFunction<Double, Double, SokobanGrid.Location> locationProvider,
			BiConsumer<SokobanGrid.Location, Direction> dragConsumer,
			Consumer<SokobanGrid.Location> clickConsumer
		) { super(locationProvider, dragConsumer, clickConsumer); }

		@Override
		public void handle(MouseEvent event) {
			double x = event.getX(), y = event.getY();
			if (event.isConsumed());
			else if (event.getEventType() == MouseEvent.MOUSE_PRESSED) {
				mousePressed(x, y);
			} else if (event.getEventType() == MouseEvent.MOUSE_DRAGGED) {
				mouseDragged(x, y);
			} else if (event.getEventType() == MouseEvent.MOUSE_RELEASED) {
				mouseReleased(x, y);
			}
		}
	}
}
