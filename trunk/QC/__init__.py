""" CTMM QC Package """

import logging
import sys

__author__ = "Marcel Kempenaar"
__contact__ = "brs@nbic.nl"
__copyright__ = "Copyright, 2012, Netherlands Bioinformatics Centre"
__license__ = "MIT"

#Configure logging format
LOG_FORMAT = '%(levelname)s\t%(asctime)s %(module)s.%(funcName)s:%(lineno)d\t%(message)s'
LOG_DATE_FORMAT = '%H:%M:%S'

#Logs WARNING messages and anything above to sys.stdout
logging.basicConfig(level=logging.DEBUG, stream=sys.stdout, format=LOG_FORMAT, datefmt=LOG_DATE_FORMAT)
