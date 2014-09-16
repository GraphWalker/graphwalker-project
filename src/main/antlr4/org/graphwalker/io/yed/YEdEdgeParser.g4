parser grammar YEdEdgeParser;

options {
	tokenVocab=YEdLabelLexer;
}

parse
 : name? guard? actions? blocked? reqtags? EOF
 | name? guard? actions? reqtags? blocked? EOF
 | name? guard? blocked? actions? reqtags? EOF
 | name? guard? blocked? reqtags? actions? EOF
 | name? guard? reqtags? actions? blocked? EOF
 | name? guard? reqtags? blocked? actions? EOF
 | name? actions? guard? reqtags? blocked? EOF
 | name? actions? guard? blocked? reqtags? EOF
 | name? actions? blocked? reqtags? guard? EOF
 | name? actions? blocked? guard? reqtags? EOF
 | name? actions? reqtags? blocked? guard? EOF
 | name? actions? reqtags? guard? blocked? EOF
 | name? blocked? guard? actions? reqtags? EOF
 | name? blocked? guard? reqtags? actions? EOF
 | name? blocked? actions? guard? reqtags? EOF
 | name? blocked? actions? reqtags? guard? EOF
 | name? blocked? reqtags? guard? actions? EOF
 | name? blocked? reqtags? actions? guard? EOF
 | name? reqtags? guard? blocked? actions? EOF
 | name? reqtags? guard? actions? blocked? EOF
 | name? reqtags? actions? blocked? guard? EOF
 | name? reqtags? actions? guard? blocked? EOF
 | name? reqtags? blocked? actions? guard? EOF
 | name? reqtags? blocked? guard? actions? EOF
 ;

actions
 : SLASH (action)+
 ;

action
 : ~(SEMICOLON)+ SEMICOLON
 ;

reqtags
 : REQTAG (COLON | ASSIGN) reqtagn
 ;

reqtagn
 : ((reqtag) COMMA)* reqtag
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

name
 : Identifier
 ;
