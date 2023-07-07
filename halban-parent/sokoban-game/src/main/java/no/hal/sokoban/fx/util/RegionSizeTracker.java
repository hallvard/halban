package no.hal.sokoban.fx.util;

import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.scene.layout.Region;

public class RegionSizeTracker implements ChangeListener<Number>{

    public static void trackSize(Region region, String name) {
        new RegionSizeTracker(region, name);
    }

    private final Region region;
    private final String name;

    private RegionSizeTracker(Region region, String name) {
        this.region = region;
        this.name = name;
        region.widthProperty().addListener(this);
        region.heightProperty().addListener(this);
    }

    @Override
    public void changed(ObservableValue<? extends Number> observable, Number oldValue, Number newValue) {
        // System.out.println(region.getClass().getSimpleName() + (name != null ? " " + name : "") + " size changed: " + region.getWidth() + " x " + region.getHeight());
    }    
}
