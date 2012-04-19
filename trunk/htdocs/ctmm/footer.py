#!c:/Python27/python.exe

#Somehow we need this.
print ""

#TODO Print whatever footer you want here
import os
for key in sorted(os.environ):
	print '<b>{}</b>'.format(key), os.environ[key], '<br/>'

print """
<div style="color: gray; font-size: x-small">
	<em>Powered by:</em><br />
	<a href="http://www.nbic.nl/support/brs">
		<img border="0" src="/ctmm/report/images/nbic_logo.png" height="25"></a>
</div>
"""
