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

import ensemble.samples.controls.dnd.images.ImageManager;
import javafx.scene.image.Image;

import java.util.ArrayList;
import java.util.List;

/**
 * Helper class that generates list of draggable details (the names of the 
 * details are hard coded here). 
 * Preview, dragged and equipped images for every detail
 * are set here. Currently, dragged and preview images are the same.
 * 
 * Images are taken from: http://karenswhimsy.com/public-domain-images/
 * 
 */
public class ClothListBuilder {
    
    private List<Cloth> clothList;
    
    public List<Cloth> getClothList() {
        if (clothList == null) {
            buildClothList();
        }
        return clothList;
    }
    
    private Image[] getClothImages(String clothName) {
        Image []clothImages = new Image[3];
        clothImages[0] = ImageManager.getImage("clothes/preview/" + clothName);
        clothImages[1] = ImageManager.getImage("clothes/preview/" + clothName);
        clothImages[2] = ImageManager.getImage("clothes/equipped/" + clothName);
        return clothImages;
    }
    
    private void buildClothList() {
        clothList = new ArrayList<>();
        for (String clothName : clothNames) {
            Cloth c = new Cloth(getClothImages(clothName));
            clothList.add(c);
        }
    }
    
    private final String []clothNames = {
        "dress1.png", "dress2.png", "dress3.png", "dress4.png"
    };
}
