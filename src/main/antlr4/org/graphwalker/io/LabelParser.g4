parser grammar LabelParser;

options {
	tokenVocab=LabelLexer;
}

parse
 : name? guard? actions? blocked? shared? EOF
 | name? actions? blocked? shared? guard? EOF
 | name? blocked? shared? guard? actions? EOF
 | name? shared? guard? actions? blocked? EOF
 | guard? actions? blocked? shared? name? EOF
 | guard? blocked? shared? name? actions? EOF
 | guard? shared? name? actions? blocked? EOF
 | guard? name? actions? blocked? shared? EOF
 | actions? blocked? shared? name? guard? EOF
 | actions? shared? name? guard? blocked? EOF
 | actions? name? guard? blocked? shared? EOF
 | actions? guard? blocked? shared? name? EOF
 | blocked? shared? name? guard? actions? EOF
 | blocked? name? guard? actions? shared? EOF
 | blocked? guard? actions? shared? name? EOF
 | blocked? actions? shared? name? guard? EOF
 | shared? name? guard? actions? blocked? EOF
 | shared? guard? actions? blocked? name? EOF
 | shared? actions? blocked? name? guard? EOF
 | shared? blocked? name? guard? actions? EOF
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

shared
 : SHARED COLON Identifier
 ;

name
 : Identifier
 ;

