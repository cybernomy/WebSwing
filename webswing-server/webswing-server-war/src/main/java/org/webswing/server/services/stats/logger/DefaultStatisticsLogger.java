package org.webswing.server.services.stats.logger;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.webswing.server.services.stats.StatisticsLogger;

public class DefaultStatisticsLogger implements StatisticsLogger {

	private static final Logger log = LoggerFactory.getLogger(DefaultStatisticsLogger.class);
	private static final MetricRule DEFAULT_RULE_AVG = new MetricRule(Aggregation.AVG, TimeUnit.SECONDS.toMillis(10), 60);
	private static final MetricRule DEFAULT_RULE_AVG_PER_SEC = new MetricRule(Aggregation.AVG_PER_SEC, TimeUnit.SECONDS.toMillis(10), 60);
	private static final MetricRule DEFAULT_RULE_FLAG = new MetricRule(Aggregation.AVG_PER_SEC, 0, 1);
	private static final Map<String, MetricRule> rules = new HashMap<>();
	private static final Map<String, List<Aggregation>> summaryRulesMap = new HashMap<>();
	private static final Map<String, WarningRule> warningRules = new HashMap<>();


	static {
		rules.put(INBOUND_SIZE_METRIC, DEFAULT_RULE_AVG_PER_SEC);
		rules.put(OUTBOUND_SIZE_METRIC, DEFAULT_RULE_AVG_PER_SEC);
		rules.put(WEBSOCKET_CONNECTED, DEFAULT_RULE_FLAG);
		rules.put(EDT_BLOCKED_SEC_METRIC, DEFAULT_RULE_FLAG);

		warningRules.put(MEMORY_USED_METRIC, WarningRule.memoryUtilizationRule(0.8));
		warningRules.put(LATENCY, WarningRule.thresholdRule(LATENCY, 700));
		warningRules.put(LATENCY_PING, WarningRule.thresholdRule(LATENCY_PING, 500));
		warningRules.put(WEBSOCKET_CONNECTED, WarningRule.thresholdRule(WEBSOCKET_CONNECTED, 2, "WebSocket connection failed. Falling back to long-polling."));
		warningRules.put(EDT_BLOCKED_SEC_METRIC, WarningRule.thresholdRule(EDT_BLOCKED_SEC_METRIC, 10, "EDT blocked for %d seconds. See Thread dump for details."));

		summaryRulesMap.put(MEMORY_ALLOCATED_METRIC, Arrays.asList(Aggregation.SUM));
		summaryRulesMap.put(MEMORY_USED_METRIC, Arrays.asList(Aggregation.SUM));
		summaryRulesMap.put(INBOUND_SIZE_METRIC, Arrays.asList(Aggregation.SUM));
		summaryRulesMap.put(OUTBOUND_SIZE_METRIC, Arrays.asList(Aggregation.SUM));
		summaryRulesMap.put(CPU_UTIL_METRIC, Arrays.asList(Aggregation.SUM));
		summaryRulesMap.put(LATENCY_PING, Arrays.asList(Aggregation.AVG));
		summaryRulesMap.put(LATENCY_NETWORK_TRANSFER, Arrays.asList(Aggregation.AVG));
		summaryRulesMap.put(LATENCY_CLIENT_RENDERING, Arrays.asList(Aggregation.AVG));
		summaryRulesMap.put(LATENCY_SERVER_RENDERING, Arrays.asList(Aggregation.AVG));
		summaryRulesMap.put(LATENCY, Arrays.asList(Aggregation.MAX));
	}

	Map<String, InstanceStats> instanceMap = new HashMap<>();

	@Override
	public void log(String instance, String name, Number value) {
		if (value != null) {
			log.trace("{},{},{}", new Object[] { instance, name, value });
			processMetric(instance, name, value);
		}
	}

	public void processMetric(String instance, String name, Number value) {
		InstanceStats instanceStats = instanceMap.get(instance);
		if (instanceStats == null) {
			instanceStats = new InstanceStats();
			instanceMap.put(instance, instanceStats);
		}
		MetricRule rule = findRule(name);
		WarningRule warn = warningRules.get(name);
		instanceStats.processMetric(rule, name, value, warn);
	}

	private MetricRule findRule(String name) {
		MetricRule rule = rules.get(name);
		if (rule == null) {
			return DEFAULT_RULE_AVG;
		}
		return rule;
	}

	@Override
	public Map<String, Map<Long, Number>> getSummaryStats() {
		SummaryStats stats = new SummaryStats();
		for (String name : summaryRulesMap.keySet()) {
			List<Aggregation> summaryAggreg = summaryRulesMap.get(name);
			for (Aggregation aggregation : summaryAggreg) {
				stats.aggregate(instanceMap, name, aggregation);
			}
		}
		return stats.getStatistics();
	}

	@Override
	public Map<String, List<String>> getSummaryWarnings() {
		Map<String, List<String>> summary = new HashMap<>();
		for (String instanceId : instanceMap.keySet()) {
			List<String> warnings = instanceMap.get(instanceId).getWarnings();
			if (warnings != null && warnings.size() > 0) {
				summary.put(instanceId, warnings);
			}
		}
		return summary;
	}

	@Override
	public Map<String, Map<Long, Number>> getInstanceStats(String instance) {
		InstanceStats stats = instanceMap.get(instance);
		if (stats != null) {
			return stats.getStatistics();
		}
		return null;
	}

	@Override
	public Map<String, Number> getInstanceMetrics(String instance) {
		InstanceStats stats = instanceMap.get(instance);
		if (stats != null) {
			return stats.getMetrics();
		}
		return null;
	}

	@Override
	public List<String> getInstanceWarnings(String instance) {
		InstanceStats stats = instanceMap.get(instance);
		if (stats != null) {
			return stats.getWarnings();
		}
		return null;
	}

	@Override
	public List<String> getInstanceWarningHistory(String instance) {
		InstanceStats stats = instanceMap.get(instance);
		if (stats != null) {
			return stats.getWarningHistory();
		}
		return null;
	}

	public void removeInstance(String instance) {
		instanceMap.remove(instance);
	}

}
