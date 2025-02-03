package no.hal.fx;

import java.util.Collection;
import javafx.scene.image.Image;

public class CompositeLabelAdapter extends CompositeAdapter<LabelAdapter> implements LabelAdapter {

    private CompositeLabelAdapter(Collection<LabelAdapter> labelAdapters) {
        super(labelAdapters);
    }
    private CompositeLabelAdapter(LabelAdapter... labelAdapters) {
        super(labelAdapters);
    }
    
    public static CompositeLabelAdapter of(Collection<LabelAdapter> labelAdapters) {
        return new CompositeLabelAdapter(labelAdapters);
    }
    public static CompositeLabelAdapter of(LabelAdapter... labelAdapters) {
        return new CompositeLabelAdapter(labelAdapters);
    }

    @Override
    public String getText(Object o) {
        return getFirst(o, adapter -> adapter.getText(o));
    }

    @Override
    public Image getImage(Object o) {
        for (var labelAdapter : adapters) {
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
