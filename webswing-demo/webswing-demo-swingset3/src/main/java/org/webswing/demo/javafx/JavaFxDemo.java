package org.webswing.demo.javafx;

import com.sun.swingset3.DemoProperties;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.embed.swing.JFXPanel;
import javafx.scene.Group;
import javafx.scene.Scene;
import javafx.scene.control.ComboBox;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.Text;

import javax.swing.JPanel;

@DemoProperties(value = "JavaFx", category = "Webswing", description = "Demonstrates embeding JavaFx components", sourceFiles = { "org/webswing/demo/javafx/JavaFxDemo.java" })
public class JavaFxDemo extends JPanel {


	public JavaFxDemo() {
		final JFXPanel fxPanel = new JFXPanel();
		add(fxPanel);
		initFX(fxPanel);
		Platform.setImplicitExit(false);
	}
	private static void initFX(JFXPanel fxPanel) {
		// This method is invoked on the JavaFX thread
		Scene scene = createScene();
		fxPanel.setScene(scene);
	}

	private static Scene createScene() {
		Group  root  =  new Group();
		Scene  scene  =  new  Scene(root, Color.ALICEBLUE);
		Text  text  =  new Text();
		text.setX(40);
		text.setY(100);
		text.setFont(new Font(25));
		text.setText("Welcome JavaFX!");
		root.getChildren().add(text);


		ObservableList<String> options =
				FXCollections.observableArrayList(
						"Option 1",
						"Option 2",
						"Option 3"
				);
		final ComboBox comboBox = new ComboBox(options);
		root.getChildren().add(comboBox);



		return (scene);
	}


}
