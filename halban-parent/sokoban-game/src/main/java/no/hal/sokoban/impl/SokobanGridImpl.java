package no.hal.sokoban.impl;

import no.hal.grid.impl.GridImpl;
import no.hal.sokoban.SokobanGrid;
import no.hal.sokoban.parser.SokobanSerializer;

public class SokobanGridImpl extends GridImpl<SokobanGrid.CellKind> implements SokobanGrid {
	
	private Location playerLocation;

	public SokobanGridImpl() {
		super(0, 0);
	}

	public SokobanGridImpl(SokobanGrid sokobanGrid) {
		super(sokobanGrid);
	}

	public SokobanGridImpl(CellKind[][] lines) {
		this();
		init(lines);
	}

	public void init(CellKind[][] lines) {
		// first we compute the width, as the maximum length of the lines
		int newWidth = 0;
		for (int y = 0; y < lines.length; y++) {
			newWidth = Math.max(newWidth, lines[y].length);
		}
		resize(newWidth, lines.length);
		int playerX = -1, playerY = -1;
		// fill the array with the characters in the lines
		// note that lines may be shorter than the width, so some cells may not be set
		for (int y = 0; y < lines.length; y++) {
			CellKind[] line = lines[y];
			for (int x = 0; x < newWidth; x++) {
				CellKind cellKind = (x < line.length ? line[x] : CellKind.EMPTY);
				if (cellKind.content() == ContentKind.PLAYER) {
					if (playerX >= 0 && playerY >= 0) {
						throw new IllegalArgumentException("Cannot have more than one player");
					}
					playerX = x;
					playerY = y;
				}
				setCell(x, y, cellKind);
			}
		}
		if (playerX < 0 && playerY < 0) {
			throw new IllegalArgumentException("Must have a player");
		}
		this.playerLocation = new Location(playerX, playerY);
		fireGridDimensionsChanged(getWidth(), getHeight());
		fireGridContentsChanged(0, 0, getWidth(), getHeight());
	}

	@Override
	public Location getPlayerLocation() {
		return playerLocation;
	}

	private final SokobanSerializer sokobanSerializer = new SokobanSerializer();

	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder();
		sokobanSerializer.toString(this, null, builder);
		return builder.toString();
	}

	@Override
	public int[] countTargets() {
		int[] counters = new int[]{0, 0};
		forEachCell((cell, _, _) -> {
			if (cell.floor() == FloorKind.TARGET) {
				counters[cell.content() == ContentKind.BOX ? 0 : 1]++;
			}
		});
		return counters;
	}

	@Override
	protected void setCell(int x, int y, CellKind cellKind) {
		super.setCell(x, y, cellKind);
		if (cellKind.content() == ContentKind.PLAYER) {
			playerLocation = new Location(x, y);
		}
	}

	void setContents(int x, int y, int w, int h, ContentKind... contents) {
		if (contents.length != Math.abs(w * h)) {
			throw new IllegalArgumentException("Wrong number of content values, should be " + (w * h) + ", but was " + contents.length);
		}
		int pos = 0;
		for (int dx = 0; dx != w; dx += Math.signum(w)) {
			for (int dy = 0; dy != h; dy += Math.signum(h)) {
				ContentKind content = contents[pos];
				setCell(x + dx, y + dy, CellKind.valueOf(getCell(x + dx, y + dy).floor(), content));
				pos++;
			}
		}
		if (w < 0) {
			x = x + w + 1;
			w = -w;
		}
		if (h < 0) {
			y = y + h + 1;
			h = -h;
		}
		fireGridContentsChanged(x, y, w, h);
	}
}
