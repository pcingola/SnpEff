package org.snpeff.stats.plot;

import com.googlecode.charts4j.AbstractAxisChart;
import com.googlecode.charts4j.BarChart;
import com.googlecode.charts4j.BarChartPlot;
import com.googlecode.charts4j.Data;
import com.googlecode.charts4j.GCharts;
import com.googlecode.charts4j.Plots;

/**
 * A simple wrapper to goolge charts API (from charts4j)
 *
 * @author pcingola
 */
public class GoogleHistogram extends GooglePlotInt {

	int maxBins = Integer.MAX_VALUE;

	public GoogleHistogram(int x[], int y[], String title, String xAxisLabel, String yAxisLabel) {
		super(x, y, title, xAxisLabel, yAxisLabel);
	}

	public int getMaxBins() {
		return maxBins;
	}

	public void setMaxBins(int maxBins) {
		this.maxBins = maxBins;
	}

	/**
	 * Create a histogram plot using Google charts
	 * @return
	 */
	@Override
	public String toURLString() {
		plotMaxData = Math.min(x.length, maxBins); // Limit number of points

		prepareData(false); // Defining data plots.
		Data data = Data.newData(dataList);
		BarChartPlot chart = Plots.newBarChartPlot(data);
		BarChart bchart = GCharts.newBarChart(chart);// Instantiating chart.
		bchart.setBarWidth(barWidth);
		bchart.setSpaceBetweenGroupsOfBars(barSpace);
		AbstractAxisChart achart = bchart;
		decorate(achart);
		return achart.toURLString();
	}

}
