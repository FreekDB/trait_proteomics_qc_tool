Installation
------------
- Add path to the 'Rscript.exe' command to $PATH (by default: "C:\Program Files\R\R-2.14.1\bin\x64")
- Install the following R packages by running:
	install.packages(c('fields', 'readMzXmlData'))
	
- Test the program by running it manually using:
	python robocopy_monitor.py [path_to_raw_files] [path_to_output_folder] [path_to_robocopy_logfile]

- To install as a service, edit robocopy.py changing the IN_DIR, OUT_DIR, COPY_LOG settings to reflect
  the proper paths. When done, install the service by running: 
	python robocopy_monitor.py install

- To see if the service is running, run:
	sc query [service_name] (ctmm_monitor by default)

- Remove the service by running:
	sc delete [service_name]

- Manage service startup options by running services.exe and browsing to the [service_name]


Running Robocopy
----------------

The QC monitor tool relies on robocopy to copy the files (for example from an acquisition system)
to the input folder. The log file created by robocopy is analyzed to scan for new files which triggers
the QC pipeline to generate a report. The following command performs a copy of new RAW files:
	robocopy "source_path" "\\destination_path" / Mir / R:10 / W:30 / Log + :"\\log_path\logfile.txt" / mot:1 / tee / np / njh / njs

The above command has the following options in effect:
	- "source_path": folder to monitor for new files
	- "destination_path": path to copy new files to
	- /Mir: Mirror
	- /R:10: 10 retries when failing
	- /W:30: wait 30 seconds between retries
	- /Log+:["path"]: log output to specified file (append mode)
	- /mot:1: monitor for new files every 1min
	- /tee: display output
	- /np: do not log progress
	- /njh: do not log job header
	- /njs: do not log job summary	
- Note: replace /Mir with /e if removing files from source but need to be kept in destination
