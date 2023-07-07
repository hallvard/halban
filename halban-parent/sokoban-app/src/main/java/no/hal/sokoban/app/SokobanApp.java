package no.hal.sokoban.app;

import javafx.application.Application;
import javafx.beans.value.ChangeListener;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.layout.Pane;
import javafx.stage.Stage;
import no.hal.plugin.Context;
import no.hal.plugin.Plugin;
import no.hal.sokoban.fx.SokobanAppController;
import no.hal.plugin.fx.SimpleFxExtensionPoint;
import no.hal.plugin.impl.ContributionContext;
import no.hal.plugin.impl.FxContributionContext;

public class SokobanApp extends Application {
    
    @Override
	public void start(Stage primaryStage) throws Exception {
        Scene scene = new Scene(new Pane(), 800, 800);
        scene.getStylesheets().add(getClass().getResource("app.css").toExternalForm());
        Context context = setupContext(scene);
        new SokobanAppController(new SimpleFxExtensionPoint<>(context, Parent.class, parent -> scene.setRoot(parent)));

        ChangeListener<? super Number> sizeListener = (prop, oldValue, newValue) -> {
            System.out.println("scene size: " + scene.getWidth() + " x " + scene.getHeight());
        };
        scene.widthProperty().addListener(sizeListener);
        scene.heightProperty().addListener(sizeListener);

		primaryStage.setScene(scene);
        primaryStage.show();
	}

	private ContributionContext setupContext(Scene root) {
		FxContributionContext context = new FxContributionContext(root);
		ContributionContext.load(Plugin.class, context);
		return context;
	}
	
    public static void main(String[] args) {
        launch(SokobanApp.class, args);
    }
}
