parser grammar YEdEdgeParser;

options {
	tokenVocab=YEdLabelLexer;
}

parse
 : name? guard? actions? blocked? EOF
 | name? actions? blocked? guard? EOF
 | name? blocked? guard? actions? EOF
 | guard? actions? blocked? name? EOF
 | guard? blocked? name? actions? EOF
 | guard? name? actions? blocked? EOF
 | actions? blocked? name? guard? EOF
 | actions? name? guard? blocked? EOF
 | actions? guard? blocked? name? EOF
 | blocked? name? guard? actions? EOF
 | blocked? guard? actions? name? EOF
 | blocked? actions? name? guard? EOF
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
