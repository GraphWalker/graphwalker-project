parser grammar YEdVertexParser;

options {
	tokenVocab=YEdLabelLexer;
}

parse
  : (start | (names? shared? blocked? actions? reqtags?)) EOF
  | (start | (names? shared? blocked? reqtags? actions?)) EOF
  | (start | (names? shared? actions? blocked? reqtags?)) EOF
  | (start | (names? shared? actions? reqtags? blocked?)) EOF
  | (start | (names? shared? reqtags? blocked? actions?)) EOF
  | (start | (names? shared? reqtags? actions? blocked?)) EOF
  | (start | (names? blocked? shared? reqtags? actions?)) EOF
  | (start | (names? blocked? shared? actions? reqtags?)) EOF
  | (start | (names? blocked? actions? reqtags? shared?)) EOF
  | (start | (names? blocked? actions? shared? reqtags?)) EOF
  | (start | (names? blocked? reqtags? actions? shared?)) EOF
  | (start | (names? blocked? reqtags? shared? actions?)) EOF
  | (start | (names? actions? shared? blocked? reqtags?)) EOF
  | (start | (names? actions? shared? reqtags? blocked?)) EOF
  | (start | (names? actions? blocked? shared? reqtags?)) EOF
  | (start | (names? actions? blocked? reqtags? shared?)) EOF
  | (start | (names? actions? reqtags? shared? blocked?)) EOF
  | (start | (names? actions? reqtags? blocked? shared?)) EOF
  | (start | (names? reqtags? shared? actions? blocked?)) EOF
  | (start | (names? reqtags? shared? blocked? actions?)) EOF
  | (start | (names? reqtags? blocked? actions? shared?)) EOF
  | (start | (names? reqtags? blocked? shared? actions?)) EOF
  | (start | (names? reqtags? actions? blocked? shared?)) EOF
  | (start | (names? reqtags? actions? shared? blocked?)) EOF
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
 : Identifier (DOT Identifier)?
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