#!c:/Python27/python.exe

#Somehow we need this.
print ""

#TODO Print whatever footer you want here
#import os

from time import mktime, strptime
from datetime import datetime
"""
for key in sorted(os.environ):
    print '<b>{}</b>'.format(key), os.environ[key], '<br/>'
"""
_PROGRESS_LOG = 'qc_status.log'
def _read_logfile(status_log):
    data = []
    with open(status_log, 'r') as logfile:
        data = logfile.readlines()
    last_update = data[-1].strip().split('\t')
    t_start = strptime(last_update[0], '%Y-%m-%d %H:%M:%S.%f')
    t_diff = datetime.now() - datetime.fromtimestamp(mktime(t_start))
    t_diff = str(t_diff).split('.')[0]
    if last_update[2] == 'running':
        img = '<img border="0" src="/ctmm/report/images/check_icon.png" height="18">'
        logline = '{0} Currently analyzing <b><i>{1}</i></b> (active for: {2})'.format(img, last_update[1], t_diff)
    else:
        img = '<img border="0" src="/ctmm/report/images/warning_icon.png" height="18">'
        logline = '{0} Idle.. (inactive for: {1})'.format(img, t_diff)
    return logline

print '<div style="color: gray; font-size: 16px">'
print '<b>Status:</b> {0}'.format(_read_logfile(_PROGRESS_LOG))
print '</div>'

print """
<br /><br />
<div style="color: gray; font-size: x-small">
    <em>Powered by:</em><br />
    <a href="http://www.nbic.nl/support/brs">
        <img border="0" src="/ctmm/report/images/nbic_logo.png" height="25"></a>
</div>
"""
