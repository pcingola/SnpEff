#!/usr/bin/perl

#-------------------------------------------------------------------------------
# Simple queue management program
# Attempts to keep 'numProc' processes running at the same time
#
# Proceses are defined in a file (one line per process)
#
# Every executed process creates two files: 'pid.stdout' and 'pid.stderr' where
# pid is the process ID. The files contain STDOUT and STDERR for that process.
#
#																Pablo Cingolani
#-------------------------------------------------------------------------------

use strict;
use POSIX;


my($uptimeCmd) = "/usr/bin/uptime";		# Uptime command
my($maxUptime);
$| = 1;									# Don't use buffers for STDIN/STDOUT

#-------------------------------------------------------------------------------
# Should a new process be run?
# Check some conditions before trying to run the next process
#-------------------------------------------------------------------------------
sub shouldRun() {
	if( $maxUptime < 0 )	{ return 1; }	# Always true if $maxUptime is negative
	my($utRes) = `$uptimeCmd`;
	my($ut) = 0;
	if( $utRes =~ /load average:\s+(\d+\.\d+),/ ) { $ut = $1; }
	return $ut < $maxUptime;
}

#-------------------------------------------------------------------------------
# Print something 'printLog' style
#-------------------------------------------------------------------------------
sub printLog($) {
	my($str) = @_;
	my($now) = strftime "%Y-%m-%d %H:%M:%S", localtime;
	print "$now\t$str\n";
}

#-------------------------------------------------------------------------------
# Main
#-------------------------------------------------------------------------------
# Usage: queue numProc File
my($maxNumProc, $sleepTime, $file);
($maxNumProc, $maxUptime, $sleepTime, $file) = @ARGV;
if( $file eq '' ) {
	print "Usage: queue.pl maxNumProc maxUptime sleepTime file\n";
	print "Where:\n";
	print "\tnumProc    Number of simultaneous processes\n";
	print "\tmaxUptime  Maximum allowed uptime (otherwise, pause before launching the next process). Negative means don't care.\n";
	print "\tsleepTime  Number of seconds to sleep after running a process (zero means no sleep)\n";
	print "\tfile       File containing all commands to be executed (one per line)\n";
	exit(10);
}

#---
# Read file and launch processes
#---
my($cmd);
my($startTime) = time();
my($numProc) = 0;
open BATCH, $file;
while( $cmd = <BATCH> ) {
	chomp $cmd;

	# Can we launch more processes?
	if( $numProc < $maxNumProc ) {

		my( $run ) = 0;

		do {	
			 # Should the next process run now? (don't run if CPU is too high)
			if( shouldRun() ) {
				my $retFork = fork();
				$run = 1;
        
				if( $retFork == 0 ) { # Child process
					# Redirect STDOUT and STDERR to files
					open STDOUT, '>', "$$.stdout" or die "Can't redirect STDOUT (PID=$$): $!";
					open STDERR, '>', "$$.stderr" or die "Can't redirect STDERR (PID=$$): $!";
					exec($cmd);
				} elsif ($retFork == '' ) { # Error launching process
					print STDERR "Error launching process:\t'$cmd'\n";
				} else {
					printLog("Executing (PID=$retFork):\t'$cmd'");
					$numProc++;
				}
			} else { printLog("No running"); }
                
			# Sleep before next process
			if( $sleepTime > 0 ) {
				printLog "Sleep $sleepTime seconds";
				sleep($sleepTime);
			}
		} while( ! $run );
	} 

	# Number of processes exceded? => Wait until one finishes
	if( $numProc >= $maxNumProc ) {
		# Wait for processes to die
		my $deadPid = wait();
		printLog "Process PID=$deadPid finished.";
		$numProc--;
		if( $numProc > 0 )	{ print "There " . ($numProc > 1 ? "are" : "is" ) . " still $numProc processes running.\n"; }
	}
}

#---
# Done, wait for the remining processes to die
#---
my($deadPid);
while( ($deadPid = wait()) >= 0 ) { # Wait for processes to die
	$numProc--;
	my($now) = localtime();
	printLog "Process PID=$deadPid finished.";
	if( $numProc > 0 )	{ print "There " . ($numProc > 1 ? "are" : "is" ) . " still $numProc processes running.\n"; }
}

my($elapsed) = time() - $startTime;
print "All processes finished.\nElapsed time $elapsed seconds.\n";

close BATCH;

