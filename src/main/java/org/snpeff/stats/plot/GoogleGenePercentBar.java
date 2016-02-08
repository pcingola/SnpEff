package org.snpeff.stats.plot;

import java.util.ArrayList;

import com.googlecode.charts4j.AxisLabelsFactory;
import com.googlecode.charts4j.BarChart;
import com.googlecode.charts4j.BarChartPlot;
import com.googlecode.charts4j.Color;
import com.googlecode.charts4j.Data;
import com.googlecode.charts4j.GCharts;
import com.googlecode.charts4j.Plots;

/**
 * A simple wrapper to goolge charts API (from charts4j)
 * 
 * @author pcingola
 */
public class GoogleGenePercentBar {

	static final int len = 11;

	int plotSizeY = 300;
	int plotSizeX = 800;
	ArrayList<String> labelList;
	String title = "";
	String xAxisLabel = "";
	String yAxisLabel = "";
	Data dinter, dup, dutr5, dexon, dssDonor, dintron, dssAcceptor, dutr3, ddown;
	double maxY = Double.MIN_VALUE;

	public GoogleGenePercentBar(String title, String xAxisLabel, String yAxisLabel //
	, double intergenic //
	, double upstream //
	, double utr5 //
	, double exon //
	, double spliceSiteDonor //
	, double intron //
	, double spliceSiteAcceptor //
	, double utr3 //
	, double downstream //
	) {
		this.title = title;
		this.xAxisLabel = xAxisLabel;
		this.yAxisLabel = yAxisLabel;

		maxY = Math.max(intergenic//
		, Math.max(upstream //
		, Math.max(utr5 //
		, Math.max(exon //
		, Math.max(intron //
		, Math.max(utr3 //
		, Math.max(downstream //
		, Math.max(spliceSiteAcceptor, spliceSiteDonor) //
		)))))));

		dinter = data(intergenic, 0, 10, len);
		dup = data(upstream, 1, -1, len);
		dutr5 = data(utr5, 2, -1, len);
		dexon = data(exon, 3, 7, len);
		dssDonor = data(spliceSiteDonor, 4, -1, len);
		dintron = data(intron, 5, -1, len);
		dssAcceptor = data(spliceSiteAcceptor, 6, -1, len);
		dutr3 = data(utr3, 8, -1, len);
		ddown = data(downstream, 9, -1, len);

		labelList = new ArrayList<String>();
		labelList.add("Intergenic");
		labelList.add("Up");
		labelList.add("5'UTR");
		labelList.add("Exon");
		labelList.add("Donor");
		labelList.add("Intron");
		labelList.add("Acceptor");
		labelList.add("Exon");
		labelList.add("3'UTR");
		labelList.add("Down");
		labelList.add("Intergenic");
	}

	/**
	 * Create a data series
	 * @param value 
	 * @param position : Position for this data value
	 * @param len : Data's length
	 */
	Data data(double value, int position, int position2, int len) {
		double scaledVal = 100 * value / maxY;

		ArrayList<Double> data = new ArrayList<Double>();
		for( int i = 0; i < len; i++ ) {
			if( (i == position) || (i == position2) ) data.add(scaledVal);
			else data.add(0.0);
		}

		return Data.newData(data);
	}

	/**
	 * Decorate plot (add titles, scales, etc.)
	 * @param chart
	 */
	void decorate(BarChart chart) {
		chart.setTitle(title);
		chart.setSize(plotSizeX, plotSizeY);

		// Adding axis info to chart.
		chart.addXAxisLabels(AxisLabelsFactory.newAxisLabels(labelList));
		chart.addYAxisLabels(AxisLabelsFactory.newNumericRangeAxisLabels(0, maxY));
		chart.addXAxisLabels(AxisLabelsFactory.newAxisLabels(xAxisLabel, 50.0));
		chart.addYAxisLabels(AxisLabelsFactory.newAxisLabels(yAxisLabel, 50.0));

		chart.setDataStacked(true);
		chart.setBarWidth(50);
	}

	/**
	 * Create a histogram plot using Google charts
	 * @return
	 */
	public String toURLString() {
		BarChartPlot plotInter = Plots.newBarChartPlot(dinter, Color.GRAY, "Intergenic");
		BarChartPlot plottUp = Plots.newBarChartPlot(dup, Color.AQUA, "Upstream");
		BarChartPlot plotUtr5 = Plots.newBarChartPlot(dutr5, Color.SALMON, "5'UTR");
		BarChartPlot plotExon = Plots.newBarChartPlot(dexon, Color.RED, "Exon");
		BarChartPlot plotSsDonor = Plots.newBarChartPlot(dssDonor, Color.BROWN, "Splice Donor");
		BarChartPlot plotIntron = Plots.newBarChartPlot(dintron, Color.ORANGE, "Intron");
		BarChartPlot plotSsAcceptor = Plots.newBarChartPlot(dssAcceptor, Color.BURLYWOOD, "Splice Acceptor");
		BarChartPlot plotUtr3 = Plots.newBarChartPlot(dutr3, Color.DARKSALMON, "3'UTR");
		BarChartPlot plotDown = Plots.newBarChartPlot(ddown, Color.AQUAMARINE, "Downstream");

		BarChart barChart = GCharts.newBarChart(plottUp, plotUtr5, plotExon, plotSsDonor, plotIntron, plotSsAcceptor, plotUtr3, plotDown, plotInter); // Instantiating chart.
		decorate(barChart);

		return barChart.toURLString();
	}

}
