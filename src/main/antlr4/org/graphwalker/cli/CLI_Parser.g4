parser grammar CLI_Parser;

options {
	tokenVocab=CLI_Lexer;
}

parse
 : (IDENTIFIER LPAREN logicalExpression RPAREN)* EOF
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
 : (IDENTIFIER LPAREN APLHA_NUM RPAREN
 || IDENTIFIER)
 ;

