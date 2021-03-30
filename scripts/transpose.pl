#!/usr/bin/perl


use strict;

my(%data, $l);
my($maxcol) = 0;
my($rownum) = 0;

# Read data
while($l = <STDIN>) {
	chomp $l;
	my(@row) = split /\t/, $l;
	my($colnum) = 0;
	foreach my $val (@row) { $data{$rownum}{$colnum++} = $val; }
	$rownum++;
	$maxcol = $colnum if $colnum > $maxcol;
}

# Print data
my $maxrow = $rownum;
for (my $col = 0; $col < $maxcol; $col++) {
	for (my $row = 0; $row < $maxrow; $row++) {
		printf "%s%s", ($row == 0) ? "" : "\t", defined $data{$row}{$col} ? $data{$row}{$col} : "";
	}
	print "\n";
}
