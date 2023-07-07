package no.hal.grid.fx;

import java.util.function.Function;

import org.kordamp.ikonli.javafx.FontIcon;

import no.hal.grid.fx.GridView.Cell;

public class IkonliFontIconGridCellFactory<T> extends GridCellFactory<T, FontIcon> {

	private Function<T, String> fontIconProvider;

	public IkonliFontIconGridCellFactory(Function<T, String> fontIconProvider) {
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
			fontIcon.setIconLiteral(fontIconLiteral);
		}
    }
}
