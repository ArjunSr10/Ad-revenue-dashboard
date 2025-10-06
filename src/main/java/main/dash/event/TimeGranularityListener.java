package main.dash.event;

import main.dash.enums.TimeGranularity;

public interface TimeGranularityListener {
	/**
	 * the callback of time changed
	 * @param timeGranularity
	 */
	public void timeGranularityChanged(TimeGranularity timeGranularity);
}
