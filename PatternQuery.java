
/* Created 9 January 2018 */

public class PatternQuery{
	

String field = "";
String matchpattern="";
String end="";

//constructor
public PatternQuery() {
	
}

//loaded constructor
public PatternQuery(String myField, String myPattern, String myEnd) {
	this.field = myField;
	this.matchpattern=myPattern;
	this.end=myEnd;
}	

public void getField() {
	return this.field;
}

public void getMatchPattern() {
	return this.matchpattern;
}

public void getEnd() {
	return this.end;
}

public String getFullPattern() {
	 String output="("+this.field+")("+this.matchpattern+")(?:"+this.end+")"; 
	 return output;
}

public String setField(String myField) {
	this.field = myField;
}

public void setMatchPattern(String myPattern) {
	this.matchpattern = myPattern;
}

public void setEnd(String myEnd) {
	this.end = myEnd;
}


}