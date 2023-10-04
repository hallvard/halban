package no.hal.plugin.fx;

import javafx.scene.image.Image;

@FunctionalInterface
public interface LabelProvider {
    
    String getText();
    
    default Image getImage() {
        return null;
    }
}
