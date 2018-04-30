package org.webswing.server.services.jms;

import org.apache.activemq.broker.BrokerService;
import org.apache.activemq.broker.region.policy.ConstantPendingMessageLimitStrategy;
import org.apache.activemq.broker.region.policy.PolicyEntry;
import org.apache.activemq.broker.region.policy.PolicyMap;
import org.apache.activemq.usage.MemoryUsage;
import org.apache.activemq.usage.SystemUsage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.webswing.Constants;
import org.webswing.server.model.exception.WsInitException;

import com.google.inject.Singleton;

@Singleton
public class JmsServiceImpl implements JmsService {

	private static final Logger log = LoggerFactory.getLogger(JmsService.class);

	private final static long defaultOveralMemLimit = 80 * 1024 * 1024;
	private final static long defaultDestMemLimit = 5 * 1024 * 1024;

	private BrokerService broker;

	public void start() throws WsInitException {
		try {
			System.setProperty("org.apache.activemq.broker.BouncyCastlePosition","9");
			broker = startService();
		} catch (Exception e) {
			log.error("Failed to start JMS service.", e);
			throw new WsInitException("Failed to start JMS service.", e);
		}
	}

	public void stop() {
		try {
			if (broker != null) {
				broker.stop();
			}
		} catch (Exception e) {
			log.error("Failed to stop JMS service.", e);
		}
	}

	public BrokerService startService() throws Exception {
		// BrokerService broker= BrokerFactory.createBroker("xbean:mq.xml");
		System.setProperty("org.apache.activemq.SERIALIZABLE_PACKAGES", Constants.JMS_SERIALIZABLE_PACKAGES);
		BrokerService broker = new BrokerService();
		broker.setUseJmx(true);
		broker.setPersistent(false);

		PolicyMap policyMap = new PolicyMap();
		PolicyEntry defaultEntry = new PolicyEntry();

		ConstantPendingMessageLimitStrategy pendingMessageLimitStrategy = new ConstantPendingMessageLimitStrategy();
		pendingMessageLimitStrategy.setLimit(10);
		defaultEntry.setPendingMessageLimitStrategy(pendingMessageLimitStrategy);
		defaultEntry.setMemoryLimit(5 * 1024 * 1024);
		defaultEntry.setMemoryLimit(getDestinationMemoryLimit());

		policyMap.setDefaultEntry(defaultEntry);
		broker.setDestinationPolicy(policyMap);

		SystemUsage memoryManager = new SystemUsage();
		MemoryUsage memoryLimit = new MemoryUsage();
		memoryLimit.setLimit(getOveralMemoryLimit());

		memoryManager.setMemoryUsage(memoryLimit);
		broker.setSystemUsage(memoryManager);
		// configure the broker
		broker.addConnector(System.getProperty(Constants.JMS_URL, Constants.JMS_URL_DEFAULT));

		broker.start();
		return broker;

	}

	private long getOveralMemoryLimit() {
		long result = defaultOveralMemLimit;
		if (System.getProperty(Constants.JMS_OVERAL_MEM_LIMIT) != null) {
			try {
				result = Long.parseLong(System.getProperty(Constants.JMS_OVERAL_MEM_LIMIT));
			} catch (NumberFormatException e) {
				log.error("System property " + Constants.JMS_OVERAL_MEM_LIMIT + " is not valid. Number value is expected (number of bytes).", e);
			}
		}
		return result;
	}

	private long getDestinationMemoryLimit() {
		long result = defaultDestMemLimit;
		if (System.getProperty(Constants.JMS_DEST_MEM_LIMIT) != null) {
			try {
				result = Long.parseLong(System.getProperty(Constants.JMS_DEST_MEM_LIMIT));
			} catch (NumberFormatException e) {
				log.error("System property " + Constants.JMS_DEST_MEM_LIMIT + " is not valid. Number value is expected (number of bytes).", e);
			}
		}
		return result;
	}
}
