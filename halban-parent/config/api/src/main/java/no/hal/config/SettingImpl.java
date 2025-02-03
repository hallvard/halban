package no.hal.config;

import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

public class SettingImpl {

    public record Array(List<Setting> settings) implements Setting.Array {

        public Array(Setting... settings) {
            this(Arrays.asList(settings));
        }

        @Override
        public int size() {
            return settings.size();
        }

        @Override
        public Setting get(Integer pos) {
            return settings.get(pos);
        }

        @Override
        public Iterator<Setting> iterator() {
            return settings.iterator();
        }
    }

    public record Object(Map<String, Setting> settings) implements Setting.Object {

        @Override
        public int size() {
            return settings.size();
        }

        @Override
        public Setting get(String fieldName) {
            return settings.get(fieldName);
        }

        @Override
        public Iterator<Map.Entry<String, Setting>> iterator() {
            return settings.entrySet().iterator();
        }
    }

    public record Value(java.lang.Object value) implements Setting.Value {

        public Value {
            Setting.getValueKind(value);
        }

        public static Value of(String s) {
            return new SettingImpl.Value(s);
        }
        public static Value of(int i) {
            return new SettingImpl.Value(i);
        }
        public static Value of(double d) {
            return new SettingImpl.Value(d);
        }
        public static Value of(boolean b) {
            return new SettingImpl.Value(b);
        }
        public static Value Null = new SettingImpl.Value(null);

        @Override
        public java.lang.Object get() {
            return value;
        }
    }
}
