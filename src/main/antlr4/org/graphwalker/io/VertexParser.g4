parser grammar VertexParser;

options {
	tokenVocab=LabelLexer;
}

parse
 : name? shared? EOF
 | shared? name? EOF
 ;

shared
 : SHARED COLON Identifier
 ;

name
 : Identifier
 ;