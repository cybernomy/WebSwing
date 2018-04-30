define(['jquery', 'text!templates/clipboard.html','text!templates/paste.html', 'webswing-util'], function amdFactory($, html,pasteHtml, util) {
    "use strict";

    return function ClipboardModule() {
        var module = this;
        var api;
        module.injects = api = {
            cfg: 'webswing.config',
            send: 'socket.send',
            getInput: 'canvas.getInput',
            translate: 'translate.translate'
        };
        module.provides = {
            cut: cut,
            copy: copy,
            paste: paste,
            displayCopyBar: displayCopyBar,
            displayPasteDialog: displayPasteDialog,
            dispose: close
        };
        module.ready = function () {
            document.addEventListener("copy", copy);
            html = api.translate(html);
            pasteHtml = api.translate(pasteHtml);
        };

        var copyBar;
        var pasteDialog;

        function cut(event) {
            copy(event, true);
        }

        function copy(event, cut) {
            if (copyBar == null) {
                if (api.cfg.ieVersion && api.cfg.ieVersion <= 11) {
                    window.clipboardData.setData('Text', '');
                } else {
                    event.clipboardData.setData('text/plain', '');
                }
                api.send({
                    copy: {
                        type: cut === true ? 'cut' : 'copy'
                    }
                });
            } else {
                var data = copyBar.wsEventData;
                if (api.cfg.ieVersion && api.cfg.ieVersion <= 11) {
                    // handling of copy events only for IE
                    var ieClipboardDiv = copyBar.find('div[data-id="ie-clipboard"]');
                    var clipboardData = window.clipboardData;
                    if (data.text != null) {
                        event.preventDefault();
                        clipboardData.setData('Text', data.text);
                    }
                    close();
                } else {
                    // handling of copy events for rest of browsers
                    event = event.originalEvent || event;
                    event.preventDefault();
                    if (data.text != null) {
                        event.clipboardData.setData('text/plain', data.text);
                    }
                    if (data.html != null && !api.cfg.ieVersion) {
                        event.clipboardData.setData('text/html', data.html);
                    }
                    close();
                }
            }
        }

        function paste(event, special) {
            if (api.cfg.hasControl) {
                var text = '';
                var html = '';
                if (api.cfg.ieVersion && api.cfg.ieVersion <= 11) {
                    text = window.clipboardData.getData('Text');
                    html = text;
                } else {
                    var data = event.clipboardData || event.originalEvent.clipboardData;
                    text = data.getData('text/plain');
                    html = data.getData('text/html');
                    if (data.items != null) {
                        for (var i = 0; i < data.items.length; i++) {
                            if (data.items[i].type.indexOf('image') === 0) {
                                var img = data.items[i];
                                var reader = new FileReader();
                                reader.onload = function (event) {
                                    sendPasteEvent(special, text, html, event.target.result);
                                };
                                reader.readAsDataURL(img.getAsFile());
                                return;
                            }
                        }
                    }
                }
                sendPasteEvent(special, text, html);
            }
        }

        function sendPasteEvent(special, text, html, img) {
            var pasteObj = {special: !!special};
            if (text != null && text.length !== 0) {
                pasteObj.text = text;
            }
            if (html != null && html.length !== 0) {
                pasteObj.html = html;
            }
            if (img != null) {
                pasteObj.img = img;
            }

            api.send({
                paste: pasteObj
            });
        }

        function displayCopyBar(data) { // trigered by swing app
            if (copyBar != null) {
                close();
            }
            api.cfg.rootElement.append(html);
            copyBar = api.cfg.rootElement.find('div[data-id="copyBar"]');
            copyBar.on('mouseleave', function (event) {
                minimize();
            });
            copyBar.wsEventData = data;
            var closeBtn = copyBar.find('button[data-id="closeBtn"]');
            closeBtn.click(function () {
                close();
            });
            copyBar.show("fast");

            /* TEXT TAB */
            var copyBtn = copyBar.find('button[data-id="text"]');
            if (api.cfg.isMac) {
                var macCopyMsg = $('<p>Copy to clipboard with CMD+C</p>');
                copyBtn.after(macCopyMsg);
                copyBtn.remove();
                copyBtn = macCopyMsg;
            }
            if ((data.text != null && data.text.length !== 0) || (data.html != null && data.html.length !== 0)) {
                var textarea = copyBar.find('div[data-id="textarea"]');
                if (data.text != null && data.text.length !== 0) {
                    textarea.append($('<pre class="ws-clipboard-text-pre"></pre>').text(data.text));
                } else {
                    textarea.html('<iframe class="ws-clipboard-text-iframe" src="data:text/html;charset=utf-8,' + encodeURIComponent(data.html) + '"></iframe>');
                }
                copyBtn.on('mouseenter', function () {
                    showTab(copyBtn, 'text');
                    maximize();
                });
            }
            copyBtn.on('click', function (e) {
                document.execCommand("copy");
            });
            showTab(copyBtn, 'text');

            /* More TAB */
            if ((data.files != null && data.files.length !== 0) || data.img != null) {
                if (data.files != null && data.files.length !== 0) {
                    var fileListElement = copyBar.find('#wsFileList');
                    for (var i = 0; i < data.files.length; i++) {
                        var fileName = data.files[i];
                        var link = $('<a>');
                        if (fileName.indexOf("#") === 0) {
                            link = $('<span>');
                            link.html(data.files[i].substring(1));
                        } else {
                            link.html(data.files[i]);
                            link.on('click', function (event) {
                                api.send({
                                    copy: {
                                        type: 'getFileFromClipboard',
                                        file: $(event.currentTarget).html()
                                    }
                                });
                            });
                        }
                        fileListElement.append(link);
                        fileListElement.append("<br/>");
                    }
                } else {
                    copyBar.find('div[data-id="files"]').remove();
                }
                if (data.img != null) {
                    var clipImgDataUrl = util.getImageString(data.img);
                    copyBar.find('div[data-id="image"]').append('<a target="_blank" download="clipboard.png" href="' + clipImgDataUrl + '"><img src="' + clipImgDataUrl + '" id="wsCopyImage" class="ws-clipboard-img-thumb"></a>');
                } else {
                    copyBar.find('div[data-id="image"]').remove();
                }

                var moreBtn = copyBar.find('button[data-id="more"]');
                moreBtn.on('mouseenter', function () {
                    showTab(moreBtn, 'more');
                    maximize();
                });
            } else {
                var moreBtn = copyBar.find('button[data-id="more"]');
                moreBtn.remove();
            }

            function showTab(tab, type) {
                copyBar.find('.ws-btn--selected').removeClass('ws-btn--selected');
                copyBar.find('.ws-clipboard-item--active').removeClass('ws-clipboard-item--active');
                $(tab).addClass('ws-btn--selected');
                copyBar.find('div[data-id="' + type + '"]').addClass('ws-clipboard-item--active');
            }

            copyBar.find('div[data-id="contentBar"]').hide();
        }

        function minimize() {
            if (copyBar != null) {
                copyBar.find('.ws-btn--selected').removeClass('ws-btn--selected');
                copyBar.find('div[data-id="contentBar"]').slideUp('fast');
                copyBar.minimized = true;
            }
            api.getInput().focus();
        }

        function maximize() {
            if (copyBar != null) {
                copyBar.find('div[data-id="contentBar"]').slideDown('fast');
                copyBar.minimized = false;
            }
        }

        function close() {
            if (copyBar != null) {
                copyBar.hide("fast");
                copyBar.remove();
                copyBar = null;
            }
            api.getInput().focus();
        }

        function displayPasteDialog(requestCtx){
            if (pasteDialog != null) {
                closePasteDialog();
            }
            api.cfg.rootElement.append(pasteHtml);
            pasteDialog = api.cfg.rootElement.find('div[data-id="pasteDialog"]');

            var title =pasteDialog.find('div[data-id="title"]');
            title.html(api.translate(requestCtx.title));

            var message =pasteDialog.find('div[data-id="message"]');
            message.html(api.translate(requestCtx.message));

            var textarea =pasteDialog.find('textarea[data-id="textarea"]');
            textarea.focus();
            textarea.on('paste', function (event) {
                event.preventDefault();
                event.stopPropagation();
                paste(event, false);
                closePasteDialog();
                return false;
            });

            var closeBtn= pasteDialog.find('button[data-id="closeBtn"]');
            closeBtn.on('click', function (event) {
                api.send({
                    paste: {}
                });
                closePasteDialog();
            });
        }

        function closePasteDialog() {
            if (pasteDialog != null) {
                pasteDialog.hide("fast");
                pasteDialog.remove();
                pasteDialog = null;
            }
            api.getInput().focus();
        }

    };
});