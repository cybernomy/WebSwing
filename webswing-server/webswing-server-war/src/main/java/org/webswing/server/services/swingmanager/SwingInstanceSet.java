package org.webswing.server.services.swingmanager;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.webswing.model.c2s.ConnectionHandshakeMsgIn;
import org.webswing.server.services.swinginstance.SwingInstance;
import org.webswing.server.services.websocket.WebSocketConnection;
import org.webswing.server.util.ServerUtil;

public class SwingInstanceSet {
	Set<SwingInstance> instances = new HashSet<SwingInstance>();

	public synchronized void add(SwingInstance swingInstance) {
		instances.add(swingInstance);
	}

	public boolean contains(SwingInstance swingInstance) {
		return instances.contains(swingInstance);
	}

	public synchronized void remove(String instanceId) {
		instances.remove(findByInstanceId(instanceId));
	}

	public int size() {
		return instances.size();
	}

	public synchronized SwingInstance findBySessionId(String sessionId) {
		for (SwingInstance i : instances) {
			if (sessionId != null && (sessionId.equals(i.getSessionId()) || sessionId.equals(i.getMirrorSessionId()))) {
				return i;
			}
		}
		return null;
	}

	public synchronized SwingInstance findByClientId(String clientId) {
		for (SwingInstance i : instances) {
			if (clientId != null && clientId.equals(i.getClientId())) {
				return i;
			}
		}
		return null;
	}

	public synchronized SwingInstance findByInstanceId(ConnectionHandshakeMsgIn h, WebSocketConnection r) {
		for (SwingInstance i : instances) {
			String idForMode = ServerUtil.resolveInstanceIdForMode(r, h, i.getAppConfig());
			if (idForMode != null && idForMode.equals(i.getInstanceId())) {
				return i;
			}
		}
		return null;
	}

	public synchronized SwingInstance findByInstanceId(String instanceId) {
		for (SwingInstance i : instances) {
			if (instanceId != null && instanceId.equals(i.getInstanceId())) {
				return i;
			}
		}
		return null;
	}

	public synchronized List<SwingInstance> getAllInstances() {
		return new ArrayList<SwingInstance>(instances);
	}

}
