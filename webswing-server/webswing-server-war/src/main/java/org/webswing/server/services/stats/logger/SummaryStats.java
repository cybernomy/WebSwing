package org.webswing.server.services.stats.logger;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class SummaryStats {
	private Map<String, Map<Long, Number>> metricsLog = new HashMap<>();

	public void aggregate(Map<String, InstanceStats> instanceMap, String name, Aggregation aggregation) {
		Set<Long> timestamps = new HashSet<>();
		Map<Long,Number> metric = new LinkedHashMap<>();
		ArrayList<InstanceStats> instances = new ArrayList<>(instanceMap.values());
		for (InstanceStats instance : instances) {
			Map<Long, Number> valueMap = instance.getStatistics().get(name);
			if (valueMap != null) {
				timestamps.addAll(new ArrayList<>(valueMap.keySet()));
			}
		}
		for (Long key : timestamps) {
			List<Number> values = new ArrayList<>();
			for (InstanceStats instance : instances) {
				Map<Long, Number> valueMap = instance.getStatistics().get(name);
				if (valueMap != null && valueMap.get(key)!=null) {
					values.add(valueMap.get(key));
				}
			}
			metric.put(key, calculateValue(aggregation, values));
		}
		metricsLog.put(name+"."+aggregation.name(), metric);
	}

	private Number calculateValue(Aggregation rule, List<Number> list) {
		//store value
		Number result = 0;
		if (list != null && list.size() > 0) {
			for (Iterator<Number> iterator = list.iterator(); iterator.hasNext();) {
				Number number = iterator.next();
				switch (rule) {
				case MIN:
					result = Math.min(number.doubleValue(), result.doubleValue());
					break;
				case MAX:
					result = Math.max(number.doubleValue(), result.doubleValue());
					break;
				case SUM:
				case AVG:
				default:
					result = result.doubleValue() + number.doubleValue();
					break;
				}
			}
			if (rule.equals(Aggregation.AVG)) {
				result = result.doubleValue() / list.size();
			}
		}
		return result;
	}

	public Map<String, Map<Long, Number>> getStatistics() {
		return metricsLog;
	}

}