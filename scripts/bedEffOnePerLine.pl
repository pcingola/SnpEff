#!/usr/bin/perl

#-------------------------------------------------------------------------------
#
# Read a BED file (via STDIN), split EFF fields from fourth column into many lines
# leaving one line per effect.
#
# Note: In lines having multiple effects, all other information will be 
#       repeated. Only the 'EFF' field will change.
#
#															Pablo Cingolani 2013
#-------------------------------------------------------------------------------

$EFF_COLUMN = 3;

while( $l = <STDIN> ) {
	# Show header lines
	if( $l =~ /^#/ ) { print $l; }	
	else {
		chomp $l;

		@t = @infos = @effs = (); # Clear arrays

		# Non-header lines: Parse fields
		@t = split /\t/, $l;

		# Parse INFO column 
		@effs = split /;/, $t[ $EFF_COLUMN ];

		# Print BED line
		if( $#effs <= 0 )	{ print "$l\n"; }	# No EFF found, just show line
		else {
			$pre = "";
			for( $i=0 ; $i < $EFF_COLUMN ; $i++ ) { $pre .= ( $i > 0 ? "\t" : "" ) . "$t[$i]"; }

			$post = "";
			for( $i=$EFF_COLUMN+1 ; $i <= $#t ; $i++ ) { $post .= "\t$t[$i]"; }

			foreach $eff ( @effs ) { print "$pre\t$eff$post\n"; }
		}
	}
}
