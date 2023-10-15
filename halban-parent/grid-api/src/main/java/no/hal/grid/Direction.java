package no.hal.grid;

public enum Direction {

	LEFT(-1, 0),
	RIGHT(1, 0),
	UP(0, -1),
	DOWN(0, 1);

	public final int dx, dy;

	private Direction(int dx, int dy) {
		this.dx = dx;
		this.dy = dy;
	}
	
	@Override
	public String toString() {
		return toChar() + ":" + dx + "," + dy;
	}
	
	public Direction opposite() {
		return switch (this) {
			case LEFT -> RIGHT;
			case RIGHT -> LEFT;
			case UP -> DOWN;
			case DOWN -> UP;
		};
	}
	
	public char toChar() {
		return Character.toLowerCase(name().charAt(0));
	}

	public static char toChar(int dx, int dy) {
		return valueOf(dx, dy).toChar();
	}
	
	public static Direction valueOf(int dx, int dy) {
		for (Direction direction : values()) {
			if (dx == direction.dx && dy == direction.dy) {
				return direction;
			}
		}
		throw new IllegalArgumentException(dx + "," + dy + " is an illegal direction vector");
	}

	public static Direction valueOf(char c) {
		for (Direction direction : values()) {
			if (Character.toLowerCase(c) == direction.toChar()) {
				return direction;
			}
		}
		throw new IllegalArgumentException(c + " is an illegal direction character");
	}
}
