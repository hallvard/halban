package no.hal.grid.fx;

import javafx.scene.paint.Paint;
import javafx.scene.shape.Shape;

public abstract class ShapeGridCellFactory<T, S extends Shape> extends GridCellFactory<T, S> {

	private Paint stroke = null;
	private Paint fill = null;

	public void setStroke(Paint stroke) {
		this.stroke = stroke;
	}

	public void setFill(Paint fill) {
		this.fill = fill;
	}

    protected abstract class ShapeCell extends GridCell<T, S> {

		protected abstract S createShape();

		@Override
		protected S createNode() {
			S shape = createShape();
			shape.setStroke(stroke);
			shape.setFill(fill);
			return shape;
		}
    }
}
