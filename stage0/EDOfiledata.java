/* A class to hold file information for a located file */

public class EDOfiledata{
	String parentdir="";
	String path = "";
	String filename="";
	long size = 0;
//constructor
public EDOfiledata () {
	
}


public void setDir(String myDir) {
	this.parentdir = myDir;
}

public void setPath(String myPath) {
	this.path = myPath;
}

public void setFilename(String myName) {
	this.filename = myName;
}

public void setSize(long mySize) {
	this.size = mySize;
}


}
