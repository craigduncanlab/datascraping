
/* 


29.12.17
This app is to read XML files in a directory and process them into a new form of succinct XML
*/

//import org.apache.commons.net.ftp.*;
//import java.NIO.*;  // the New IO classes
import java.io.IOException;
import java.util.*; //arraylist


public class EDOmain3 {

//constructor
public EDOmain3() {

}

/* By default the directory (folder) to be processed is test
Config: 2 argument.  Letter codes for choosing folders; XML output toggle. 
TO DO: add additional options for turning logging mode on or off
 */

public static void main(String[] args) {
<<<<<<< HEAD
        XMLapp myXML = new XMLapp(); 
        long start=System.currentTimeMillis();   
        myXML.processDirectory("xml_PDF_searchable");
        Long end=System.currentTimeMillis();
        Long delay = end-start;
        Long unitsecs = delay/1000;
        Long secsPerDoc = delay/2089;
        Long DPSec = 2089/unitsecs;
=======
        String myFile="test";
        String folderCode="";
        Boolean myXMLoutput=false;
        String toggleCode="";
        if (args.length==1) {
            //System.out.println("length:"+args.length+","+args[args.length-1]);
            folderCode=args[0];
        }

        if (args.length==2) {
            //System.out.println("length:"+args.length+","+args[args.length-1]);
            folderCode=args[0];
            toggleCode=args[1];
        }
        //System.exit(0);

        //TO DO: outputfile option "-o" and next argument is filename or o= and startsWith

        switch (folderCode) {
            case "help":  help(); 
            break;
            case "test":  myFile = "test";
            break;
            case "s":   myFile ="xml_PDF_searchable";
            break;
            case "ns": myFile = "xml_PDF_notsearchable";
            break;
            case "xml":  myFile="test"; myXMLoutput=true;
            break;
            default:   myFile = "test"; myXMLoutput=false;
        }

        switch (toggleCode) {
            case "xml": myXMLoutput=true;
            break;
            default:   ;
        }
    
        //exit on help
        if (folderCode.equals("help")) {
            System.exit(0);
        }

        long start=System.currentTimeMillis();  
        XMLapp myXMLapp = new XMLapp(myXMLoutput);  //or use set method inside XMLapp
        int NumDocs=myXMLapp.processDirectory(myFile);
        //Record time taken
        Long end=System.currentTimeMillis();
        Long delay = end-start;
        Long unitsecs = delay/1000;
        Long secsPerDoc = delay/NumDocs;
        Long DPSec=0L;
        if (unitsecs>0) {
            DPSec = NumDocs/unitsecs;
        }
        String docstring = Long.toString(NumDocs);
>>>>>>> reggy
        String timeOut=Long.toString(delay);
        String secsOut=Long.toString(unitsecs);
        String unittimeOut=Long.toString(secsPerDoc);
        String DPSOut=Long.toString(DPSec);
        System.out.println("Process time (millseconds): "+timeOut);
        System.out.println("Process time (seconds): "+secsOut);
<<<<<<< HEAD
        System.out.println("Milliseconds per document (2089):"+unittimeOut);
        System.out.println("Documents per second (2089):"+DPSOut);
        //myXML.processDirectory("test");
=======
        System.out.println("Documents processed:"+docstring);
        System.out.println("Milliseconds per document:"+unittimeOut);
        System.out.println("Documents per second :"+DPSOut);
        //nb disk access may be slowest part of process (logging, reading etc)
        
    }

    public static void help() {
        System.out.println ("\n\nHelp for EDOmain3:");
        System.out.println ("------------------");
        System.out.println ("Default folder is 'test'");
        System.out.println ("Folder codes:");
        System.out.println ("s for folder xml_PDF_searchable");
        System.out.println ("ns for folder xml_PDF_notsearchable");
        System.out.println ("Example: java EDOmain3 s\n");
        System.out.println ("XML output mode toggle:");
        System.out.println ("Default is off.  xml for on.");
        System.out.println ("Example : java EDOmain3 s xml");
        System.out.println ("Example2: java EDOmain3 xml   (no folder code = xml mode, test folder)");
>>>>>>> reggy
    }
}
