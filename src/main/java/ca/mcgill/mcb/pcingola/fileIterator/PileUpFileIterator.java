package ca.mcgill.mcb.pcingola.fileIterator;

import java.io.IOException;

import ca.mcgill.mcb.pcingola.interval.Chromosome;
import ca.mcgill.mcb.pcingola.interval.Genome;
import ca.mcgill.mcb.pcingola.interval.SeqChange;
import ca.mcgill.mcb.pcingola.util.Gpr;

/**
 * Opens a sequence change file and iterates over all sequence changes
 * 
 * Format: Tab-separated format. Columns in the pileup output (see http://sourceforge.net/apps/mediawiki/samtools/index.php?title=SAM_FAQ#I_do_not_understand_the_columns_in_the_pileup_output.):
 * 
 *     1. reference sequence name
 *     2. reference coordinate
 *     3. reference base, or `*' for an indel line
 *     4. genotype where heterozygotes are encoded in the IUB code: M=A/C, R=A/G, W=A/T, S=C/G, Y=C/T and K=G/T; indels are indicated by, for example, * / +A, -A / * or +CC / -C. There is no difference between * / +A or +A / *.
 *     5. Phred-scaled likelihood that the genotype is wrong, which is also called `consensus quality'.
 *     6. Phred-scaled likelihood that the genotype is identical to the reference, which is also called `SNP quality'. Suppose the reference base is A and in alignment we see 17 G and 3 A. We will get a low consensus quality because it is difficult to distinguish an A/G heterozygote from a G/G homozygote. We will get a high SNP quality, though, because the evidence of a SNP is very strong.
 *     7. root mean square (RMS) mapping quality
 *     8. # reads covering the position
 *     9. read bases at a SNP line (check the manual page for more information); the 1st indel allele otherwise
 *     10. base quality at a SNP line; the 2nd indel allele otherwise
 *     11. indel line only: # reads directly supporting the 1st indel allele
 *     12. indel line only: # reads directly supporting the 2nd indel allele
 *     13. indel line only: # reads supporting a third indel allele 
 *     
 * E.g.
 * 2L      5372    T       W       228     228     46      23      A$a$a,..A,,aAAaAA,,,,.,a^].     YfefdbeaffdcfYfffeefdff
 * 2L      5390    T       W       155     155     46      18      ,,aAAaAA,,,,.,a.,.      efffffaffffffdffdY
 * 2L      5403    C       S       228     228     52      19      GgGG,,,,.,g.,GG,.G^]G   ef]bdffffdfffffafff
 * 2L      5465    C       M       201     201     60      25      A,.AA,A,,AA.....,.A....,^]A     dff`ffffffeeffeYffYffffef
 * 2L      5598    C       S       228     228     58      19      .$G.,,GG,.,gg..,G,tg    eedffdff_fefffeBfKd
 * 2L      5698    G       R       2       2       58      18      .,,,A.....,...,a,,      ^fbfSYffdfd^ffeBff
 * 
 * @author pcingola
 */
public class PileUpFileIterator extends SeqChangeFileIterator {

	public PileUpFileIterator(String fileName, Genome genome) {
		super(fileName, genome, 1);
	}

	@Override
	protected SeqChange readNext() {
		// Try to read a line
		try {
			while (ready()) {
				line = readLine();

				if (line == null) return null; // End of file?

				// Ignore empty lines and comment lines
				if ((line.length() > 0) && (!line.startsWith("#"))) {
					// Parse line
					String fields[] = line.split("\\s");

					// Is line OK?
					if (fields.length >= 4) {
						String chromosome = fields[0].trim();
						Chromosome chromo = getChromosome(chromosome);
						sanityCheckChromo(chromosome, chromo); // Sanity check

						int start = parsePosition(fields[1]);
						String reference = fields[2];
						String change = fields[3];
						int strand = +1;
						double quality = Gpr.parseDoubleSafe(fields[5]);
						int coverage = Gpr.parseIntSafe(fields[7]);

						return new SeqChange(chromo, start, reference, change, strand, "", quality, coverage);
					}
				}
			}
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
		return null;
	}
}
