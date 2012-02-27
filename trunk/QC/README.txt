Notes:

Installation:

- Add path to 'Rscript.exe' command to $PATH (by default: "C:\Program Files\R\R-2.14.1\bin\x64")

Robocopy command:
	(Mirror, 10 retries, waiting 30sec, monitor every 1min, display output, no progress, no job header, no job summary)
	-robocopy "source_path" "\\destination_path" / Mir / R:10 / W:30 / Log + :"\\log_path\logfile.txt" / mot:1 / tee / np / njh / njs
	- Note: replace \mir with \e if removing files from source but need to be kept in destination
