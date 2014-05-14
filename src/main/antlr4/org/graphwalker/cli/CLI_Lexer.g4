lexer grammar CLI_Lexer;

OR
 : '||' | 'or'
 ;

AND
 : '&&' | 'and'
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

INTEGER
 : [0-9]+
 ;

APLHA_NUM
 : [a-zA-Z0-9]+
 ;

IDENTIFIER
 : Letter LetterOrDigit*
 ;

fragment
Letter
 : [a-zA-Z$_]
 | ~[\u0000-\u00FF\uD800-\uDBFF]
   {Character.isJavaIdentifierStart(_input.LA(-1))}?
 | [\uD800-\uDBFF] [\uDC00-\uDFFF]
   {Character.isJavaIdentifierStart(Character.toCodePoint((char)_input.LA(-2), (char)_input.LA(-1)))}?
 ;

fragment
LetterOrDigit
 : [a-zA-Z0-9$_]
 | ~[\u0000-\u00FF\uD800-\uDBFF]
   {Character.isJavaIdentifierPart(_input.LA(-1))}?
 | [\uD800-\uDBFF] [\uDC00-\uDFFF]
   {Character.isJavaIdentifierPart(Character.toCodePoint((char)_input.LA(-2), (char)_input.LA(-1)))}?
 ;
