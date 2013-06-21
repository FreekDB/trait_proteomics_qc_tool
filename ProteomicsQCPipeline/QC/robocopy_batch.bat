@echo OFF

set orig="\\path\to\source_dir"
set dest="\\path\to\destination_dir"
set log="\\path\to\robocopy_logfile"

@echo Copying from: %orig%
@echo Copying to:   %dest%
@echo Logging in:   %log%

robocopy %orig% %dest% *.RAW /E /R:10 /W:30 /Log+:%log% /mot:120 /tee /np /njs /njh