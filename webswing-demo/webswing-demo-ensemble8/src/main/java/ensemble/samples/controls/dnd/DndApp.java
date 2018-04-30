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

package ensemble.samples.controls.dnd;

import ensemble.samples.controls.dnd.body.Body;
import ensemble.samples.controls.dnd.clothes.Cloth;
import ensemble.samples.controls.dnd.clothes.ClothListBuilder;
import ensemble.samples.controls.dnd.images.ImageManager;
import javafx.application.Application;
import javafx.geometry.Insets;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.image.ImageView;
import javafx.scene.input.DragEvent;
import javafx.scene.input.Dragboard;
import javafx.scene.input.TransferMode;
import javafx.scene.layout.*;
import javafx.stage.Stage;

import java.util.HashMap;
import java.util.List;

/**
 * The main application class. Most of presentation logic is here.
 * Also, a container for unequipped details is created and set up here.
 * 
 * This application uses the Public Domain Images 
 * from http://karenswhimsy.com/public-domain-images/
 *
 */

public class DndApp extends Application {

    public static void main(String[] args) {
        launch(args);
    }

    /**
     * All laying out goes here.
     * @param primaryStage 
     */
    
    @Override
    public void start(Stage primaryStage) {
        primaryStage.setTitle("Paper Doll");
        
        primaryStage.setScene(new Scene(createContent()));
        primaryStage.show();
    }

    public Parent createContent(){
        ImageView header = new ImageView(ImageManager.getImage("ui/flowers.jpg"));
        VBox title = new VBox();
        title.getChildren().addAll(header);
        title.setPadding(new Insets(10.0));

        GridPane content = new GridPane();
        content.add(Body.getBody().getNode(), 1, 1);
        content.add(createItemPane(Body.getBody().getBodyPane()), 0, 1);
        ColumnConstraints c1 = new ColumnConstraints();
        c1.setHgrow(Priority.ALWAYS);
        ColumnConstraints c2 = new ColumnConstraints();
        c2.setHgrow(Priority.NEVER);
        c2.setPrefWidth(Body.getBody().getBodyPane().getMinWidth() + 20);
        content.getColumnConstraints().addAll(c1, c2);

        items = new HashMap<>();
        Body.getBody().setItemsInfo(itemPane, items);
        populateClothes();

        VBox root = new VBox();
        root.getChildren().addAll(title, content);
        return root;
    }

    private FlowPane itemPane = null;
    
    private HashMap<String, Cloth> items;
    
    /**
     * A container for unequipped items is created here.
     * @param bodyPane body container is needed so that the item is removed from
     * it when dropped here.
     * @return 
     */
    private FlowPane createItemPane(final Pane bodyPane) {
        if (!(itemPane == null))
            return itemPane;
        
        itemPane = new FlowPane();
        itemPane.setPadding(new Insets(10.0));
        
        itemPane.setOnDragDropped((DragEvent event) -> {
            Dragboard db = event.getDragboard();
            //Get an item ID here, which was stored when the drag started.
            boolean success = false;
            // If this is a meaningful drop...
            if (db.hasString()) {
                String nodeId = db.getString();
                // ...search for the item on body. If it is there...
                ImageView cloth = (ImageView) bodyPane.lookup("#" + nodeId);
                if (cloth != null) {
                    // ... it is removed from body
                    // and added to an unequipped container.
                    bodyPane.getChildren().remove(cloth);
                    itemPane.getChildren().add(cloth);
                    success = true;
                }
                // ...anyway, the item is not active or equipped anymore.
                items.get(nodeId).takeOff();
            }
            event.setDropCompleted(success);
            event.consume();
        });
        
        itemPane.setOnDragOver((DragEvent event) -> {
            if (event.getGestureSource() != itemPane &&
                    event.getDragboard().hasString()) {
                event.acceptTransferModes(TransferMode.MOVE);
            }
            event.consume();
        });        
        
        return itemPane;
    }
    
    /**
     * Here items are added to unequipped items container.
     */
    private void populateClothes() {
        ClothListBuilder clothBuilder = new ClothListBuilder();
        if (itemPane == null)
            throw new IllegalStateException("Should call getItems() before populating!");
        List<Cloth> clothes = clothBuilder.getClothList();
        clothes.stream().map((c) -> {
            itemPane.getChildren().add(c.getNode());
            return c;
        }).forEach((c) -> {
            items.put(c.getImageViewId(), c);
        });
    }
}