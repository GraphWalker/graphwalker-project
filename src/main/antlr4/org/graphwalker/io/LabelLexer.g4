lexer grammar LabelLexer;

SLASH     : '/';
COLON     : ':';
SEMICOLON : ';';
BLOCKED   : 'BLOCKED';
SHARED    : 'SHARED';

NestedBrackets
 :  '[' ( ~('[' | ']') | NestedBrackets )* ']'
 ;

Identifier
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

WHITESPACE
 : [ \t\r\n\u000C]+ -> skip
 ;

COMMENT
 : '/*' .*? '*/' -> skip
 ;

ANY
 : .
 ;

