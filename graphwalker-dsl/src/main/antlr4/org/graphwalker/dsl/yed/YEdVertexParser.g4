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
 | WHITESPACE
 ;

start
 : WHITESPACE* (START) WHITESPACE*
 ;

shared
 : SHARED WHITESPACE* COLON WHITESPACE* Identifier
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
 : INIT WHITESPACE* COLON WHITESPACE* (action)+
 ;

action
 : .+ SEMICOLON
 ;

reqtags
 : REQTAG WHITESPACE* (COLON | ASSIGN) WHITESPACE* reqtagList
 ;

reqtagList
 : (reqtag WHITESPACE* COMMA WHITESPACE*)* reqtag
 ;

reqtag
 : ~(COMMA)+
 ;
