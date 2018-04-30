package org.webswing.toolkit;

import java.awt.Desktop;
import java.awt.Desktop.Action;
import java.awt.peer.DesktopPeer;
import java.io.File;
import java.io.IOException;
import java.net.URI;

import org.webswing.Constants;
import org.webswing.model.internal.OpenFileResultMsgInternal;
import org.webswing.toolkit.util.Logger;
import org.webswing.toolkit.util.Util;

public class WebDesktopPeer implements DesktopPeer {


    public WebDesktopPeer(Desktop d) {
    }

    @Override
    public boolean isSupported(Action action) {
        switch (action) {
            case BROWSE:
            case MAIL:
            case EDIT:
            case OPEN:
            case PRINT:
                return true;
            default:
                return false;
        }

    }

    @Override
    public void open(File file) throws IOException {
        Logger.info("WebDesktopPeer:open", file);
        sendFile(file);
    }

    @Override
    public void edit(File file) throws IOException {
        Logger.info("WebDesktopPeer:edit", file);
        sendFile(file);
    }

    @Override
    public void print(File file) throws IOException {
        Logger.info("WebDesktopPeer:print", file);
        sendFile(file);
    }

    private void sendFile(File file) {
        OpenFileResultMsgInternal f= new OpenFileResultMsgInternal();
        f.setClientId(System.getProperty(Constants.SWING_START_SYS_PROP_CLIENT_ID));
        f.setFile(file);
        Util.getWebToolkit().getPaintDispatcher().sendObject(f);
    }

    @Override
    public void mail(URI mailtoURL) throws IOException {
        Util.getWebToolkit().getPaintDispatcher().notifyOpenLinkAction(mailtoURL);
    }

    @Override
    public void browse(URI url) throws IOException {
        Util.getWebToolkit().getPaintDispatcher().notifyOpenLinkAction(url);
    }
}
