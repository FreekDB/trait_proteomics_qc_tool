import sys
import os
import re
from string import Template
from argparse import ArgumentParser
from subprocess import Popen, PIPE
#from watchdog.observers import Observer
#from watchdog.events import LoggingEventHandler

# Globals
_RAW_FILE = ''
_BASE_NAME = ''
_R_GRAPHICS = 'r_ms_graphics.R'
# Compiles all metrics to extract from the NIST metrics output
# Each value is a list of 3 holding the line to match (to get the line number),
# the offset from that line that holds the value of interest and the regex to
# retrieve this value.
_METRICS = {
	# Experiment details
	'ms1_scans': ['', 0, re.compile('')],
	'ms2_scans': ['', 0, re.compile('')],
	'f_ms1_rt': ['First and Last MS1 RT', 1, re.compile('First MS1\s+([0-9\.]+)')],
	'l_ms1_rt': ['First and Last MS1 RT', 2, re.compile('Last MS1\s+([0-9\.]+)')],
	'm_p_w': ['', 0, re.compile('')],
	# Ion details
	'i_i_t_ms1': ['Ion Injection Times for IDs', 1, re.compile('MS1 Median\s+([0-9\.]+)')],
	'i_i_t_ms2': ['Ion Injection Times for IDs', 3, re.compile('MS2 Median\s+([0-9\.]+)')],
	# Peptide details
	'p_c_pep': ['', 0, re.compile('')],
	'p_c_ion': ['', 0, re.compile('')],
	'p_c_ids': ['', 0, re.compile('')]	
}

# Paths (They should be adapted for the system they run on)
_IN_DIR = ''
_OUT_DIR = ''
_COPY_LOG = ''
_WEB_ROOT = os.path.normpath(r'E:\Web')
_NIST = os.path.normpath(r'C:\Users\nbic\Documents\NISTMSQCv1_2_0')
_PROGRES_LOG = 'qc_status.log'
_QC_HOME = os.path.normpath(r'E:\QC')

def monitor_input():
	print 'Version 0.0.2'
	"""Checks input directory for new RAW files to analyze, keeping track
	of all processed files. Once a new RAW file has been placed in this directory
	a report will be generated with this file as input."""
	global _RAW_FILE, _BASE_NAME, _OUT_DIR
	files = _read_logfile()

	# The following code should be threaded, one thread to monitor for new files
	# and another thread for each file to be processed
	files = _parse_robocopy_log(files)

	#sys.exit('Files: {0}'.format(files))
	if not files:
		sys.exit('No files to proces..')
		
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
	Rcmd = 'Rscript {0} {1} {2} {3}'.format(_R_GRAPHICS, '{0}\\{1}'.format(_OUT_DIR, _BASE_NAME), 
											'{0}\\images\\{1}'.format(_WEB_ROOT, _BASE_NAME), 1)
	_run_command(Rcmd)
	
def _create_metrics():
	""" Parses NIST metrics output file extracting relevant metrics subset """
	metrics_file = os.path.normpath('{0}\\{1}_report.msqc'.format(_OUT_DIR, _BASE_NAME))
	metrics_log_file = os.path.normpath('{0}.LOG'.format(metrics_file))
	print "\n### Metrics File: ", metrics_file

	# Extracting metrics from NIST report file
	with open(metrics_file, 'r') as f:
		nist_metrics = f.readlines()
		
	for metric in _METRICS.keys():
		index = next((num for num, line in enumerate(nist_metrics) if _METRICS[metric][0] in line), None)
		if index != None:
			print index
			result = _METRICS[metric][-1].search(nist_metrics[index + _METRICS[metric][1]])
			_METRICS[metric] = result.group(1)
			
	# Extracting metrics (MS1, MS2 scans) from NIST log file
	with open(metrics_log_file, 'f') as f:
		nist_metrics_log = f.readlines()
		
	_METRICS['ms1_spectra'] = re.search('([0-9]+) ms1 spectra').group(1)
	_METRICS['ms2_spectra'] = re.search('([0-9]+) ms2 spectra').group(1)
	
	# Other generic metrics
	_METRICS['f_size'] = "%0.1f" % (os.stat(_RAW_FILE).st_size/(1024*1024.0))

def _create_report():
	# Graphics to include
	heatmap = '{0}_heatmap.pdf'.format(_BASE_NAME)
	ion_cnt = '{0}_ions.pdf'.format(_BASE_NAME)

	# Place values and graphics in template HTML file
	with open('../web/templates/report.html', 'r') as f:
		template = f.readlines()
	
	report_template = Template(template)
	report_updated = report_template.safe_substitute(# General
													raw_file=_RAW_FILE,
													date='',
													time='',
													runtime='',
													# Metrics
													m_fs=_METRICS['f_size'],
													m_ms1_scans=_METRICS['ms1_spectra'],
													m_ms2_scans=_METRICS['ms2_spectra'],
													m_f_ms1_rt=_METRICS['f_ms1_rt'],
													m_l_ms1_rt=_METRICS['l_ms1_rt'],
													m_m_p_w=_METRICS['m_p_w'],
													m_i_i_t_ms1=_METRICS['i_i_t_ms1'],
													m_i_i_t_ms2=_METRICS['i_i_t_ms2'],
													m_p_c_pep=_METRICS['p_c_pep'],
													m_p_c_ion=_METRICS['p_c_ion'],
													m_p_c_ids=_METRICS['p_c_ids'],
													# Figures
													heatmap_img=heatmap,
													ions_img=ion_cnt)
	
	# Write report file to directory holding all reports
	with open('{0}/{1}_report.html'.format(_WEB_ROOT, _BASE_NAME), 'w') as f:
		f.writelines(report_updated)

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
	
	""" NO NEED, adapted the R package reading in the mzXML file (does not check for version anymore)
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