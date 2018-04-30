/*
 * Copyright (c) 2013, 2014, Oracle and/or its affiliates. All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions
 * are met:
 *
 *   - Redistributions of source code must retain the above copyright
 *     notice, this list of conditions and the following disclaimer.
 *
 *   - Redistributions in binary form must reproduce the above copyright
 *     notice, this list of conditions and the following disclaimer in the
 *     documentation and/or other materials provided with the distribution.
 *
 *   - Neither the name of Oracle or the names of its
 *     contributors may be used to endorse or promote products derived
 *     from this software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS
 * IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO,
 * THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR
 * PURPOSE ARE DISCLAIMED.  IN NO EVENT SHALL THE COPYRIGHT OWNER OR
 * CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL,
 * EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO,
 * PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR
 * PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF
 * LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING
 * NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */

package ensemble.samples.controls.dnd.body;

import ensemble.samples.controls.dnd.clothes.Cloth;
import ensemble.samples.controls.dnd.images.ImageManager;
import javafx.geometry.Insets;
import javafx.geometry.Orientation;
import javafx.scene.Node;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.image.ImageView;
import javafx.scene.input.DragEvent;
import javafx.scene.input.Dragboard;
import javafx.scene.input.TransferMode;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Pane;
import javafx.stage.DirectoryChooser;
import javafx.stage.FileChooser;
import sun.nio.ch.IOUtil;

import javax.swing.filechooser.FileNameExtensionFilter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.util.Date;
import java.util.List;
import java.util.Map;

/**
 * Container for body that accepts drops. Draggable details dropped here
 * are equipped.
 *
 */

public class BodyElement {
	private final FlowPane bodyPane;
	private final ImageView bodyImage;
	private final Button saveButton;
	private final Button loadButton;
	private Pane itemPane;
	private Map<String, Cloth> items;

	public void setItemsInfo(FlowPane p, Map<String, Cloth> m) {
		itemPane = p;
		items = m;
	}

	public Pane getBodyPane() {
		return bodyPane;
	}

	public BodyElement() {
		bodyPane = new FlowPane(Orientation.VERTICAL);
		bodyImage = new ImageView(ImageManager.getResource("body.png"));
		saveButton = new Button("Save");
		saveButton.setOnAction(event -> {
			FileChooser fc = new FileChooser();
			fc.setInitialFileName("body.txt");
			File file = fc.showSaveDialog(null);
			try {
				PrintWriter out = new PrintWriter(file);
				out.write("body test file :" + new Date());
			} catch (FileNotFoundException e) {
				e.printStackTrace();
			}
		});
		loadButton = new Button("Load");
		loadButton.setOnAction(event -> {
			FileChooser fc = new FileChooser();
			fc.getExtensionFilters().addAll(new FileChooser.ExtensionFilter("png Files", "*.png"));
			List<File> file = fc.showOpenMultipleDialog(null);
			Alert alert = new Alert(Alert.AlertType.INFORMATION);
			alert.setTitle("Selected Files");
			alert.setContentText("" + file);
			alert.showAndWait();
		});
		Button folderButton = new Button("Select folder");
		folderButton.setOnAction(event -> {
			DirectoryChooser directoryChooser = new DirectoryChooser();
			final File selectedDirectory = directoryChooser.showDialog(null);
			String path = selectedDirectory.getAbsolutePath();
			Alert alert = new Alert(Alert.AlertType.INFORMATION);
			alert.setTitle("Selected Folder");
			alert.setContentText("" + path);
			alert.showAndWait();
		});
		bodyPane.setOnDragDropped((DragEvent event) -> {
			Dragboard db = event.getDragboard();
			boolean success = false;
			// If this is a meaningful drop...
			if (db.hasString()) {
				// Get an item ID here, which was stored when the drag started.
				String nodeId = db.getString();
				// ...search for the item in unequipped items. If it is there...
				ImageView cloth = (ImageView) itemPane.lookup("#" + nodeId);
				if (cloth != null) {
					// ... the item is removed from the unequipped list
					// and attached to body.
					itemPane.getChildren().remove(cloth);
					bodyPane.getChildren().add(cloth);
					cloth.relocate(0, 0);
					success = true;
				}
				// ...anyway, the item is now equipped.
				items.get(nodeId).putOn();
			}
			event.setDropCompleted(success);
			event.consume();
		});

		bodyPane.setOnDragOver((DragEvent event) -> {
			if (event.getGestureSource() != bodyImage && event.getDragboard().hasString()) {
				event.acceptTransferModes(TransferMode.MOVE);
			}
			event.consume();
		});

		bodyPane.getChildren().add(bodyImage);
		bodyPane.getChildren().add(saveButton);
		bodyPane.getChildren().add(loadButton);
		bodyPane.getChildren().add(folderButton);
		bodyPane.setMinWidth(bodyImage.getImage().getWidth());
		bodyPane.setPadding(new Insets(10.0));
	}

	public Node getNode() {
		return bodyPane;
	}

}
