parser grammar YEdVertexParser;

options {
	tokenVocab=YEdLabelLexer;
}

parse
 locals [java.util.Set<String> fields = new java.util.HashSet<String>();]
 : start
 | field* EOF
 ;

field
 : {!$parse::fields.contains("names")}? names {$parse::fields.add("names");}
 | {!$parse::fields.contains("shared")}? shared {$parse::fields.add("shared");}
 | {!$parse::fields.contains("blocked")}? blocked {$parse::fields.add("blocked");}
 | {!$parse::fields.contains("actions")}? actions {$parse::fields.add("actions");}
 | {!$parse::fields.contains("reqtags")}? reqtags {$parse::fields.add("reqtags");}
 ;

start
 : START
 ;

shared
 : SHARED COLON Identifier
 ;

names
 : name (SEMICOLON name)*
 ;

name
 : Identifier (DOT Identifier)*
 ;

blocked
 : BLOCKED
 ;

actions
 : INIT COLON (action)+
 ;

action
 : ~(SEMICOLON)* SEMICOLON
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