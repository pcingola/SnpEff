#!/usr/bin/perl

#-------------------------------------------------------------------------------
#
# Transform file name to genome short name
#
#-------------------------------------------------------------------------------

$file = $ARGV[0];
$base = `basename $file`;
chomp $base;

if( $base =~ /(.*?)\.(.*)\..?dna\.(.*)\.fa\.gz/ ) { ($gen, $short) = ($1, $2); }
elsif( $base =~ /(.*?)\.(.*)\.gtf\.gz/ ) { ($gen, $short) = ($1, $2); }
elsif( $base =~ /(.*?)\.(.*)\.pep\.all\.fa\.gz/ ) { ($gen, $short) = ($1, $2); }

if( $short =~ /(.*)\.\d+/ ) { $id = $1; }
$full = "$gen.$short";

print "$file\t$base\t$full\t$gen\t$short\t$id\t$gen.$id\n"; 

