package no.hal.grid.util;

import no.hal.grid.impl.GridImpl;

class CharGrid extends GridImpl<Character> {

    public CharGrid(int width, int height) {
        super(width, height);
    }

    public void setCell(int x, int y, char c) {
        super.setCell(x, y, c);
    }

    public void fill(CharSequence chars, int x, int y, int w) {
        int num = 0;
        for (int i = 0; i < chars.length(); i++) {
            char c = chars.charAt(i);
            if (! Character.isWhitespace(c)) {
                setCell(x + num % w, y + num / w, c);
                num++;
            }
        }
        fireGridContentsChanged(x, y, w, num / w);
    }

    public void fill(CharSequence chars) {
        fill(chars, 0, 0, getWidth());
    }
}
