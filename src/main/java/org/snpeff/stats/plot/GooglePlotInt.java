package org.snpeff.stats.plot;

import java.util.ArrayList;

import com.googlecode.charts4j.AbstractAxisChart;
import com.googlecode.charts4j.AxisLabelsFactory;
import com.googlecode.charts4j.BarChartPlot;
import com.googlecode.charts4j.Data;
import com.googlecode.charts4j.GCharts;
import com.googlecode.charts4j.Plots;

/**
 * A simple wrapper to goolge charts API (from charts4j)
 * Plots integer data
 *
 * @author pcingola
 */
public class GooglePlotInt {

	public static final int MAX_DATA_POINTS = 300;

	int x[];
	int y[];
	int numberLabelsXaxis = 10;
	int barWidth = 8;
	int barSpace = 2;
	int plotSizeY = 300;
	int plotSizeX = 800;
	int plotMaxData = (plotSizeX / (barWidth + barSpace));
	String title = "";
	String xAxisLabel = "";
	String yAxisLabel = "";
	ArrayList<Integer> dataList;
	ArrayList<String> labelList;
	int minX, maxX, minY, maxY;

	public GooglePlotInt(int x[], int y[], String title, String xAxisLabel, String yAxisLabel) {
		this.x = x;
		this.y = y;
		this.title = title;
		this.xAxisLabel = xAxisLabel;
		this.yAxisLabel = yAxisLabel;
	}

	/**
	 * Decorate plot (add titles, scales, etc.)
	 * @param chart
	 */
	void decorate(AbstractAxisChart chart) {
		chart.setTitle(title);
		chart.setSize(plotSizeX, plotSizeY);

		int gridx = (maxX - minX) / 10;
		int gridy = (maxY - minY) / 10;
		if ((gridx > 0) && (gridy > 0)) chart.setGrid((maxX - minX) / 10, (maxY - minY) / 10, 3, 2);

		// Adding axis info to chart.
		chart.addXAxisLabels(AxisLabelsFactory.newAxisLabels(labelList));
		chart.addXAxisLabels(AxisLabelsFactory.newAxisLabels(xAxisLabel, 50.0));
		chart.addYAxisLabels(AxisLabelsFactory.newNumericRangeAxisLabels(minY, maxY));
		chart.addYAxisLabels(AxisLabelsFactory.newAxisLabels(yAxisLabel, 50.0));

	}

	public int getBarSpace() {
		return barSpace;
	}

	public int getBarWidth() {
		return barWidth;
	}

	public int getNumberLabelsXaxis() {
		return numberLabelsXaxis;
	}

	public int getPlotMaxData() {
		return plotMaxData;
	}

	public int getPlotSizeX() {
		return plotSizeX;
	}

	public int getPlotSizeY() {
		return plotSizeY;
	}

	public String getTitle() {
		return title;
	}

	public String getxAxisLabel() {
		return xAxisLabel;
	}

	/**
	 * Prepare data in a suitable format
	 */
	void prepareData(boolean averageY) {
		int numPoints = Math.min(MAX_DATA_POINTS, plotMaxData);
		if (x.length > numPoints) subsample(numPoints, averageY);

		// Find min & max values
		minX = Integer.MAX_VALUE;
		maxX = Integer.MIN_VALUE;
		minY = Integer.MAX_VALUE;
		maxY = Integer.MIN_VALUE;
		for (int i = 0; (i < x.length) && (i < plotMaxData); i++) {
			int xx = x[i];
			minX = Math.min(minX, xx);
			maxX = Math.max(maxX, xx);

			int yy = y[i];
			minY = Math.min(minY, yy);
			maxY = Math.max(maxY, yy);
		}

		if (minY == maxY) minY = 0; // Otherwise the plot does not show anything when the variance is zero

		// Iterate over all sorted keys
		dataList = new ArrayList<Integer>();
		labelList = new ArrayList<String>();
		int labelEvery = Math.max(Math.min(x.length, plotMaxData) / numberLabelsXaxis, 10);
		for (int i = 0; (i < x.length) && (i < plotMaxData); i++) {
			int xx = x[i];

			// Make sure the last point has a coordinate
			if ((i % labelEvery == 0) || (i == (x.length - 1)) || (i == (plotMaxData - 1))) labelList.add(Integer.toString(xx));
			else labelList.add("");

			int yy = y[i];
			int scaledCount = (int) ((100.0 * (yy - minY)) / (maxY - minY)); // 'Count' scaled form 0 to 100
			dataList.add(scaledCount);
		}
	}

	public void setBarSpace(int barSpace) {
		this.barSpace = barSpace;
	}

	public void setBarWidth(int barWidth) {
		this.barWidth = barWidth;
	}

	public void setNumberLabelsXaxis(int numberLabelsXaxis) {
		this.numberLabelsXaxis = numberLabelsXaxis;
	}

	public void setPlotMaxData(int plotMaxData) {
		this.plotMaxData = plotMaxData;
	}

	public void setPlotSizeX(int plotSizeX) {
		this.plotSizeX = plotSizeX;
	}

	public void setPlotSizeY(int plotSizeY) {
		this.plotSizeY = plotSizeY;
	}

	public void setTitle(String title) {
		this.title = title;
	}

	public void setxAxisLabel(String xAxisLabel) {
		this.xAxisLabel = xAxisLabel;
	}

	/**
	 * Re sample data so it has at most 'numberOfPoints'
	 */
	public void subsample(int numberOfPoints, boolean averageY) {
		int xx[] = new int[numberOfPoints];
		int count[] = new int[numberOfPoints];
		int yy[] = new int[numberOfPoints];

		for (int i = 0; i < x.length; i++) {
			int j = i * numberOfPoints / x.length;
			xx[j] += x[i];
			yy[j] += y[i];
			count[j]++;
		}

		for (int j = 0; j < xx.length; j++) {
			if (count[j] > 1) {
				xx[j] /= count[j];
				if (averageY) yy[j] /= count[j];
			}
		}

		x = xx;
		y = yy;
	}

	/**
	 * Create a histogram plot using Google charts
	 * @return
	 */
	public String toURLString() {
		prepareData(true); // Prepare data
		BarChartPlot chart = Plots.newBarChartPlot(Data.newData(dataList)); // Create plot
		AbstractAxisChart achart = GCharts.newLineChart(chart); // Instantiating chart.
		decorate(achart);
		return achart.toURLString();
	}
}
