Configuration Instructions
--------------------------

The configuration file contained in this folder defines the metrics shown in the report 
as well as the order of appearance. Refer to XXXX to identify the metrics included in
the configuration file using their identifier.

There are three sections in the configuration file, namely 'top', 'bottom' and 'hidden'.
The 'top' and 'bottom' sections define two tables that are displayed separately in the 
report. Metrics of less importance can be placed in this 'bottom' table to keep a more
clean overview of the metrics that matter most.

Metrics placed in the 'hidden' section will not be displayed at all.


Notes
-----

Changing the configuration will affect all previously generated reports as well.

When changing the configuration, please make sure that it is syntactically valid JSON
code. If unsure, use a validator (such as http://jsonlint.com/) to verify the syntax. All metric ID's
should be quoted and the line must end with a comma if it is not the last item in the
above described sections.