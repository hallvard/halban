package no.hal.config;

import java.util.Map;
import java.util.NoSuchElementException;
import java.util.function.Function;
import java.util.function.Supplier;

public interface Setting {

    public enum Kind {
        ARRAY, OBJECT, BOOLEAN, STRING, NUMBER, NULL
    }

    public Kind getKind();

    public interface Composite<I, C> extends Setting, Iterable<C> {
        int size();
        default boolean isEmpty() {
            return size() == 0;
        }

        boolean has(I key);
        Setting get(I key);
    }

    public interface Array extends Composite<Integer, Setting> {

        @Override
        default Kind getKind() {
            return Kind.ARRAY;
        }

        @Override
        default boolean has(Integer key) {
            return has(key.intValue());
        }

        default boolean has(int key) {
            return key >= 0 && key < size();
        }

        default Setting get(int key) {
            return get(Integer.valueOf(key));
        }
    }

    public interface Object extends Configuration, Composite<String, Map.Entry<String, Setting>> {

        @Override
        default Kind getKind() {
            return Kind.OBJECT;
        }

        @Override
        default boolean has(String key) {
            return get(key) != null;
        }

        @Override
        default <T extends Setting> boolean has(Class<T> clazz, String path) {
            Setting current = this;
            int start = 0;
            while (start < path.length()) {
                int end = path.indexOf('.', start);
                if (end < 0) {
                    end = path.length();
                }
                if (current instanceof Setting.Object object) {
                    current = object.get(path.substring(start, end));
                } else {
                    return false;
                }
                start = end + 1;
            }
            return clazz.isInstance(current);
        }

        @Override
        default <T extends Setting> T get(Class<T> clazz, String path) {
            Setting current = this;
            int start = 0;
            while (start < path.length()) {
                int end = path.indexOf('.', start);
                if (end < 0) {
                    end = path.length();
                }
                if (current instanceof Setting.Object object) {
                    current = object.get(path.substring(start, end));
                } else {
                    throw new NoSuchElementException("Cannot find field " + path + " of " + this);
                }
                start = end + 1;
            }
            if (! clazz.isInstance(current)) {
                throw new NoSuchElementException("Setting " + path + " is not of class " + clazz.getSimpleName());
            }
            return (T) current;
        }
    }

    public interface Value extends Setting, Supplier<java.lang.Object> {

        @Override
        default Kind getKind() {
            return getValueKind(get());
        }

        default String asString() {
            return String.valueOf(get());
        }

        default int asInt() {
            return Integer.parseInt(asString());
        }

        default boolean asBoolean() {
            return as(s -> {
                if ("true".equalsIgnoreCase(s)) {
                    return true;
                } else if ("false".equalsIgnoreCase(s)) {
                    return false;
                }
                throw new IllegalArgumentException("A boolean must be either true og false (ignoring case)");
            });
        }
        default double asDouble() {
            return Double.parseDouble(asString());
        }

        default <T> T as(Function<String, T> converter) {
            return converter.apply(asString());
        }
    }

    public static Kind getValueKind(java.lang.Object value) {
        if (value == null) {
            return Kind.NULL;
        } else if (value instanceof String) {
            return Kind.STRING;
        } else if (value instanceof Number) {
            return Kind.NUMBER;
        } else if (value instanceof Boolean) {
            return Kind.BOOLEAN;
        }
        throw new IllegalArgumentException("Json value must be null, String, Number or Boolean, but was " + value);
    }
}
