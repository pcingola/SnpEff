package ca.mcgill.mcb.pcingola.interval;

/**
 * An empty human genome 
 * It only contains the chromosomes with their respective lengths
 * 
 * @author pcingola
 */
public class GenomeHuman extends Genome {

	// Chromosome names and sizes
	public static String chrNames[] = { "10", "11", "12", "13", "14", "15", "16", "17", "18", "19", "1", "20", "21", "22", "2", "3", "4", "5", "6", "7", "8", "9", "MT", "X", "Y" };
	public static int chrLength[] = { 135534747, 135006516, 133851895, 115169878, 107349540, 102531392, 90354753, 81195210, 78077248, 59128983, 249250621, 63025520, 48129895, 51304566, 243199373, 198022430, 191154276, 180915260, 171115067, 159138663, 146364022, 141213431, 16569, 155270560, 59373566 };

	private static final long serialVersionUID = -4982020248272353569L;

	public GenomeHuman() {
		super("hg");

		// Create all chromosomes
		for (int i = 0; i < chrNames.length; i++) {
			Chromosome chr = new Chromosome(this, 0, chrLength[i], chrNames[i]);
			add(chr);
		}
	}

}
