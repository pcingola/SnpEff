#!/usr/bin/perl

#-------------------------------------------------------------------------------
#
# Transform file name to genome short name
#
#-------------------------------------------------------------------------------

$file = $ARGV[0];
print "$file";

$base = `basename $file`;
chomp $base;
print "\t$base";

if( $base =~ /(.*?)\.(.*)\..?dna\.(.*)\.fa\.gz/ ) { ($gen, $short) = ($1, $2); }
elsif( $base =~ /(.*?)\.(.*)\.gtf\.gz/ ) { ($gen, $short) = ($1, $2); }
elsif( $base =~ /(.*?)\.(.*)\.pep\.all\.fa\.gz/ ) { ($gen, $short) = ($1, $2); }
$full = "$gen.$short";
print "\t$full\t$gen\t$short";


print "\n";

