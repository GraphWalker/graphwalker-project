parser grammar YEdVertexParser;

options {
	tokenVocab=YEdLabelLexer;
}

parse
 : (start | (name? shared? blocked? actions?)) EOF
 | (start | (name? shared? actions? blocked?)) EOF
 | (start | (name? blocked? shared? actions?)) EOF
 | (start | (name? blocked? actions? shared?)) EOF
 | (start | (name? actions? shared? blocked?)) EOF
 | (start | (name? actions? blocked? shared?)) EOF
 | (start | (shared? name? actions? blocked?)) EOF
 | (start | (shared? name? blocked? actions?)) EOF
 | (start | (shared? blocked? actions? name?)) EOF
 | (start | (shared? blocked? name? actions?)) EOF
 | (start | (shared? actions? blocked? name?)) EOF
 | (start | (shared? actions? name? blocked?)) EOF
 | (start | (blocked? name? shared? actions?)) EOF
 | (start | (blocked? name? actions? shared?)) EOF
 | (start | (blocked? shared? name? actions?)) EOF
 | (start | (blocked? shared? actions? name?)) EOF
 | (start | (blocked? actions? name? shared?)) EOF
 | (start | (blocked? actions? shared? name?)) EOF
 | (start | (actions? name? blocked? shared?)) EOF
 | (start | (actions? name? shared? blocked?)) EOF
 | (start | (actions? shared? blocked? name?)) EOF
 | (start | (actions? shared? name? blocked?)) EOF
 | (start | (actions? blocked? shared? name?)) EOF
 | (start | (actions? blocked? name? shared?)) EOF
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
