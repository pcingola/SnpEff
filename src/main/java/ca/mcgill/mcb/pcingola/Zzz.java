package ca.mcgill.mcb.pcingola;

import ca.mcgill.mcb.pcingola.fileIterator.VcfFileIterator;
import ca.mcgill.mcb.pcingola.snpEffect.commandLine.SnpEff;
import ca.mcgill.mcb.pcingola.util.Gpr;
import ca.mcgill.mcb.pcingola.vcf.VcfEffect;
import ca.mcgill.mcb.pcingola.vcf.VcfEntry;

public class Zzz extends SnpEff {

	public static void main(String[] args) {
		String vcfFile = Gpr.HOME + "/workspace/SnpEff/tests/test.chr1.eff.vcf";
		VcfFileIterator vcf = new VcfFileIterator(vcfFile);
		for (VcfEntry ve : vcf) {
			if (vcf.isHeadeSection()) System.out.println(vcf.getVcfHeader());

			System.out.println(ve);

			StringBuilder effs = new StringBuilder();
			for (VcfEffect veff : ve.parseEffects()) {
				String gt = veff.getGenotype();
				int gtIdx = Gpr.parseIntSafe(gt) - 1;

				String gtNew = (gtIdx >= 0 ? ve.getAlts()[gtIdx] : gt);
				if (gtNew.equals("null")) gtNew = null;

				veff.setGenotype(gtNew);

				if (effs.length() > 0) effs.append(",");
				effs.append(veff.toString());

				System.out.println("\t" + gt + "\t" + gtNew + "\t" + veff);
			}

			ve.addInfo("EFF", effs.toString());
			System.out.println("\t=>" + ve);
		}

	}
}
