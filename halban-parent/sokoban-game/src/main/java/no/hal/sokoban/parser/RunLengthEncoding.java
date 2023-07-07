package no.hal.sokoban.parser;

public class RunLengthEncoding {
    
    public static CharSequence decode(String s) {
        StringBuffer buffer = new StringBuffer(s.length());
        int pos = 0;
        while ((pos = decode(1, s, pos, -1, buffer)) < s.length());
        return buffer;
    }

    private static int decode(int count, String s, int start, int length, StringBuffer buffer) {
        int pos = start, bufStart = buffer.length();
        while (pos < s.length()) {
            var c = s.charAt(pos);
            if (Character.isDigit(c)) {
                pos = decode(c - '0', s, pos + 1, 1, buffer);
            } else if (c == '(') {
                pos = decode(1, s, pos + 1, -1, buffer);
            } else if (c == ')') {
                break;
            } else {
                buffer.append(c);
                if (pos + 1 - start >= length) {
                    break;
                }
                pos++;
            }
        }
        if (count != 1) {
            int bufEnd = buffer.length();
            for (int i = 1; i < count; i++) {
                buffer.append(buffer, bufStart, bufEnd);
            }
            buffer.setLength(bufStart + count * (bufEnd - bufStart));
        }
        return pos + 1;
    }

    public static void main(String[] args) {
        System.out.println(decode("Hallvard"));
        System.out.println(decode("Ha2lvard"));
        System.out.println(decode("Ha2lvar1d"));
        System.out.println(decode("Ha2lvar1d0!"));
        System.out.println(decode("Ha2(l)var1d0!"));
        System.out.println(decode("Ha1(2(l)v)ar1d0!"));
    }
}
