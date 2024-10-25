#!/usr/bin/perl

#-------------------------------------------------------------------------------
#
# Find variants having either:
#
#	1) EFF impact HIGH
#
#	2) EFF impact MODERATE and NEXT_PROT impact HIGH 
#
#
#																Pablo Cingolani
#-------------------------------------------------------------------------------

# Debug mode?
$debug=0;

# Effect impact coded as number
$MODERATE = 1;
$HIGH = 2;

#-------------------------------------------------------------------------------
# Funciton max
#-------------------------------------------------------------------------------
sub max($$) {
	my($a,$b) = @_;
	if( $a >= $b )	{ return $a; }
	return $b;
}

#-------------------------------------------------------------------------------
# Main
#-------------------------------------------------------------------------------

# Read STDIN
while( $l = <STDIN> ) {

	# Always show VCF headers
	if( $l =~ /^#/ ) {
		print $l;
	} else {
		# Split VCF entry
		@t = split /\t/, $l;

		# Get INFO field
		$info = $t[7];
		print "INFO: $info\n" if $debug;

		# Parse INFO field: Get EFF tag
		$eff = '';
		@infos = split /;/, $info;
		foreach $info ( @infos ) {
			if( $info =~ /^EFF=(.*)/ ) { $eff = $1; }
		}

		# Parse EFF tag
		if( $eff ne '' ) {
			@effs = split /,/, $eff;

			# Try to find : ( EFF HIGH ) or ( EFF MODERATE + one NEXT_PROT HIGH )
			$effBest = 0;
			$nextprotBest = '';
			foreach $f ( @effs ) {
				# Nextprot effect?
				if( $f =~/^NEXT_PROT/ ) {	
					if( $f =~ /\(HIGH/ ) {	# We only care about high impact nextprot effects
						print "NEXT_PROT: $f\n" if $debug;
						$nextprotBest = $f;
					}
				} else {					
					# Effect (not nextprot)
					if( $f =~ /\(HIGH/ ) { 
						$effBest = max($effBest, $HIGH); 
						print "EFF : $effBest\t$f\n" if $debug;
					} elsif( $f =~ /\(MODERATE/ ) { 
						$effBest = max($effBest, $MODERATE); 
						print "EFF : $effBest\t$f\n" if $debug;
					}
				} 
			}

			# Do we have at least one Moderate EFFect AND one High NEXT_PROT?
			if( ($effBest == $HIGH) || (($effBest == $MODERATE) && ($nextprotBest ne '')) ) {
				print "$effBest\t$nextprotBest\t" if $debug; 
				print "$l"; 
			} 
		}
	}
}
