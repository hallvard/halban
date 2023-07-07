package no.hal.plugin.fx;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

import javafx.scene.image.Image;
import no.hal.plugin.Context;

public class CompositeLabelAdapter implements LabelAdapter {

    private List<LabelAdapter> labelAdapters;

    private CompositeLabelAdapter(Collection<LabelAdapter> labelAdapters) {
        setLabelAdapters(labelAdapters);
    }
    private CompositeLabelAdapter(LabelAdapter... labelAdapters) {
        this(Arrays.asList(labelAdapters));
    }
    public void setLabelAdapters(Collection<LabelAdapter> labelAdapters) {
        this.labelAdapters = new ArrayList<>(labelAdapters);
    }
    
    public static CompositeLabelAdapter of(Collection<LabelAdapter> labelAdapters) {
        return new CompositeLabelAdapter(labelAdapters);
    }
    public static CompositeLabelAdapter of(LabelAdapter... labelAdapters) {
        return new CompositeLabelAdapter(labelAdapters);
    }
    public static CompositeLabelAdapter fromContext(Context context) {
        var composite = new CompositeLabelAdapter();
        context.updateAllComponents(LabelAdapter.class, composite::setLabelAdapters);
        return composite;
    }

    @Override
    public String getText(Object o) {
        for (var labelAdapter : labelAdapters) {
            if (labelAdapter.isFor(o)) {
                var text = labelAdapter.getText(o);
                if (text != null) {
                    return text;
                }
            }
        }
        return null;
    }

    @Override
    public Image getImage(Object o) {
        for (var labelAdapter : labelAdapters) {
            if (labelAdapter.isFor(o)) {
                var image = labelAdapter.getImage(o);
                if (image != null) {
                    return image;
                }
            }
        }
        return null;
    }
}
