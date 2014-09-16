parser grammar YEdVertexParser;

options {
	tokenVocab=YEdLabelLexer;
}

parse
  : (start | (name? shared? blocked? actions? reqtags?)) EOF
  | (start | (name? shared? blocked? reqtags? actions?)) EOF
  | (start | (name? shared? actions? blocked? reqtags?)) EOF
  | (start | (name? shared? actions? reqtags? blocked?)) EOF
  | (start | (name? shared? reqtags? blocked? actions?)) EOF
  | (start | (name? shared? reqtags? actions? blocked?)) EOF
  | (start | (name? blocked? shared? reqtags? actions?)) EOF
  | (start | (name? blocked? shared? actions? reqtags?)) EOF
  | (start | (name? blocked? actions? reqtags? shared?)) EOF
  | (start | (name? blocked? actions? shared? reqtags?)) EOF
  | (start | (name? blocked? reqtags? actions? shared?)) EOF
  | (start | (name? blocked? reqtags? shared? actions?)) EOF
  | (start | (name? actions? shared? blocked? reqtags?)) EOF
  | (start | (name? actions? shared? reqtags? blocked?)) EOF
  | (start | (name? actions? blocked? shared? reqtags?)) EOF
  | (start | (name? actions? blocked? reqtags? shared?)) EOF
  | (start | (name? actions? reqtags? shared? blocked?)) EOF
  | (start | (name? actions? reqtags? blocked? shared?)) EOF
  | (start | (name? reqtags? shared? actions? blocked?)) EOF
  | (start | (name? reqtags? shared? blocked? actions?)) EOF
  | (start | (name? reqtags? blocked? actions? shared?)) EOF
  | (start | (name? reqtags? blocked? shared? actions?)) EOF
  | (start | (name? reqtags? actions? blocked? shared?)) EOF
  | (start | (name? reqtags? actions? shared? blocked?)) EOF
  ;

start
 : START
 ;

shared
 : SHARED COLON Identifier
 ;

name
 : Identifier
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
 : REQTAG (COLON | ASSIGN) reqtagn
 ;

reqtagn
 : ((reqtag) COMMA)* reqtag
 ;

reqtag
 : ~(COMMA)+
 ;