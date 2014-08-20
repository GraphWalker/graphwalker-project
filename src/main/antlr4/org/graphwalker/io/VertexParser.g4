parser grammar VertexParser;

options {
	tokenVocab=LabelLexer;
}

parse
 : (start | (name? shared? blocked?))? EOF
 | (start | (blocked? shared? name?))? EOF
 | (start | (shared? name? blocked?))? EOF
 | (start | (blocked? name? shared?))? EOF
 | (start | (name? blocked? shared?))? EOF
 | (start | (shared? blocked? name?))? EOF
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
