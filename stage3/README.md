# Process XML files (decision reports) and search for useful predefined content

---
Created: 2017

Author: Craig Duncan

Recap written: 22 June 2021

---

# Intro 

## Stage 2

Stage 2 took downloaded PDF files and identified where PDF files contained OCR and were searchable.
The PDF files were converted to XML where OCR existed (it used Adobe Acrobat to creat XML files, in bulk, from OCR content).  

The XML files were placed into the folder 'test' and this contained two folders, one for PDF with searchable (OCR) content, and the other for non-searchable content.

## Stage 3

This Stage 3 was a rather 'scripty' set of tools that was able to automate some preprocessing and then searches.  The workflow in the pipeline can be summarised as follows: 

1. Take a directory of XML files (default Folder 'test', with subfolders) that had been created from PDF files.
2. Process the XML files into a simpler data format (a succinct XML).
3. Defined some standard regex queries that could be performed in a modular fashion.
4. Passed the queries on to a specific search class that ran the queries on the succinct XML string data
5. Iterated as required. 

Since the PDF files were from a particular source, hard-coded search queries were specific to that domain and the goals of the enquiries.

# Instructions for Stage 3

Default:

```
java EDOmain3
(no folder code = xml mode, test folder)
```

Optional folder codes:

```
s for folder xml_PDF_searchable
ns for folder xml_PDF_notsearchable");

Example: 
java EDOmain3 s
```
  
Turn XML output mode on (default with no code is off):

```
Example2: 

java EDOmain3 xml 
```

Combine options in this order:

```
Example : 

java EDOmain3 s xml 
```

# Internals for Stage 3

## EDOmain3

Main user interface, operates on prepared data files in predefined folder structure.

The EDOmain3 creates runs XMLApp object, then runs extractPDFXML and XMLSearchAPI instances to carry out queries.

## extractPDFXML (which internally runs XMLSearchAPI).

The main purpose of the extractPDFXML class was to write some standard queries and then invoke XMLSearchAPI:

1. Write the 'output.txt' file that contains structured information as a result of running simple NLP queries on each file.

2. Write helper log files to cross-reference the fields written to output with success/failure for each file.

The queries are hard-coded regex expressions used subsequentially in the code, to navigate through file.  They were written with knowledge of the 'expected' content of the Clearing memos/files, but contain regex patterns to allow for OCR errors, and inconsistencies or spelling errors made by the authors of the reports.

## XMLSearchAPI

The XMLsearchAPI object performs any pattern matching against a String representing the contents of that file.

The search will generally return a string to this object that is a first match to the pattern specified, except for:
(a) the FindWithin method returns a count of the matches within a predefined structure.
(b) ResultsStruct object is used for returning both text and integer information.

In mySearch navigation is generally not done by absolute position in file: the fields are used to identify a relative position, and then further pattern matching occurs.

The function names in XMLsearchAPI have been written to be as descriptive of the navigation and pattern-matching goals as possible.

Main methods:
1.  Search for results (words or pattern matches) within [x] characters and between words [y] and [z]
2.  return a count for the search in 1.
3.  return the first match using a set of standard, high-level search functions that use 3 pattern groups (as explained further below)
4.  return the contents of specific XMP tags that appear in PDF documents.

## Observations on search success

This was initially built for finding partially structured text in PDF-based XML text.  

For now, it mainly assumes that in most cases there will only be one 'record' to find for a given 'field', and that multiple hits are not being returned.

The inconsistencies within PDF text sequences and in spelling etc mean that it is necessary to try and locate text relative to other text or document markers.
This may only be relative to words or tags: the XML tag values that are based on tables or paragraphs are not always very useful.

## Search metholodgy

This class uses up to 3 pattern groups for the 'relative navigation' logic i.e. phrases with a (a)(b)(c) structure.  

Although any pattern within each of patterns a,b or c can be made up of smaller parts, the main 'returnFirstMatch' function accepts 3 arguments on the assumption that (a) and (c) are boundary phrases and that the most interest is in the middle group (b).   It simplifies the definition of a 'group' within the regular expressions if there are always, ultimately, 3 groups representing a, b and c when results are returned.

This class provides helper functions that comprise some common ways of using the 3 pattern groups to achieve a higher-level text goal.

Sometimes this includes grabbing any text, or a range of text between two patterns e.g. between 'a' and 'c'.

At other times it includes grabbing text that meets a pattern, wherever it is.

(a) and (c) are mainly used as boundary patterns - i.e boundary phrases or character sequences.

Since the default text direction runs left to right, the first pattern 'a' is mainly used to locate localising text within the file, usually occurring prior to the text desired to be found.  

It will then return any text matching pattern 'b'.  This allows 2 different uses for 'b' - to only return text that meets a pattern, or to loosen the constrains on 'b' and find text within certain boundaries.

The boundaries could be text matching pattern 'a' and some end of line or spaces, or it could be text matching pattern 'c'.

Pattern 'c' is optional : it could be useful if there is a phrase or character beyond 'b' that limits the length of (b), or is commonly found next to (b) but is not to be returned as part of the string. 

Also, pattern 'a' is optional if the pattern 'b' can be found anywhere.  

The aim is to fine tune this so that it behaves like a regular search engine, allowing range searches, and also some case insensitive searching.

It could also benefit from a function which will allow ASCII sentences of mixed case to be transformed into searches for any case and which will also recover unicode equivalents (e.g. spaces and non-printable space characters etc)


22.6.21
