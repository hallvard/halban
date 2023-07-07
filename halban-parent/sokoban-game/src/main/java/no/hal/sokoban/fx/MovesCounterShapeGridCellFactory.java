package no.hal.sokoban.fx;

import java.util.function.IntFunction;

import javafx.scene.shape.Circle;
import no.hal.grid.fx.GridView.Cell;
import no.hal.grid.fx.ShapeGridCellFactory;
import no.hal.sokoban.LocationMovesCounters;

public class MovesCounterShapeGridCellFactory<T> extends ShapeGridCellFactory<T, Circle> {

	private final LocationMovesCounters counters;

	private IntFunction<Double> counterRadiusMapper = c -> (double) c;

	public MovesCounterShapeGridCellFactory(LocationMovesCounters counters) {
		this.counters = counters;
	}

	public void setCounterRadiusMapper(IntFunction<Double> counterRadiusMapper) {
		this.counterRadiusMapper = counterRadiusMapper;
	}

	@Override
	protected Cell<T> createGridCell() {
		return new ShapeCell() {
			@Override
			protected Circle createShape() {
				return new Circle();
			}
			@Override
			protected void setGridItem(Circle circle, T item, int x, int y) {
				var sum = counters.getCounter(x, y).sum();
				var radius = counterRadiusMapper.apply(sum);
				circle.setRadius(radius);
			}
			@Override
			protected void setNodeSize(Circle circle, double width, double height) {
				circle.setCenterX(width / 2);
				circle.setCenterY(width / 2);
			}	
		};
	}
}
