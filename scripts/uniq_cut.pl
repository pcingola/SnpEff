#!/usr/bin/perl

use strict;

# Debug mode?
my($debug) = 0;

# Parameters
my($showDups, $showCount, $showKey) = (0, 0, 0);

# Data
my( %count, %values );

#-------------------------------------------------------------------------------
# Show an output line
#-------------------------------------------------------------------------------

sub printKey($) {
	my($key) = @_;
	print "$count{$key}\t" if $showCount;
	print "$key\t" if $showKey;
	print "$values{$key}\n";
}

#-------------------------------------------------------------------------------
# Main
#-------------------------------------------------------------------------------

#---
# Parse command line arguments
#---
my( $arg, @fields );
foreach $arg ( @ARGV ) {
	print "ARG: '$arg'\n" if $debug;
	if( $arg eq '-d' )			{ $showDups = 1; }
	elsif( $arg eq '-c' )		{ $showCount = 1; }
	elsif( $arg eq '-k' )		{ $showKey = 1; }
	elsif( $arg eq '-debug' )	{ $debug = 1; }
	else					{ push(@fields, $arg); }
}
print "showDups :$showDups\nshowCount:$showCount\nshowKey  :$showKey\n" if $debug;
die "Usage: cat file_tab_separated.txt | uniq_cut.pl [-c] [-d] field_1 field_2 ... field_N" if $#fields < 1;

#---
# Parse STDIN
#---
my($l, $f, $idx, $key);
while( $l = <STDIN> ) {
	# Split input line
	chomp $l;
	my(@t) = split /\t/, $l;
	
	# Create output line by adding fields in the same order
	$key = "";
	foreach $f ( @fields ) {
		$key .= "\t" if $key ne '';
		$key .= $t[$f-1];
	}

	if( exists $count{$key} ) {
		$count{$key} += 1;
	} else {
		$values{$key} = $l; 
		$count{$key} = 1;
	} 

	print "$l\n\tKEY   : '$key'\n\tCOUNT : $count{$key}\n\tDATA  : $l\n\n" if $debug;
}

#---
# Show unique keys
#---
foreach $key ( sort keys %values ) {
	if(( $showDups && $count{$key} > 1 ) || ( !$showDups && $count{$key} <= 1 ))	{ printKey( $key ); }
}
