lexer grammar YEdLabelLexer;

DOT       : '.';
SLASH     : '/';
COLON     : ':';
SEMICOLON : ';';
COMMA     : ',';
ASSIGN    : '=';
BLOCKED   : 'BLOCKED';
SHARED    : 'SHARED';
INIT      : 'INIT';
START     : [Ss][Tt][Aa][Rr][Tt];
REQTAG    : 'REQTAG';
WEIGHT    : [Ww][Ee][Ii][Gg][Hh][Tt];
DEPENDENCY    : [Dd][Ee][Pp][Ee][Nn][Dd][Ee][Nn][Cc][Yy];

NestedBrackets
 :  '[' ( ~('[' | ']') | NestedBrackets )* ']'
 ;

Identifier
 : Letter LetterOrDigit*
 ;

String
 : '"' ~["] '"'
 ;

Value
 : Integer | Integer? ('.' Digit+)
 ;

fragment
Integer
 : '0'
 | NonZeroDigit Digit*
 ;

fragment
Digit
 : '0'
 | NonZeroDigit
 ;

fragment
NonZeroDigit
 : [1-9]
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

COMMENT
 : '/*' .*? '*/' -> skip
 ;

LINE_COMMENT
 :   WHITESPACE '//' ~[\r\n]* -> skip
 ;

WHITESPACE
 : [ \t\r\n\u000C]+
 ;

ANY
 : .
 ;

