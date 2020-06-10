
/* 


15.12.2017
This is an FTPClient app that will utilise Apache Commons Net libraries for convenience.
https://commons.apache.org/proper/commons-net/

They are installed in a series of subfolders inside a 'java' folder.
(alternatively copy the 40MB of org.apache etc subfolders into same folder as this and other app files)

If classpath is not set otherwise, place this file in the main 'java' folder supplied by the Commons Net java packages.

The primary purpose of this client is to retrieve files from a directory structure on an FTP server.
*/

import org.apache.commons.net.ftp.*;
import java.io.IOException;
import java.util.*; //arraylist


public class EDOclient {

FTPClient myFTPClient;
myQ queue = new myQ();
ArrayList<EDOfiledata> filenameA = new ArrayList<EDOfiledata>(); //needed or use FTPFile[]?
LinkedList<String> dirqueue = new LinkedList<String>();
String currentdir = "";
int globalFiles=0;
long globalSize=0;
//default
EDOfileApp fileWrite;

//basic constructor with default file
public EDOclient() {
   fileWrite = new EDOfileApp("permits.txt");
}

//constructor with filename for output supplied
public EDOclient(String myOutput) {
    fileWrite = new EDOfileApp(myOutput);
}


public void start() {
        this.connect();
    }


public void connect() {
        myFTPClient= new FTPClient();
        FTPClientConfig config = new FTPClientConfig(); //this creates a new socket for you.
        System.out.println ("EDO file client started...attempting login");
        //---handle connection
        String server = "ftp.dec.wa.gov.au";
        int port = 21; //FTP default - not needed?
        int reply;
        boolean error = false;
        //myFTPClient.connect(host,port);
        //
        try {

        myFTPClient.connect(server);
        //myFTPClient.connect(host,port);
        System.out.println("Connected to " + server + ".");
        System.out.print(myFTPClient.getReplyString());
         
         //verify connection success
         reply = myFTPClient.getReplyCode();
        
         if(!FTPReply.isPositiveCompletion(reply)) {
          myFTPClient.disconnect();
          System.err.println("FTP server refused connection.");
          System.exit(1);
        }
        //passive mode helps a lot - enables the FTP listing to occur
        myFTPClient.enterLocalPassiveMode();
        //FTP Login
        String username = "anonymous"; //see https://tools.ietf.org/html/rfc1635
        String password = "anonymous@domain.com"; //in case there is email parsing instead of 'guest'
        myFTPClient.login(username,password); //needed or just do default?
        String myStatus = myFTPClient.getStatus();
        System.out.println ("EDO file status is : "+myStatus);
        /* here is an API example that is supplied - the engine could be useful
        FTPListParseEngine engine = f.initiateListParsing(directory);
 
 *       while (engine.hasNext()) {
 *       FTPFile[] files = engine.getNext(25);  // "page size" you want
 *       //do whatever you want with these files, display them, etc.
 *       //expensive FTPFile objects not created until needed.
        */

        // Intro
        System.out.println("DEC Permit file information. \nToday's date: 18 December 2017\n\n");
        String[] header = {"count","pathname","size","download total"};
        fileWrite.addDelimRecord(4,header,"#");
        //STAGE 1: PUT A DIRECTORY ON 'QUEUE'
        
        String pathname = "Permit";
        String thisdir ="/";
        /* option to output this 
        System.out.println ("Present directory is : "+myFTPClient.printWorkingDirectory());
        */
        //change directory to next item on queue
        Boolean success=myFTPClient.changeWorkingDirectory(pathname);
        String directory = myFTPClient.printWorkingDirectory();
        if (success==true) {
                /* Optional output
                System.out.println ("Present directory is : "+directory);
                */
                //String[] myDirList = myFTPClient.listNames();
                
        }
        
        //STAGE 2: PROCESS FIRST DIRECTORY (INCLUDES ADDING TO QUEUE)
        int check = processDirectory(directory);
        //Obtain list of 'files' in the current directory
        //now we have to create a file list so interface can use it
        //FTPFileList list = myFTPClient.createFileList(directory, parser);
        
        //String[] ftpFiles = myFTPClient.listNames(); //String is ok but FTPFile has some metadata
       
        
        
        //work through queue, processing directories each time
        //queue.push(ftpFiles);//put first list on queue for iteration

        //STAGE 2: PROCESS CURRENT DIRECTORY POPPED OFF STACK
        //TO DO: keep track of path to each directory in a queue item to come back out.
        //Tree structure for navigation - not in Apache library?
        

        while (!dirqueue.isEmpty()) {
            String directoryname = dirqueue.pop(); //use it like a stack (i.e. remove last entry)
            //change directory subroutine    
            Boolean ok =myFTPClient.changeWorkingDirectory(directoryname);
            //String currentdir = myFTPClient.printWorkingDirectory();
            if (ok==true) {
                /* optional output
                System.out.println ("Present directory is : "+directoryname);
                */
                //String[] myDirList = myFTPClient.listNames();   
                int result = processDirectory(directoryname);  
                if (result==0) {
                    Boolean test = myFTPClient.changeToParentDirectory();
                }
            }
            
        }

        /*WAIT
        
        String next = queue.pull();
        */
        /* WAIT
        //*Get and print directory
        
        System.out.println(pathname+" listing:");
        */

        //STAGE 3: FINISH AND WRAP UP

        myFTPClient.logout(); //not needed?
        //myFTPClient.disconnect();

        } catch(IOException e) {
        error = true;
        e.printStackTrace();
         } finally {
        if(myFTPClient.isConnected()) {
         try {
        myFTPClient.disconnect();
        } catch(IOException ioe) {
           // do nothing
          }
        }
      }
}


/* Method to process directory */
public int processDirectory(String rootdir) {

        Boolean error=false;
        
        //String rootdir = "/"+dirname;
        FTPFile[] ftpFiles=null;
        //STAGE 1: Check contents of this directory
        try {
            ftpFiles = myFTPClient.listFiles();
            /* Optional output
            System.out.println ("Processing folder : "+rootdir+" ("+ftpFiles.length+") files or DIRs\n");
            */
            if (ftpFiles.length>0 && ftpFiles!=null) {
                    for (FTPFile file : ftpFiles)
                        {
                            //System.out.println(file.getRawListing()); //.getName()
                            if (file.isDirectory()) {
                                //FTPFile[] listFiles
                                dirqueue.push(rootdir+"/"+file.getName()); //Use it like a stack
                            }
                            if (file.isFile()) {
                                globalFiles++;
                                globalSize=globalSize+file.getSize();
                                String filepath=rootdir;
                                String filepathname = rootdir+"/"+file.getName();
                                String name = file.getName();
                                String filecount=Integer.toString(globalFiles);
                                String filesize=Long.toString(file.getSize());
                                String download=Long.toString(globalSize);
                                System.out.print(globalFiles+". Filename : "+filepath+"(size : "+file.getSize()+")(Total:"+globalSize+")\n");
                                String[]row={filecount,name,filepath,filepathname,filesize,download};
                                fileWrite.addDelimRecord(6,row,"#");
                                //System.out.print("size : "+file.getSize()+"\n");
                                /* If we want to store this in an object rather than console:
                                EDOfiledata b = new EDOfiledata();
                                b.setDir(dirname);
                                //b.setPath(path); //TO DO: path needs an update
                                b.setFilename(file.getName());
                                b.setSize(file.getSize());
                                */
                                //TO DO: Push into general filename container
                            }
                            
                        }
                    }
                    else {
                        /* System.out.println ("Folder : "+rootdir+" is empty.");
                        //TO DO: write this up in separate file for record of information
                        */
                        return 0;
                    }

                } catch(IOException e) {
                    error = true;
                    e.printStackTrace();
         } 
    return ftpFiles.length;
}

/* Method to put details of files and put folders on queue */
/*
public void processFTPfiles() {

        //SETUP
        FTPFile[] ftpFiles = myFTPClient.listFiles();
        System.out.println("Number of files or DIR found : "+ftpFiles.length);

        //STAGE 1: Check this directory
        System.out.println ("checking this folder");
        System.out.println ("length : "+myList.length);
        
                for (FTPFile file : myList)
                {
                    //System.out.println(file.getRawListing()); //.getName()
                    if (file.isDirectory()) {
                        //FTPFile[] listFiles
                        queue.push(file.listFiles()); //Use it like a stack
                    }
                    if (file.isFile()) {
                        System.out.print("filename : "+file.getName()+"\n");
                        EDOfiledata b = new EDOfiledata();
                        b.setDir(directory);
                        b.setPath(path); //TO DO: path needs an update
                        b.setFilename(file.getName());
                        //TO DO: Push into general filename container
                    }
                    
                }
                */
        /*
        //STAGE 2 - Check the subfolders of the Permit directory, one by one
        Iterator<FTPFile> myIterator = this.dirqueue.iterator();
        while (myIterator.hasNext()) {
            FTPFile subdirectory = myIterator.next();
            String subdirname = subdirectory.getName();
            path = path+"/"+subdirname;
            processFTPFile()
            /*
            FTPFile subdirectory = myIterator.next();
            String subdirname = subdirectory.getName();
            boolean error = false;
            try {
                Boolean success=myFTPClient.changeWorkingDirectory(subdirname);
                //String newdirectory = myFTPClient.printWorkingDirectory();
                if (success==true) {
                        System.out.println ("Present directory is : "+subdirname);
                        //String[] myDirList = myFTPClient.listNames();   
                        //System.out.println(subdirectory.getRawListing());    
                }
                //GO BACK UP ONE LEVEL
                success=myFTPClient.changeWorkingDirectory("..");
            } catch(IOException e) {
                error = true;
                e.printStackTrace();
            }
            
        }

         //FTPFile[] ftpFiles = myFTPClient.listFiles("Permit");  //list the subdirectory directly
                    /*
                if (myDirList != null && myDirList.length > 0) {
                        for (String aFile: myDirList) {
                           System.out.println(aFile+"\n");
                        }
                }
                else {
                    System.out.println("returned nothing");
                }
               
}
 */

public void processFolder(String[] myList) {


        //FTPFile[] ftpFiles = myFTPClient.listFiles("Permit");  //list the subdirectory directly
        System.out.println ("checking this folder:");
        System.out.println ("length : "+myList.length);
                for (String file : myList)
                {
                    System.out.println(file); //.getName()
                    //queue.push(file);
                }
        }

        /*
                if (myDirList != null && myDirList.length > 0) {
                        for (String aFile: myDirList) {
                           System.out.println(aFile+"\n");
                        }
                }
                else {
                    System.out.println("returned nothing");
                }
               
        
        success=myFTPClient.changeWorkingDirectory("1017");
        if (success==true) {
                System.out.println ("Present directory is : "+myFTPClient.printWorkingDirectory());
        }
        else {
            System.out.println("done");
        }
        ftpFiles = myFTPClient.listNames();
        System.out.println ("length : "+ftpFiles.length);
                for (String file : ftpFiles)
                {
                    System.out.println(file);
                }
         success=myFTPClient.changeWorkingDirectory("Permit");
         if (success==true) {
                System.out.println ("Present directory is : "+myFTPClient.printWorkingDirectory());
        }
        else {
            System.out.println("done");
        }
        ftpFiles = myFTPClient.listNames();
        System.out.println ("length : "+ftpFiles.length);
                for (String file : ftpFiles)
                {
                    System.out.println(file);
                }
}
    */
}
