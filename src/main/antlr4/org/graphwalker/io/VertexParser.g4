parser grammar VertexParser;

options {
	tokenVocab=LabelLexer;
}

parse
 : (start | (name? shared? actions?))? EOF
 | (start | (actions? shared? name?))? EOF
 | (start | (shared? name? actions?))? EOF
 | (start | (actions? name? shared?))? EOF
 | (start | (name? actions? shared?))? EOF
 | (start | (shared? actions? name?))? EOF
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

actions
 : INIT COLON (action)+
 ;

action
 : ~(SEMICOLON)* SEMICOLON
 ;
