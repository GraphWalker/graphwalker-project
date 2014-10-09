parser grammar YEdEdgeParser;

options {
	tokenVocab=YEdLabelLexer;
}

parse
 : names? guard? actions? blocked? reqtags? EOF
 | names? guard? actions? reqtags? blocked? EOF
 | names? guard? blocked? actions? reqtags? EOF
 | names? guard? blocked? reqtags? actions? EOF
 | names? guard? reqtags? actions? blocked? EOF
 | names? guard? reqtags? blocked? actions? EOF
 | names? actions? guard? reqtags? blocked? EOF
 | names? actions? guard? blocked? reqtags? EOF
 | names? actions? blocked? reqtags? guard? EOF
 | names? actions? blocked? guard? reqtags? EOF
 | names? actions? reqtags? blocked? guard? EOF
 | names? actions? reqtags? guard? blocked? EOF
 | names? blocked? guard? actions? reqtags? EOF
 | names? blocked? guard? reqtags? actions? EOF
 | names? blocked? actions? guard? reqtags? EOF
 | names? blocked? actions? reqtags? guard? EOF
 | names? blocked? reqtags? guard? actions? EOF
 | names? blocked? reqtags? actions? guard? EOF
 | names? reqtags? guard? blocked? actions? EOF
 | names? reqtags? guard? actions? blocked? EOF
 | names? reqtags? actions? blocked? guard? EOF
 | names? reqtags? actions? guard? blocked? EOF
 | names? reqtags? blocked? actions? guard? EOF
 | names? reqtags? blocked? guard? actions? EOF
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
