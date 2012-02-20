library(fields)
library(readMzXmlData)

ms_image <- function(mzXML, pfile=FALSE, min.rt=NULL, max.rt=NULL, n.bins=100, mslevel=1, n.peaks=100, method="max") {	
	## Creates a heatmap-like image of all scans from the experiment
	## Bins the data into n.bins bins and shows either the 'max' or 'average' of all bins
	
	if (is.null(min.rt))
	    min.rt = mzXML[[1]]$metaData$retentionTime
	if (is.null(max.rt))
	    max.rt = mzXML[[length(mzXML)]]$metaData$retentionTime
	    
	window = (max.rt - min.rt) / n.bins
	s = c()
	for ( i in 1:((max.rt - min.rt) / window))
		s[i] = min.rt + window * (i-1)

    logger("Creating heatmap image..")
	m = matrix(ncol=length(s))
	r = c()
	rt = c()
	for (scan in 1:length(mzXML)) {
	    ## Get data for all scans for the given ms level and with a minimum number of 'n.peaks' peaks. 
		if (mzXML[[scan]]$metaData$peaksCount > n.peaks & 
			mzXML[[scan]]$metaData$msLevel == mslevel) {
			r = bin.scan(mzXML[[scan]]$spectrum, s, window, min.rt, max.rt, method)
			m = rbind(m, r)
			## Register all retention times used for x-axis
			rt = c(rt, mzXML[[scan]]$metaData$retentionTime)
		}
	}
	m = m[-1,]
	
	logger(paste("Done preparing data..", sep=""))
	print("Creating image")
    bin.plot(m, s, rt, pfile)
}

bin.scan <- function(scan, bins, window, min.rt, max.rt, method) {
    ## Combines all data from a bin given its left and right margin parameters
    ## using either the 'max' or 'average' method.
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

bin.plot <- function(m, s, rt, pfile) {
	## Change margins
	par(mar=c(4, 4, 2, 1))

	if (pfile != FALSE) {
	    ## Save image to both PDF and PNG files
		png(paste(pfile, '_heatmap.png', sep=''), width = 800, height = 800)
    	image.plot(rt, s, log(m), xlab="Retention Time", ylab="mass", main="Spectra overview")
        dev.off()
        
        pdf(paste(pfile, '_heatmap.pdf', sep=''))
    	image.plot(rt, s, log(m), xlab="Retention Time", ylab="mass", main="Spectra overview")
    	dev.off()
	} else {
	    image.plot(rt, s, log(m), xlab="Retention Time", ylab="mass", main="Spectra overview")
	}
	logger(paste("Done creating heatmap, saved as ", pfile, "_heatmap.png", sep=""))
}

ion_count <- function(mzXML, pfile=FALSE, mslevel=1) {
    ## The 'total Ion count' plot shows the sum of the intensities for each 
    ## complete scan with the RT on the X-axis
    logger("Creating total Ion count graph")
	ions = c()
	rt = c()
	for (scan in 1:length(mzXML)) {
		# Only plot scans with at least one peak
		if (mzXML[[scan]]$metaData$peaksCount > 1 & 
			mzXML[[scan]]$metaData$msLevel == mslevel) {
			ions = c(ions, sum(mzXML[[scan]]$spectrum$intensity))
			rt = c(rt, mzXML[[scan]]$metaData$retentionTime)
        }
	}
	
	## Change margins
	par(mar=c(4,4,2,2))
	
	if (pfile != FALSE) {
	    ## Save image to both PDF and PNG files
	    png(paste(pfile, '_ions.png', sep=''), width = 800, height = 400)
	    plot(ions, type='l', xlab="Scan number (RT)", ylab="Total Ion Count", main="Ion count per scan")
	    dev.off()

	    pdf(paste(pfile, '_ions.pdf', sep=''))
	    plot(ions, type='l', xlab="Scan number (RT)", ylab="Total Ion Count", main="Ion count per scan")
	    dev.off()
	}
	
	plot(ions, type='l', xlab="Scan number (RT)", ylab="Total Ion Count", main="Ion count per scan")
	
	logger(paste("Done creating total Ion count graph, saved as ", pfile, "_ions.png", sep=""))
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
    ## Logs progress, adds a timestamp for each event
    process <<- c(process, paste(Sys.time(), "\t", logdata, sep=""))
}

####
args  = commandArgs(TRUE)

mzXML   = args[1] # Input mzXML filename
o_file  = args[2] # PNG/PDF path prefix
msl     = args[3] # MS level
logfile = args[4] # Log file for writing status and some metrics
process = c()

data = read_mzXML(mzXML)

## Creating heatmap of all data
cat("\t\tCreating heatmap image..\n")
ms_image(data, o_file, mslevel=msl)
## Creating total ion count plot of all data
cat("\t\tCreating total Ion count plot..\n")
#ion_count(data, o_file, mslevel=msl) 
## Generating metrics
cat("\t\tGenerating metrics..\n")
ms_metrics(data)
## Write logfile
write(process, logfile)