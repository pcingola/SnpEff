#!/usr/bin/env perl

#-------------------------------------------------------------------------------
# Galaxy wrapper for SnpEff
#
#																Pablo Cingolani
#-------------------------------------------------------------------------------

# Debug mode?
$debug = 1;

# Get command's dir (full path provided by Galaxy)
$dir = `dirname $0`;
$dir =~ tr/\r\n//d;

# Basic SnpEff command
$command = $ARGV[0];	# Which snpSift's command do we want to execute?
@snpSiftCmd = ("java", "-Xmx4g", "-jar", "$dir/SnpSift.jar", "$command" );

# Add all command line options
for( $i=1 ; $i <= $#ARGV ; $i++ ) {
	$snpSiftCmd[$#snpSiftCmd + 1] = $ARGV[$i];
}

# Execute
print STDERR "Command to execute: " . join(" ", @snpSiftCmd) . "\n\n" if $debug;
exec @snpSiftCmd;
