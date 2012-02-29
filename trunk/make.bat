pep8 QC > pep8.txt

nosetests --with-xunit
pylint --max-line-length=120 -f parseable --include-ids=y QC > pylint.txt