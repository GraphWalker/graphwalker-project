lexer grammar LogicalLexer;

OR
 : '||' | 'OR' | 'or'
 ;

AND
 : '&&' | 'AND' | 'and'
 ;

LPAREN
 : '('
 ;

RPAREN
 : ')'
 ;

WHITESPACE
 : [ \t\r\n\u000C]+ -> skip
 ;

fragment Digit: '0'..'9' ;
fragment Alpha: '_' | '-' | 'A'..'Z' | 'a'..'z' ;

Number: Digit+ ;
Alphanumeric: (Alpha | Digit)+ ;
