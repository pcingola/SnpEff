#!/usr/bin/perl

#-------------------------------------------------------------------------------
#
# Read a VCF file (via STDIN), split EFF fields from INFO column into many lines
# leaving one line per effect.
#
# Note: In lines having multiple effects, all other information will be 
#       repeated. Only the 'EFF' field will change.
#
#															Pablo Cingolani 2012
#-------------------------------------------------------------------------------

$INFO_FIELD_NUM = 7;

while( $l = <STDIN> ) {
	# Show header lines
	if( $l =~ /^#/ ) { print $l; }	
	else {
		chomp $l;
		$l =~ tr/\n\r//d;

		@t = @infos = @effs = (); # Clear arrays

		# Non-header lines: Parse fields
		@t = split /\t/, $l;

		# Get INFO column
		$info = $t[ $INFO_FIELD_NUM ];

		# Parse INFO column 
		@infos = split /;/, $info;

		# Find EFF field
		$infStr = "";
		foreach $inf ( @infos ) {
			# Is this the EFF field? => Find it and split it
			if( $inf =~/^EFF=(.*)/ ) { 
				@effs = split /,/, $1; 
				$fieldName = "EFF";
			} elsif( $inf =~/^ANN=(.*)/ ) { 
				@effs = split /,/, $1; 
				$fieldName = "ANN";
			} else { 
				$infStr .= ( $infStr eq '' ? '' : ';' ) . $inf; 
			}
		}	

		# Print VCF line
		if( $#effs <= 0 )	{ print "$l\n"; }	# No EFF found, just show line
		else {
			$pre = "";
			for( $i=0 ; $i < $INFO_FIELD_NUM ; $i++ ) {
				$pre .= ( $i > 0 ? "\t" : "" ) . "$t[$i]"; 
			}

			$post = "";
			for( $i=$INFO_FIELD_NUM+1 ; $i <= $#t ; $i++ ) {
				$post .= "\t$t[$i]"; 
			}

			foreach $eff ( @effs ) {
				print "$pre\t$infStr" . ( $infStr eq '' ? '' : ';' ) . "$fieldName=$eff$post\n" ; 
			}
		}
	}
}
