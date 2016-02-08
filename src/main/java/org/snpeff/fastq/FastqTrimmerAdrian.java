package org.snpeff.fastq;

import java.util.Collections;
import java.util.LinkedList;

/**
 * Trim fastq sequence when:
 * 	- Median quality drops below a threshold (mean is calculated every 2 bases instead of every base)
 * 	- Sequence length is at least 'minBases'
 * 
 * From Adrian Platts
 * ...Also the sliding window was not every base.
 * It may be easier if I just clip out the vbscript (below) and you can let me know if there are major problems
 * I was also conservative in not taking the frame in which the median dropped below 25 which means I never
 * read the last 5 bases (which given the profile is probably not too bad a thing):
 * 
 *		[read loop]
 *		     sa=s.readline()
 *		     sb=s.readline()
 *		     sc=s.readline()
 *		     sd=s.readline()
 *		     for x=0 to 103 step 2
 *		       for y=1 to 5
 *		         v(y)=asc(midb(sd,x+y,1))-64
 *		       next
 *		       v.sort()
 *		       if v(3)<25 then exit
 *		     next
 *		     x=x-5
 *		     all=all+108
 *		     if x>50 then
 *		       t.write sa+EndOfLine.UNIX
 *		       t.write left(sb,x)+EndOfLine.UNIX
 *		       t.write sc+EndOfLine.UNIX
 *		       t.write left(sd,x)+EndOfLine.UNIX
 *		       tot=tot+x
 *		     end if
 *		...
 *		other stuff
 * 
 * @author pcingola
 *
 */
public class FastqTrimmerAdrian extends FastqTrimmerMedian {

	public FastqTrimmerAdrian(int qualityThreshold, int minBases, int runningMedianLength) {
		super(qualityThreshold, minBases, runningMedianLength);
		this.minBases = minBases;
	}

	/**
	 * Return index where the sequence should be trimmed
	 * First time quality median drops below 'qualityThreshold'
	 */
	@Override
	int trimIndex(Fastq fastq) {
		int qual[] = FastqTools.qualtityArray(fastq);

		int i;
		for( i = 0; i < qual.length - runningMedianLength; i += 2 ) {

			// Sorted list of qualities
			LinkedList<Integer> list = new LinkedList<Integer>();
			for( int k = 1; k <= runningMedianLength; k++ )
				list.add(qual[i + k]);
			Collections.sort(list);

			// Median
			int size = list.size(), med = 0;
			if( list.size() % 2 == 1 ) med = list.get(size / 2);
			else med = (list.get(size / 2 - 1) + list.get(size / 2)) / 2;

			if( med < qualityThreshold ) break;
		}

		// Do we have at least 'minBases'?
		i -= runningMedianLength - 1;
		if( i > minBases ) return i;
		return 0;
	}
}
