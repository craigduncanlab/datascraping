
/* Class that provides search API
Uses Regexp
Designed for XML parsing
Created 2.1.18 by Craig Duncan

This class has mostly been used as a singleton instance, called by the client that examines a specific file for its contents.

This class can become a comprehensive search engine based on regex, but currently provides these main methods:
1.  Search for results (words or pattern matches) within [x] characters and between words [y] and [z]
2.  return a count for the search in 1.
3.  return the first match using a set of standard, high-level search functions that use 3 pattern groups (as explained further below)
4.  return the contents of specific XMP tags that appear in PDF documents.

{Consider if any of these should be in separate classes}

This was initially built for finding partially structured text in PDF-based XML text.  For now, it mainly assumes that in most cases there will only be one 'record' to find for a given 'field', and that multiple hits are not being returned.
The inconsistencies within PDF text sequences and in spelling etc mean that it is necessary to try and locate text relative to other text or document markers.
This may only be relative to words or tags: the XML tag values that are based on tables or paragraphs are not always very useful.

This class uses up to 3 pattern groups for the 'relative navigation' logic i.e. (a)(b)(c).  Although any pattern within each of patterns a,b or c can be made up of smaller parts, the main 'returnFirstMatch' function accepts 3 arguments on the assumption that (a) and (c) are boundary phrases and that the most interest is in the middle group (b).   It simplifies the definition of a 'group' within the regular expressions if there are always, ultimately, 3 groups representing a, b and c when results are returned.

This class provides helper functions that comprise some common ways of using the 3 pattern groups to achieve a higher-level text goal.
Sometimes this includes grabbing any text, or a range of text between two patterns e.g. between 'a' and 'c'.
Other times it includes grabbing text that meets a pattern, wherever it is.

(a) and (c) are mainly used as boundary patterns - i.e boundary phrases or character sequences.
Since the default text direction runs left to right, the first pattern 'a' is mainly used to locate localising text within the file, usually occurring prior to the text desired to be found.  
It will then return any text matching pattern 'b'.  This allows 2 different uses for 'b' - to only return text that meets a pattern, or to loosen the constrains on 'b' and find text within certain boundaries.
The boundaries could be text matching pattern 'a' and some end of line or spaces, or it could be text matching pattern 'c'.
Pattern 'c' is optional : it could be useful if there is a phrase or character beyond 'b' that limits the length of (b), or is commonly found next to (b) but is not to be returned as part of the string. 
Also, pattern 'a' is optional if the pattern 'b' can be found anywhere.  

The aim is to fine tune this so that it behaves like a regular search engine, allowing range searches, and also some case insensitive searching.
It could also benefit from a function which will allow ASCII sentences of mixed case to be transformed into searches for any case and which will also recover unicode equivalents (e.g. spaces and non-printable space characters etc)


TO DO (2.1.18):
A User API on the front end that turns plain text case-insensitive searches into REGEXP.
THis would effectively be a general purpose search engine, which is then used to find and extract
fields and values for records in semi-structured PDF content.
This would be used by client apps instead of having to build regexp patterns for fields, values.

TO DO (9.1.18): 
Minimise public methods for method calls; convert as many to private as possible.

Testing some file re-write and improvement work in reggy branch.

The public function of the regexp object should include 'returnfirstmatch'.  Call that from here.
The pattern of field(prefix), pattern, end can be a data structure, passed to the matching object.

*/

import java.util.*;
import java.io.*;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.HashMap;
//for pattern matching:
import java.util.regex.Matcher;
import java.util.regex.Pattern;
//buffered reader to string (Java 8 and 9)
import java.util.stream.Collectors;

public class XMLsearchAPI {
	
  //for buffers
  String testfile="";
  //useful instance variables for regexp
  String Uni_NonBreakspace="\\xC2\\u00A0\\xA0 ";
  String soft_break="\\x0d";
  String all_breaks="\\x0d\\x0a";
  String tabs="\\u0009\\x09";
  //straight single quote is u0027; u2018 LHS u2019 RHS curly Windows E2 80 99
  String Uni_single_qt = "[\\u0027\\u2018\\u2019]+"; 
  String Uni_sgl_qt = "\\u0027\\u2018\\u2019"; 
  String Uni_dashes = "\\-\\u2010\\u2011\\u2012\\u2013\\u2014\\u2015"; //u2010 is hyphen
  String Uni_dbl_qt = "\\u0022\\u201c\\u201d\\u201e\\u201f\\\""; //not using \\x22 for now
  //This LooseRegEx includes the : ; and . 
  String NBSpace="[\\xC2\\xA0 ]+"; //non breaking space and regular space
  
	//multiple lines init
  String GeneralSameLineChar="[\\w\\d\\s\\t\\(\\)\\_\\-\\-\\=\\x26,\\/\\’'\\<\\>\\[\\]\\.:;,"+all_breaks+Uni_dbl_qt+Uni_sgl_qt+Uni_dashes+Uni_NonBreakspace+tabs+"]";
  String multiLinesSimpleGreedy="[:/\\w\\t\\s\\d\\<\\>\\x0D\\x0A\\x22\\x2D\\x3D ]*";
  String SameLineCharsLazy="[ :;,/_\\-\\=\\x26\\w\\s\\t\\<\\>"+NBSpace+Uni_sgl_qt+Uni_dbl_qt+tabs+"]+?";
  //start or end patterns for XML files
  String TagStart_or_Spaces="\\<|"+NBSpace;
  String EOL_or_nextTag="[\\x0D\\x0A\\<]";
  //spaces ends or stops
  String spacesOptional="["+NBSpace+"]*";
  String TagStops="[\\>]+";
  String WrapLineToTag="[\\x0D\\x0A]*\\>"+spacesOptional;
  //FatFieldSpacers
  String uptoATagEnd=GeneralSameLineChar+"*?"+TagStops;
  String uptoATagWithWrap = SameLineCharsLazy+WrapLineToTag;
  //
  String WordOnSameLine = "[\\w]+";
  //String AllWordsOnSameLine = "\\w["+NBSpace+Uni_dashes+Uni_single_qt+"&,:;/.\\.\\s\\w\\t\\(\\) ]*?"; //lazy before EOL
  String SameLineNoTagsChar="["+NBSpace+Uni_dashes+Uni_single_qt+"&,:;/.\\.\\s\\w\\t\\(\\) ]";
  String AllWordsOnSameLine = "\\w"+SameLineNoTagsChar+"*?"; 

  //Dates
  //String YMDSystemDatePattern="[\\d]+"+NBSpace+"[\\D]+"+NBSpace+"[\\d]+";-
  String YMDSystemDatePattern="\\d{4}\\D\\d{2}\\D\\d{2}";
  String DMYDatePattern="[\\d]+"+NBSpace+"[\\w]+"+NBSpace+"[\\d]{4}";

public XMLsearchAPI(String myFile) {
	     this.testfile=myFile;
}

      /* Find Pattern Lazy */
      
      public String getPatternFirstMatch(String myPattern) {
      
      QueryOb myPQ = new QueryOb("",myPattern,"");
      return getFirstMatch(myPQ);
      }

      /* Find Pattern Return Count if True */
      
      public ResultsStruct FindPatternFreq(String myPattern) {
      
      ResultsStruct myStruct = doCounterQuery("",myPattern,"");
      return myStruct;
      }

      /* Find Pattern on Same Line 
      TO DO: consider if this adds anything to lazy*/
      
      public String FindPatternAnyLine(String myPattern) {
      
      QueryOb myPQ = new QueryOb("",myPattern,EOL_or_nextTag);
      return getFirstMatch(myPQ);
      }

      /* Find Pattern grab rest of Same Line */
      
      public String getPatternWithRestOfLine(String myPattern) {
      
      StringBuilder fatPattern = new StringBuilder(50);
      fatPattern.append(myPattern);
      fatPattern.append(AllWordsOnSameLine);
      QueryOb myPQ = new QueryOb("",fatPattern.toString(),EOL_or_nextTag);
      return getFirstMatch(myPQ);
      }

    /* Find Field ; grabs pattern on same line 
    Unused?
    */

      public String getPatternAfterFieldBeforeEOLTag(String myField, String myPattern) {
      
      QueryOb myPQ = new QueryOb(myField,myPattern,EOL_or_nextTag);
      return getFirstMatch(myPQ);
      }

      /* Find Field ; grabs word on same line */

      public String getWordAfterField(String myField) {
      QueryOb myPQ = new QueryOb(myField,WordOnSameLine,TagStart_or_Spaces);
      return getFirstMatch(myPQ);
      }

      /* Find Field ; grabs pattern on same line */

      public String getPatternAfterField(String myField, String myPattern) {
      
      QueryOb myPQ = new QueryOb(myField,myPattern,"");
      return getFirstMatch(myPQ);
      }

      /* Find Field ; grabs Field Value pattern on same line */

      public String getPatternAfterField_and_SpaceTab(String myField, String myPattern) {
      
      StringBuilder fatField = new StringBuilder(50);
      fatField.append(myField);
      fatField.append("["+NBSpace+tabs+"]*");
      QueryOb myPQ = new QueryOb(fatField.toString(),myPattern,"");
      return getFirstMatch(myPQ);
      }

      /* Find Field ; grabs same line */

      public String getWordsBetweenField_and_EOL(String myField) {

      QueryOb myPQ = new QueryOb(myField,AllWordsOnSameLine,EOL_or_nextTag);
      return getFirstMatch(myPQ);
      }

      /* Find Field ; grabs same line; stops where next Field exists */

      public String getLineWordsBetweenPhrases(String myField, String myNextField) {

      QueryOb myPQ = new QueryOb(myField,AllWordsOnSameLine,myNextField);
      return getFirstMatch(myPQ);
      }

      /* Get everything between first and second phrases */

      public String getAllBetweenPhrases(String myField, String myNextField) {

      QueryOb myPQ = new QueryOb(myField,multiLinesSimpleGreedy,myNextField);
      return getFirstMatch(myPQ);
      }

    /* Find Field that includes everything up to next line's tag; grabs next line */
      public String getLineAfterTagBelow(String myField) {
      StringBuilder fatField = new StringBuilder(50);
      fatField.append(myField);
      fatField.append(uptoATagWithWrap);
      QueryOb myPQ = new QueryOb(fatField.toString(),AllWordsOnSameLine,EOL_or_nextTag);
      return getFirstMatch(myPQ);
      }

      /* Find Field that includes everything up to next line's tag; grabs next line */
      
      public String getPatternLineBelow(String myField, String myPattern) {

      StringBuilder fatField = new StringBuilder(50);
      fatField.append(myField);
      fatField.append(uptoATagEnd);
      QueryOb myPQ = new QueryOb(fatField.toString(),myPattern,"");
      return getFirstMatch(myPQ);
      }

      /* Find Field that includes everything up to next line's tag; grabs Pattern on next line */
      public String getPatternLineBelowAfterTag(String myField, String myPattern) {
      StringBuilder fatField = new StringBuilder(50);
      fatField.append(myField);
      fatField.append(uptoATagWithWrap);
      QueryOb myPQ = new QueryOb(fatField.toString(),myPattern,"");
      return getFirstMatch(myPQ);
      }

      /* helper function */
      public String makeFatField(String myField,String spacer)
      {
        return myField+spacer;
      }

      /* Find Field in tag, Followed By Pattern in different tag 
      TO DO:  use a FollowedBy function and simplify */
      
      public String getMatchAfterField_anyline(String myField, String myValue) {
      StringBuilder fatField = new StringBuilder(50);
      fatField.append(myField);
      fatField.append(uptoATagEnd);
      fatField.append(multiLinesSimpleGreedy);
      String end = TagStart_or_Spaces;
      QueryOb myPQ = new QueryOb(fatField.toString(),myValue,end);
      return getFirstMatch(myPQ);
      }

    /* replace non-printable UTF8 encoded as Hex */
    public String doCleanString(String myString) {

      //nb whilst "\\xC2\\xA0" may work in Pattern, rest of Java code needs \\uXXXX
      String output1 = myString.replaceAll("\\u00A0"," "); //fix non-blank spaces
      //These are single marks/straight quote: \\u0027|\\u2018|\\u2019|
      String output2 = output1.replaceAll("\\u0027|\\u2018|\\u2019|\\u00AE|\\u000A|\\<[\\w]+\\>|\\</[\\w]+\\>",""); //fix registered TM, line feed, tag start and end
      return output2;
    
    }


    /*Find first date no field */

    public String FindCreateDate() {   
      
      QueryOb myPQ = new QueryOb("",YMDSystemDatePattern,TagStart_or_Spaces);
      return getFirstMatch(myPQ);
    }

     /*Find XMP metadata namespace entries e.g. CreateDate, CreatorTool  
     <xmp:CreateDate>2011-02-24T10:14:36Z</xmp:CreateDate>
     */

    public String getXMPfieldvalue(String myField) {
      //ignore time zone data by putting it into tail
      StringBuilder fatField = new StringBuilder(50);
      StringBuilder fatEnd = new StringBuilder(50);
      if (myField.equals("CreateDate")) {
        fatField.append("\\<xmp:CreateDate\\>");
        fatEnd.append("T.*\\</xmp:CreateDate\\>");
      }
      else {
        fatField.append("\\<xmp:");
        fatField.append(myField);
        fatField.append("\\>");
        fatEnd.append("</xmp:");
        fatEnd.append(myField);
        fatEnd.append("\\>");
      }
      QueryOb myPQ = new QueryOb(fatField.toString(),".*",fatEnd.toString());
      return getFirstMatch(myPQ);

    }

   /*Find XMP metadata 'PDF' namespace entries e.g. Producer  
   For now just set it to read Producer.  May be modified to accept String*/

    public String getXMPproducer() {
      
      QueryOb myPQ = new QueryOb("\\<pdf:Producer\\>",".*","\\</pdf:Producer\\>");
      return getFirstMatch(myPQ);
    }

    /* Find the author information in PDFs XMP metadata especially that created through MS Word 
     */

    public String getXMPauthor() {
      
      //QueryOb myPQ = new QueryOb();
      String TAGname="rdf:li";
      String tagstart="\\<"+TAGname+"\\>";
      String myField = "<dc:creator>[ \\x0d\\x0a]*<rdf:Seq>[ \\x0d\\x0a]*"+tagstart;
      String tagend="\\</"+TAGname+"\\>";
      String pattern=".*";
      //String output= getFirstMatch(myField,pattern,tagend);
      QueryOb myPQ = new QueryOb(myField,pattern,tagend);
      return getFirstMatch(myPQ);
    }

    /* unusued? */

    public String getDMYdateAfterField(String myField) {
      String spacer = uptoATagEnd;
      QueryOb myPQ = new QueryOb(makeFatField(myField,spacer),DMYDatePattern,TagStart_or_Spaces);
      return getFirstMatch(myPQ);
    }

    

    /* Match XML based solely on pattern - currently not used */  

    public String MatchXML(String myPattern) {

		Pattern p = Pattern.compile(myPattern);
    Matcher matcher = p.matcher(this.testfile);
    int groupCount = matcher.groupCount();
    String output="";
    //The find method will go through and find containers with matchers
    //to restrict to first match, just do matcher.find() once.
        int count=0;
        while (matcher.find())
          {
          	count++;
            for (int i = 1; i <= groupCount; i++) {
                // Group i substring
                //System.out.println("Group " + i + ": " + matcher.group(i));
          }
          if (groupCount>=2 && !matcher.group(2).equals("") && count==1) {
     		   output = matcher.group(2);
     		   //System.out.println("Return: "+output+"\n");
           return output; //return at first opportunity
     		}
     	}
     return output;
     }

  /* Main function to search for structured text in PDF Files (e.g. those based on Word document forms) 
  where there is a general pattern of:
  - field name or label 
  - tab or space after label, then the relevant text (i.e. word match or pattern)
  - an end of line of XML tag (by-product of converting PDF text to XML format).
  */

    //public String getFirstMatch(String myField, String myPattern, String myTail) {

    public String getFirstMatch(QueryOb myPQ) {
    //String thisPattern="("+myField+")("+myPattern+")(?:"+myTail+")"; 
    String thisPattern = myPQ.getFullPattern();
    Pattern p = Pattern.compile(thisPattern);
    Matcher matcher = p.matcher(this.testfile);
    int groupCount = matcher.groupCount();
    String output="";
    //The find method will go through and find containers with matchers
    //to restrict to first match, just do matcher.find() once.
        int count=0;
        while (matcher.find())
          {
            count++;
            for (int i = 1; i <= groupCount; i++) {
                // Group i substring
                //System.out.println("Group " + i + ": " + matcher.group(i));
          }
          if (groupCount>=2 && !matcher.group(2).equals("") && count==1) {
           output = matcher.group(2);
           //System.out.println("Return: "+output+"\n");
           return doCleanString(output); //return at first opportunity
        }
      }
     return doCleanString(output);
     }


    /* Find Pattern within [x] lines */

    /* Find Pattern within [x] characters 
    This will return the string inclusive of both
    TO DO: specify characters either side?
     */

    
    /* FindWithinSpecified */
    public String FindWithin(String myPattern1, int myStartIndex, String myPattern2) {

    //String thisPattern="("+myPattern1+")(?:"+GeneralSameLineChar+"){0,"+myStartIndex+"}("+myPattern2+")"; 
    String inclusivePattern=myPattern1+"(?:"+GeneralSameLineChar+"){0,"+myStartIndex+"}"+myPattern2; 
    QueryOb myPQ = new QueryOb("",inclusivePattern,"");
    return getFirstMatch(myPQ);
    }


    /* Find Pattern within [x] words of Pattern 
    Return first match or all matches?  
    Different if you give results to users, or if you just want to take a result
    for further automated procedures */

    public String FindWithinLoop(String myPattern1, int myStartIndex, String myPattern2) {

    int indexy=1;
    String myQuery="";
    while (myQuery.equals("") && indexy<myStartIndex) { //stop 
      myQuery=this.FindWithin(myPattern1,indexy,myPattern2); //this is AFTER
      indexy++;
    }
    return doCleanString(myQuery);
    }
    //TO DO: convert spaces in each pattern to NBSpace
    //int startIndex=0;
    

    /* Find Pattern and Count Frequency */

    public ResultsStruct doCounterQuery(String myField, String myPattern, String myTail) {

    ResultsStruct thisStruct = new ResultsStruct();
    QueryOb myPQ = new QueryOb(myField,myPattern,myTail);
    String thisPattern=myPQ.getFullPattern();
    Pattern p = Pattern.compile(thisPattern);
    Matcher matcher = p.matcher(this.testfile);
    int groupCount = matcher.groupCount();
    String output="";
    //The find method will go through and find containers with matchers
    //to restrict to first match, just do matcher.find() once.
        int count=0;
        while (matcher.find())
          {
            count++;
            for (int i = 1; i <= groupCount; i++) {
                // Group i substring
                //System.out.println("Group " + i + ": " + matcher.group(i));
                thisStruct.setText(matcher.group(2));
          }
        }
     thisStruct.setCount(count);
     return thisStruct;
     }

     }

