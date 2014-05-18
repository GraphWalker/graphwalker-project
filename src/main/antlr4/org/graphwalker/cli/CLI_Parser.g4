parser grammar CLI_Parser;

options {
	tokenVocab=CLI_Lexer;
}

parse
 : (generator)* EOF
 ;

generator
 : Alphanumeric LPAREN logicalExpression RPAREN
 ;

logicalExpression
 :  booleanAndExpression ( OR booleanAndExpression )*
 ;

booleanAndExpression
 : primaryExpression ( AND primaryExpression )*
 ;

primaryExpression
 : stopCondition
 | '(' logicalExpression ')'
 ;

stopCondition
 : (Alphanumeric LPAREN Alphanumeric RPAREN
 | Alphanumeric LPAREN Number RPAREN
 | Alphanumeric)
 ;

