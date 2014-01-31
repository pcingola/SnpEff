package ca.mcgill.mcb.pcingola;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;

import ca.mcgill.mcb.pcingola.fileIterator.RegulationFileIterator;
import ca.mcgill.mcb.pcingola.interval.Regulation;
import ca.mcgill.mcb.pcingola.util.Gpr;
import ca.mcgill.mcb.pcingola.util.Timer;

/**
 * Create a regulation consensus from a regulation file.
 * 
 * @author pcingola
 */
public class RegulationFileConsensus {

	class RegulationConsensus {

		int count = 1;
		Regulation consensus = null;

		void add(Regulation r) {
			if (consensus == null) {
				consensus = r;
				count = 1;
			} else {
				if (consensus.intersects(r)) {
					consensus.setStart(Math.max(consensus.getStart(), r.getStart()));
					consensus.setEnd(Math.max(consensus.getEnd(), r.getEnd()));
					count++;
				} else {
					flush();
					consensus = r;
					count = 1;
				}
			}
		}

		void flush() {
			if (consensus != null) {
				totalCount++;
				totalLength += consensus.size();

				List<Regulation> regs = getRegulationList(consensus.getCellType());
				regs.add(consensus);
			}
		}
	}

	boolean verbose = false;
	int totalCount = 0;
	long totalLength = 0;
	int totalLineNum = 0;

	HashMap<String, RegulationConsensus> regConsByName = new HashMap<String, RegulationFileConsensus.RegulationConsensus>();
	HashMap<String, ArrayList<Regulation>> regByCell = new HashMap<String, ArrayList<Regulation>>();

	public RegulationFileConsensus(boolean verbose) {
		this.verbose = verbose;
	}

	/**
	 * Add to consensus
	 * @param reg
	 */
	public void consensus(Regulation reg) {
		String name = reg.getName();
		String cell = reg.getCellType();
		String key = cell + "_" + name;

		// Get or create
		RegulationConsensus regCons = regConsByName.get(key);
		if (regCons == null) {
			regCons = new RegulationConsensus();
			regConsByName.put(key, regCons);
		}

		regCons.add(reg);
	}

	public Collection<String> getCellTypes() {
		return regByCell.keySet();
	}

	/**
	 * Get regulation list by cell type (or create a new list)
	 * @param cellType
	 * @return
	 */
	public ArrayList<Regulation> getRegulationList(String cellType) {
		ArrayList<Regulation> regs = regByCell.get(cellType);
		if (regs == null) {
			regs = new ArrayList<Regulation>();
			regByCell.put(cellType, regs);
		}
		return regs;
	}

	/**
	 * Read a file and add all regulation intervals 
	 * @param regulationFileIterator
	 */
	public void readFile(RegulationFileIterator regulationFileIterator) {
		String chromo = "";
		int lineNum = 1;
		for (Regulation reg : regulationFileIterator) {

			// Different chromosome? flush all
			if (!chromo.equals(reg.getChromosomeName())) {
				if (verbose) Timer.showStdErr("\tChromosome '" + reg.getChromosomeName() + "'\tline: " + regulationFileIterator.getLineNum());
				for (RegulationConsensus regCons : regConsByName.values())
					regCons.flush();
				chromo = reg.getChromosomeName();
			}

			// Create consensus
			consensus(reg);

			// Show every now and then
			// if( lineNum % 100000 == 0 ) System.err.println("\t" + lineNum + " / " + totalCount + "\t" + String.format("%.1f%%", (100.0 * totalCount / lineNum)));
			lineNum++;
			totalLineNum++;
		}

		// Finished, flush all
		for (RegulationConsensus regCons : regConsByName.values())
			regCons.flush();

		// Show stats
		Timer.showStdErr("Done");
		double perc = (100.0 * totalCount / totalLineNum);
		System.err.println("\tTotal lines                 : " + lineNum);
		System.err.println("\tTotal annotation count      : " + totalCount);
		System.err.println("\tPercent                     : " + String.format("%.1f%%", perc));
		System.err.println("\tTotal annotated length      : " + totalLength);
		System.err.println("\tNumber of cell/annotations  : " + regConsByName.size());
	}

	/**
	 * Save databases (one file per cellType)
	 * @param outputDir
	 */
	public void save(String outputDir) {
		for (String cellType : regByCell.keySet()) {
			String fileName = outputDir + "/regulation_" + cellType + ".bin";
			Timer.showStdErr("Saving database '" + cellType + "' in file '" + fileName + "'");
			Gpr.toFileSerializeGz(fileName, regByCell.get(cellType));
		}
	}

	void show(Regulation reg) {
		System.out.println(reg);
	}

}
