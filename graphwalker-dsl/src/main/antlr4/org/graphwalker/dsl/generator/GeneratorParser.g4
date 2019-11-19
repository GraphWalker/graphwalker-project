parser grammar GeneratorParser;

options {
	tokenVocab=LogicalLexer;
}

parse
 : (generator)* EOF
 ;

generator
 : Alphanumeric LPAREN parameters RPAREN
 | Alphanumeric LPAREN parameters RPAREN RPAREN {notifyErrorListeners("The generator has too many parentheses");}
 | Alphanumeric LPAREN parameters {notifyErrorListeners("The generator is missing closing parentheses");}
 | Alphanumeric  {notifyErrorListeners("A generator needs parentheses");}
 ;

parameters
 : ( logicalExpression
 | seed COMMA logicalExpression )
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

seed
 : Number
 ;
