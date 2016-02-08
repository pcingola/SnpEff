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
 * 
 * @author pcingola
 */
public class GooglePlot {

	public static final int MAX_DATA_POINTS = 300;

	double x[];
	double y[];
	int numberLabelsXaxis = 10;
	int barWidth = 8;
	int barSpace = 2;
	int plotSizeY = 300;
	int plotSizeX = 800;
	String title = "";
	String xAxisLabel = "";
	String yAxisLabel = "";
	ArrayList<Integer> dataList;
	ArrayList<String> labelList;
	double minX, maxX, minY, maxY;

	public GooglePlot(double x[], double y[], String title, String xAxisLabel, String yAxisLabel) {
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

		double gridx = (maxX - minX) / 10;
		double gridy = (maxY - minY) / 10;
		if( (gridx > 0) && (gridy > 0) ) chart.setGrid((maxX - minX) / 10, (maxY - minY) / 10, 3, 2);

		// Adding axis info to chart.
		chart.addXAxisLabels(AxisLabelsFactory.newAxisLabels(labelList));
		chart.addXAxisLabels(AxisLabelsFactory.newAxisLabels(xAxisLabel, 50.0));
		chart.addYAxisLabels(AxisLabelsFactory.newNumericRangeAxisLabels(minY, maxY));
		chart.addYAxisLabels(AxisLabelsFactory.newAxisLabels(yAxisLabel, 50.0));
	}

	/**
	 * Prepare data in a suitable format
	 */
	void prepareData() {
		int numPoints = Math.min(MAX_DATA_POINTS, x.length);
		if( x.length > numPoints ) subsample(numPoints);

		// Find min & max values 
		minX = Double.MAX_VALUE;
		maxX = Double.MIN_VALUE;
		minY = Double.MAX_VALUE;
		maxY = Double.MIN_VALUE;
		for( int i = 0; (i < x.length) && (i < numPoints); i++ ) {
			double xx = x[i];
			minX = Math.min(minX, xx);
			maxX = Math.max(maxX, xx);

			double yy = y[i];
			minY = Math.min(minY, yy);
			maxY = Math.max(maxY, yy);
		}

		if( minY == maxY ) minY = 0; // Otherwise the plot does not show anything when the variance is zero

		// Iterate over all sorted keys
		dataList = new ArrayList<Integer>();
		labelList = new ArrayList<String>();
		int labelEvery = Math.max(numPoints / numberLabelsXaxis, 10);
		for( int i = 0; i < numPoints; i++ ) {
			double xx = x[i];

			// Make sure the last point has a coordinate
			if( (i % labelEvery == 0) || (i == (x.length - 1)) || (i == (numPoints - 1)) ) labelList.add(Double.toString(xx));
			else labelList.add("");

			double yy = y[i];
			int scaledY = (int) ((100.0 * (yy - minY)) / (maxY - minY)); // 'Count' scaled form 0 to 100
			dataList.add(scaledY);
		}
	}

	/** 
	 * Re sample data so it has at most 'numberOfPoints'
	 */
	void subsample(int numberOfPoints) {
		double xx[] = new double[numberOfPoints];
		int count[] = new int[numberOfPoints];
		double yy[] = new double[numberOfPoints];

		for( int i = 0; i < x.length; i++ ) {
			int j = i * numberOfPoints / x.length;
			xx[j] += x[i];
			yy[j] += y[i];
			count[j]++;
		}

		for( int j = 0; j < xx.length; j++ ) {
			if( count[j] > 1 ) {
				xx[j] /= count[j];
				yy[j] /= count[j];
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
		prepareData(); // Prepare data
		BarChartPlot chart = Plots.newBarChartPlot(Data.newData(dataList)); // Create plot
		AbstractAxisChart achart = GCharts.newLineChart(chart); // Instantiating chart.
		decorate(achart);
		return achart.toURLString();
	}
}
