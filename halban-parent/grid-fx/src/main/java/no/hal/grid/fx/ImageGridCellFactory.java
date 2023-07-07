package no.hal.grid.fx;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import no.hal.grid.fx.GridView.Cell;

public class ImageGridCellFactory<T> extends GridCellFactory<T, ImageView> {

	private Function<T, String> imageProvider;

	public ImageGridCellFactory(Function<T, String> imageProvider) {
		this.imageProvider = imageProvider;
	}

	@Override
	protected Cell<T> createGridCell() {
		return new ImageCell();
	}

    private class ImageCell extends GridCell<T, ImageView> {

		@Override
		protected ImageView createNode() {
			return new ImageView();
		}

		@Override
		protected void setNodeSize(ImageView imageView, double w, double dw, double sw, double h, double dh, double sh) {
			imageView.setFitWidth(w);
			imageView.setFitHeight(h);
		}

		@Override
		protected void setGridItem(ImageView imageView, T item, int x, int y) {
			var image = getImage(item);
			imageView.setImage(image);
		}
    }

	private Map<T, Image> images = new HashMap<>();

	private Image getImage(T key) {
		var image = images.get(key);
		if (image == null) {
			image = new Image(imageProvider.apply(key));
			images.put(key, image);
		}
		return image;
	}
}
