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

package ensemble.samples.controls.dnd.clothes;

import javafx.scene.Node;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.*;

/**
 * Class for a draggable detail. Draggable detail has three states: preview,
 * active (dragged), and equipped. Currently, dragged and preview images
 * are the same (see the ClothListBuilder class).
 * 
 */
public class Cloth {
    
    private final Image previewImage;
    private final Image activeImage;
    private final Image equippedImage;
    
    private final ImageView currentImage;
    
    public void putOn() {
        currentImage.setImage(equippedImage);
    }
    
    public void takeOff() {
        currentImage.setImage(previewImage);
    }
    
    private void activate() {
        currentImage.setImage(activeImage);
    }
    
    public String getImageViewId() {
        return currentImage.getId();
    }
    
    public Node getNode() {
        return currentImage;
    }
    
    public Cloth(Image[] images) {
        this.previewImage = images[0];
        this.activeImage = images[1];
        this.equippedImage = images[2];
        
        currentImage = new ImageView();
        currentImage.setImage(previewImage);
        currentImage.setId(this.getClass().getSimpleName() + System.currentTimeMillis());
        
        currentImage.setOnDragDetected((MouseEvent event) -> {
            activate();
            Dragboard db = currentImage.startDragAndDrop(TransferMode.MOVE);
            ClipboardContent content = new ClipboardContent();
            // Store the node ID in order to know what is dragged.
            content.putString(currentImage.getId());
            db.setContent(content);
            event.consume();
        });
    }
}
