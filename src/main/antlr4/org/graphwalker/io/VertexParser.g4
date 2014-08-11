parser grammar VertexParser;

options {
	tokenVocab=LabelLexer;
}

parse
 : (start | (name? shared?))? EOF
 | (start | (shared? name?))? EOF
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