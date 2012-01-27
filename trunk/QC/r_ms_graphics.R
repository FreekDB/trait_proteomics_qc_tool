library(fields)
library(readMzXmlData)

ms_image <- function(mzXML, PDF, bins=100, min.mz=300, max.mz=2000, mslevel=1, peaks=100, method="max") {	
	
	window = (max.mz - min.mz) / bins
	s = c()
	for ( i in 1:((max.mz - min.mz) / window))
		s[i] = min.mz + window * (i-1)

	m = matrix(ncol=length(s))
	r = c()
	n = c()
	for (scan in 1:length(mzXML)) {
		if (mzXML[[scan]]$metaData$peaksCount > 500 & 
			mzXML[[scan]]$metaData$msLevel == mslevel) {
			n = c(n, scan)
			r = bin.scan(mzXML[[scan]]$spectrum, s, window, min.mz, max.mz, method)
			m = rbind(m, r)
		}
	}
	m = m[-1,]
	bin.plot(m, s, n, PDF)
}

bin.scan <- function(scan, bins, window, min.mz, max.mz, method) {
	val = NULL
	mz = scan$mass
	int = scan$intensity
	for (step in 1:length(bins)) {
		left = bins[step]
		right = left + window
		
		index = which(left <= mz & if (step < length(bins)) mz < right else mz <= right)
		if (length(index) > 0) {
			if (method == "max")
				val[step] = max(int[index])
			if (method == "avg")
				val[step] = mean(int[index])
		} else {
			val[step] = 1
		}
	}
	
	return(val)
}

bin.plot <- function(m, s, n, PDF=FALSE) {
	## Create a grayscale, NOTE: unused
	low <- col2rgb("black")/255
	high <- col2rgb("white")/255
	r <- seq(low[1], high[1], len=50)
	g <- seq(low[2], high[2], len=50)
	b <- seq(low[3], high[3], len=50)
	pallette <- rgb(r, g, b)
	if (PDF != FALSE)
		png(paste(PDF, '_heatmap.png', sep=''), width = 800, height = 800)
	
	## Create image with legend
	image.plot(s, n, log(t(m)), xlab="mass", ylab="Scan number")
	
	if (PDF != FALSE)
		dev.off()
}

ion_count <- function(mzXML, PDF, mslevel) {
	ions = c()
	for (scan in 1:length(mzXML)) {
		# Only plot scans with at least one peak
		if (mzXML[[scan]]$metaData$peaksCount > 1)
			ions = c(ions, sum(mzXML[[scan]]$spectrum$intensity))
	}
	
	if (PDF != FALSE)
		png(paste(PDF, '_ions.png', sep=''), width = 800, height = 400)
	
	plot(ions, type='l', xlab="Scans (RT)", ylab="Total Ion Count")
	
	if (PDF != FALSE)
		dev.off()
}

read_mzXML <- function(mzXML) {
	cat("\t\tReading mzXML file..\n")
	return(readMzXmlFile(mzXML))
}

####
args  = commandArgs(TRUE)

cat("\n\n")
print(args)
cat("\n\n")
mzXML = args[1] # Input mzXML filename
o_PDF = args[2] # PDF path prefix
msl   = args[3] # MS level



data = read_mzXML(mzXML)

## Creating heatmap of all data
cat("\t\tCreating heatmap image..\n")
ms_image(data, o_PDF, mslevel=msl)
## Creating total ion count plot of all data
cat("\t\tCreating total Ion count plot..\n")
ion_count(data, o_PDF, mslevel=msl) 
