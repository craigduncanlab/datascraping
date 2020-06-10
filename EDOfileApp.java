
//Only use package reference if running from outside the folder
//package s19007617;

//import cits3001_2016s2.*;

import java.util.*;
import java.io.*;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.HashMap;
//for pattern matching:
import java.util.regex.Matcher;
import java.util.regex.Pattern;
//


/**
 * A file IO helper class for logging statistics to a csv file
 * This uses character codes for writing blocks of data to files
 * i.e. the write process takes characters in a program (represented as chars in memory or strings) and converts them to character codes.
 * They are the default character codes - so when read by other apps will be
 * interpreted correctly as, for example, a 'text' file.
 * This class is not suitable for writing binary data, or non-chars.
 * Last modified 14 April 2017
   Modified 18 Dec 2017 for EDO App
 * **/


public class EDOfileApp{

    //setup (declare) instance variables
    String filename="";

    public EDOfileApp(String filename){
      //object instance variables here

      //define filename using the passed parameter
      this.filename = filename;
      //this.addHeaderRow (5, new String[]{"Rnd","name","players", "spies","failures"});
      }

    /**
     * Method to add a header and a row entry to the file
     * @filename the text (csv) file to add a line to
     * @count the number of array entries to be added in the row
     * @values the array of entries to be added
     * */

     public void addHeaderRow(int count, String[] header) {
         FileWriter fws;
           try {
               fws = new FileWriter(new File(this.filename),false); //boolean append false = Overwrite file.
               //BufferedWriter bw = new BufferedWriter(fws);
               //or maybe a PrintWriter.   What IO or functions do these add to the FileWriter class?
               //Some of the differences to check are IO reporting, printing and buffering differences
  	            for (int y=0; y<count; y++) {
                fws.write(header[y]);
                if (y<(count-1)) {
                  fws.write(",");
                }
                }
                fws.write("\n");
                fws.flush();
                fws.close();
                //System.out.println("Done");
          } catch (IOException ex) {
              ex.printStackTrace();
          }
       }

     public void addRecord(int count, String[] values) {
       FileWriter fws;
         try {
             fws = new FileWriter(new File(this.filename),true); //boolean append
             //BufferedWriter bw = new BufferedWriter(fws);
	            for (int y=0; y<count; y++) {
               fws.write(values[y]);
               if (y<(count-1)) {
                 fws.write(",");
               }
              }
              fws.write("\n");
              fws.flush();
              fws.close();
              //System.out.println("Done");
        } catch (IOException ex) {
            ex.printStackTrace();
        }
     }

     public void addDelimRecord(int count, String[] values, String delim) {
       FileWriter fws;
         try {
             fws = new FileWriter(new File(this.filename),true); //boolean append
             //BufferedWriter bw = new BufferedWriter(fws);
              for (int y=0; y<count; y++) {
               fws.write(values[y]);
               if (y<(count-1)) {
                 fws.write(delim);
               }
              }
              fws.write("\n");
              fws.flush();
              fws.close();
              //System.out.println("Done");
        } catch (IOException ex) {
            ex.printStackTrace();
        }
     }

     /* add record with an EOL character */

     public void addOneRecord(String values) {
      FileWriter fws;
         try {
             fws = new FileWriter(new File(this.filename),true);
             fws.write(values);
             fws.write("\n"); //delimiter
             fws.flush();
             fws.close();
             } catch (IOException ex) {
            ex.printStackTrace();
        }
     }

     /* add text with no EOL character */

     public void addText(String values) {
      FileWriter fws;
         try {
             fws = new FileWriter(new File(this.filename),true);
             fws.write(values);
             //fws.write("\n"); //delimiter
             fws.flush();
             fws.close();
             } catch (IOException ex) {
            ex.printStackTrace();
        }
     }

     public void parseLinesCSV(String outputFile) {

      //String[] output = new String[];
      EDOfileApp myOutput = new EDOfileApp(outputFile);
      String output[] = new String[5];
      String line = "start";
      String tempLine = "";
      Boolean error = false;
      try {
        BufferedReader bufferedReader = new BufferedReader(new FileReader(this.filename));
      // TO DO: check if above line is ok or if it needs to work with file input stream

      // use the readLine method of the BufferedReader to read one line at a time.
      while ((line = bufferedReader.readLine())!=null) {
        if (line==null) {
          System.out.println("caught a null");
          break;
        }
        System.out.println(line);
        //split file but also ignore CSV to replace with new delimiter
        String myPattern="(^[0-9]+)[?=#](\\/Permit.*)([?=\\/]+)(.*\\.pdf)[?=#]([0-9]+)[?=#]([0-9]*)";
        Pattern p = Pattern.compile(myPattern);
        Matcher matcher = p.matcher(line);
        int groupCount = matcher.groupCount();
        while (matcher.find())
          {
          //System.out.println(matcher.group(1)+" group 2:"+matcher.group(2)+" group 3:"+matcher.group(3));
            for (int i = 1; i <= groupCount; i++) {
                // Group i substring
                System.out.println("Group " + i + ": " + matcher.group(i));
          }
        //TO DO:  get rid of these and just write as needed
        output[0]=matcher.group(1);
        output[1]=matcher.group(2);
        output[2]=matcher.group(4); 
        output[3]=matcher.group(5); 
        output[4]=matcher.group(6);  
        myOutput.addDelimRecord(5,output,"#"); //with delimiter
        }
      }
      //myOutput.close();
      bufferedReader.close();
      } catch(IOException e) {
                    error = true;
                    e.printStackTrace();
      } 
      //whatever parent object is ?? for outputFile.close();
      //

     }

     public void parseDuplicates(String outputFile) {

       //String[] output = new String[];
      EDOfileApp myOutput = new EDOfileApp(outputFile);
      String output[] = new String[6];
      String line = "start";
      String tempLine = "";
      Boolean error = false;
      try {
        BufferedReader bufferedReader = new BufferedReader(new FileReader(this.filename));
      // TO DO: check if above line is ok or if it needs to work with file input stream

      // use the readLine method of the BufferedReader to read one line at a time.
      while ((line = bufferedReader.readLine())!=null) {
        if (line==null) {
          System.out.println("caught a null");
          break;
        }
        System.out.println(line);
        //split file but also ignore CSV to replace with new delimiter
        String myPattern="(^[0-9]+)[?=#](\\/Permit\\/)([\\d]+)(.*[?=\\/]+)([Dd]+[Ee]+[Cc]+[Ii]+[Ss]+[Ii]+[Oo]+[Nn]+[ ]*[Rr]+[Ee]+[Pp]+[Oo]+[Rr]+[Tt]+[Ss]*\\.pdf)[?=#]([0-9]+)[?=#]([0-9]*)";
        Pattern p = Pattern.compile(myPattern);
        Matcher matcher = p.matcher(line);
        int groupCount = matcher.groupCount();
        while (matcher.find())
          {
          //System.out.println(matcher.group(1)+" group 2:"+matcher.group(2)+" group 3:"+matcher.group(3));
            for (int i = 1; i <= groupCount; i++) {
                // Group i substring
                System.out.println("Group " + i + ": " + matcher.group(i));
          }
        output[0]=matcher.group(1); //sequence
        //output[1]=matcher.group(2);
        output[1]=matcher.group(2)+matcher.group(3)+matcher.group(4); //folder path
        output[2]=matcher.group(5); //filename
        output[3]=matcher.group(7);//cumulative
        output[4]=matcher.group(6); //size
        output[5]=matcher.group(3); //this is the permit application number
        myOutput.addDelimRecord(6,output,"#"); //with delimiter - always output
        /*Boolean isIncluded = false;
        isIncluded=checkDecision(output[4]);
          if (isIncluded==true) {
            myOutput.addDelimRecord(6,output,"#"); //with delimiter
            break;
          }
          */
      }
      }
      //myOutput.close();
      bufferedReader.close();
      } catch(IOException e) {
                    error = true;
                    e.printStackTrace();
      } 
      //whatever parent object is ?? for outputFile.close();
      //

     }

     public void parseDecisionPDF(String outputFile) {

       //String[] output = new String[];
      EDOfileApp myOutput = new EDOfileApp(outputFile);
      String output[] = new String[6];
      String line = "start";
      String tempLine = "";
      Boolean error = false;
      try {
        BufferedReader bufferedReader = new BufferedReader(new FileReader(this.filename));
      // TO DO: check if above line is ok or if it needs to work with file input stream

      // use the readLine method of the BufferedReader to read one line at a time.
      while ((line = bufferedReader.readLine())!=null) {
        if (line==null) {
          System.out.println("caught a null");
          break;
        }
        System.out.println(line);
        //split file but also ignore CSV to replace with new delimiter
        String myPattern="(^[0-9]+)[?=#](\\/Permit\\/)([\\d]+)(.*[?=\\/]+)([Dd]+[Ee]+[Cc]+[Ii]+[Ss]+[Ii]+[Oo]+[Nn]+\\.pdf)[?=#]([0-9]+)[?=#]([0-9]*)";
        Pattern p = Pattern.compile(myPattern);
        Matcher matcher = p.matcher(line);
        int groupCount = matcher.groupCount();
        while (matcher.find())
          {
          //System.out.println(matcher.group(1)+" group 2:"+matcher.group(2)+" group 3:"+matcher.group(3));
            for (int i = 1; i <= groupCount; i++) {
                // Group i substring
                System.out.println("Group " + i + ": " + matcher.group(i));
          }
        output[0]=matcher.group(1); //sequence
        //output[1]=matcher.group(2);
        output[1]=matcher.group(2)+matcher.group(3)+matcher.group(4); //folder path
        output[2]=matcher.group(5); //filename
        output[3]=matcher.group(7);//cumulative
        output[4]=matcher.group(6); //size
        output[5]=matcher.group(3); //this is the permit application number
        myOutput.addDelimRecord(6,output,"#"); //with delimiter - always output
        /*Boolean isIncluded = false;
        isIncluded=checkDecision(output[4]);
          if (isIncluded==true) {
            myOutput.addDelimRecord(6,output,"#"); //with delimiter
            break;
          }
          */
      }
      }
      //myOutput.close();
      bufferedReader.close();
      } catch(IOException e) {
                    error = true;
                    e.printStackTrace();
      } 

     }

/* Method to locate filename information for the 'decision reports' 
*/

     public void parseDecisionReports(String outputFile) {

      //String[] output = new String[];
      EDOfileApp myOutput = new EDOfileApp(outputFile);
      String output[] = new String[5];
      String line = "start";
      String tempLine = "";
      Boolean error = false;
      try {
        BufferedReader bufferedReader = new BufferedReader(new FileReader(this.filename));
      // TO DO: check if above line is ok or if it needs to work with file input stream

      // use the readLine method of the BufferedReader to read one line at a time.
      while ((line = bufferedReader.readLine())!=null) {
        if (line==null) {
          System.out.println("caught a null");
          break;
        }
        System.out.println(line);
        //split file but also ignore CSV to replace with new delimiter
        String myPattern="(^[0-9]+)[?=#](\\/Permit.*)([?=\\/]+)(.*\\.pdf)[?=#]([0-9]+)[?=#]([0-9]*)";
        Pattern p = Pattern.compile(myPattern);
        Matcher matcher = p.matcher(line);
        int groupCount = matcher.groupCount();
        while (matcher.find())
          {
          //System.out.println(matcher.group(1)+" group 2:"+matcher.group(2)+" group 3:"+matcher.group(3));
            for (int i = 1; i <= groupCount; i++) {
                // Group i substring
                System.out.println("Group " + i + ": " + matcher.group(i));
          }
        output[0]=matcher.group(1);
        output[1]=matcher.group(2);
        output[2]=matcher.group(4); 
        output[3]=matcher.group(5); 
        output[4]=matcher.group(6);  
        Boolean isIncluded = false;
        isIncluded=checkDecision(output[2]);
          if (isIncluded==true) {
            myOutput.addDelimRecord(5,output,"#"); //with delimiter
          }
        }
      }
      //myOutput.close();
      bufferedReader.close();
      } catch(IOException e) {
                    error = true;
                    e.printStackTrace();
      } 
      //whatever parent object is ?? for outputFile.close();
      //

     }

     public Boolean checkDecision (String myFilename) {

      String myPattern="^.*[dD]+[Ee]+[Cc]+[Ii]+[Ss]+[Ii]+[Oo]+[Nn]+.*$";
      Pattern p = Pattern.compile(myPattern);
      Matcher matcher = p.matcher(myFilename);
      int groupCount = matcher.groupCount();
        while (matcher.find())
          {
            return true;
          }
        return false;
      }

     //read from CSV/text file 'filename', each row having 'count' records
     //the field structure is stored in a pre-defined class; referred to generically here as 'Object'
     /**
     public String[] readFile(int count, String filename) {

       FileReader frd;
       String[] values = new String[count];
         try {
             frd = new FileReader(new File(filename)); //boolean append
             for (int y=0; y<count; y++) {
              frd.read(values[y]);
              if (y<(count-1)) {
                frd.read(",");
              }
             }
             frd.flush();
             frd.close();
             System.out.println("Done");
           } catch (IOException ex) { //this will occur for IO error or end of file
               ex.printStackTrace();
           }
           return values;
     }
     */
       public static void main(String args[]){

       /**
       FileApp FW = new FileApp("testfileB.csv");
       int num=3;
       String[] toprow = {"ID","name","good"};
       String[] data = {"hello","there","butthead"};
       FW.addHeaderRow(num, toprow);
       FW.addRecord(num, data);
       FW.addRecord(num, new String[] {"how","are","you"});
       FW.addRecord(num, new String[] {"well","with","flushing"});
       */
       }


}
