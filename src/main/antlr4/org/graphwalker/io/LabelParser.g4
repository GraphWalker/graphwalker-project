parser grammar LabelParser;

options {
	tokenVocab=LabelLexer;
}

parse
 : name? guard? actions? blocked? EOF
 | blocked? name? guard? actions? EOF
 | actions? guard? name? blocked? EOF
 ;

actions
 : SLASH (action)+
 ;

action
 : ~(SEMICOLON)* SEMICOLON
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

