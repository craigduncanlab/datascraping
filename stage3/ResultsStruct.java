
/* class to hold the relevant stats for variance analysis */

public class ResultsStruct {
	String searchText="";
	int Count = 0;
	String resultsText="";

public ResultsStruct() {
	
}

public int getCount() {
	return this.Count;
}

public String getText() {
	return this.searchText;
}

public String getRT() {
	return this.resultsText;
}

public void setText(String myText) {
	this.searchText=myText;
}

public void setStrings(String myRT, String myQ) {
	this.searchText=myQ;
	this.resultsText=myRT;
}

public void setCount(int myCount) {
	this.Count=myCount;
}

public void setRT(String myRT) {
	this.resultsText=myRT;
}



}
