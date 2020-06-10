# FTP Client-side File Utilities
(c) 2018 Craig Duncan

Utility programs to assist with:
- transferring files from an FTP server; and
- perform data manipulation on some output text files.

The workflow uses a main transfer program (EDOFiletransfers.java).  It is assisted by two other programs that prepare the file list for the main transfers. 

The main application can be used to progress through the workflow in stages, using previous output.

1. EOUUtil is a control program that enables choice of functions, broadly either:
    (a) transferring files using the transfer program, and passing it a selected pre-processed file list;
    (b) calling a utility function to use to select a sub-set of files to transfer.

2. EDOFiletransfers.java carries out the file transfers.  The transfers to be made are managed using a pre-processed file list.

3. EDOfileapp is the utility program containing functions that can perform some work on the file list that is used to manage the transfers


# Workflow: how the EDOUtil program can call the other programs in a sequence #

1. Parse Lines CSV *Filter the FTP server full path information to retain only file names for workflow*

	Utility to fix and split out filenames from the FTP server file list
        The input file has the full file path as generated from FTP server by the java app EDOclient.java
        EDOfileApp myParsedFile = new EDOfileApp("fullpathsonly.txt");  
        myParsedFile.parseLinesCSV("newfilenameset.txt"); 
        

2. Parse Decision Reports *Filter the file list*

	Utility to reduce the file list to only the 'decision reports' 
        (already prepared with # delimiter)
        EDOfileApp myParsedFile = new EDOfileApp("fullpathsonly.txt"); 
        myParsedFile.parseDecisionReports("transferlist_DR.txt");

3.  Transfer from file list *Use the transfer list to request transfer of files*

	Conduct transfers from the prepared file list 
        EDOfiletransfers myMover = new EDOfiletransfers();
        myMover.transfer("transferlist.txt");
        */

4.  Parse duplicates. *Filter the transfer list so that only the repeated filename 'decision report' is in it*

     Utility to include only the 'decision report.pdf' duplicates in the file transfer list
        EDOfileApp myParsedFile = new EDOfileApp("fullpathsonly.txt"); 
        myParsedFile.parseDuplicates("transferlist_duplicates.txt");
        */

5.  Transfer the files that are in the duplicate reports list *Transfer the replicated files*

     Conduct transfers from a file list of duplicate 'decision reports' 
        EDOfiletransfers myMover = new EDOfiletransfers();
        myMover.transferduplicates("transferlist_duplicates.txt");
        */

6.  Include only the 'decision' files in the transfer list. *Filter so that only repeated 'decision.pdf' files are in the list.

     Utility to include only the 'decision.pdf' files/duplicates in the file transfer list 
        EDOfileApp myParsedFile = new EDOfileApp("fullpathsonly.txt"); 
        myParsedFile.parseDecisionPDF("transferlist_decision_dups.txt");
   
