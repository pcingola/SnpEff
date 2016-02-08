package org.snpeff.stats.plot;

import java.util.ArrayList;

import org.snpeff.interval.Downstream;
import org.snpeff.interval.Exon;
import org.snpeff.interval.Intergenic;
import org.snpeff.interval.Intron;
import org.snpeff.interval.Upstream;
import org.snpeff.interval.Utr3prime;
import org.snpeff.interval.Utr5prime;
import org.snpeff.stats.CoverageByType;
import org.snpeff.stats.PosStats;

public class GoogleGeneRegionChart {

	protected static final String[] DEFAULT_TYPES = { Intergenic.class.getSimpleName() //
			, Upstream.class.getSimpleName() //
			, Utr5prime.class.getSimpleName() //
			, Exon.class.getSimpleName() //
			, Intron.class.getSimpleName() //
			, Utr3prime.class.getSimpleName() //
			, Downstream.class.getSimpleName() //
	};

	protected String[] types;
	protected CoverageByType coverageByType;
	protected GoogleLineChart lineChart;
	protected String name, header, body;

	public GoogleGeneRegionChart(CoverageByType coverageByType, String name) {
		this.coverageByType = coverageByType;
		this.name = name;
		init();
	}

	/**
	 * Add a data column
	 */
	int addCol(int nullBefore, String type) {
		ArrayList<String> values = createCol(nullBefore, type);
		lineChart.addColumn(type, values);
		return values.size();
	}

	/**
	 * Create chart
	 */
	void createChart() {
		initTypes();

		int nullBefore = 0;
		for (String type : types)
			nullBefore = addCol(nullBefore, type);

		header = lineChart.toStringHtmlHeader();
		body = lineChart.toStringHtmlBody();
	}

	/**
	 * Create a data column
	 * @param nullBefore
	 * @param type
	 * @return
	 */
	ArrayList<String> createCol(int nullBefore, String type) {
		ArrayList<String> col = new ArrayList<String>();
		PosStats posStats = coverageByType.get(type);

		for (int i = 0; i < nullBefore; i++)
			col.add(null);

		if (posStats != null) {
			if (posStats.size() <= 0) col.add("0");
			else {
				for (int i = 0; i < posStats.size(); i++)
					col.add("" + posStats.getCount(i));
			}
		} else col.add("0");

		return col;
	}

	void init() {
		lineChart = new GoogleLineChart(name);
	}

	void initTypes() {
		types = DEFAULT_TYPES;
	}

	public String toStringHtmlBody() {
		if (body == null) createChart();
		return body;
	}

	public String toStringHtmlHeader() {
		if (header == null) createChart();
		return header;
	}

}
