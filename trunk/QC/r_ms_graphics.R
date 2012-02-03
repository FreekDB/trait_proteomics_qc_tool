library(fields)
library(readMzXmlData)

ms_image <- function(mzXML, PDF, bins=100, min.mz=300, max.mz=2000, mslevel=1, peaks=100, method="max") {	
	
	window = (max.mz - min.mz) / bins
	s = c()
	for ( i in 1:((max.mz - min.mz) / window))
		s[i] = min.mz + window * (i-1)

    logger("Creating heatmap image..")
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
	
	logger(paste("Done preparing data..", sep=""))
	
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
	
	## Change margins
	par(mar=c(4, 4, 2, 1))
	## Create image with legend
	image.plot(n, s, log(m), xlab="Scan number (RT)", ylab="mass", main="Spectra overview")
	
	if (PDF != FALSE)
		dev.off()
		
	logger(paste("Done creating heatmap, saved as ", PDF, "_heatmap.png", sep=""))
}

ion_count <- function(mzXML, PDF, mslevel) {
    logger("Creating total Ion count graph")
	ions = c()
	for (scan in 1:length(mzXML)) {
		# Only plot scans with at least one peak
		if (mzXML[[scan]]$metaData$peaksCount > 1 & 
			mzXML[[scan]]$metaData$msLevel == mslevel)
			ions = c(ions, sum(mzXML[[scan]]$spectrum$intensity))
	}
	
	if (PDF != FALSE)
		png(paste(PDF, '_ions.png', sep=''), width = 800, height = 400)
	## Change margins
	par(mar=c(4,4,2,2))
	plot(ions, type='l', xlab="Scan number (RT)", ylab="Total Ion Count", main="Ion count per scan")
	
	if (PDF != FALSE)
		dev.off()
	logger(paste("Done creating total Ion count graph, saved as ", PDF, "_ions.png", sep=""))
}

read_mzXML <- function(mzXML) {
	cat("\t\tReading mzXML file..\n")
	logger(paste("Reading mzXML file: ", mzXML, sep=""))
	data = readMzXmlFile(mzXML)
	logger(paste("Done processing mzXML file, read ", length(mzXML), " scans", sep=""))
	return(data)
}

ms_metrics <- function(mzXML) {
	logger("## Metrics ##")
    # Retrieve all MS levels from the nested list into a single list
    msLevel = unlist(lapply(mzXML, function(x) lapply(x, function(y) c(y$msLevel, y$peaksCount))))
	msLevel = matrix(msLevel, ncol=2, byrow=T)
    # Count MS levels
    ms1 = msLevel[which(msLevel[,1] == 1),]
    ms2 = msLevel[which(msLevel[,1] == 2),]
    logger(paste("Number of MS1 scans: ", length(ms1[,1]), sep=""))
    logger(paste("\tMS1 scans containing peaks: ", length(which(ms1[,2] > 0)), sep=""))
    logger(paste("Number of MS2 scans: ", length(ms2[,1]), sep=""))
    logger(paste("\tMS2 scans containing peaks: ", length(which(ms2[,2] > 0)), sep=""))
}

logger <- function(logdata) {
    process <<- c(process, paste(Sys.time(), "\t", logdata, sep=""))
}

####
args  = commandArgs(TRUE)

mzXML   = args[1] # Input mzXML filename
o_PDF   = args[2] # PDF path prefix
msl     = args[3] # MS level
logfile = args[4] # Log file for writing status and some metrics
process = c()

data = read_mzXML(mzXML)

## Creating heatmap of all data
cat("\t\tCreating heatmap image..\n")
ms_image(data, o_PDF, mslevel=msl)
## Creating total ion count plot of all data
cat("\t\tCreating total Ion count plot..\n")
ion_count(data, o_PDF, mslevel=msl) 
## Generating metrics
cat("\t\tGenerating metrics..\n")
ms_metrics(data)
## Write logfile
write(process, logfile)