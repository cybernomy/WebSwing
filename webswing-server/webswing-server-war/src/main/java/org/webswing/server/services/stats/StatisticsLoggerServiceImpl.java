package org.webswing.server.services.stats;

import org.webswing.server.services.stats.logger.DefaultStatisticsLogger;

import com.google.inject.Inject;

public class StatisticsLoggerServiceImpl implements StatisticsLoggerService{

	@Inject
	public StatisticsLoggerServiceImpl() {
	}
	
	public StatisticsLogger createLogger(){
		return new DefaultStatisticsLogger();
	}
	
}
