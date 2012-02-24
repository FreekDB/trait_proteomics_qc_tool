library(fields)
library(readMzXmlData)

ms_image <- function(mzXML, pfile=FALSE, min.mz=300, max.mz=2000, n.bins=100, mslevel=1, n.peaks=500, method="max") {	
	## Creates a heatmap-like image of all scans from the experiment
	## Bins the data into n.bins bins and shows either the 'max' or 'average' of all bins

	window = (max.mz - min.mz) / n.bins
	s = c()
	for ( i in 1:((max.mz - min.mz) / window))
		s[i] = min.mz + window * (i-1)
    
    logger(paste("Processing data.. (min.mz: ", min.mz, ", max.mz: ", max.mz, ", window: ", window, ")\n", sep=""))
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
	print("Creating image")
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
	
	## Change margins
	par(mar=c(4,4,2,2))
	
	if (pfile != FALSE) {
	    ## Save image to both PDF and PNG files
	    png(paste(pfile, '_ions.png', sep=''), width = 800, height = 400)
	    bp = barplot(ions, rt, xlab="Scan number (RT)", ylab="Total Ion Count", main="Ion count per scan")
	    text(bp, par("usr")[3], labels=seq(min(rt), max(rt), length.out=10), srt=25, adj = c(1.1,1.1), xpd = TRUE, cex=.75)
	    dev.off()

	    pdf(paste(pfile, '_ions.pdf', sep=''))
	    bp = barplot(ions, rt, xlab="Scan number (RT)", ylab="Total Ion Count", main="Ion count per scan")
        text(bp, par("usr")[3], labels=seq(min(rt), max(rt), length.out=10), srt=25, adj = c(1.1,1.1), xpd = TRUE, cex=.75)
	    dev.off()
	}
	
	barplot(ions, rt, xlab="Scan number (RT)", ylab="Total Ion Count", main="Ion count per scan")
	
	logger(paste("Done creating total Ion count graph, saved as ", pfile, "_ions.[png|pdf]", sep=""))
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
ion_count(data, o_file, mslevel=msl) 
## Generating metrics
cat("\t\tGenerating metrics..\n")
ms_metrics(data)
## Write logfile
write(process, logfile)