package ca.mcgill.mcb.pcingola;

<<<<<<< HEAD
=======
import ca.mcgill.mcb.pcingola.interval.Gene;
import ca.mcgill.mcb.pcingola.interval.Intron;
import ca.mcgill.mcb.pcingola.interval.Transcript;
>>>>>>> 760382689d60c4d5c197b57e512f12babf5111af
import ca.mcgill.mcb.pcingola.snpEffect.commandLine.SnpEff;
import ca.mcgill.mcb.pcingola.util.Timer;

public class Zzz extends SnpEff {

	StringBuilder sb = new StringBuilder();

	public static void main(String[] args) {
		Timer.showStdErr("Start");

<<<<<<< HEAD
		Timer.showStdErr("End");
	}

=======
		Zzz zzz = new Zzz(null);
		zzz.load("testHg3771Chr1");
		zzz.run();
		Timer.showStdErr("End");
	}

	public Zzz(String[] args) {
		super(args);
		verbose = true;
	}

	void load(String genome) {
		setGenomeVer(genome);
		//		parseArgs(args);
		loadConfig();
		canonical = true;
		loadDb();
	}

	@Override
	public boolean run() {
		for (Gene g : config.getSnpEffectPredictor().getGenome().getGenes()) {
			//			System.out.println(g.getGeneName());
			for (Transcript tr : g) {
				if (!tr.isProteinCoding()) continue;
				if (tr.introns().size() < 2) continue;

				// System.out.println("\t" + tr.getId());
				for (Intron i : tr.introns()) {
					int pos = i.getStart() + (int) (Math.random() * (i.size() - 2)) + 1;

					String line = i.getChromosomeName() + "\t" + pos + "\t.\tA\tT\t.\t.\tAC=1;GENE=" + g.getGeneName() + ";TR=" + tr.getId() + ";INTRON=" + i.getRank();
					System.out.println(line);
					sb.append(line + "\n");
				}
			}
		}

		Gpr.toFile(Gpr.HOME + "/introns_test.vcf", sb);

		return true;
	}
>>>>>>> 760382689d60c4d5c197b57e512f12babf5111af
}
