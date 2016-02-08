package org.snpeff.reactome;

import java.util.ArrayList;
import java.util.Collections;

import org.snpeff.util.Gpr;

public class Monitor {
	ArrayList<Entity> entities;
	ArrayList<double[]> results;
	ArrayList<String> labels;

	public Monitor() {
		entities = new ArrayList<Entity>();
		results = new ArrayList<double[]>();
		labels = new ArrayList<String>();
	}

	public void add(Entity e) {
		entities.add(e);
	}

	public void addResults(String label) {
		// Create results array
		double[] res = new double[size()];
		for (int i = 0; i < res.length; i++) {
			Entity e = entities.get(i);
			if (e.hasOutput()) res[i] = e.getOutput();
			else res[i] = Double.NaN;
		}

		// Add to results
		results.add(res);
		labels.add(label);
	}

	public void save(String fileName) {
		Gpr.toFile(fileName, this);
	}

	public int size() {
		return entities.size();
	}

	public int sizeResults() {
		return results.size();
	}

	public void sort() {
		Collections.sort(entities);
	}

	@Override
	public String toString() {
		// Create output
		StringBuilder out = new StringBuilder();

		out.append("enityId\tentityName");
		for (int j = 0; j < results.size(); j++) {
			String lab = Gpr.sanityzeName(labels.get(j));
			out.append("\t" + lab);
		}
		out.append("\n");

		for (int i = 0; i < size(); i++) {
			out.append(entities.get(i).getId());

			String entName = Gpr.sanityzeName(entities.get(i).getName());
			out.append("\t" + entName);

			for (int j = 0; j < results.size(); j++)
				out.append("\t" + results.get(j)[i]);
			out.append("\n");
		}

		return out.toString();
	}
}
