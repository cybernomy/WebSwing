---
title: "Desktop"
weight: 10
---

## File system

Swing application running inside Webswing are ultimately using file system of server where Webswing is running. This cause problems to applications that consume or generate files and interact with file system. 

Webswing solves this problem by providing an automatic `JFileChooser` integration dialog, which offers users ability to interact with server's file system by **uploading, downloading and deleting** files from browser.

![file dialog](../img/filedialog.png)

**Permissions**

It is possible to disable upload, download or deleting of files in the `Security` tab of application settings in Admin console.

**Isolated file system**

If the users are not supposed to have access to full servers file system, Webswing offers option to limit the access to isolated folder which they are allowed to interact with. This setting `isolatedFs` can be set in `Security` tab of application settings in Admin console. If it is enabled, user will only see folder `"upload"` in `homeDir` folder.

![isolated File System](../img/isolatedfs.png)

## Printing

Webswing implements seamless printing support, which works out of the box without any change needed in your application. 
Webswing generates a pdf from the printed document and opens it in new browser tab using `PDF.js` viewer.

Printing integration handles printing using:

* Toolkit.getDefaultToolkit().getPrintJob() method
* PrinterJob.getPrinterJob()
* multi paged documents
* landscape and portrait orientation

Printing is demonstrated in SwingSet3 demo with sample source codes included. 

![isolated File System](../img/printing.png)

## Clipboard

Webswing has a built-in clipboard integration. Due to various browser security limitations some clipboard operations are limited at the moment. 

### Copy

When user triggers copy operation from swing application by pressing `CTRL+c` or in menu option, Webswing analyzes the data written to clipboard and notifies the client what type of data is available in clipboard by small dialog.

Webswing recognizes 5 basic types. 

1. Plain text 
2. HTML  ![html copy dialog](../img/htmlcopy.png)
3. Image ![image copy dialog](../img/imagecopy.png)
4. Files ![file copy dialog](../img/filescopy.png)
5. Other (i.e. application specific types)

This dialog only informs the user that data is available in swing clipboard. To copy the `Plain text` or `HTML` type to local clipboard user has to press `ctrl+c` again while the dialog is visible. For copying the `image` user has to use the browser default behavior to copy the image to local clipboard. 

If the swing clipboard contains `files` and `allowDownload` option is enabled, user will be presented with list links allowing to download those files from Swing application. 

>Note: `isolatedFs` option does not apply here. Access is not limited to files in isolated folder.

### Paste

Paste operation can be triggered by `CTRL+v` keyboard shortcut. But here it gets a little tricky, because we have our swing clipboard data sitting on the server and we have a local browser clipboard data. So we need to decide which one to use for the paste operation.    

Solution is simple. If the clipboard dialog is open we use the Swing clipboard, because we know user has just copied some data there. If the clipboard is not visible, we try to get the data from local browser clipboard. 

![minimized copy dialog](../img/paste.png)

>Note: if paste operation is triggered from context menu, it will always use the swing clipboard data. 

**Pasting from swing clipboard**
All types are accessible to application if pasting from swing clipboard. 

**Pasting from local browser clipboard**
See the following matrix for supported types based on browser.   

Browser | Supported content types
--------| -----------------------
Chrome  | Plain text, HTML, Images
Firefox | Plain text, HTML
IE      | Plain text, HTML


## java.awt.Desktop

The AWT framework Desktop class is the default interface for triggering OS specific operations like edit file or open link. Webswing implements this interface and interprets these commands in web browser friendly way. 

Method   |  Webswing operation
---------|--------------------
browse   | Opens the specified link in new tab
edit     | Referenced file is downloaded 
mail     | Opens a new email window in default mail client
open     | Referenced file is downloaded 
print    | Referenced file is downloaded 