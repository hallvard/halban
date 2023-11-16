package no.hal.plugin.fx.xp;

import java.util.function.Function;

import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import no.hal.plugin.InstanceRegistry;
import no.hal.plugin.fx.ContentProvider;
import no.hal.plugin.fx.LabelProvider;

public interface LabeledChildExtender extends LabelProvider, ContentProvider.Child {

    public static SimpleFxExtensionPoint createTabPaneExtensionPoint(TabPane tabPane, InstanceRegistry instanceRegistry) {
        return new SimpleFxExtensionPoint<>(instanceRegistry, LabeledChildExtender.class, LabeledChildExtender.createTabPaneExtender(tabPane));
    }

    public static Function<LabeledChildExtender, Runnable> createTabPaneExtender(TabPane tabPane) {
        return extender -> {
			var newTab = new Tab(extender.getText(), extender.getContent());
			newTab.setClosable(false);
			tabPane.getTabs().add(newTab);
			tabPane.getSelectionModel().select(newTab);
			return () -> tabPane.getTabs().remove(newTab);
		};
    }
}
