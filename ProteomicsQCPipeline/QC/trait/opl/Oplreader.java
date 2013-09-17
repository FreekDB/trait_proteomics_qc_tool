
/*Read more: http://javarevisited.blogspot.com/2011/12/parse-read-xml-file-java-sax-parser.html#ixzz2Stgyyjk1*/ 
package nl.ctmm.trait.opl; 

import java.io.*;
import java.util.ArrayList;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException; 
import org.xml.sax.helpers.DefaultHandler;

/**
 *
 * @author t.pham
 */

public class Oplreader  extends DefaultHandler {

    class xy {
        public double x;
        public double y;
        public xy(double _x, double _y) {
            x = _x;
            y = _y;        
        }
    }

    int ms1Spectra = 0;
    int ms2Spectra = 0;
    double maxIntensity = 0.0;
    ArrayList<xy> tic = new ArrayList<xy>();
    
    
    /**
     * @param args indir, rawbasename, webdir
     */
    
    /*The main method should accept three arguments: indir, rawbasename and webdir
     * on a line similar to r_ms_graphics.R
     * 
     * e.g. indir = E:\\qc-data\\temp-outputLTQ\\PH090924_HCW090828_293NE_P3A02z9egrd_QC
     * rawbasename = PH090924_HCW090828_293NE_P3A02
     * webdir = C:\\Program Files (x86)\\Apache Software Foundation\\Apache2.2\\htdocs\\ctmm\\2013\\May\\data01QE2_130409_OPL1013_CvA_Bonemarrow_TiOx_S5
     * 
     */
    
    public static void main(String[] args)  {
        String indir = args[0];
        String rawbasename = args[1];
        String webdir = args[2];
        String mzXML = indir + "/" + rawbasename + ".RAW.mzXML";
        String ticmatrix_file = webdir + "/" + rawbasename + "_ticmatrix.csv";
        String rlog_file = indir + "/" + rawbasename + ".RLOG"; 
        //Create an instance of this class; it defines all the handler methods
        Oplreader handler = new Oplreader();
        System.out.println("Indir = " + indir);
        System.out.println("rawbasename = " + rawbasename);
        System.out.println("webdir = " + webdir);
        System.out.println("mzXML = " + mzXML);
        System.out.println("output_file = " + ticmatrix_file);

        //Create a "parser factory" for creating SAX parsers
        SAXParserFactory spfac = SAXParserFactory.newInstance();
        //Now use the parser factory to create a SAXParser object
        SAXParser sp;
        try {
            sp = spfac.newSAXParser();
          //Finally, tell the parser to parse the input and notify the handler
            sp.parse(mzXML, handler);
        } catch (ParserConfigurationException | SAXException | IOException e) {
            e.printStackTrace();
        }
        handler.print(ticmatrix_file, rlog_file);
    }
    
    	/*
        * Every time the parser encounters the beginning of a new element,
        * it calls this method, which resets the string buffer
        */ 
       public void startElement(String uri, String localName,
                     String qName, Attributes attributes) throws SAXException {
              if (qName.equals("scan")) {
                     String msLevel = attributes.getValue("msLevel");
                     if (msLevel.equals("1")) {
                         ms1Spectra++;
                         String retentionTime = attributes.getValue("retentionTime");
                         String totIonCurrent = attributes.getValue("totIonCurrent");
                         double x = Double.parseDouble(retentionTime.substring(2, retentionTime.length()-1));
                         double y = Double.parseDouble(totIonCurrent);
                         if (y > maxIntensity) {
                             maxIntensity = y;
                         }
                         tic.add(new xy(x, y));
                     }
                     else {
                         if (msLevel.equals("2")) {
                             ms2Spectra++;
                        }  
                     }
              }
       }       

    private void print(String ticmatrix_file, String rlog_file) {
              System.out.println("ms1Spectra   = " + ms1Spectra);
              System.out.println("ms2Spectra   = " + ms2Spectra);
              System.out.println("maxIntensity = " + maxIntensity);
          	try {
            	//Save reportUnit values to .csv file
                FileWriter fWriter = new FileWriter(ticmatrix_file);
                BufferedWriter bWriter = new BufferedWriter(fWriter);
    			//first line in _ticmatrix.csv file: "rt","ions"
    			bWriter.write("\"rt\",\"ions\"\n");
    			for(xy item: tic) {
    				bWriter.write(item.x + "," + item.y + "\n");
     			}
    			bWriter.close();
    			fWriter.close();
    			fWriter = new FileWriter(rlog_file);
    			bWriter = new BufferedWriter(fWriter);
    			bWriter.write("Number of MS1 scans: " + ms1Spectra + "\n");
    			bWriter.write("Number of MS2 scans: " + ms2Spectra + "\n");
    			bWriter.write("maxIntensity: " + maxIntensity + "\n");
    			bWriter.close();
    			fWriter.close();
    		} catch (IOException e) {
    			e.printStackTrace();
    		}
    }
}
