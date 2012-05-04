# Read commandline arguments
args  = commandArgs(TRUE)
if (length(args) < 4)
	stop(cat("Missing arguments, usage:\n\tRscript r_ms_graphics.R indir rawfilebasename outdir mslevel [heatmap|iongraph]\n"))

indir   = args[1]
rawbasename = args[2]
webdir   = args[3]
mslvl    = args[4]

# Set custom plot options
if (length(args) > 4) {
	graph = args[5]
} else {
	graph = c('heatmap', 'iongraph')
}

# Load required libraries
library(fields)
library(readMzXmlData)

ms_image <- function(mzXML, pfile=FALSE, min.mz=300, max.mz=2000, n.bins=100, mslevel=1, n.peaks=500, method="max") {	
	## Creates a heatmap-like image of all scans from the experiment
	## Bins the data into n.bins bins and shows either the 'max' or 'average' of all bins

	window = (max.mz - min.mz) / n.bins
	s = c()
	for ( i in 1:((max.mz - min.mz) / window))
		s[i] = min.mz + window * (i-1)
    
    logger(paste("Processing data.. (min.mz: ", min.mz, ", max.mz: ", max.mz, ", window: ", 
				  window, ", minimum number of peaks: ", n.peaks, ")", sep=""))
    logger("Creating heatmap image..")
	m = matrix(ncol=length(s))
	r = c()
	rt = c()
	for (scan in 1:length(mzXML)) {
	    ## Get data for all scans for the given ms level and with a minimum number of 'n.peaks' peaks. 
		if (mzXML[[scan]]$metaData$peaksCount > n.peaks & 
			mzXML[[scan]]$metaData$msLevel == mslevel) {
			r = bin.scan(mzXML[[scan]]$spectrum, s, window, min.mz, max.mz, method)
			m = rbind(m, r)
			## Register all retention times used for x-axis
			rt = c(rt, mzXML[[scan]]$metaData$retentionTime)
		}
	}
	m = m[-1,]

	logger(paste("Done preparing data..", sep=""))
    bin.plot(m, s, rt, pfile)
}

bin.scan <- function(scan, bins, window, min.mz, max.mz, method) {
    ## Combines all data from a bin given its left and right margin parameters
    ## using either the 'max' or 'average' method.
	val = NULL
	mz = scan$mass
	int = scan$intensity
	for (step in 1:length(bins)) {
		left = bins[step]
		right = left + window
		## Get all intensities for the current interval 
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
	## Plots the heatmap image(s)
	if (pfile != FALSE) {
	    ## Save image to both PDF and PNG files
		png(paste(pfile, '_heatmap.png', sep=''), width = 800, height = 800)
		## Change margins
		par(mar=c(4, 4, 2, 1))
    	image.plot(rt, s, log(m), xlab="Retention Time", ylab="mass", main="Spectra overview")
        dev.off()
        
        pdf(paste(pfile, '_heatmap.pdf', sep=''))
		## Change margins
		par(mar=c(4, 4, 2, 1))
    	image.plot(rt, s, log(m), xlab="Retention Time (s)", ylab="mass", main="Spectra overview")
    	dev.off()
	} else {
	    image.plot(rt, s, log(m), xlab="Retention Time (s)", ylab="mass", main="Spectra overview")
	}
	logger(paste("Done creating heatmap, saved as ", pfile, "_heatmap.[png|pdf]", sep=""))
}

ion_count <- function(mzXML, pfile=FALSE, mslevel=1) {
    ## The 'total Ion count' barplot shows the sum of the intensities for each 
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

	if (pfile != FALSE) {
	    ## Save image to both PDF and PNG files
	    png(paste(pfile, '_ions.png', sep=''), width = 800, height = 400)
		## Change margins
		par(mar=c(4,4,2,2))
	    plot(rt, ions, type="l", xlab="Retention Time (s)", ylab="Total Ion Count", 
			 main="Ion count per scan", col="blue", lwd=2)
	    dev.off()

	    pdf(paste(pfile, '_ions.pdf', sep=''))
		## Change margins
		par(mar=c(4,4,2,2))
	    plot(rt, ions, type="l", xlab="Retention Time (s)", ylab="Total Ion Count", 
			 main="Ion count per scan", col="blue", lwd=2)
	    dev.off()
	}
	
	barplot(ions, rt, xlab="Scan number (RT)", ylab="Total Ion Count", main="Ion count per scan")
	
	logger(paste("Done creating total Ion count graph, saved as ", pfile, "_ions.[png|pdf]", sep=""))
}

read_mzXML <- function(mzXML) {
	logger(paste("Reading mzXML file: ", mzXML, sep=""))
	data = readMzXmlFile(mzXML)
	logger(paste("Done processing mzXML file, read ", length(data), " scans", sep=""))
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
	cat(paste(Sys.time(), "\t", logdata, "\n", sep=""))
    progress <<- c(progress, paste(Sys.time(), "\t", logdata, sep=""))
}

### Main ###
mzXML  = paste(indir, '/', rawbasename, '.RAW.mzXML', sep="")
o_file = paste(webdir, '/', rawbasename, sep="")
logfile = paste(indir, '/', rawbasename, '.RLOG', sep="")

progress = c()

data = read_mzXML(mzXML)

## Creating heatmap of all data
if ('heatmap' %in% graph)
	ms_image(data, o_file, mslevel=mslvl)
## Creating total ion count plot of all data
if ('iongraph' %in% graph)
	ion_count(data, o_file, mslevel=mslvl)

## Generating metrics
ms_metrics(data)
## Write logfile
write(progress, logfile)