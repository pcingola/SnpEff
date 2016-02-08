package org.snpeff.util;

/**
 * General stuff realted to HTML
 * @author pcingola
 */
public class GprHtml {

	/**
	 * Create a color (heat-map) based in a value and maximum possible value
	 * @param value
	 * @param maxValue
	 * @return
	 */
	public static String heatMapColor(double value, double maxValue, double minValue, int rgbMaxValue, int rgbMinValue) {
		if( maxValue == minValue ) return "ffffff"; // Default: white

		double color = (value - minValue) / (maxValue - minValue);

		// Red
		int colorMax = (rgbMaxValue & 0xFF0000) >> 16;
		int colorMin = (rgbMinValue & 0xFF0000) >> 16;
		int rColor = (int) (color * colorMax + (1 - color) * colorMin);

		// Green
		colorMax = (rgbMaxValue & 0xFF00) >> 8;
		colorMin = (rgbMinValue & 0xFF00) >> 8;
		int gColor = (int) (color * colorMax + (1 - color) * colorMin);

		// Blue
		colorMax = (rgbMaxValue & 0xFF);
		colorMin = (rgbMinValue & 0xFF);
		int bColor = (int) (color * colorMax + (1 - color) * colorMin);

		return String.format("#%02x%02x%02x", rColor, gColor, bColor);
	}

	/**
	 * Create a color (heat-map) based in a value and maximum possible value
	 * @param value
	 * @param maxValue
	 * @return
	 */
	public static String heatMapColor(long value, long maxValue, long minValue, int rgbMaxValue, int rgbMinValue) {
		if( maxValue == minValue ) return "ffffff"; // Default: white

		double color = (value - minValue) / ((double) (maxValue - minValue));

		// Red
		int colorMax = (rgbMaxValue & 0xFF0000) >> 16;
		int colorMin = (rgbMinValue & 0xFF0000) >> 16;
		int rColor = (int) (color * colorMax + (1 - color) * colorMin);

		// Green
		colorMax = (rgbMaxValue & 0xFF00) >> 8;
		colorMin = (rgbMinValue & 0xFF00) >> 8;
		int gColor = (int) (color * colorMax + (1 - color) * colorMin);

		// Blue
		colorMax = (rgbMaxValue & 0xFF);
		colorMin = (rgbMinValue & 0xFF);
		int bColor = (int) (color * colorMax + (1 - color) * colorMin);

		return String.format("#%02x%02x%02x", rColor, gColor, bColor);
	}

}
