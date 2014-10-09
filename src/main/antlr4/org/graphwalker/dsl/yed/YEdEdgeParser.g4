parser grammar YEdEdgeParser;

options {
	tokenVocab=YEdLabelLexer;
}

parse
 locals [java.util.Set<String> fields = new java.util.HashSet<String>();]
 : field* EOF
 ;

field
 : {!$parse::fields.contains("names")}? names {$parse::fields.add("names");}
 | {!$parse::fields.contains("guard")}? guard {$parse::fields.add("guard");}
 | {!$parse::fields.contains("actions")}? actions {$parse::fields.add("actions");}
 | {!$parse::fields.contains("blocked")}? blocked {$parse::fields.add("blocked");}
 | {!$parse::fields.contains("reqtags")}? reqtags {$parse::fields.add("reqtags");}
 ;

actions
 : SLASH (action)+
 ;

action
 : ~(SEMICOLON)+ SEMICOLON
 ;

reqtags
 : REQTAG (COLON | ASSIGN) reqtagList
 ;

reqtagList
 : (reqtag COMMA)* reqtag
 ;

reqtag
 : ~(COMMA)+
 ;

guard
 : NestedBrackets
 ;

blocked
 : BLOCKED
 ;

names
 : name (SEMICOLON name)*
 ;

name
 : Identifier (DOT Identifier)?
 ;
