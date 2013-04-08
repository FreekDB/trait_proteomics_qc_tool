#!/Perl/bin/Perl.exe

use strict;
use Getopt::Long;
use lib '.';
use lib 'C:/qc-data/QCArchive27Feb/archive/QC/NISTMSQCv1_5_0/scripts/';
use MetricsPipeline;
use ParseMetrics;

# NIST Mass Spectrometry Data Center
# Paul A. Rudnick
# paul.rudnick@nist.gov
# 09/09/09

# Pipeline program to generate NIST LC-MS/MS QC metrics from Thermo Raw
# files.

#**
# Data analysis of Agilent QTOF, Orbitrap HCD, and Orbitrap CID were added.
# 2011-5-20
# Xiaoyu(Sara) Yang
#*

main();

exit(0);

sub main {
	#### Globals
	my $version = '1.5.0beta';
	my $version_date = '12-20-2012'; 

	# Install Parallel::ForkManager using the following:
	# ppm install Parallel::ForkManager from cmd prompt
	my $use_multiple_threads = 6;
	#eval { use Parallel::ForkManager; };
	#unless ($@) {
	#	$use_multiple_threads = 1; #$ENV{NUMBER_OF_PROCESSORS}-1;
	#}
	
	### Read command-line parameters
	my @in_dirs = (); 
	my @libs = (); 
	my (
	    $out_dir,
	    # $report_name, depracated 2/4/2010
	    # $out_file depracated 1/25/2010
	    $instrument_type,
	    $fasta,
	    $overwrite_all,
	    $overwrite_searches,
	    $no_peptide,
	    #$mcp_summary, depracated 4/13/2012
	    $help,
	    $log_file,
	    $ini_tag,
	    $target_file,
	    $verbose,
	    $msconvert,
	    $srmd,
	    $updated_converter,
	    $itraq,
	    $omssa_threshold,
	    $msgfplus_threshold,
	    $phospho,
	    $se_dir,
	    );
	my $search_engine = 'mspepsearch'; # Default
	my $sort_by = 'date'; # Default
	my $mode = 'full';  # Default
	my $pro_ms = 1; # Default 04/13/2012, no longer configurable from cl
	$updated_converter = 1;
	
	# 12/20/2012
	# Adding MSGF+
	# Adding phospho
	
	my $pl = new MetricsPipeline($version, $version_date);
	my $cl; # Command-line used
	
	if (!@ARGV) {
		$pl->usage();
		$pl->exiting();
	} else {
		$cl = join(' ', @ARGV);
	}
	
	# Command-line specification
	
	if (!GetOptions (
		'in_dir=s' => \@in_dirs,
		'out_dir=s' => \$out_dir,
		#'report_name=s' => \$report_name,
		'library=s' => \@libs,
		'instrument_type=s' => \$instrument_type,
		'fasta:s' => \$fasta,
		'search_engine:s' => \$search_engine,
		'overwrite_all!' => \$overwrite_all,
		'overwrite_searches!' => \$overwrite_searches,
		'sort_by:s' => \$sort_by,
		'mode:s' => \$mode,
		'no_peptide!' => \$no_peptide,
		'pro_ms!' => \$pro_ms,
		#'mcp_summary!' => \$mcp_summary, depracated 4/13/2012
		'help|?' => \$help,
		'log_file!' => \$log_file,
		'ini_tag:s' => \$ini_tag,
		'target_file:s' => \$target_file,
		'verbose!' => \$verbose,
		'msconvert!' => \$msconvert,
		'srmd!'	=> \$srmd,
		'updated_converter!' => \$updated_converter,
		'itraq:s' => \$itraq,
		'omssa_threshold:s' => \$omssa_threshold,
		'phospho!' => \$phospho,
		'se_dir:s' => \$se_dir,
		)
	   ) {
		$pl->exiting();
	}
	
	$pl->set_base_paths(); # Must be run from scripts directory.
	
	if ($ini_tag) {
		@ARGV = split(' ', $pl->get_cl_from_ini_tag($ini_tag));
		# Re-process options if ini_tag given.
		if (!GetOptions (
			'in_dir=s' => \@in_dirs,
			'out_dir=s' => \$out_dir,
			#'report_name=s' => \$report_name,
			'library=s' => \@libs,
			'instrument_type=s' => \$instrument_type,
			'fasta:s' => \$fasta,
			'search_engine:s' => \$search_engine,
			'overwrite_all!' => \$overwrite_all,
			'overwrite_searches!' => \$overwrite_searches,
			'sort_by:s' => \$sort_by,
			'mode:s' => \$mode,
			'no_peptide!' => \$no_peptide,
			'pro_ms!' => \$pro_ms,
			#'mcp_summary!' => \$mcp_summary, depracated 4/13/2012
			'help|?' => \$help,
			'log_file!' => \$log_file,
			'ini_tag:s' => \$ini_tag,
			'target_file:s' => \$target_file,
			'verbose!' => \$verbose,
			'msconvert!' => \$msconvert,
			'srmd!' => \$srmd,
			'updated_converter!' => \$updated_converter,
			'itraq:s' => \$itraq,
			'omssa_threshold:s' => \$omssa_threshold,
			'phospho!' => \$phospho,
			'se_dir:s' => \$se_dir,
			)
		   ) {
			$pl->exiting();
		}
	}
	if ($help) {
		$pl->usage();
		$pl->exiting();
	}
	# Required commnad-line arguments.
	if (! $in_dirs[0] && !$ini_tag ) {
		#print STDERR "One or more \'--in_dir\' is required.\n";
		$pl->exiting();
	}
	if (! $out_dir && !$ini_tag ) {
		print STDERR "Argument \'--out_dir\' is required.\n";
		$pl->exiting();
	}
	
	if (! $libs[0] && !$ini_tag ) {
		print STDERR "Argument \'--library\' is required.\n";
		$pl->exiting();
	}
	if (! $instrument_type && !$ini_tag ) {
		print STDERR "Argument \'--instrument_type\' is required.\n";
		$pl->exiting();
	}
	
	### Pipeline Configuration (advanced/debugging)
	my $run_converter = 1; # Programs can be skipped by setting these to 0.
	my $run_search_engine = 1;
	my $run_nistms_metrics = 1;
	my $num_matches = 1; # Number of ID's per spectrum to return by search engines (only top match counted)
	my $mspepsearch_threshold = 450; # Score
	my $spectrast_threshold = 0.45; # fval
	if (!$omssa_threshold) {
		$omssa_threshold = 0.05; # Default E-value parsing threshold
	}
	if (!$msgfplus_threshold) {
		$msgfplus_threshold = 0.01; # Default QValue threshold
	}
	# Currently allowable instrument and engines
	my @instrument_types = ('LCQ', 'LXQ', 'LTQ', 'FT', 'AGILENT_QTOF', 'ORBI_HCD', 'ORBI_CID', 'QEXACTIVE', 'TRIPLETOF'); # Allowable instrument types.
	###Paul original my @search_engines = ('mspepsearch', 'spectrast', 'omssa', 'msgf+'); # Available search engines
	my @search_engines = ('mspepsearch', 'SpectraST', 'omssa', 'msgf+'); # Available search engines
	# MSGF+ parameters
	my $msgfplus_semi = 0;
	
	# iTRAQ
	my @itraq_types = ('4plex', '8plex');
	
	# General search engine parameters
	my $low_accuracy_pmt = 1.6; # used by set_instrument()
	my $high_accuracy_pmt = 0.2;
	
	# OMSSA configurable parameters
	my $omssa_semi = 0; # semitryptic search
	my $missed_cleavages = 2;
	my $omssa_fmods = '3';
	my $omssa_vmods = '1,10,110,196'; # metox, cam, n-term aceytlation, pyro-glu
	
	### End configuration
	#### End Globals
	
	# Initiate pipeline
	$pl->set_global_configuration(
				      $cl,
				      $overwrite_all,
				      $overwrite_searches,
				      $run_converter,
				      $run_search_engine,
				      $run_nistms_metrics,
				      \@instrument_types,
				      \@search_engines,
				      $no_peptide,
				      $pro_ms,
				      #$mcp_summary,
				      $log_file,
				      $ini_tag,
				      $target_file,
				      $verbose,
				      $msconvert,
				      $srmd,
				      $use_multiple_threads,
				      $updated_converter,
				      $itraq,
				      );
	if ( $pl->set_search_engine($search_engine, $num_matches) ) {
		$pl->exiting();
	}
	# Validate instrument selection
	if ( $pl->set_instrument($instrument_type, $high_accuracy_pmt, $low_accuracy_pmt) ) {
		$pl->exiting();
	}
	# Check ProMS configuration.
	if ($pl->check_proms()) {
		$pl->exiting();
	}
	# check executible files
	if ( $pl->check_executables() ) {
		$pl->exiting();
	}
	
	# Validate in_directories
	if ( $pl->check_in_dirs(\@in_dirs) ) {
		$pl->exiting();
	}
	
	# Validate out_dir
	if ( $pl->check_out_dir($out_dir) ) {
		$pl->exiting();
	}
	
	if ( $pl->check_report_location() ) {
		$pl->exiting();
	}
	if ($se_dir) {
		if ($pl->check_se_dir() ) {
			$pl->exiting($se_dir);
		}
	}
	
	# Search engine configuration, including setting hard thresholds
	
	if ($pl->search_engine() eq 'mspepsearch') {
		$pl->set_score_threshold($mspepsearch_threshold);
	} elsif ($pl->search_engine() eq 'spectrast') {
		$pl->set_score_threshold($spectrast_threshold);
	} elsif ($pl->search_engine() eq 'omssa') {
		$pl->set_score_threshold($omssa_threshold);
		$pl->configure_omssa($omssa_semi, $missed_cleavages, $omssa_fmods, $omssa_vmods);
	} elsif ($pl->search_engine() eq 'msgf+') {
		$pl->set_score_threshold($msgfplus_threshold);
		$pl->configure_msgfplus($missed_cleavages, $phospho, $msgfplus_semi);
	} else {
		print STDERR "Search engine not identified.\nExiting.\n";
		$pl->exiting();
	}
	
	# Check/validate search libs
	if ( (scalar(@libs)>1) && ($pl->search_engine() ne 'mspepsearch') ) {
		print STDERR "Searching multiple databases/libraries only allowed for MSPepSearch.\n";
		$pl->exiting();
	}
	## Set mode (Currently, default mode ONLY is recommended)
	if ( $pl->set_mode($mode) ) {
		$pl->exiting();
	}
	if ( $pl->is_peptide() ) {
		if ( $pl->check_fastas($fasta, \@libs) ) {
			$pl->exiting();
		}
	}
	if ( $pl->check_search_libs(\@libs) ) {
		$pl->exiting();
	}
	
	# Set data file sort option
	if ( $pl->set_sort_option($sort_by) ) {
		$pl->exiting();
	}
	
	if ( $pl->check_target_file() ) {
		$pl->exiting();
	}
	
	# validate iTRAQ settings
	if ($pl->check_itraq(\@itraq_types)) {
		$pl->exiting();
	}
	
	### Begin processing.
	
	# Convert raw data
	if ( $pl->running_converter() ) {
		print "NISTMSQC: Running converter.\n";
		if ( $pl->run_converter() ) {
			$pl->exiting();
		}
	}
	
	# Identify peptide MS/MS spectra with a search engine
	if ( $pl->running_search_engine() ) {
		print "NISTMSQC: Running search engine.\n";
		if ( $pl->run_search_engine() ) {
			$pl->exiting();
		} else {
			print "NISTMSQC: Done with searches.\n";
		}
	}
	# Run ProMS as MS1 analysis option
	if ( $pl->running_pro_ms() ) {
		print "NISTMSQC: Running ProMS.\n";
		if ($pl->run_pro_ms() ) {
			$pl->exiting();
		} else {
			print "NISTMSQC: Done running ProMS.\n";
		}
	}
	
	# Calculate metrics using output from the above programs
	if ( $pl->running_nistms_metrics() ) {
		print "NISTMSQC: Running nistms_metrics.\n";
		if ( $pl->run_nistms_metrics() ) {
			$pl->exiting();
		} else {
			print "NISTMSQC: Done running nistms_metrics.\n";
		}
	}
	
	print "\n#--> NISTMSQC: Pipeline completed and exiting. <--#\n";
}

