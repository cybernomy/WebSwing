package org.webswing.toolkit;

import java.awt.EventQueue;
import java.awt.GraphicsEnvironment;
import java.awt.HeadlessException;
import java.awt.peer.FramePeer;
import java.awt.peer.KeyboardFocusManagerPeer;

import org.webswing.toolkit.ge.WebGraphicsEnvironment;

import sun.awt.LightweightFrame;
import sun.awt.datatransfer.DataTransferer;
import sun.awt.image.SurfaceManager;
import sun.java2d.SurfaceData;

@SuppressWarnings("restriction")
public class WebToolkit8 extends WebToolkit {

    private KeyboardFocusManagerPeer kfmp;

    @Override
    public void displayChanged() {
        EventQueue.invokeLater(new Runnable() {

            public void run() {
            	GraphicsEnvironment ge = GraphicsEnvironment.getLocalGraphicsEnvironment();
            	WebGraphicsEnvironment wge = (WebGraphicsEnvironment) ge;
            	wge.displayChanged();
            }
        });
    }

    @Override
    public KeyboardFocusManagerPeer getKeyboardFocusManagerPeer() throws HeadlessException {
        if (kfmp == null) {
            kfmp = new WebKeyboardFocusManagerPeer();
        }
        return kfmp;
    }

    @Override
    protected boolean syncNativeQueue(long paramLong) {
        return false;
    }

    @Override
    public boolean webConpoenentPeerUpdateGraphicsData() {
        ((WebGraphicsEnvironment) GraphicsEnvironment.getLocalGraphicsEnvironment()).displayChanged();
        return true;
    }

    @Override
    public SurfaceData webComponentPeerReplaceSurfaceData(SurfaceManager mgr) {
        return mgr.getPrimarySurfaceData();
    }

	@Override
	public DataTransferer getDataTransferer() {
		return WebDataTransfer.getInstanceImpl();
	}

	@Override
	public FramePeer createLightweightFrame(LightweightFrame arg0)
			throws HeadlessException {
		// TODO Auto-generated method stub
		return null;
	}

}
