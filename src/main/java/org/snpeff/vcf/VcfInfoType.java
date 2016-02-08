package org.snpeff.vcf;

public enum VcfInfoType {

	UNKNOWN, String, Integer, Float, Flag, Character;

	public static VcfInfoType parse(String str) {
		str = str.toUpperCase();
		if (str.equals("STRING")) return VcfInfoType.String;
		if (str.equals("INTEGER")) return VcfInfoType.Integer;
		if (str.equals("FLOAT")) return VcfInfoType.Float;
		if (str.equals("FLAG")) return VcfInfoType.Flag;
		if (str.equals("CHARACTER")) return VcfInfoType.Character;
		if (str.equals("UNKNOWN")) return VcfInfoType.UNKNOWN;
		throw new RuntimeException("Unknown VcfInfoType '" + str + "'");
	}
}
