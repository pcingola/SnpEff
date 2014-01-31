#!/usr/bin/perl

#-------------------------------------------------------------------------------
#
# Extract error rates from "SnpEff build" log files
#
#
#															Pablo Cingolani 2013
#-------------------------------------------------------------------------------

# Debug mode?
$debug = 0;

# Maximum tolerated error is 2%
$maxError = 2.0;

#-------------------------------------------------------------------------------
# Show error percentage
#-------------------------------------------------------------------------------
sub num($) {
	my($num) = @_;

	if( $num eq '' ) { return "MISSING\t"; }
	elsif( $num > $maxError ) { return sprintf("ERROR\t%.4f%%", $num); }
	return sprintf("OK\t%.4f%%", $num);
}

#-------------------------------------------------------------------------------
# Main
#-------------------------------------------------------------------------------

while( $l = <STDIN> ) {
	chomp $l;

	if( $l =~ /(\S+) check:\s+(\S+).*Error percentage: (.*)%/ ) {
		($checkType, $genome, $perc) = ($1, $2, $3);
		$err{$genome}->{$checkType} = $perc;
		print STDERR "$checkType\t$genome\t$perc\n";
	}
}

# Show summary information
print "Genome\tCDS_Check\tError%\tProtein_Check\tError%\n";
foreach $genome ( sort keys %err ) {
	printf  "%s\t%s\t%s\n", $genome, num($err{$genome}->{'CDS'}), num($err{$genome}->{'Protein'});
}
