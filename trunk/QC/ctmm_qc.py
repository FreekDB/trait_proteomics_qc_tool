import sys
import os
from argparse import ArgumentParser
from subprocess import Popen, PIPE
#from watchdog.observers import Observer
#from watchdog.events import LoggingEventHandler

# Globals
_RAW_FILE = ''
_BASE_NAME = ''
_R_GRAPHICS = 'r_ms_graphics.R'
_METRICS = { # All metrics to extract from the NIST metrics output
	'': '',
	'': '',
	'': ''
}

# Paths
_IN_DIR = ''
_OUT_DIR = ''
_COPY_LOG = ''
_NIST = os.path.normpath('C:\\Users\\nbic\\Documents\\NISTMSQCv1_2_0')
_PROGRES_LOG = 'qc_status.log'
_QC_HOME = os.path.normpath("X:\\\\brs2011p09_ctmm\\QC")

def monitor_input():
	print 'Version 0.0.1'
	"""Checks input directory for new RAW files to analyze, keeping track
	of all processed files. Once a new RAW file has been placed in this directory
	a report will be generated with this file as input."""
	global _RAW_FILE, _BASE_NAME, _OUT_DIR
	files = _read_logfile()

	# The following code should be threaded, one thread to monitor for new files
	# and another thread for each file to be processed
	files = _parse_robocopy_log(files)

	#sys.exit('Files: {0}'.format(files))
	for f, status in files.iteritems():
		if status == 'new':
			print "-----------\nProcessing:\n\t", f, "\n-----------\n"
			# Set for current file
			_RAW_FILE = '{0}\\{1}'.format(_IN_DIR, f)
			_BASE_NAME = os.path.splitext(f)[0]
			_OUT_DIR = '{0}\\{1}_QC\\'.format(_OUT_DIR, _BASE_NAME)

			# Create _OUT_DIR storing NIST output as well as graphics etc.
			d = os.path.dirname(_OUT_DIR)
			if not os.path.exists(d):
				os.makedirs(d)
			
			files[f] = 'processing'
			
			# Run QC workflow
			print "Running NIST.."
			_run_NIST() # Done
			print "Creating Graphics.."
			_run_R_script() # Done
			print "Creating metrics.."
			_create_metrics()
			print "Creating report.."
			_create_report()
	
			# Once completed, update status and logfile
			files[f] = 'completed'
	
			# Update logfile showing completed analysis
			_log_progress()
	
def _read_logfile():
	# Logfile layout:
	# Filename	Status	Report_path
	files = dict()
	with open(_PROGRES_LOG, 'r') as logfile:
		log = logfile.readlines()
	if log:
		for f in log:
			f = f.split("\t")
			files[f[0]] = f[1]
	return files
	
def _log_progress():
	"""Keeps track of processed RAW files, this logfile is used to create a
	simple status report through a webserver."""
	pass

def _run_NIST():
	"""Starts the NIST metrics workflow using a RAW file as input. Reads parameters
	from a configuration file to pass as arguments to the workflow."""
	# -WORKAROUND-
	# ReAd4W2Mascot is not working on the VM, using 'msconvert' for the conversion
	# of RAW to mzXML, which needs to be done manually as well as fixing the mzXML header
	_run_msconvert() # DONE
	print "\tRunning NIST pipeline.."
	nist_library = 'human_2011_05_26_it'
	instrument = 'LTQ'
	# Run NIST pipeline
	# TODO: validate parameters, check if in- and out-dir can be the same
	NIST_exe = 'perl {0}\\scripts\\run_NISTMSQC_pipeline.pl'.format(_NIST)
	NIST_cmd = '{0} --in_dir {1} --out_dir {2} --library {3} --instrument_type {4} {5} {6} {7} {8}'.format(NIST_exe,
				_IN_DIR, _OUT_DIR, nist_library, instrument, '--overwrite_searches', '--pro_ms', '--log_file', '--mode lite')
	#sys.exit("NIST_cmd: \n----------\n\n{0}\n\n".format(NIST_cmd))
	
	# TODOs: 
	# 	- error handling
	# 	- 'nistms_metrics.exe' has a tendency to hang if something is wrong, process should
	# 	  resume after a set amount of time
	_run_command(NIST_cmd)

def _run_R_script():
	"""After running the NIST metrics workflow, the mzXML file created can be read in R
	and processed further (graphics and basic metrics)"""
	# Execute Rscript (arguments: input mzXML, output PDF prefix, MSlevel)
	in_out_path = '{0}\\{1}'.format(_OUT_DIR, _BASE_NAME)
	Rcmd = 'Rscript {0} {1} {2} {3}'.format(_R_GRAPHICS, '{0}.RAW.mzXML'.format(in_out_path), in_out_path, 1)
	_run_command(Rcmd)
	
def _create_metrics():
	""" Parses NIST metrics output file extracting relevant metrics subset """
	metrics_file = os.path.normpath('{0}\\{1}_report.msqc'.format(_OUT_DIR, _BASE_NAME))
	print "\n### Metrics File: ", metrics_file
	
def _create_report():
	# Graphics to include
	heatmap = '{0}_heatmap.pdf'.format(_BASE_NAME)
	ion_cnt = '{0}_ions.pdf'.format(_BASE_NAME)

	# Metrics
	
	# Combine into PDF
	
	
def _parse_robocopy_log(files):
	""" Check Robocopy logfile for new files copied """
	with open(_COPY_LOG, 'r') as logfile:
		log = logfile.readlines()
	
	# Parsing a file 'block'
	# TODOs:
	# 	- use regex
	#	- test with other valid file names
	for i, j in enumerate(log):
		if 'Started' in j:
			for k, l in enumerate(log[i:]):
				if 'Monitor' in l:
					for m, n in enumerate(log[i:k+i]):
						if 'New File' in n:
							f = n.split("\t")[-1].split(' ')[-1].strip()
							# If a new file has been found, set status to 'new'
							if f not in files:
								files[f] = 'new'
					break

	return files

""" Temporary (workaround) functions """
def _run_msconvert():
	msconvert = '{0}\\bin\\2562\\msconvert.exe'.format(_NIST)
	# Both mzXML and MGF files need to be created
	mzXML_cmd = '{0} {1} -o {2} --mzXML -e .RAW.mzXML'.format(msconvert, _RAW_FILE, _OUT_DIR)
	MGF_cmd   = '{0} {1} -o {2} --mgf -e .RAW.MGF'.format(msconvert, _RAW_FILE, _OUT_DIR)

	# Execute msconvert for both output files
	print "\tConverting to mzXML.."
	_run_command(mzXML_cmd)
	print "\tConverting to MGF.."
	_run_command(MGF_cmd)
	
	""" 
	# Change mzXML schema from 3.1 to 2
	# TODO: copies the complete file, need to do an inplace replace!
	print "\tChanging mzXML header to v2.0.."
	mzXMLFile = '{0}{1}.RAW.mzXML'.format(_OUT_DIR, _BASE_NAME)
	for line in fileinput.FileInput(mzXMLFile, inplace=1):
		if 'http://sashimi.sourceforge.net/schema_revision/mzXML_3.1' in line:
			print line.replace('3.0', '2.0'),
		else:
			print line,
	"""
	
def _run_command(cmd):
	# Runs a single command, no output is returned
	run = Popen(cmd, stdout=PIPE, stderr=PIPE)
	result, err = run.communicate()
	if run.returncode != 0:
		raise IOError(err)

def main(args):
	global _IN_DIR, _OUT_DIR, _COPY_LOG
	_IN_DIR = args.in_folder
	_OUT_DIR = args.out_folder
	_COPY_LOG = args.copy_log
	
	# Start monitoring
	monitor_input()

	
if __name__ == "__main__":
	# Create and parse commandline arguments
	parser = ArgumentParser(description="QC-workflow for MS data using NIST metrics")
	parser.add_argument('in_folder', type=str, help='Input folder containing (Thermo) RAW files outputted by a mass-spectrometer')
	parser.add_argument('out_folder', type=str, help='Folder in which output (report) PDF files will be written')
	parser.add_argument('copy_log', type=str, help='Logfile (local) that Robocopy uses to write status')

	args = parser.parse_args()
	sys.exit(main(args))

""" *NOTES*

Robocopy command:
Mirror, 10 retries, waiting 30sec, monitor every 1min, display output, no progress, no job header, no job summary
- robocopy "source_path" "\\destination_path" /Mir /R:10 /W:30 /Log+:"\\log_path\logfile.txt" /mot:1 /tee /np /njh /njs
- Note: replace \mir with \e if removing files from source but need to be kept in destination

Graphics:
- time/scan on horizontal (flip the axis)
- projection to one axis (vertical total ion-count)
- maxquant also has heatmap.
- linegraph showing total ion-count per scan (histogram)
	- data might be in the pipeline already
- heatmap for both MS1 and MS2

Metrics:
- Red metrics are based on library searching
- OMMSA is database searching
- Switch OMMSA with Mascot (if possible)
- Add total time of the experiment (combine First / Last MS1)

Notes:
- Optimizing mzXML generation (speed)
- 

"""