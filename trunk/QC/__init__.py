""" CTMM QC Package """

import logging
import sys

#Configure logging LOG_FORMAT
LOG_FORMAT = '%(levelname)s\t%(asctime)s %(module)s.%(funcName)s:%(lineno)d\t%(message)s'
LOG_DATE_FORMAT = '%H:%M:%S'

#Logs WARNING messages and anything above to sys.stdout
logging.basicConfig(level=logging.INFO, stream=sys.stdout, format=LOG_FORMAT, datefmt=LOG_DATE_FORMAT)
