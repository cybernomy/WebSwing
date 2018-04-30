(function (root, factory) {
    if (typeof define === "function" && define.amd) {
        // AMD
        define(['ProtoBuf', 'text!directdraw.proto'], factory);
    } else {
        root.WebswingDirectDraw = factory(dcodeIO.ProtoBuf);
    }
}(this, function (ProtoBuf, webswingProto) {
    return function (c) {
        "use strict";
        c = c || {};
        var config = {
            logDebug: c.logDebug || false,
            onErrorMessage: c.onErrorMessage || function (message) {
                console.log(message.stack);
            }
        };
        var ctxId = Math.floor(Math.random() * 0x10000).toString(16);
        var proto = webswingProto != null ? ProtoBuf.loadProto(webswingProto, "directdraw.proto") : ProtoBuf.loadProtoFile("/directdraw.proto");
        var WebImageProto = proto.build("org.webswing.directdraw.proto.WebImageProto");
        var InstructionProto = proto.build("org.webswing.directdraw.proto.DrawInstructionProto.InstructionProto");
        var SegmentTypeProto = proto.build("org.webswing.directdraw.proto.PathProto.SegmentTypeProto");
        var ArcTypeProto = proto.build("org.webswing.directdraw.proto.ArcProto.ArcTypeProto");
        var CyclicMethodProto = proto.build("org.webswing.directdraw.proto.CyclicMethodProto");
        var StrokeJoinProto = proto.build("org.webswing.directdraw.proto.StrokeProto.StrokeJoinProto");
        var StrokeCapProto = proto.build("org.webswing.directdraw.proto.StrokeProto.StrokeCapProto");
        var StyleProto = proto.build("org.webswing.directdraw.proto.FontProto.StyleProto");
        var CompositeTypeProto = proto.build("org.webswing.directdraw.proto.CompositeProto.CompositeTypeProto");
        var constantPoolCache = c.constantPoolCache || {};
        var fontsArray = [];
        var canvasBuffer = [];
        var xorLayer;


        function draw64(data, targetCanvas) {
            return drawWebImage(WebImageProto.decode64(data), targetCanvas);
        }

        function drawBin(data, targetCanvas) {
            return drawWebImage(WebImageProto.decode(data), targetCanvas);
        }

        function drawProto(data, targetCanvas) {
            return drawWebImage(data, targetCanvas);
        }

        function drawWebImage(image, targetCanvas) {
            return new Promise(function (resolve, reject) {
                try {
                    drawWebImageInternal(image, targetCanvas, resolve, reject);
                } catch (e) {
                    config.onErrorMessage(e);
                    reject(e);
                }
            });
        }

        function drawWebImageInternal(image, targetCanvas, resolve, reject) {
            var newCanvas;
            var renderStart = new Date().getTime();
            if (targetCanvas != null) {
                newCanvas = targetCanvas;
            } else {
                newCanvas = document.createElement("canvas");
            }
            if (newCanvas.width != image.width || newCanvas.height != image.height) {
                newCanvas.width = image.width;
                newCanvas.height = image.height;
            }

            var imageContext = {
                canvas: newCanvas,
                graphicsStates: {},
                currentStateId: null
            };

            var images = populateConstantsPool(image.constants);
            prepareImages(images)
                .then(function () {
                    return initializeFontFaces(image.fontFaces)
                })
                .then(function () {
                    var ctx = imageContext.canvas.getContext("2d");
                    if (image.instructions != null) {
                        ctx.save();
                        image.instructions.reduce(function (seq, instruction) {
                            return seq.then(function (resolved) {
                                return interpretInstruction(ctx, instruction, imageContext);
                            });
                        }, Promise.resolve()).then(function () {
                            ctx.restore();
                            logRenderTime(renderStart, image);
                            resolve(imageContext.canvas);
                        }, function (error) {
                            ctx.restore();
                            reject(error);
                            config.onErrorMessage(error);
                        });
                    }
                }, function (error) {
                    config.onErrorMessage(error);
                });
        }

        function populateConstantsPool(constants) {
            var images = [];
            constants.forEach(function (constant) {
                constantPoolCache[constant.id] = constant;
                if (constant.image != null) {
                    images.push(constant.image);
                } else if (constant.texture != null) {
                    images.push(constant.texture.image);
                } else if (constant.glyph != null && constant.glyph.data != null) {
                    images.push(constant.glyph);
                }
            });
            return images;
        }

        function interpretInstruction(ctx, instruction, imageContext) {
            var ctxOriginal = ctx;
            var args = resolveArgs(instruction.args, constantPoolCache);
            var graphicsState = imageContext.graphicsStates[imageContext.currentStateId];
            var xorMode = isXorMode(graphicsState);
            if (xorMode) {
                ctx = initXorModeCtx(graphicsState, ctxOriginal);
            }
            switch (instruction.inst) {
                case InstructionProto.GRAPHICS_CREATE:
                    iprtGraphicsCreate(ctx, instruction.args[0], args, imageContext);
                    break;
                case InstructionProto.GRAPHICS_SWITCH:
                    iprtGraphicsSwitch(ctx, instruction.args[0], imageContext);
                    break;
                case InstructionProto.GRAPHICS_DISPOSE:
                    iprtGraphicsDispose(instruction.args[0], imageContext);
                    break;
                case InstructionProto.DRAW:
                    iprtDraw(ctx, args, graphicsState.transform);
                    break;
                case InstructionProto.FILL:
                    iprtFill(ctx, args);
                    break;
                case InstructionProto.DRAW_IMAGE:
                    iprtDrawImage(ctx, args);
                    break;
                case InstructionProto.DRAW_WEBIMAGE:
                    return iprtDrawWebImage(ctx, args, instruction.webImage);
                    break;
                case InstructionProto.DRAW_STRING:
                    iprtDrawString(ctx, args, graphicsState.fontTransform);
                    break;
                case InstructionProto.COPY_AREA:
                    iprtCopyArea(ctx, args);
                    break;
                case InstructionProto.SET_STROKE:
                    graphicsState.strokeArgs = args;
                    iprtSetStroke(ctx, args);
                    break;
                case InstructionProto.SET_PAINT:
                    graphicsState.paintArgs = args;
                    iprtSetPaint(ctx, args);
                    break;
                case InstructionProto.SET_COMPOSITE:
                    graphicsState.compositeArgs = args;
                    iprtSetComposite(ctx, args);
                    break;
                case InstructionProto.SET_FONT:
                    graphicsState.fontArgs = args;
                    graphicsState.fontTransform = iprtSetFont(ctx, args);
                    break;
                case InstructionProto.TRANSFORM:
                    graphicsState.transform = concatTransform(graphicsState.transform, iprtTransform(ctx, args));
                    break;
                case InstructionProto.DRAW_GLYPH_LIST:
                    iprtDrawGlyphList(ctx, args);
                    break;
                default:
                    console.log("instruction code: " + instruction.inst + " not recognized");
            }

            if (xorMode) {
                var bbox = ctx.popBoundingBox();

                var xorModeColor = parseColorSamples(graphicsState.compositeArgs[0].composite.color);
                applyXorModeComposition(ctxOriginal, ctx, xorModeColor, bbox);

                // ctxOriginal.save();
                // ctxOriginal.strokeStyle = '#000000';
                // ctxOriginal.lineWidth = 1;
                // ctxOriginal.setTransform(1, 0, 0, 1, 0, 0);
                // ctxOriginal.drawImage(xorLayer, 0, 0);
                // var bbox = ctx.popBoundingBox();
                // if (bbox != null) {
                //     ctxOriginal.beginPath();
                //     ctxOriginal.rect(bbox.x, bbox.y, bbox.w, bbox.h);
                //     ctxOriginal.stroke();
                // }
                // ctxOriginal.restore();
            }
            return Promise.resolve();
        }

        function isXorMode(graphicsState) {
            return graphicsState != null && graphicsState.compositeArgs != null && graphicsState.compositeArgs[0].composite.type == CompositeTypeProto.XOR_MODE;
        }

        function initXorModeCtx(graphicsState, original) {
            if (xorLayer == null) {
                xorLayer = document.createElement("canvas");
            }
            xorLayer.width = original.canvas.width;
            xorLayer.height = original.canvas.height;
            var ctx = xorLayer.getContext("2d");
            wrapContext(ctx);
            setCtxState(graphicsState, ctx);
            return ctx;
        }

        function wrapContext(ctx) {
            if (!ctx.wrapped) {
                ctx.wrapped = true;
                ctx.boundingBox = null;
                ctx.pathBBox = null;
                //track bounding boxes of changed areas:
                var beginPathOriginal = ctx.beginPath;
                ctx.beginPath = function () {
                    this.pathBBox = {};
                    this.pathBBox.minX = this.pathBBox.minY = 99999999999;
                    this.pathBBox.maxX = this.pathBBox.maxY = -99999999999;
                    return beginPathOriginal.call(this);
                };
                var setTransformOriginal = ctx.setTransform;
                ctx.setTransform = function (m11, m12, m21, m22, dx, dy) {
                    this.transfomMatrix = [m11, m12, m21, m22, dx, dy];
                    return setTransformOriginal.call(this, m11, m12, m21, m22, dx, dy);
                };

                var transformOriginal = ctx.transform;
                ctx.transform = function (m11, m12, m21, m22, dx, dy) {
                    this.transfomMatrix = concatTransform(this.transfomMatrix, [m11, m12, m21, m22, dx, dy]);
                    return transformOriginal.call(this, m11, m12, m21, m22, dx, dy);
                };

                ctx.updateMinMax = function (x, y) {
                    var tp = transformPoint(x, y, this.transfomMatrix);
                    if (tp.x < this.pathBBox.minX) this.pathBBox.minX = tp.x;
                    if (tp.x > this.pathBBox.maxX) this.pathBBox.maxX = tp.x;
                    if (tp.y < this.pathBBox.minY) this.pathBBox.minY = tp.y;
                    if (tp.y > this.pathBBox.maxY) this.pathBBox.maxY = tp.y;
                };

                var fillTextOriginal = ctx.fillText;
                ctx.fillText = function (text, x, y, maxWidth) {
                    this.pathBBox = {};
                    this.pathBBox.minX = this.pathBBox.minY = 99999999999;
                    this.pathBBox.maxX = this.pathBBox.maxY = -99999999999;
                    var width = maxWidth || this.measureText(text).width;
                    var height = this.measureText("M").width * 2;//approximation
                    this.updateMinMax(x - 3, y - height * 0.7);
                    this.updateMinMax(x - 3, y + height * 0.3);
                    this.updateMinMax(x + width * 1.2, y - height * 0.7);
                    this.updateMinMax(x + width * 1.2, y + height * 0.3);
                    this.setBoundingBox();
                    return fillTextOriginal.call(this, text, x, y, maxWidth);
                };

                var moveToOriginal = ctx.moveTo;
                ctx.moveTo = function (x, y) {
                    this.updateMinMax(x, y);
                    return moveToOriginal.call(this, x, y);
                };

                var lineToOriginal = ctx.lineTo
                ctx.lineTo = function (x, y) {
                    this.updateMinMax(x, y);
                    return lineToOriginal.call(this, x, y);
                };

                var quadraticCurveToOriginal = ctx.quadraticCurveTo
                ctx.quadraticCurveTo = function (cpx, cpy, x, y) {
                    this.updateMinMax(x, y);
                    this.updateMinMax(cpx, cpy);
                    return quadraticCurveToOriginal.call(this, cpx, cpy, x, y);
                };

                var bezierCurveToOriginal = ctx.bezierCurveTo
                ctx.bezierCurveTo = function (cp1x, cp1y, cp2x, cp2y, x, y) {
                    this.updateMinMax(x, y);
                    this.updateMinMax(cp1x, cp1y);
                    this.updateMinMax(cp2x, cp2y);
                    return bezierCurveToOriginal.call(this, cp1x, cp1y, cp2x, cp2y, x, y);
                };

                var rectOriginal = ctx.rect
                ctx.rect = function (x, y, w, h) {
                    this.updateMinMax(x, y);
                    this.updateMinMax(x, y + h);
                    this.updateMinMax(x + w, y);
                    this.updateMinMax(x + w, y + h);
                    return rectOriginal.call(this, x, y, w, h);
                };

                var fillOriginal = ctx.fill;
                ctx.fill = function () {
                    this.setBoundingBox();
                    return fillOriginal.call(this);
                };

                var drawImageOriginal = ctx.drawImage;
                ctx.drawImage = function () {
                    this.pathBBox = {};
                    this.pathBBox.minX = this.pathBBox.minY = -99999999999;
                    this.pathBBox.maxX = this.pathBBox.maxY = 99999999999;
                    this.setBoundingBox();
                    return drawImageOriginal.apply(this, arguments);
                };

                var strokeOriginal = ctx.stroke;
                ctx.stroke = function () {
                    this.setBoundingBox(this.lineWidth / 2 + 3);
                    return strokeOriginal.call(this);
                }

                ctx.setBoundingBox = function (excess) {
                    excess = excess || 0;
                    var x = Math.min(Math.max(0, this.pathBBox.minX - excess), this.canvas.width);
                    var y = Math.min(Math.max(0, this.pathBBox.minY - excess), this.canvas.height);
                    var mx = Math.min(Math.max(0, this.pathBBox.maxX + excess), this.canvas.width);
                    var my = Math.min(Math.max(0, this.pathBBox.maxY + excess), this.canvas.height);
                    this.boundingBox = {x: x, y: y, w: mx - x, h: my - y};
                };

                ctx.popBoundingBox = function () {
                    var result = this.boundingBox;
                    this.boundingBox = null;
                    return result;
                }
            }
        }

        function applyXorModeComposition(dest, src, xor, bbox) {
            if (bbox == null || bbox.w == 0 || bbox.h == 0) {
                return;
            }
            var start = new Date().getTime();
            var destData = dest.getImageData(bbox.x, bbox.y, bbox.w, bbox.h);
            var srcData = src.getImageData(bbox.x, bbox.y, bbox.w, bbox.h);
            for (var i = 0; i < destData.data.length / 4; i++) {
                if (srcData.data[4 * i + 3] > 0) {
                    destData.data[4 * i] = srcData.data[4 * i] ^ xor.r ^ destData.data[4 * i];    // RED (0-255)
                    destData.data[4 * i + 1] = srcData.data[4 * i + 1] ^ xor.g ^ destData.data[4 * i + 1];    // GREEN (0-255)
                    destData.data[4 * i + 2] = srcData.data[4 * i + 2] ^ xor.b ^ destData.data[4 * i + 2];    // BLUE (0-255)
                    destData.data[4 * i + 3] = destData.data[4 * i + 3];  // APLHA (0-255)
                }
            }
            dest.putImageData(destData, bbox.x, bbox.y);
            if (config.logDebug){
                console.log('DirectDraw DEBUG xormode - composition pixelsize:'+ (bbox.w* bbox.h) +' duration(ms): ' + (new Date().getTime() - start));
            }
        }

        function iprtGraphicsDispose(id, imageContext) {
            delete imageContext.graphicsStates[id];
        }

        function iprtGraphicsSwitch(ctx, id, imageContext) {
            var graphicsStates = imageContext.graphicsStates;
            if (graphicsStates[id] != null) {
                setCtxState(graphicsStates[id], ctx);
            } else {
                console.log("Graphics with id " + id + " not initialized!");
            }
            imageContext.currentStateId = id;
        }

        function setCtxState(graphicsState, ctx) {
            if (graphicsState != null) {
                if (graphicsState.strokeArgs != null) {
                    iprtSetStroke(ctx, graphicsState.strokeArgs);
                }
                if (graphicsState.paintArgs != null) {
                    iprtSetPaint(ctx, graphicsState.paintArgs);
                }
                if (graphicsState.compositeArgs != null) {
                    iprtSetComposite(ctx, graphicsState.compositeArgs);
                }
                if (graphicsState.fontArgs != null) {
                    iprtSetFont(ctx, graphicsState.fontArgs);
                }
                if (graphicsState.transform != null) {
                    var t = graphicsState.transform;
                    ctx.setTransform(t[0], t[1], t[2], t[3], t[4], t[5]);
                } else {
                    ctx.setTransform(1, 0, 0, 1, 0, 0);
                }
            }
        }

        function iprtGraphicsCreate(ctx, id, args, imageContext) {
            var graphicsStates = imageContext.graphicsStates;
            if (graphicsStates[id] == null) {
                graphicsStates[id] = {};
                imageContext.currentStateId = id;
                args.shift();
                graphicsStates[id].transform = iprtTransform(ctx, args, true);
                args.shift();
                iprtSetStroke(ctx, args);
                graphicsStates[id].strokeArgs = args.slice(0, 1);
                args.shift();
                iprtSetComposite(ctx, args);
                graphicsStates[id].compositeArgs = args.slice(0, 1);
                args.shift();
                iprtSetPaint(ctx, args);
                graphicsStates[id].paintArgs = args.slice(0, 1);
                args.shift();
                graphicsStates[id].fontArgs = args;
                graphicsStates[id].fontTransform = iprtSetFont(ctx, args);
            } else {
                console.log("Graphics with id " + id + " already exist!");
            }
        }

        function iprtDraw(ctx, args, transform) {
            ctx.save();
            if (path(ctx, args[1])) {
                ctx.clip(fillRule(args[1]));
            }
            path(ctx, args[0], true, transform);
            ctx.stroke();
            ctx.restore();
        }

        function iprtFill(ctx, args) {
            ctx.save();
            if (path(ctx, args[1])) {
                ctx.clip(fillRule(args[1]));
            }
            path(ctx, args[0]);
            ctx.fill(fillRule(args[0]));
            ctx.restore();
        }

        function iprtDrawImage(ctx, args) {
            ctx.save();
            var image = args[0].image.data;
            var transform = args[1];
            var crop = args[2];
            var bgcolor = args[3];
            var clip = args[4];

            if (path(ctx, clip)) {
                ctx.clip(fillRule(clip));
            }
            if (transform != null) {
                iprtTransform(ctx, [transform]);
            }
            if (bgcolor != null) {
                ctx.fillStyle = parseColor(bgcolor.color.rgba);
                ctx.beginPath();
                if (crop == null) {
                    ctx.rect(0, 0, image.width, image.height);
                } else {
                    ctx.rect(0, 0, crop.rectangle.w, crop.rectangle.h);
                }
                ctx.fill();
            }
            if (crop == null) {
                ctx.drawImage(image, 0, 0);
            } else {
                crop = crop.rectangle;
                ctx.drawImage(image, crop.x, crop.y, crop.w, crop.h, 0, 0, crop.w, crop.h);
            }
            ctx.restore();
        }

        function iprtDrawWebImage(ctx, args, webImageData) {
            var transform = args[0];
            var crop = args[1];
            var bgcolor = args[2];
            var clip = args[3];

            var buffer = canvasBuffer.pop();
            return drawBin(webImageData, buffer).then(function (imageCanvas) {
                ctx.save();
                if (path(ctx, clip)) {
                    ctx.clip(fillRule(clip));
                }
                if (transform != null) {
                    iprtTransform(ctx, [transform]);
                }
                if (bgcolor != null) {
                    ctx.fillStyle = parseColor(bgcolor.color.rgba);
                    ctx.beginPath();
                    if (crop == null) {
                        ctx.rect(0, 0, imageCanvas.width, imageCanvas.height);
                    } else {
                        ctx.rect(0, 0, crop.rectangle.w, crop.rectangle.h);
                    }
                    ctx.fill();
                }
                if (crop == null) {
                    ctx.drawImage(imageCanvas, 0, 0);
                } else {
                    crop = crop.rectangle;
                    ctx.drawImage(imageCanvas, crop.x, crop.y, crop.w, crop.h, 0, 0, crop.w, crop.h);
                }
                ctx.restore();

                imageCanvas.width = 0;//clear the buffer image for future reuse
                canvasBuffer.push(imageCanvas);
            });
        }

        function iprtDrawGlyphList(ctx, args) {
            var combinedArgs = resolveArgs(args[0].combined.ids, constantPoolCache);
            var size = combinedArgs[0].points;
            var points = combinedArgs[1].points;
            var glyphs = combinedArgs.slice(2);
            var clip = args[1];
            ctx.save();
            if (path(ctx, clip)) {
                ctx.clip(fillRule(clip));
            }
            if (glyphs.length > 0) {
                var buffer = document.createElement("canvas");
                buffer.width = size.points[2];
                buffer.height = size.points[3];
                var bufctx = buffer.getContext("2d");
                for (var i = 0; i < glyphs.length; i++) {
                    if (glyphs[i].glyph.data != null) {
                        var img = glyphs[i].glyph.data;
                        var x = points.points[i * 2];
                        var y = points.points[i * 2 + 1];
                        bufctx.drawImage(img, 0, 0, img.width, img.height, x, y, img.width, img.height);
                    }
                }
                bufctx.fillStyle = ctx.fillStyle;
                bufctx.globalCompositeOperation = 'source-in';
                bufctx.fillRect(0, 0, buffer.width, buffer.height);
                ctx.drawImage(buffer, 0, 0, buffer.width, buffer.height, size.points[0], size.points[1], buffer.width, buffer.height);
            }
            ctx.restore();
        }

        function iprtDrawString(ctx, args, fontTransform) {
            var string = args[0].string;
            var points = args[1].points.points;
            var clip = args[2];
            ctx.save();
            if (path(ctx, clip)) {
                ctx.clip(fillRule(clip));
            }
            if (fontTransform != null) {
                var t = fontTransform;
                ctx.transform(t.m00, t.m10, t.m01, t.m11, t.m02 + points[0], t.m12 + points[1]);
                ctx.fillText(string, 0, 0);
            } else {
                var canvasWidth = ctx.measureText(string).width;
                var scaleX = points[2] / canvasWidth;
                ctx.scale(scaleX, 1);
                ctx.fillText(string, points[0] / scaleX, points[1]);
            }
            ctx.restore();
        }

        function iprtSetFont(ctx, args) {
            if (args[0] == null) {
                return ctx.font;
            }
            var font = args[0].font;
            var style = '';
            switch (font.style) {
                case StyleProto.NORMAL:
                    style = '';
                    break;
                case StyleProto.OBLIQUE:
                    style = 'bold';
                    break;
                case StyleProto.ITALIC:
                    style = 'italic';
                    break;
                case StyleProto.BOLDANDITALIC:
                    style = 'bold italic';
                    break;
            }
            var fontFamily = font.family;
            if (font.family !== 'sans-serif' && font.family !== 'serif' && font.family !== 'monospace') {
                fontFamily = "\"" + ctxId + font.family + "\"";
            }
            ctx.font = style + " " + font.size + "px " + fontFamily;
            return font.transform;
        }

        function iprtCopyArea(ctx, args) {
            var p = args[0].points.points;
            var clip = args[1];
            ctx.save();

            if (path(ctx, clip)) {
                ctx.clip(fillRule(clip));
            }
            ctx.beginPath();
            ctx.setTransform(1, 0, 0, 1, 0, 0);
            ctx.rect(p[0], p[1], p[2], p[3]);
            ctx.clip();
            ctx.translate(p[4], p[5]);
            ctx.drawImage(ctx.canvas, 0, 0);
            ctx.restore();
        }

        function iprtTransform(ctx, args, reset) {
            var t = args[0].transform;
            if (reset) {
                ctx.setTransform(t.m00, t.m10, t.m01, t.m11, t.m02, t.m12);
            } else {
                ctx.transform(t.m00, t.m10, t.m01, t.m11, t.m02, t.m12);
            }
            return [t.m00, t.m10, t.m01, t.m11, t.m02, t.m12];
        }

        function iprtSetStroke(ctx, args) {
            var stroke = args[0].stroke;
            ctx.lineWidth = stroke.width;
            ctx.miterLimit = stroke.miterLimit;
            switch (stroke.cap) {
                case StrokeCapProto.CAP_BUTT:
                    ctx.lineCap = "butt";
                    break;
                case StrokeCapProto.CAP_ROUND:
                    ctx.lineCap = "round";
                    break;
                case StrokeCapProto.CAP_SQUARE:
                    ctx.lineCap = "square";
                    break;
            }
            switch (stroke.join) {
                case StrokeJoinProto.JOIN_MITER:
                    ctx.lineJoin = "miter";
                    break;
                case StrokeJoinProto.JOIN_ROUND:
                    ctx.lineJoin = "round";
                    break;
                case StrokeJoinProto.JOIN_BEVEL:
                    ctx.lineJoin = "bevel";
                    break;
            }
            if (stroke.dash != null) {
                if (ctx.setLineDash != null) {//ie10 does fails on dash
                    ctx.setLineDash(stroke.dash);
                    ctx.lineDashOffset = stroke.dashOffset;
                }
            }
        }

        function iprtSetPaint(ctx, args) {
            var constant = args[0];
            if (constant.color != null) {
                var color = parseColor(constant.color.rgba);
                ctx.fillStyle = color;
                ctx.strokeStyle = color;
            } else if (constant.texture != null) {
                var anchor = constant.texture.anchor;
                var preloadedImage = constant.texture.image.data;
                var ptrn;
                if (anchor.x == 0 && anchor.y == 0 && anchor.w == preloadedImage.width && anchor == preloadedImage.height) {
                    ptrn = ctx.createPattern(preloadedImage, 'repeat');
                } else {
                    var ptrnCanvas = document.createElement('canvas');
                    var ax = anchor.x < 0 ? ((anchor.x % anchor.w) + anchor.w) : (anchor.x % anchor.w);
                    var ay = anchor.y < 0 ? ((anchor.y % anchor.h) + anchor.h) : (anchor.y % anchor.h);
                    ptrnCanvas.width = anchor.w;
                    ptrnCanvas.height = anchor.h;
                    var ptrnContext = ptrnCanvas.getContext("2d");
                    ptrnContext.fillRect(0, 0, anchor.w, anchor.h);
                    ptrnContext.fillStyle = ptrnContext.createPattern(preloadedImage, 'repeat');
                    ptrnContext.setTransform(anchor.w / preloadedImage.width, 0, 0, anchor.h / preloadedImage.height, ax, ay);
                    ptrnContext.fillRect(-ax * preloadedImage.width / anchor.w, -ay * preloadedImage.height / anchor.h, preloadedImage.width,
                        preloadedImage.height);
                    ptrn = ctx.createPattern(ptrnCanvas, 'repeat');
                }
                ctx.fillStyle = ptrn;
                ctx.strokeStyle = ptrn;
            } else if (constant.linearGrad != null) {
                var gradient = iprtLinearGradient(ctx, constant.linearGrad);
                ctx.fillStyle = gradient;
                ctx.strokeStyle = gradient;
            } else if (constant.radialGrad != null) {
                var gradient = iprtRadialGradient(ctx, constant.radialGrad);
                ctx.fillStyle = gradient;
                ctx.strokeStyle = gradient;
            }
        }

        function iprtLinearGradient(ctx, g) {
            var x0 = g.xStart;
            var y0 = g.yStart;
            var dx = g.xEnd - x0;
            var dy = g.yEnd - y0;
            // in case of cyclic gradient calculate repeat counts
            var repeatCount = 1, increaseCount = repeatCount, decreaseCount = 0;
            if (g.repeat != CyclicMethodProto.NO_CYCLE && (dx != 0 || dy != 0)) {
                // calculate how many times gradient will completely repeat in both directions until it touches canvas corners
                var c = ctx.canvas;
                var times = [calculateTimes(x0, y0, dx, dy, 0, 0),
                    calculateTimes(x0, y0, dx, dy, c.width, 0),
                    calculateTimes(x0, y0, dx, dy, c.width, c.height),
                    calculateTimes(x0, y0, dx, dy, 0, c.height)];
                // increase count is maximum of all positive times rounded up
                increaseCount = Math.ceil(Math.max.apply(Math, times));
                // decrease count is maximum of all negative times rounded up (with inverted sign)
                decreaseCount = Math.ceil(-Math.min.apply(Math, times));
                repeatCount = increaseCount + decreaseCount;
            }
            var gradient = ctx.createLinearGradient(x0 - dx * decreaseCount, y0 - dy * decreaseCount, x0 + dx * increaseCount, y0 + dy * increaseCount);
            for (var rep = -decreaseCount, offset = 0; rep < increaseCount; rep++, offset++) {
                if (g.repeat != CyclicMethodProto.REFLECT || rep % 2 == 0) {
                    for (var i = 0; i < g.colors.length; i++) {
                        gradient.addColorStop((offset + g.fractions[i]) / repeatCount, parseColor(g.colors[i]));
                    }
                } else {
                    // reflect colors
                    for (var i = g.colors.length - 1; i >= 0; i--) {
                        gradient.addColorStop((offset + (1 - g.fractions[i])) / repeatCount, parseColor(g.colors[i]));
                    }
                }
            }
            return gradient;
        }

        // calculates how many times vector (dx, dy) will repeat from (x0, y0) until it touches a straight line
        // which goes through (x1, y1) and perpendicular to the vector
        function calculateTimes(x0, y0, dx, dy, x1, y1) {
            return ((x1 - x0) * dx + (y1 - y0) * dy) / (dx * dx + dy * dy);
        }

        function iprtRadialGradient(ctx, g) {
            fixFocusPoint(g);
            var fX = g.xFocus;
            var fY = g.yFocus;
            var dx = g.xCenter - fX;
            var dy = g.yCenter - fY;
            var r = g.radius;
            // in case of cyclic gradient calculate repeat counts
            var repeatCount = 1;
            if (g.repeat != CyclicMethodProto.NO_CYCLE) {
                if (dx == 0 && dy == 0) {
                    // calculate how many times gradient will completely repeat in both directions until it touches canvas corners
                    var c = ctx.canvas;
                    var times = [getDistance(fX, fY, 0, 0) / r,
                        getDistance(fX, fY, c.width, 0) / r,
                        getDistance(fX, fY, c.width, c.height) / r,
                        getDistance(fX, fY, 0, c.height) / r];
                    repeatCount = Math.ceil(Math.max.apply(Math, times));
                } else {
                    var distance = Math.sqrt(dx * dx + dy * dy);
                    // calculate vector which goes from focus point through center to the circle bound
                    var vdX = dx + r * dx / distance;
                    var vdY = dy + r * dy / distance;
                    // and in opposite direction
                    var ovdX = dx - r * dx / distance;
                    var ovdY = dy - r * dy / distance;
                    // calculate how many times gradient will completely repeat in both directions until it touches canvas corners
                    var c = ctx.canvas;
                    var times = [calculateTimes(fX, fY, vdX, vdY, 0, 0),
                        calculateTimes(fX, fY, vdX, vdY, c.width, 0),
                        calculateTimes(fX, fY, vdX, vdY, c.width, c.height),
                        calculateTimes(fX, fY, vdX, vdY, 0, c.height),
                        calculateTimes(fX, fY, ovdX, ovdY, 0, 0),
                        calculateTimes(fX, fY, ovdX, ovdY, c.width, 0),
                        calculateTimes(fX, fY, ovdX, ovdY, c.width, c.height),
                        calculateTimes(fX, fY, ovdX, ovdY, 0, c.height)];
                    repeatCount = Math.ceil(Math.max.apply(Math, times));
                }
            }
            // in case of repeat focus stays in the same place, radius and distance between focus and center are multiplied
            var gradient = ctx.createRadialGradient(fX, fY, 0, fX + repeatCount * dx, fY + repeatCount * dy, r * repeatCount);
            for (var rep = 0; rep < repeatCount; rep++) {
                if (g.repeat != CyclicMethodProto.REFLECT || rep % 2 == 0) {
                    for (var i = 0; i < g.colors.length; i++) {
                        gradient.addColorStop((rep + g.fractions[i]) / repeatCount, parseColor(g.colors[i]));
                    }
                } else {
                    // reflect colors
                    for (var i = g.colors.length - 1; i >= 0; i--) {
                        gradient.addColorStop((rep + (1 - g.fractions[i])) / repeatCount, parseColor(g.colors[i]));
                    }
                }
            }
            return gradient;
        }

        // fix gradient focus point as java does
        function fixFocusPoint(gradient) {
            var dx = gradient.xFocus - gradient.xCenter;
            var dy = gradient.yFocus - gradient.yCenter;
            if (dx == 0 && dy == 0) {
                return;
            }
            var scaleBack = 0.99;
            var radiusSq = gradient.radius * gradient.radius;
            var distSq = (dx * dx) + (dy * dy);
            // test if distance from focus to center is greater than the radius
            if (distSq > radiusSq * scaleBack) {
                // clamp focus to radius
                var scale = Math.sqrt(radiusSq * scaleBack / distSq);
                // modify source object to skip fixes later
                gradient.xFocus = gradient.xCenter + dx * scale;
                gradient.yFocus = gradient.yCenter + dy * scale;
            }
        }

        function getDistance(x0, y0, x1, y1) {
            return Math.sqrt((x1 - x0) * (x1 - x0) + (y1 - y0) * (y1 - y0));
        }

        function iprtSetComposite(ctx, args) {
            var composite = args[0].composite;
            if (composite != null) {
                ctx.globalAlpha = composite.alpha;
                switch (composite.type) {
                    case CompositeTypeProto.CLEAR:
                        ctx.globalCompositeOperation = "destination-out";
                        ctx.globalAlpha = 1;
                        break;
                    case CompositeTypeProto.SRC:
                        ctx.globalCompositeOperation = "source-over";
                        break;
                    case CompositeTypeProto.DST:
                        ctx.globalCompositeOperation = "destination-over";
                        ctx.globalAlpha = 0;
                        break;
                    case CompositeTypeProto.SRC_OVER:
                        ctx.globalCompositeOperation = "source-over";
                        break;
                    case CompositeTypeProto.DST_OVER:
                        ctx.globalCompositeOperation = "destination-over";
                        break;
                    case CompositeTypeProto.SRC_IN:
                        ctx.globalCompositeOperation = "source-in";
                        break;
                    case CompositeTypeProto.DST_IN:
                        ctx.globalCompositeOperation = "destination-in";
                        break;
                    case CompositeTypeProto.SRC_OUT:
                        ctx.globalCompositeOperation = "source-out";
                        break;
                    case CompositeTypeProto.DST_OUT:
                        ctx.globalCompositeOperation = "destination-out";
                        break;
                    case CompositeTypeProto.SRC_ATOP:
                        ctx.globalCompositeOperation = "source-atop";
                        break;
                    case CompositeTypeProto.DST_ATOP:
                        ctx.globalCompositeOperation = "destination-atop";
                        break;
                    case CompositeTypeProto.XOR:
                        ctx.globalCompositeOperation = "xor";
                        break;
                    case CompositeTypeProto.XOR_MODE://handled with custom pixel processing (no effect)
                        ctx.globalCompositeOperation = "source-over";
                        break;
                }
            }
        }

        function path(ctx, arg, biased, transform) {
            if (arg == null) {
                return false;
            }

            var bias = calculateBias(ctx, biased, transform);

            if (arg.rectangle != null) {
                ctx.beginPath();
                pathRectangle(ctx, arg.rectangle, bias);
                return true;
            }

            if (arg.roundRectangle != null) {
                ctx.beginPath();
                pathRoundRectangle(ctx, arg.roundRectangle, bias);
                return true;
            }

            if (arg.ellipse != null) {
                ctx.beginPath();
                pathEllipse(ctx, arg.ellipse, bias);
                return true;
            }

            if (arg.arc != null) {
                ctx.beginPath();
                pathArc(ctx, arg.arc, bias);
                return true;
            }

            // generic path
            if (arg.path != null) {
                ctx.beginPath();
                var path = arg.path;
                var off = 0;
                path.type.forEach(function (type, index) {
                    switch (type) {
                        case SegmentTypeProto.MOVE:
                            ctx.moveTo(path.points[off + 0] + bias.x, path.points[off + 1] + bias.y);
                            off += 2;
                            break;
                        case SegmentTypeProto.LINE:
                            ctx.lineTo(path.points[off + 0] + bias.x, path.points[off + 1] + bias.y);
                            off += 2;
                            break;
                        case SegmentTypeProto.QUAD:
                            ctx.quadraticCurveTo(path.points[off + 0] + bias.x, path.points[off + 1] + bias.y,
                                path.points[off + 2] + bias.x, path.points[off + 3] + bias.y);
                            off += 4;
                            break;
                        case SegmentTypeProto.CUBIC:
                            ctx.bezierCurveTo(path.points[off + 0] + bias.x, path.points[off + 1] + bias.y,
                                path.points[off + 2] + bias.x, path.points[off + 3] + bias.y,
                                path.points[off + 4] + bias.x, path.points[off + 5] + bias.y);
                            off += 6;
                            break;
                        case SegmentTypeProto.CLOSE:
                            ctx.closePath();
                            break;
                        default:
                            console.log("segment.type:" + segment.type + " not recognized");
                    }
                });
                return true;
            }
            return false;
        }

        function pathRectangle(ctx, rect, bias) {
            ctx.rect(rect.x + bias.x, rect.y + bias.y, rect.w, rect.h);
        }

        function pathEllipse(ctx, elli, bias) {
            var kappa = 0.5522847498307933;
            var pcv = 0.5 + kappa * 0.5;
            var ncv = 0.5 - kappa * 0.5;

            ctx.moveTo(elli.x + bias.x + elli.w, elli.y + bias.y + 0.5 * elli.h);
            var pts = getEllipseCoords([1.0, pcv, pcv, 1.0, 0.5, 1.0], elli, bias);
            ctx.bezierCurveTo(pts[0], pts[1], pts[2], pts[3], pts[4], pts[5]);
            pts = getEllipseCoords([ncv, 1.0, 0.0, pcv, 0.0, 0.5], elli, bias);
            ctx.bezierCurveTo(pts[0], pts[1], pts[2], pts[3], pts[4], pts[5]);
            pts = getEllipseCoords([0.0, ncv, ncv, 0.0, 0.5, 0.0], elli, bias);
            ctx.bezierCurveTo(pts[0], pts[1], pts[2], pts[3], pts[4], pts[5]);
            pts = getEllipseCoords([pcv, 0.0, 1.0, ncv, 1.0, 0.5], elli, bias);
            ctx.bezierCurveTo(pts[0], pts[1], pts[2], pts[3], pts[4], pts[5]);
            ctx.closePath();
        }

        function getEllipseCoords(pts, elli, bias) {
            pts[0] = elli.x + bias.x + pts[0] * elli.w;
            pts[1] = elli.y + bias.y + pts[1] * elli.h;
            pts[2] = elli.x + bias.x + pts[2] * elli.w;
            pts[3] = elli.y + bias.y + pts[3] * elli.h;
            pts[4] = elli.x + bias.x + pts[4] * elli.w;
            pts[5] = elli.y + bias.y + pts[5] * elli.h;
            return pts;
        }

        function pathRoundRectangle(ctx, rr, bias) {
            var acv = 0.22385762508460333;

            var pts = getRRCoords([0, 0, 0, 0.5], rr, bias);
            ctx.moveTo(pts[0], pts[1]);

            pts = getRRCoords([0, 0, 1, -0.5], rr, bias);
            ctx.lineTo(pts[0], pts[1]);
            pts = getRRCoords([0, 0, 1, -acv, 0, acv, 1, 0, 0, 0.5, 1, 0], rr, bias);
            ctx.bezierCurveTo(pts[0], pts[1], pts[2], pts[3], pts[4], pts[5]);

            pts = getRRCoords([1, -0.5, 1, 0], rr, bias);
            ctx.lineTo(pts[0], pts[1]);
            pts = getRRCoords([1, -acv, 1, 0, 1, 0, 1, -acv, 1, 0, 1, -0.5], rr, bias);
            ctx.bezierCurveTo(pts[0], pts[1], pts[2], pts[3], pts[4], pts[5]);

            pts = getRRCoords([1, 0, 0, 0.5], rr, bias);
            ctx.lineTo(pts[0], pts[1]);
            pts = getRRCoords([1, 0, 0, acv, 1, -acv, 0, 0, 1, -0.5, 0, 0], rr, bias);
            ctx.bezierCurveTo(pts[0], pts[1], pts[2], pts[3], pts[4], pts[5]);

            pts = getRRCoords([0, 0.5, 0, 0], rr, bias);
            ctx.lineTo(pts[0], pts[1]);
            pts = getRRCoords([0, acv, 0, 0, 0, 0, 0, acv, 0, 0, 0, 0.5], rr, bias);
            ctx.bezierCurveTo(pts[0], pts[1], pts[2], pts[3], pts[4], pts[5]);

            ctx.closePath();
        }

        function getRRCoords(pts, rr, bias) {
            var coords = [];
            var nc = 0;
            for (var i = 0; i < pts.length; i += 4) {
                coords[nc++] = rr.x + bias.x + pts[i + 0] * rr.w + pts[i + 1] * Math.abs(rr.arcW);
                coords[nc++] = rr.y + bias.y + pts[i + 2] * rr.h + pts[i + 3] * Math.abs(rr.arcH);
            }
            return coords;
        }

        function pathArc(ctx, arc, bias) {
            var w = arc.w / 2, h = arc.h / 2, x = arc.x + bias.x + w, y = arc.y + bias.y + h;
            var angStRad = -(arc.start * Math.PI / 180);
            var ext = -arc.extent;
            var arcSegs = 4;
            var increment = ext < 0 ? Math.PI / 2 : -Math.PI / 2;
            var cv = ext < 0 ? 0.5522847498307933 : -0.5522847498307933;
            if (ext > -360 && ext < 360) {
                arcSegs = Math.ceil(Math.abs(ext) / 90);
                increment = (ext / arcSegs) * Math.PI / 180;
                cv = 4.0 / 3.0 * Math.sin(increment / 2) / (1.0 + Math.cos(increment / 2));
                arcSegs = cv == 0 ? 0 : arcSegs;
            }
            ctx.moveTo(x + Math.cos(angStRad) * w, y + Math.sin(angStRad) * h);
            for (var i = 0; i < arcSegs; i++) {
                var angle = angStRad + increment * i;
                var relx = Math.cos(angle);
                var rely = Math.sin(angle);
                var pts = [];
                pts[0] = (x + (relx - cv * rely) * w);
                pts[1] = (y + (rely + cv * relx) * h);
                angle += increment;
                relx = Math.cos(angle);
                rely = Math.sin(angle);
                pts[2] = (x + (relx + cv * rely) * w);
                pts[3] = (y + (rely - cv * relx) * h);
                pts[4] = (x + relx * w);
                pts[5] = (y + rely * h);
                ctx.bezierCurveTo(pts[0], pts[1], pts[2], pts[3], pts[4], pts[5]);
            }
            switch (arc.type) {
                case ArcTypeProto.OPEN:
                    break;
                case ArcTypeProto.CHORD:
                    ctx.closePath();
                    break;
                case ArcTypeProto.PIE:
                    ctx.lineTo(x, y);
                    ctx.closePath();
                    break;
            }
        }

        function calculateBias(ctx, biased, transform) {
            if (!biased) {
                return {x: 0, y: 0};
            } else {
                return {
                    x: (ctx.lineWidth * transform[0]) & 1 ? 0.5 / transform[0] : 0,
                    y: (ctx.lineWidth * transform[3]) & 1 ? 0.5 / transform[3] : 0
                }
            }
        }

        function parseColor(rgba) {
            var samles = parseColorSamples(rgba);
            return 'rgba(' + samles.r + ',' + samles.g + ',' + samles.b + ',' + (samles.a / 255) + ')';
        }

        function parseColorSamples(rgba) {
            var mask = 0x000000FF;
            return {
                r: ((rgba >>> 24) & mask),
                g: ((rgba >>> 16) & mask),
                b: ((rgba) >>> 8 & mask),
                a: (rgba & mask)
            };
        }

        function prepareImages(images) {
            return new Promise(function (resolve, reject) {
                try {
                    prepareImagesInternal(images, resolve, reject);
                } catch (e) {
                    config.onErrorMessage(error);
                    reject(e);
                }
            });
        }

        function initializeFontFaces(fontFaces) {
            return new Promise(function (resolve, reject) {
                try {
                    if (fontFaces.length > 0) {
                        var loadedFonts = fontFaces.map(function (fontFace) {
                            return new Promise(function (resolve) {
                                if (fontsArray.indexOf(fontFace.name) >= 0) {
                                    resolve();
                                } else {
                                    fontsArray.push(fontFace.name);
                                    var fontCss = document.createElement("style");
                                    fontCss.type = "text/css";
                                    fontCss.setAttribute("data-dd-ctx", ctxId);
                                    var fontName = ctxId + fontFace.name;
                                    fontCss.innerHTML = getFontFaceData(fontName, fontFace.font, fontFace.style);
                                    document.body.appendChild(fontCss);
                                    if (isFontAvailable(fontName)) {
                                        resolve();
                                    } else {
                                        setTimeout(resolve, 5);
                                    }
                                }
                            });
                        });
                        Promise.all(loadedFonts).then(resolve);
                    } else {
                        resolve();
                    }
                } catch (e) {
                    config.onErrorMessage(error);
                    reject(e);
                }
            });
        }

        function isFontAvailable(fontName) {
            var canvas = document.createElement("canvas");
            var context = canvas.getContext("2d");
            var text = "abcdefghijklmnopqrstuvwxyz0123456789";
            context.font = "72px monospace";
            var baselineSize = context.measureText(text).width;
            context.font = "72px '" + fontName + "', monospace";
            var newSize = context.measureText(text).width;
            if (newSize == baselineSize) {
                return false;
            } else {
                return true;
            }
        }

        function getFontFaceData(name, font, style) {
            var fontFaceCss = "@font-face {";
            fontFaceCss += "font-family: '" + name + "';";
            fontFaceCss += "src: url(data:font/truetype;base64," + toBase64(font) + ");";
            if (style != null) {
                fontFaceCss += "font-style: " + style + ";";
            }
            fontFaceCss += "}";
            return fontFaceCss
        }

        function prepareImagesInternal(images, resolve, reject) {
            if (images.length > 0) {
                var loadedImages = images.map(function (image) {
                    return new Promise(function (resolve) {
                        var img = new Image();
                        img.onload = function () {
                            image.data = img;
                            resolve();
                        };
                        img.src = getImageData(image);
                    });
                });
                Promise.all(loadedImages).then(resolve);
            } else {
                resolve();
            }
        }

        function toBase64(data) {
            var binary = '';
            var bytes = new Uint8Array(data.buffer, data.offset, data.limit - data.offset);

            for (var i = 0, l = bytes.byteLength; i < l; i++) {
                binary += String.fromCharCode(bytes[i]);
            }
            return window.btoa(binary);
        }

        function getImageData(image) {
            return "data:image/png;base64," + toBase64(image.data);
        }

        function fillRule(constant) {
            if (constant.path != null) {
                return constant.path.windingOdd ? 'evenodd' : 'nonzero';
            }
            return 'nonzero';
        }

        function resolveArgs(args, cache) {
            var result = [];
            for (var i = 0; i < args.length; i++) {
                result[i] = constantPoolCache[args[i]];
            }
            return result;
        }

        function concatTransform(m, t) {
            var r = [];
            if (m == null) {
                return t;
            } else {
                r[0] = m[0] * t[0] + m[2] * t[1];
                r[1] = m[1] * t[0] + m[3] * t[1];
                r[2] = m[0] * t[2] + m[2] * t[3];
                r[3] = m[1] * t[2] + m[3] * t[3];
                r[4] = m[0] * t[4] + m[2] * t[5] + m[4];
                r[5] = m[1] * t[4] + m[3] * t[5] + m[5];
            }
            return r;
        }

        function transformPoint(x, y, t) {
            var xt = t[0] * x + t[2] * y + t[4];
            var yt = t[1] * x + t[3] * y + t[5];
            return {x: xt, y: yt};
        }

        function dispose() {
            var styles = document.body.getElementsByTagName("style");
            var toRemove = [];
            for (var i = 0; i < styles.length; i++) {
                if (styles[i].getAttribute("data-dd-ctx") === ctxId) {
                    toRemove.push(styles[i]);
                }
            }
            toRemove.forEach(function (element) {
                document.body.removeChild(element);
            });
        }

        function logRenderTime(startTime, webImage) {
            if (config.logDebug && webImage != null) {
                var time = new Date().getTime() - startTime;
                var instLength = webImage.instructions == null ? 0 : webImage.instructions.length;
                var constLength = webImage.constants == null ? 0 : webImage.constants.length;
                var fontsLength = webImage.fontFaces == null ? 0 : webImage.fontFaces.length;
                console.log("DirectDraw DEBUG render time " + time + "ms (insts:" + instLength + ", consts:" + constLength + ", fonts:" + fontsLength + ")");
            }
        }

        return {
            draw64: draw64,
            drawBin: drawBin,
            drawProto: drawProto,
            dispose: dispose,
            getConstantPoolCache: function () {
                return constantPoolCache;
            }
        };
    };
}));
