#!/usr/bin/perl

#-------------------------------------------------------------------------------
# Split a tab-separated (or space-separated) file based on the
# first column (chromosome name)
#
# Note: Lines starting with "#" are ignored.
#
# Usage: cat file.vcf | ./splitChr.pl [name [ext]]
#
#																Pablo Cingolani
#-------------------------------------------------------------------------------
$chrPrev = "";

$name = "chr";
$ext = "txt";
if( $ARGV[0] ne '' )	{ $name = $ARGV[0]; }
if( $ARGV[1] ne '' )	{ $ext = $ARGV[1]; }

# Iterate on STDIN
for( $i=1 ; $l = <STDIN> ; $i++ ) {
	if( $l =~/^#/ ) {
		# Ignore headers
	} else {
		($chr) = split /\t/, $l;

		# Different chromosome? => Create new file
		if( $chr ne $chrPrev ) {
			$fileName = "$name$chr.$ext";
			print "Input line $i. Creating file '$fileName'\n";
			open CHR, ">$fileName";
			$chrPrev = $chr;
		}

		print CHR $l;
	}
}
close CHR;
