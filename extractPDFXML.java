
/* Class to help parse the XML files created by Adobe Acrobat 

Each instance is created with information about the directory and filename of the current file.

The main purpose of the class is to write the 'output.txt' file that contains information structured by this class.
It also writes helper log files to cross-reference the fields written to output with success/failure for each file.

This class achieves its goal by building patterns, and navigating through the PDF with the help of designed Fields (these reflect actual PDF content but are pattern base to allow for OCR errors, and human inconsistencies in content, as well as non-printing characters in the text embedded in the PDF)
Rather than navigate PDFs directly, it works with XML files that are exported from the PDF (save as in Adobe Acrobat).  These are generally equivalent to the text of the PDF, and have the advantage of having PDF and XMP metadata.

To build patterns, instance variables with the Field and Values Patterns are generally used.
Where possible, all other patterns used to define spacer text, or particular boundaries in the XML files are encapsulated in the XMLsearch API class.
In this class, only one instance of the XMLsearchAPI is used (i.e. a singleton), called mySearch.

This class is instantiated with only one file reference.  It passes this on to mySearch, which then performs any pattern matching against a String representing the contents of that file.

mySearch will generally return a string to this object that is a first match to the pattern specified.
There are some exceptions to this: the FindWithin method returns a count of the matches within a predefined structure.
The ResultsStruct object is used for returning both text and integer information.

In mySearch navigation is generally not done by absolute position in file: the fields are used to identify a relative position, and then further pattern matching occurs.
The function names in XMLsearchAPI have been written to be as descriptive of the navigation and pattern-matching goals as possible.

File work history:

The Apple Adobe Acrobat creates line ends after <XML> tags using \\x0D\\x0A
Created 29.12.17 by Craig Duncan.   
Split out XMLsearchAPI.java on 2.1.18.
Since then, regex has been contained in XMLsearchAPI as much as possible.
9.1.18
Performance tips: any reused, mutable String should be a StringBuilder object.
StringBuilder objects can be initialised with a capacity or initial string values.
In C, Strings actually take up quite a lot less memory.  Additional structs only created as needed.
Another option is to write a class that works with Chars and convert to string as needed.
10.1.18 
Have included Applicant and not just Proponent too see e.g. CPS 7091_1.  Word "Granted" sometimes used instead of Grant originates in 2014 or 2015.
TO DO:  continue to break this file down into smaller methods, encapsulate as private methods.

*/

import java.util.*;
import java.io.*;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.HashMap;
//for regex pattern matching:
import java.util.regex.Matcher;
import java.util.regex.Pattern;
//buffered reader to string (Java 8 and 9)
import java.util.stream.Collectors;
//

public class extractPDFXML {
	
  //for buffers
  String searchFile="";
  XMLsearchAPI mySearch;
  //regexp - any way to hide this in OOP?
  String Uni_sgl_qt = "\\u0027\\u2018\\u2019"; 
  String Uni_single_qt = "[\\u0027\\u2018\\u2019]+"; 
  String EOLstring= "[\\u000D\\u000A]";
  String NBSpace="[\\xC2\\xA0 ]+"; //non breaking space and regular space
  String tabs="\\u0009\\x09";
  //Std Fields
  String CreatorToolField="CreatorTool";
  String PermitField="Permit"+NBSpace+"[Aa]pplication"+NBSpace+"No[\\.,][\\.:(?:quot)&;]+?"; //lazy with space
  String DateField="CreateDate";
  String LocalGovField="[Ll]ocal"+NBSpace+"[gG]overnment"+NBSpace+"(?:[Aa]rea:|[Aa]uthority:)"+"["+NBSpace+tabs+"]*?";//NBSpace;
  String ProponentField="(?:[Pp]roponent|[Aa]pplicant)["+Uni_single_qt+"]*s"+NBSpace+"[Nn]ame:["+NBSpace+tabs+"]*";
  String ProponentFieldCell="(?:[Pp]roponent|[Aa]pplicant)"+NBSpace+"details"+NBSpace+"(?:Proponent|Applicant)["+Uni_single_qt+"]*s"+NBSpace+"[Nn]ame:["+NBSpace+tabs+"]*";
  String ProponentFieldCellSplit="(?:(?:[Pp]roponent|[Aa]pplicant)"+NBSpace+"details"+NBSpace+"\\<)";
  String ProponentFieldCellSpl2="(?:(?:[Pp]roponent|[Aa]pplicant)"+NBSpace+"details \\<)";
  //String ProponentFieldCellSplit="Proponent"+NBSpace+"details["+NBSpace+"]*[\\<]*["+EOLstring+"]*";
  //String ProponentNavVert="Proponent"+NBSpace+"details"+NBSpace+"Proponent["+Uni_single_qt+"]*s"+NBSpace+"name:";
  String PostalAddressField="["+NBSpace+"]*Postal"; //short for now
  String displacedFieldText="Colloquial"+NBSpace+"name:"; //UNUSED
  String DecisionResultField="[Dd]ecision"+NBSpace+"[Oo]n"+NBSpace+"[Pp]ermit"+NBSpace+"[Aa]pplication:"+"["+NBSpace+tabs+"]*";
  String DecisionDateField="[Dd]ecision"+NBSpace+"[dD]ate:"+"["+NBSpace+tabs+"]*";

  //Std patterns
  String LocalGovPattern="(?:Shire[s]*|Town[s]*|City)"+NBSpace+"[Oo]f"+NBSpace+"";
  String PermitPattern="(?:.*)[\\d]+[\\/]*[\\d]+";
  String PermitTypeField="[Pp]ermit"+NBSpace+"[Tt]ype:["+NBSpace+"]*?";
  String PermitTypePattern="\\w[\\w"+NBSpace+"]+[Pp]ermit"; //first letter excludes "Permit" as the first word
  String PermitTypePattern2 = "(?:[Pp]urpose|[Aa]rea)"+NBSpace+"[Pp]e[rn]mit";
  String PermitTypePattern2A="(?:[Pp]urpose|[Aa]rea)"+NBSpace+"[Pp]e[rn]mit"+NBSpace+"\\<";
  String LongDatePattern="[\\d]+"+NBSpace+"[\\w]+"+NBSpace+"\\d{4}";
  //String PermitTypePattern3 = "(?:[Pp]urpose|[Aa]rea)"+"["+NBSpace+"]*";
  
  String PermitTypePattern3 = "(?:[Pp]urpose"+"["+NBSpace+"]*)|(?:[Aa]rea"+"["+NBSpace+"]*)";
  String PermitTypePattern4 = "[Pp]urpose|[Aa]rea";
  String VariancePattern = "is"+NBSpace+"at"+NBSpace+"variance";
  String VariancePattern2 = "is"+NBSpace+"likely"+NBSpace+"to"+NBSpace+"be"+NBSpace+"at"+NBSpace+"variance";
  // output configuration
  Boolean XMLoutput=false;

public extractPDFXML() {
	
}

//constructor with XML outputsetting
public extractPDFXML(Boolean myBool) {
    this.XMLoutput=myBool;
}

public void parseFile(String directory, String myFile) {

      int count=0;
      int arraysize=33;
      String output[] = new String[arraysize];
      //init
      for (int od=1;od<arraysize;od++) {
          output[od]="";
        }
      //init strings for matching pattern segments
      String FieldText="";
      String Pattern="";
      String suffix="";
      String tempLine = "";
      Boolean error = false;
      try {
        //whole file is buffered
        BufferedReader bufferedReader = new BufferedReader(new FileReader("./"+directory+"/"+myFile));
      
       //search whole file as string
       this.searchFile="";
       this.searchFile = bufferedReader.lines().collect(Collectors.joining("\n")); //with EOL 
    
    mySearch = new XMLsearchAPI(this.searchFile);
    
    //PART 1 - TO DO: have a config option for verbose output
    //System.out.println ("Now parsing: "+myFile+"\n");
    doXMLoutput("filename",myFile);
        
    //new search range function.  
    //TO DO: Output options - log everything and 'hits' or just list successes
    String myQuery="";
    myQuery=mySearch.FindWithin("vegetation",50,"disturbed"); //this is AFTER
    log("SearchWordsResults.txt",myFile+":"+myQuery);
    doXMLoutput("searchwords",myQuery);

    //extract the PDF producer: relevant to tag layout and accuracy
    myQuery="";
    myQuery=mySearch.getXMPfieldvalue(CreatorToolField);
    log("createXMPtool.txt",myFile+":"+myQuery+"#");
    doXMLoutput("creatorXMP",myQuery);

    //getXMPproducer - pdf:Producer
    myQuery="";
    myQuery=mySearch.getXMPproducer();
    log("PDFtool.txt",myFile+":"+myQuery);
    doXMLoutput("PDFproducer",myQuery);
    String storeProducer=myQuery+"#";

    //getXMPauthor
    myQuery="";
    myQuery=mySearch.getXMPauthor();
    log("author.txt",myFile+":"+myQuery);
    doXMLoutput("author",myQuery);
    storeProducer=myQuery+"#"+storeProducer; //put author before the tool

    //variance - a count of how many matches for 'is at variance'
    ResultsStruct myVarianceCount=mySearch.FindPatternFreq(VariancePattern);
    int countOutput=myVarianceCount.getCount();
    String matchText=myVarianceCount.getText();
    StringBuilder myContents = new StringBuilder(myFile);
    myContents.append("#");
    myContents.append(matchText);
    myContents.append("#");
    myContents.append(countOutput);
    log("variance.txt",myContents.toString());
    doXMLoutput("variance",matchText+"#"+countOutput);

    //variance - a count of how many matches for 'is likely to at variance'
    myVarianceCount=mySearch.FindPatternFreq(VariancePattern2);
    countOutput=myVarianceCount.getCount();
    matchText=myVarianceCount.getText();
    log("variance2.txt",myFile+"#"+matchText+"#"+countOutput);
    doXMLoutput("variance2",matchText+"#"+countOutput);

    //Proponent  
    ResultsStruct PropSearch = doProponentSearches();
    doXMLoutput("proponent",PropSearch.getText());
    logExtended("proponentMatches.txt",myFile,PropSearch.getRT(),storeProducer);
    
    myQuery="";
    //PDF Created Date    
    myQuery=mySearch.getXMPfieldvalue(DateField); //try FindCreateDate
    suffix="#0#";
    if(myQuery.equals(""))
    {
      suffix="#NF#";
    }
    doXMLoutput("createdate",myQuery);
    log("createdates.txt",myFile+suffix);

    //PART 2 Permit Number ; 9-11
    myQuery="";
    suffix="";
    //
    myQuery=mySearch.getPatternAfterField_and_SpaceTab(PermitField,PermitPattern);
    suffix="#0#";
    if (myQuery.equals("")) {
      myQuery=mySearch.getPatternLineBelowAfterTag(PermitField,PermitPattern);
      suffix="#1#";
    }
    if (myQuery.equals("")) {
      myQuery=mySearch.getPatternLineBelow(PermitField,PermitPattern);
      suffix="#2#";
    }
    
    /*correction for scanning errors where "/1" at end is "11"
     To DO: take the first 4 chars and replace 11 on RHS with /1
     add a '*' to indicate altered
    */
    String substr = myQuery.substring(myQuery.length()-2,myQuery.length());
    //System.out.println(substr);
    if (myQuery.length()>=6 && substr.equals("11")) { 
      String temp=myQuery.substring(0,myQuery.length()-2)+"/1*";
      myQuery=temp;
    }
    if(myQuery.equals("")) {
      //System.out.println ("No permit found: "+myFile+"\n");
      suffix="#NF#";
    }
    doXMLoutput("permit",myQuery);
    log("PermitResults.txt",myFile+suffix+storeProducer);

    //Permit type 12 -14
    myQuery="";
    myQuery=mySearch.getPatternAfterField(PermitTypeField,PermitTypePattern);
    
    suffix="#0#";
    if (!myQuery.equals("Purpose Permit") && !myQuery.equals("Area Permit") && !myQuery.equals("")) {
      suffix="#0A#"+myQuery;
    }
    if (myQuery.equals("")) {
      myQuery=mySearch.getPatternFirstMatch(PermitTypePattern2);
      suffix="#1#";
    }
    if (myQuery.equals("")) {
      myQuery=mySearch.getPatternAfterField(PermitTypeField,PermitTypePattern3);
      suffix="#2#";
    }
    //getPatternLineBelowAfterTag
    if (myQuery.equals("")) {
      myQuery=mySearch.getPatternLineBelowAfterTag(PermitTypeField,PermitTypePattern3);
      suffix="#3#";
    }
    //TO DO: Check reg ex for this and test Permit Types NF
    if (myQuery.equals("")) {
      myQuery=mySearch.getPatternAfterField(PermitTypeField,PermitTypePattern4);
      suffix="#4#";
    } 
    if (myQuery.equals("")) {
      myQuery=mySearch.getPatternLineBelow(PermitTypeField,PermitTypePattern4);//"(?:[Pp]urpose|[Aa]rea)"
      suffix="#5#";
    } 
    if (myQuery.equals("")) {
      myQuery=mySearch.getPatternFirstMatch(PermitTypePattern4);
      suffix="#6#";
    }
    // finally
    if (myQuery.equals("")){
      //System.out.println ("No permit type found: "+myFile+"\n");
      suffix="#NF#";
    }
    doXMLoutput("permittype",myQuery);
    log("PermitTypes.txt",myFile+suffix+storeProducer); 
    
    /*Property Details 15-17 */

    /* Local Government Area 18-20 */
    myQuery="";
    myQuery=mySearch.getPatternWithRestOfLine(LocalGovPattern); //compare getPatternFirstMatch
    suffix="#1#";
    /*TO DO: strip out displaced field text at end of output
      Take a line with the field as the prefix 
    */   
    if (myQuery.equals("")) {
          myQuery=mySearch.getWordsBetweenField_and_EOL(LocalGovField);
          suffix="#2#";
        }
        if (myQuery.equals("")) {
          myQuery=mySearch.getLineAfterTagBelow(LocalGovField);
           suffix="#3#";
        }
        if(myQuery.equals("")) {
          //System.out.println ("No LGA found: "+myFile+"\n");
          suffix=("#NF#");
        }
      doXMLoutput("LocalgovArea",myQuery);
      log("LGAMatches.txt",myFile+suffix+storeProducer);

        //Clearing Area 21-23
        
        //Purpose 24-26

        //PART 3 - Decision Date 27-29 and Decision Result 30-32
        /* TO DO:  10.1.18
        Some of the Decision Date results have "reasons for decision" text after them.  This needs to be found and excluded from the date results.  i.e. LongDatePattern */
        
        myQuery="";
        Boolean success=false;
        String preDD="";
        suffix="";
        String optionPattern="(?:[Ss]hould a clearing permit be granted)|(?:[Ss]hould the clearing permit be granted)|(?:[Ss]hould a permit be granted)";
        String[] permitPattern = {"Grant|Granted",optionPattern,"grant|granted", "seriously at variance","Refus.*|Refused|Refusal|refuse|refused"};
        
        myQuery=mySearch.getWordAfterField(DecisionResultField); //consider if getPatternAfterFieldBeforeEOLTag works
        suffix="(DR0)";
        if (isExpectedDecision(myQuery)==false) {
          myQuery="";
        }
        else {
          success=true;
        }
        int permitCount=0;
        while (success==false && permitCount<permitPattern.length) {
          myQuery=mySearch.getPatternFirstMatch(permitPattern[permitCount]);
          suffix="(DR"+Integer.toString(permitCount)+")"+ myQuery;
          if (!myQuery.equals("")) {
              success=true;
            }
            permitCount++;
          }
        doXMLoutput("decisionresult",myQuery);
        //
        myQuery="";
        if (success==false) {
          suffix="#NF# - possible refusal";
        }
        else {
          myQuery=mySearch.getWordsBetweenField_and_EOL(DecisionDateField);
          preDD="(DD0)";
          if (myQuery.equals("")) {
            myQuery=mySearch.getPatternAfterField(DecisionDateField,LongDatePattern);
            preDD="(DD1)";
          }
          if (myQuery.equals("")) {
            myQuery=mySearch.getPatternLineBelow(DecisionDateField,LongDatePattern); 
            preDD="(DD2)";
          }
          if (myQuery.equals("")) {
            myQuery=mySearch.getMatchAfterField_anyline(DecisionDateField,LongDatePattern); 
            preDD="(DD3)";
          }
          //finally
          if (myQuery.equals("")) {
            //System.out.println ("No decision date found: "+myFile+"\n");
            preDD="(NFDD)";
          }
          doXMLoutput("decisiondate",myQuery);
        }
        //end and write decision search results to file
        log("DecisionResults.txt",myFile+suffix+preDD+storeProducer);
        
        //FINISH 
        writeEndRecord();
        bufferedReader.close();
      
      	} catch(IOException e) {
                    error = true;
                    e.printStackTrace();
      } 
    }

    
    /* Method to fine tune the searches for Proponent/Applicant.  Makes some use of the fact that some of the CLearing Permit Decision Reports in PDF are preceded by a copy of the Permit, which also has Permit Holder information.
    Note that in some Clearing Decision Reports it refers to Applicant and in others Proponent, so both must be in search.
    
    Some of the logic is based around trying to anticipate what kind of offset areas in tables have occurred through the OCR process.
    By checking whether Field text is in one line or over several lines, some general predictions can be made.
    The OCR using Adobe Acrobat 9 still has poor uptake on low resolution PDFs, and shaded areas become dots that do not assist.

    */

    public ResultsStruct doProponentSearches() 
    {
    ResultsStruct myResults = new ResultsStruct();
    int matchtype=0; 
    String myQuery="";
    String firstLineMatch="";
    String nextLineAfter="";
    //first check if field cell values read as a single row 
    /* ---- Test --- what kind of layout situation we are in */
    //if we get a match on a split cell string high chance that the results are also displaced;
    String SplitField="";
    //SplitField=mySearch.getPatternFirstMatch(ProponentFieldCellSplit);
    SplitField=mySearch.getPatternFirstMatch(ProponentFieldCellSpl2);

    //cell text on one line
    String SameLineField="";
    SameLineField=mySearch.getPatternFirstMatch(ProponentFieldCell);
    
    /*<-----SCENARIO 1---->*/

    if (SameLineField.length()>0) {
        firstLineMatch=mySearch.getWordsBetweenField_and_EOL(SameLineField);
    
    if (firstLineMatch.length()>0 && isValidApplicant(firstLineMatch)) {
        myQuery=firstLineMatch;
        System.out.println("Match 10 (firstline) accepted: "+myQuery);
        myResults.setStrings("10",myQuery);
        return myResults;
    }
    else {
      nextLineAfter=mySearch.getLineAfterTagBelow(SameLineField);
    }
    
    if (nextLineAfter.length()>0 && isValidApplicant(nextLineAfter)) {
        myQuery=nextLineAfter;
        myResults.setStrings("11",myQuery);
        System.out.println("Match 11 (nextline) accepted: "+myQuery);
        return myResults;
    }
  }
 
    /*  END EXCLUSIVE SCENARIO 1 --- BEGIN COMBINED CONDITIONAL SECTION 2*/

    if (SameLineField.length()>0 || SplitField.length()>0) {
    
    //we search for the values that might appear immediately above in the higher row of table (i.e. purpose permit/area permit:  this might precede what we want

    String AfterPermit=mySearch.getLineAfterTagBelow(PermitTypePattern2A);

    //Do validity checks
    if (AfterPermit.length()>0 && !isValidApplicant(AfterPermit)) {
      AfterPermit=mySearch.getLineAfterTagBelow(ProponentField);
    }

    if (!isValidApplicant(AfterPermit)) {
      AfterPermit="";
    }

    //sometimes we find there's a Permit at the front and where get an early hit on its line
    //if so, trim
    if(AfterPermit.startsWith("Permit")){
          String[] mySplits=AfterPermit.split(":");
          if (mySplits.length>=2) {
            AfterPermit=mySplits[1];
          }
          myQuery=AfterPermit;
          System.out.println("Match 12 (Permit ...) accepted: "+myQuery);
          myResults.setStrings("12",myQuery);
          return myResults;
    }

    //sometimes we find there's a Permit at the front and where get an early hit on its field
    //if so, recognise it and find the Permit Holder that follows before next field
    String PHpattern="PERMIT HOLDER |PERMIT HOLDER";//.*)PERMIT HOLDER|PERMIT HOLDER 
    if(AfterPermit.matches(PHpattern)){
        String tempField=AfterPermit;
        AfterPermit=mySearch.getAllBetweenPhrases(tempField,"LAND ON");
    }

    //any valid result, assuming we've already matched the split string...
    if (AfterPermit.length()>0)
    {
      myQuery=AfterPermit;
      myResults.setStrings("13",myQuery);
      System.out.println("Match 13 (Permit Holder) accepted: "+myQuery);
      return myResults;
    }
  }    /*<-----END SCENARIO 2 COMBINED SECTION ---->*/

    //back to general searches
    matchtype++;
    myQuery=mySearch.getLineWordsBetweenPhrases(ProponentField,PostalAddressField);
    if(!myQuery.equals("") && isValidApplicant(myQuery)) {
      myResults.setStrings(Integer.toString(matchtype),myQuery);
      return myResults;
      }
    matchtype++;
    myQuery=mySearch.getWordsBetweenField_and_EOL(ProponentField);
    if(!myQuery.equals("") && isValidApplicant(myQuery)) {
      myResults.setStrings(Integer.toString(matchtype),myQuery);
      return myResults;
     }
    matchtype++;
    myQuery=mySearch.getLineAfterTagBelow(ProponentField);
    if(!myQuery.equals("") && isValidApplicant(myQuery)) {
        myResults.setStrings(Integer.toString(matchtype),myQuery);
      return myResults;
     }
    matchtype++;
    if (myQuery.equals("Postal address: ")) {
         myResults.setStrings(Integer.toString(matchtype)+" postal ",myQuery);
    }
    if (!isValidApplicant(myQuery)) {
      myQuery="";
    }
    //finally
    if (myQuery.equals("")) {
        myResults.setRT("NF");
      //System.out.println("No proponent found:"+myFile);
      }  
      return myResults;
    }

    /* Method to test whether Proponent/Applicant queries have returned invalid lines */

    public Boolean isValidApplicant(String myString) {

    if(myString.startsWith("1.2",0) || myString.startsWith("1,2",0) || myString.startsWith("1.3",0) || myString.startsWith("1,3",0)  ) {
        return false;
      }
      if (myString.startsWith("LOT",0) || myString.startsWith("Lot",0) || myString.startsWith("File",0)) 
      {
        return false;
      }
      else
        {
          return true;
        }
      }

      public Boolean isExpectedDecision(String myString) {
      //reject unexpected results
      String[] expectedResult = {"grant","Grant","Granted","refusal","Refusal","Refuse","Refused"};
      Boolean test=false;
      for (String h : expectedResult) {
      if (h.equals(myString)) {
            test = true;
            break;
          }
        }
      return test;
    }


    public void doXMLoutput (String myTag, String myContent) {
      String filename="outputfile.txt";
      String path = "./output/"+filename;
      EDOfileApp myOutput = new EDOfileApp(path);
      if (this.XMLoutput==true) {
        myOutput.addText("<"+myTag+">"+myContent+"</"+myTag+">"); 
      }
      else {
        myOutput.addText(myContent+"#");
      }
    return;
    }

    public void writeEndRecord() {
      String filename="outputfile.txt";
      String path = "./output/"+filename;
      EDOfileApp myOutput = new EDOfileApp(path);
      myOutput.addText("\n");
    }

    public void doXMLsearch(String code, String myField, String MyPattern) {
      

    }

    public void log(String filename, String content) {

      String path = "./logs/"+filename;
      EDOfileApp myLog = new EDOfileApp(path);
      myLog.addOneRecord(content);

    }

    public void logExtended(String filename, String XMLfile, String myCode, String myResults) {

      StringBuilder content =new StringBuilder(XMLfile); 
      content.append("#");
      content.append(myCode);
      content.append("#");
      content.append(myResults);
      String path = "./logs/"+filename;
      EDOfileApp myLog = new EDOfileApp(path);
      myLog.addOneRecord(content.toString());
    }

}

