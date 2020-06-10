
/* Created 9 January 2018 
TO DO: Rename as 'PatternGroup' object
*/

public class QueryOb{
	
//StringBuilder FullPattern = new StringBuilder(100);
String field = "";
String matchpattern="";
String end="";

//constructor
public QueryOb() {
	
}

//loaded constructor all strings
public QueryOb(String myField, String myPattern, String myEnd) {
	this.field = myField;
	this.matchpattern=myPattern;
	this.end=myEnd;
}		

public String getField() {
	return this.field;
}

public String getMatchPattern() {
	return this.matchpattern;
}

public String getEnd() {
	return this.end;
}

/*Using StringBuilder object for append as it's generally quicker than String + 
String in java is immutable, so the use of + necessitates creating a new String each time 
something else is added.  That's costly.
*/

public String getFullPattern() {

	 StringBuilder output=new StringBuilder(200);
	 output.append("(");
	 output.append(this.field);
	 output.append(")(");
	 output.append(this.matchpattern);
	 output.append(")(?:");
	 output.append(this.end);
	 output.append(")"); 
	 return output.toString();
}

public void setField(String myField) {
	this.field = myField;
}

public void setMatchPattern(String myPattern) {
	this.matchpattern = myPattern;
}

public void setEnd(String myEnd) {
	this.end = myEnd;
}


}
