package main.dash.common;

import java.util.Comparator;

public class SortRange implements Comparator<String> {
	@Override
	public int compare(String s1, String s2) {
		float start1 = Float.parseFloat(s1.split("-")[0]);
		float start2 = Float.parseFloat(s2.split("-")[0]);

		return Float.compare(start1, start2);
	}
}