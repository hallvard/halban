package no.hal.sokoban.movements.fx;

public class Averager {

    private int count;
    private int pos;
    private final double values[];
    private double sum;

    public Averager(int size) {
        this.count = 0;
        this.pos = 0;
        values = new double[size];
        sum = 0.0;
    }

    private void add(double value) {
        if (count < values.length) {
            count++;
        } else {
            sum -= values[pos];
        }
        values[pos] = value;
        sum += value;
        pos = (pos + 1) % values.length;
    }

    public double average(double value) {
        add(value);
        return sum / count;
    }
} 