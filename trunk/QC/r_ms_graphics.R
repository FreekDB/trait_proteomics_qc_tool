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

ms_image <- function(mzXML, pfile=FALSE, min.mz=300, max.mz=2000, n.bins=100, mslevel=1, 
		     n.peaks.list=c(500, 250, 100, 50, 20, 10), n.peaks.perc=40, method="max") {	
	## Creates a heatmap-like image of all scans from the experiment
	## Bins the data into n.bins bins and shows either the 'max' or 'average' of all bins
	n.peaks = n.peaks.list[1]
	window = (max.mz - min.mz) / n.bins
	s = c()
	for ( i in 1:((max.mz - min.mz) / window))
		s[i] = min.mz + window * (i-1)
	
	logger(paste("Processing data for heatmap (min.mz: ", min.mz, ", max.mz: ", max.mz, ", window: ", 
					window, ", minimum number of peaks/scan: ", n.peaks, ")", sep=""))
	m = matrix(ncol=length(s))
	r = c()
	rt = c()
	has.data = 0
	ms.scans = 0
	for (scan in 1:length(mzXML)) {
		## Get data for all scans for the given ms level and with a minimum number of 'n.peaks' peaks. 
		if (mzXML[[scan]]$metaData$msLevel == mslevel) { 
			ms.scans = ms.scans + 1
			if (mzXML[[scan]]$metaData$peaksCount > n.peaks) {
				r = bin.scan(mzXML[[scan]]$spectrum, s, window, min.mz, max.mz, method)
				m = rbind(m, r)
				## Register all retention times used for x-axis
				rt = c(rt, mzXML[[scan]]$metaData$retentionTime)
				has.data = has.data + 1
			}
		}
	}
	## If < n.peaks_perc of the scans have more than n.peaks, recursively step through the peak limits in n.peaks_list
	print(ms.scans)
	if (has.data > 0)
		has.data.perc = round(has.data / ms.scans * 100, 2)
	else
		has.data.perc = 0
	if (has.data.perc < n.peaks.perc) {
		if (length(n.peaks.list) > 1) {
			logger(paste("Less than ", n.peaks.perc, "% (", has.data.perc, "%) of the scans have more than ",
				     n.peaks, " peaks present, trying again with ", n.peaks.list[2], " peaks", sep=""))
			ms_image(mzXML, pfile, min.mz, max.mz, n.bins, mslevel, n.peaks.list[2:length(n.peaks.list)], 
				 n.peaks.perc, method)
		} else {
			logger(paste("Less than ", n.peaks.perc, "% (", has.data.perc, "%) of the scans have more than ",
				     n.peaks, " peaks, stopping", sep=""))
			return(FALSE)
		}
	} else {
		logger(paste(has.data, " scans with more than ", n.peaks, " peaks have been found (", has.data.perc, 
			     "% out of a total of ", ms.scans, " ms", mslevel," scans)", sep=""))

		m = m[-1,]
		## Remove columns holding no useful data
		index = c()
		for (col in 1:ncol(m)) {
			if (length(unique(m[,col])) == 1)
				index = c(index, col)
		}
		if (!is.null(index)) {
		## Remove (-)Inf occurrences
		m <- log(m[,-index])
		s <- s[-index]
	    } else {
		    m <- log(m)
	    }
	    m[is.infinite(m)] <- 0

	    logger(paste("Done preparing data, creating heatmap image..", sep=""))
	    bin.plot(m, s, rt, pfile, method)
	}
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
			val[step] = 0
		}
	}
	return(val)
}

bin.plot <- function(m, s, rt, pfile, method) {
	## Plots the heatmap image(s)
	if (pfile != FALSE) {
		## Save image to both PDF and PNG files
		png(paste(pfile, '_heatmap.png', sep=''), width = 800, height = 800)
		image_plot(rt, s, m, method)
		dev.off()
		pdf(paste(pfile, '_heatmap.pdf', sep=''))
		image_plot(rt, s, m, method)
		dev.off()
	} else {
		image_plot(rt, s, m, method)
	}
	logger(paste("Done creating heatmap, saved as ", pfile, "_heatmap.[png|pdf]", sep=""))
}

image_plot <- function(rt, s, m, method) {
	## Creates an heatmap like image of the data together with a legend
	op <- par(mar=c(4, 4, 2, 1))
	image.plot(rt, s, m, xlab="Retention Time", ylab="Mass", main="Spectra Overview", 
			legend.args=list(text=paste(method, "of intensities (log scaled)"), 
					side=4, line=2))
	box()
	par(op)
}

ion_count <- function(mzXML, pfile=FALSE, mslevel=1) {
    #Find size of mzXML
    mzxmlSize <- object.size(mzXML)
    logger(paste("Size of mzXML object = ", mzxmlSize))
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
	    #Write graph coordinates to the CSV file for use by QC Report Viewer
        write.csv(cbind(rt, ions), file = paste(pfile, '_ticmatrix.csv'), row.names = FALSE)
		## Save image to both PDF and PNG files
		png(paste(pfile, '_ions.png', sep=''), width = 800, height = 400)
		ion_count_plot(ions, rt)    
		dev.off()   
		pdf(paste(pfile, '_ions.pdf', sep=''))
		ion_count_plot(ions, rt)
		dev.off()
	}	
	
	logger(paste("Done creating total Ion count graph, saved as ", pfile, "_ions.[png|pdf]", sep=""))
}

ion_count_plot <- function(ions, rt) {
	## Change margins
	op <- par(mar=c(4,6,2,2))
	plot(rt, ions, type="l", xlab="", ylab="", 
			main="Ion count per scan", col="blue", lwd=1.25, axes=FALSE)
	axis(2, las=2, cex.axis=0.8)
	loc = pretty(min(rt):max(rt), n=8)
	axis(1, at=loc, cex.axis=0.8)
	mtext("Total Ion count", side=2, line=5)
	mtext("Retention Time", side=1, line=2)
	box()
	par(op)
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
logger(paste("Writing logfile to ", logfile, sep=""))
write(progress, logfile)