#!/usr/bin/perl

use strict;

# Debug mode?
my($debug) = 1;

# Parameters
my($showDups, $showAllDups, $showCount, $showKey) = (0, 0, 0, 0);

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
# Show ll duplicate lines
#-------------------------------------------------------------------------------

sub printKeyAll($) {
	my($key) = @_;
	my($lines) = $values{$key};
	my(@lines) = split /\n/, $lines;
	my($l);
	foreach $l ( @lines ) { 
		print "$count{$key}\t" if $showCount;
		print "$key\t" if $showKey;
		print "$l\n";
	}
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
	elsif( $arg eq '-D' )		{ $showAllDups = 1; }
	elsif( $arg eq '-c' )		{ $showCount = 1; }
	elsif( $arg eq '-k' )		{ $showKey = 1; }
	elsif( $arg eq '-debug' )	{ $debug = 1; }
	else					{ push(@fields, $arg); }
}

die "Usage: cat file_tab_separated.txt | uniq_cut.pl [-c] [-d] [-k] [-D] field_1 field_2 ... field_N" if $#fields < 1;

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
		$values{$key} .= "\n$l" if $showAllDups;
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
	if( $showAllDups && $count{$key} > 1 )	{ printKeyAll( $key ); }
	elsif(( $showDups && $count{$key} > 1 ) || ( !$showDups && $count{$key} <= 1 ))	{ printKey( $key ); }
}
