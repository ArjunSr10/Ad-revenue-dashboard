package main.dash.event;

import javafx.scene.text.Text;
import main.dash.enums.Metric;

public interface MetricListener {
	/**
	 * the callback of metric changed
	 * @param viewIndex the index of data view
	 * @param metric the data of metric
	 * @param index the index of user data
	 */
		public void metricChanged(int viewIndex,Metric metric, int index);
}
