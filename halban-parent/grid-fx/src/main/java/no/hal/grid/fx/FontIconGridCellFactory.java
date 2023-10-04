package no.hal.grid.fx;

import java.util.function.Function;

import org.kordamp.ikonli.javafx.FontIcon;

import no.hal.grid.fx.GridView.Cell;

public class FontIconGridCellFactory<T> extends GridCellFactory<T, FontIcon> {

	private Function<T, String> fontIconProvider;

	public FontIconGridCellFactory(Function<T, String> fontIconProvider) {
		this.fontIconProvider = fontIconProvider;
	}

	@Override
	protected Cell<T> createGridCell() {
		return new FontIconCell();
	}

    private class FontIconCell extends GridCell<T, FontIcon> {

		@Override
		protected FontIcon createNode() {
			return new FontIcon();
		}

		@Override
		protected void setGridItem(FontIcon fontIcon, T item, int x, int y) {
			var fontIconLiteral = fontIconProvider.apply(item);
			if (fontIconLiteral != null) {
				fontIcon.setIconLiteral(fontIconLiteral);
			}
		}

		@Override
		protected void setNodeSize(FontIcon fontIcon, double w, double h) {
			getNode().setIconSize((int) Math.max(w, h));
		}
    }
}
