
/* 


29.12.17
This app is to read XML files in a directory and process them into a new form of succinct XML
*/

//import org.apache.commons.net.ftp.*;
import java.nio.file.*;  // the New IO classes
import java.io.*;
import java.io.IOException;
import java.util.*; //arraylist
//


public class XMLapp {

Boolean XMLoutput=false;

//constructor
public XMLapp() {

}

//XML option
public XMLapp(Boolean myBool) {
  this.XMLoutput = myBool;
}

/* Method to iterate through Files in a given Path

The Path object has several useful methods for working with Path(s) and filenames */

public void start(){

	/* WIP
	Path myPath = FileSystems.getDefault().getPath("/xml_PDF_searchable");
	//Files myFile = FileSystems.getDefault().getFile

	//walk returns a stream cf walkfilestream
	Files myFile = Files.walk(myPath)  
     .filter(Files::isRegularFile)
     .forEach(System.out::println);
     */
}

/*   Method to process all files in given directory.
Sub-directories are not processed, but are printed to System output.

Some ideas obtained from here:
https://stackoverflow.com/questions/5694385/getting-the-filenames-of-all-files-in-a-folder 

*/

public int processDirectory(String directory) {

	File folder = new File("./"+directory); 
	File[] listOfFiles = folder.listFiles();

    for (int i = 0; i < listOfFiles.length; i++) {
      if (listOfFiles[i].isFile()) {
      
        extractPDFXML myParser = new extractPDFXML(XMLoutput);
        myParser.parseFile(directory,listOfFiles[i].getName());

      } else if (listOfFiles[i].isDirectory()) {
        System.out.println("Directory " + listOfFiles[i].getName());
      }
    }
    return listOfFiles.length;
}

public void setXMLoutput(Boolean myBool) {
  this.XMLoutput=myBool;
}

public static void main(String[] args) {
        
    }
}