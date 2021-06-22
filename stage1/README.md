# Utility FTP File Transfers from predefined list

Ad hoc program (2018), scripted and run, to assist with transferring files from FTP server and performing some data manipulation on master file list.

# Usage

EDOUtil was a control program that enabled choice of functions, broadly either:

    (a) transferring files using the transfer program, and passing it a selected pre-processed file list;
    (b) calling a utility function to use to select a sub-set of files to transfer.

Usage (after compilation):

```
//1. Modify internal functions to be called in EDOUtil.java

//2. Compile
javac EDOUtil.java

//3. Run
java EDOUtil

//3. repeat as necessary for workflow
```

No CLI for parameters. Was prepared to run once.  

# Helper classes

EDOFiletransfers.java carries out the file transfers.  The transfers to be made are managed using a pre-processed file list.

EDOfileAPP is the utility program containing functions that can perform some work on the file list that is used to manage the transfers

# Sequential steps in workflow

The settings followed this workflow sequencesprogram can call the other programs in a sequence.

Precondition : a list of files that are to be transferred from FTP server.  The workflow includes modifying that file list as needed, to modify what files need to be transferred. 

## Filter the FTP server full path information to retain only file names for workflow

EDOfileApp function: Parse Lines CSV 

Utility to fix and split out filenames from the FTP server file list
The input file has the full file path as generated from FTP server by the java app EDOclient.java

Usage:
```
EDOfileApp myParsedFile = new EDOfileApp("fullpathsonly.txt");  
myParsedFile.parseLinesCSV("newfilenameset.txt"); 
```

## Filter the file list

Utility to reduce the file list to only the 'decision reports' 
(already prepared with # delimiter)

EDOfileApp function: parseDecisionReport

Usage:
```
EDOfileApp myParsedFile = new EDOfileApp("fullpathsonly.txt"); 
myParsedFile.parseDecisionReports("transferlist_DR.txt");
```

## Use the transfer list to request transfer of files

Conduct transfers from the prepared file list 

EDOfiletransfers function: transfer

Usage:

```
EDOfiletransfers myMover = new EDOfiletransfers();
myMover.transfer("transferlist.txt");
```

## Filter the transfer list so that only the repeated filename 'decision report' is in it

Utility to include only the 'decision report.pdf' duplicates in the file transfer list

EDOfileApp function: parseDuplicates

Usage:
```
EDOfileApp myParsedFile = new EDOfileApp("fullpathsonly.txt"); 
myParsedFile.parseDuplicates("transferlist_duplicates.txt");
```

## Transfer the replicated files

Conduct transfers from a file list of duplicate 'decision reports' 

EDOfiletransfers function: transferduplicates

Usage:
```
EDOfiletransfers myMover = new EDOfiletransfers();
myMover.transferduplicates("transferlist_duplicates.txt");
```

## Filter so that only repeated 'decision.pdf' files are in the list

Utility to include only the 'decision.pdf' files/duplicates in the file transfer list 

EDOfileApp function: parseDecisionPDF

Usage:
```
EDOfileApp myParsedFile = new EDOfileApp("fullpathsonly.txt"); 
myParsedFile.parseDecisionPDF("transferlist_decision_dups.txt");
```   

