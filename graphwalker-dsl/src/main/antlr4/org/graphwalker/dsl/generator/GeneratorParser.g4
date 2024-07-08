parser grammar GeneratorParser;

options {
	tokenVocab=LogicalLexer;
}

parse
 : (generator)* EOF
 ;

generator
 : Alphanumeric LPAREN logicalExpression RPAREN
 | Alphanumeric LPAREN logicalExpression RPAREN RPAREN {notifyErrorListeners("The generator has too many parentheses");}
 | Alphanumeric LPAREN logicalExpression {notifyErrorListeners("The generator is missing closing parentheses");}
 | Alphanumeric  {notifyErrorListeners("A generator needs parentheses");}
 ;

logicalExpression
 : primaryExpression
 | andExpression
 | orExpression
 ;

andExpression
 : primaryExpression ( AND primaryExpression )*
 ;

orExpression
 : primaryExpression ( OR primaryExpression )*
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

