
/* 


15.12.2017
This is an FTPClient app that will utilise Apache Commons Net libraries for convenience.
https://commons.apache.org/proper/commons-net/

They are installed in a series of subfolders inside a 'java' folder.

Place this file in the main 'java' folder supplied by the Commons Net java packages.

The SocketClient.java file is located there, and as the FTPClient extends SocketClient it will easily find it.
Each file has a 'package' command that helps identify its folder path for the Java compiler.
Any specific classes required by this App should use the 'import' command but the dependencies of those files should be found by the Java compiler without further info.
Import using * to get all files in the ftp folder; otherwise specify the FTPClient.java file directly
*/

import org.apache.commons.net.ftp.*;
import java.io.*;
import java.io.IOException;
import java.util.*; //arraylist

public class EDOutil {

myQ queue = new myQ();
ArrayList<EDOfiledata> filenameA = new ArrayList<EDOfiledata>(); //needed or use FTPFile[]?

//constructor
public EDOutil() {

}


public static void main(String[] args) {
        /* Utility to fix and split out filenames from the FTP server file list
        //The input file has the full file path as generated from FTP server by the java app EDOclient.java
        EDOfileApp myParsedFile = new EDOfileApp("fullpathsonly.txt");  
        myParsedFile.parseLinesCSV("newfilenameset.txt"); 
        */

        /*utility to reduce the file list to only the 'decision reports' 
        (already prepared with # delimiter)
        EDOfileApp myParsedFile = new EDOfileApp("fullpathsonly.txt"); 
        myParsedFile.parseDecisionReports("transferlist_DR.txt");
        */

        /* Conduct transfers from the prepared file list 
        EDOfiletransfers myMover = new EDOfiletransfers();
        myMover.transfer("transferlist.txt");
        */

        /*utility to include only the 'decision report.pdf' duplicates in the file transfer list
        EDOfileApp myParsedFile = new EDOfileApp("fullpathsonly.txt"); 
        myParsedFile.parseDuplicates("transferlist_duplicates.txt");
        */

        /* Conduct transfers from a file list of duplicate 'decision reports' 
        EDOfiletransfers myMover = new EDOfiletransfers();
        myMover.transferduplicates("transferlist_duplicates.txt");
        */

        /*utility to include only the 'decision.pdf' files/duplicates in the file transfer list 
        EDOfileApp myParsedFile = new EDOfileApp("fullpathsonly.txt"); 
        myParsedFile.parseDecisionPDF("transferlist_decision_dups.txt");
        */

        /* Conduct transfers from a file list of duplicate 'decision.pdf' reports  */
        EDOfiletransfers myMover = new EDOfiletransfers();
        myMover.transferduplicates("transferlist_decision_dups.txt");
        
    }
}
