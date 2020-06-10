
/* 


18.12.2017
This is a client program to retrieve files from a directory structure on an FTP server.
It will use a prepared list of files and URL paths.

The file format should be:
path,filename (including extension)
The base server address will be added
So will ftp://prefix if required to convert to URL?

*/

import org.apache.commons.net.ftp.*;
import org.apache.commons.io.*;  //copyURLtoFile
import java.io.IOException;
import java.io.*;
import java.util.*; //arraylist
import java.net.URL; //object for the copyURLtoFile arguments
//for pattern matching:
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class EDOfiletransfers {

//EDOfileApp fileWrite = new EDOfileApp("permits.txt");

//constructor
public EDOfiletransfers() {

}

/* transfer using Apache Commons io and some timeouts
source - the URL to copy bytes from, must not be null
destination - the non-directory File to write bytes to (possibly overwriting), must not be null
connectionTimeout - the number of milliseconds until this method will timeout if no connection could be established to the source
readTimeout - the number of milliseconds until this method will timeout if no data could be read from the source

public static void copyURLToFile(URL source,
                                 File destination,
                                 int connectionTimeout,
                                 int readTimeout)
                          throws IOException

*/

public void transfer(String transferList) {
        
        String server = "ftp://ftp.dec.wa.gov.au"; //no ftp:// prefix
        boolean error = false;
        FileUtils myFileUtil= new FileUtils();
        int connectTO=3000;
        int readTO=11000;
        String[] values = new String[4];
        String line = "start";
        String fname="";
        try {
        
        BufferedReader bufferedReader = new BufferedReader(new FileReader(transferList));
        while ((line = bufferedReader.readLine())!=null) {
        if (line==null) {
          System.out.println("caught a null");
          break;
        }
        //split line
        values=this.splitLine(line);
        String path = values[1];
        fname = values[2];
        //assemble filenames for output from input line 
        String thisURL = server+path+"/"+fname;
        URL myURL = new URL(thisURL);
        String thisFile = "downloads/"+fname;
        //if refusal copy to different folder
        Boolean isRefused=false;
            isRefused=checkRefused(path); //check folder
            if (isRefused==true) {
               thisFile="refusals/"+fname;
            }
        File myDestination = new File(thisFile);
        //do transfer
        myFileUtil.copyURLToFile(myURL,myDestination,connectTO,readTO);
        }
        bufferedReader.close();
    } catch (IOException ex) { //this will occur for IO error or end of file
               ex.printStackTrace(); //<--- stop
               //print and continue processing
               System.out.println("Error (I/O) occurred while parsing file : " + fname);
           }
          
}

/* Method to transfer duplicates and rename 
Relies on the transfer list having same sequence as regular list with the CPS 
application number being added on at end as index position 6
*/

public void transferduplicates(String transferList) {
        
        String server = "ftp://ftp.dec.wa.gov.au"; //no ftp:// prefix
        boolean error = false;
        FileUtils myFileUtil= new FileUtils();
        int connectTO=3000;
        int readTO=11000;
        String[] values = new String[4];
        String line = "start";
        String fname="";
        try {
        
        BufferedReader bufferedReader = new BufferedReader(new FileReader(transferList));
        while ((line = bufferedReader.readLine())!=null) {
        if (line==null) {
          System.out.println("caught a null");
          break;
        }
        //split line
        values=this.splitLine(line);
        String path = values[1];
        fname = values[2];
        //assemble filenames for output from input line 
        String thisURL = server+path+"/"+fname;
        URL myURL = new URL(thisURL);
        String newfilename=fname = "CPS "+values[5]+" (RENAME)"+values[2];
        String thisFile = "downloads/"+newfilename;
        //if refusal copy to different folder
        Boolean isRefused=false;
            isRefused=checkRefused(path); //check folder
            if (isRefused==true) {
               thisFile="refusals/"+fname;
            }
        File myDestination = new File(thisFile);
        //do transfer
        myFileUtil.copyURLToFile(myURL,myDestination,connectTO,readTO);
        }
        bufferedReader.close();
    } catch (IOException ex) { //this will occur for IO error or end of file
               ex.printStackTrace(); //<--- stop
               //print and continue processing
               System.out.println("Error (I/O) occurred while parsing file : " + fname);
           }
          
}


/* Method to check if folder name includes 'refused' or similar 
Also remember to transfer CPS 920 Decision Report as this isn't in a 'refused' folder
*/

public Boolean checkRefused (String myFoldername) {

      String myPattern="^.*[Rr]+[Ee]+[Ff]+[Uu]+[Ss]+.*$";
      Pattern p = Pattern.compile(myPattern);
      Matcher matcher = p.matcher(myFoldername);
      int groupCount = matcher.groupCount();
        while (matcher.find())
          {
            return true;
          }
        return false;
      }

/* Method to split line of file containing path and filename */

public String[] splitLine(String filename) {

       String output[] = new String[6];  //should only need 2
       Scanner linescan = new Scanner(filename); //not file no try and catch needed
       linescan.useDelimiter("#"); //don't use comma as some filenames contain commas
       //read
       int index=0;
       while (linescan.hasNext()) {
           output[index] = linescan.next();
           index++;
       }
       linescan.close();
       return output;
     }
}
