// Generated from /home/krikar/dev/mbt/GW3/gw-cli/src/main/antlr4/org/graphwalker/cli/CLI.g4 by ANTLR 4.x
import org.antlr.v4.runtime.Lexer;
import org.antlr.v4.runtime.CharStream;
import org.antlr.v4.runtime.Token;
import org.antlr.v4.runtime.TokenStream;
import org.antlr.v4.runtime.*;
import org.antlr.v4.runtime.atn.*;
import org.antlr.v4.runtime.dfa.DFA;
import org.antlr.v4.runtime.misc.*;

@SuppressWarnings({"all", "warnings", "unchecked", "unused", "cast"})
public class CLILexer extends Lexer {
	protected static final DFA[] _decisionToDFA;
	protected static final PredictionContextCache _sharedContextCache =
		new PredictionContextCache();
	public static final int
		T__9=1, T__8=2, T__7=3, T__6=4, T__5=5, T__4=6, T__3=7, T__2=8, T__1=9, 
		T__0=10, Identifier=11, INTEGER=12, OR=13, AND=14, LPAREN=15, RPAREN=16, 
		WHITESPACE=17;
	public static String[] modeNames = {
		"DEFAULT_MODE"
	};

	public static final String[] tokenNames = {
		"'\\u0000'", "'\\u0001'", "'\\u0002'", "'\\u0003'", "'\\u0004'", "'\\u0005'", 
		"'\\u0006'", "'\\u0007'", "'\b'", "'\t'", "'\n'", "'\\u000B'", "'\f'", 
		"'\r'", "'\\u000E'", "'\\u000F'", "'\\u0010'", "'\\u0011'"
	};
	public static final String[] ruleNames = {
		"T__9", "T__8", "T__7", "T__6", "T__5", "T__4", "T__3", "T__2", "T__1", 
		"T__0", "Identifier", "Letter", "LetterOrDigit", "INTEGER", "OR", "AND", 
		"LPAREN", "RPAREN", "WHITESPACE"
	};


	public CLILexer(CharStream input) {
		super(input);
		_interp = new LexerATNSimulator(this,_ATN,_decisionToDFA,_sharedContextCache);
	}

	@Override
	public String getGrammarFileName() { return "CLI.g4"; }

	@Override
	public String[] getTokenNames() { return tokenNames; }

	@Override
	public String[] getRuleNames() { return ruleNames; }

	@Override
	public String getSerializedATN() { return _serializedATN; }

	@Override
	public String[] getModeNames() { return modeNames; }

	@Override
	public ATN getATN() { return _ATN; }

	@Override
	public boolean sempred(RuleContext _localctx, int ruleIndex, int predIndex) {
		switch (ruleIndex) {
		case 11: return Letter_sempred((RuleContext)_localctx, predIndex);
		case 12: return LetterOrDigit_sempred((RuleContext)_localctx, predIndex);
		}
		return true;
	}
	private boolean LetterOrDigit_sempred(RuleContext _localctx, int predIndex) {
		switch (predIndex) {
		case 2: return Character.isJavaIdentifierPart(_input.LA(-1));
		case 3: return Character.isJavaIdentifierPart(Character.toCodePoint((char)_input.LA(-2), (char)_input.LA(-1)));
		}
		return true;
	}
	private boolean Letter_sempred(RuleContext _localctx, int predIndex) {
		switch (predIndex) {
		case 0: return Character.isJavaIdentifierStart(_input.LA(-1));
		case 1: return Character.isJavaIdentifierStart(Character.toCodePoint((char)_input.LA(-2), (char)_input.LA(-1)));
		}
		return true;
	}

	public static final String _serializedATN =
		"\3\u0430\ud6d1\u8206\uad2d\u4417\uaef1\u8d80\uaadd\2\23\u00cf\b\1\4\2"+
		"\t\2\4\3\t\3\4\4\t\4\4\5\t\5\4\6\t\6\4\7\t\7\4\b\t\b\4\t\t\t\4\n\t\n\4"+
		"\13\t\13\4\f\t\f\4\r\t\r\4\16\t\16\4\17\t\17\4\20\t\20\4\21\t\21\4\22"+
		"\t\22\4\23\t\23\4\24\t\24\3\2\3\2\3\2\3\2\3\2\3\2\3\2\3\2\3\2\3\2\3\2"+
		"\3\2\3\2\3\3\3\3\3\3\3\3\3\3\3\3\3\3\3\4\3\4\3\4\3\4\3\4\3\4\3\4\3\5\3"+
		"\5\3\5\3\5\3\5\3\5\3\5\3\6\3\6\3\6\3\6\3\6\3\6\3\6\3\6\3\6\3\6\3\6\3\6"+
		"\3\6\3\6\3\6\3\6\3\6\3\6\3\6\3\6\3\6\3\7\3\7\3\7\3\7\3\7\3\7\3\b\3\b\3"+
		"\b\3\b\3\b\3\t\3\t\3\t\3\t\3\t\3\t\3\t\3\t\3\t\3\t\3\t\3\t\3\t\3\t\3\n"+
		"\3\n\3\n\3\n\3\n\3\n\3\n\3\n\3\n\3\n\3\n\3\n\3\n\3\n\3\n\3\13\3\13\3\13"+
		"\3\13\3\13\3\13\3\13\3\13\3\13\3\13\3\13\3\13\3\13\3\13\3\13\3\13\3\f"+
		"\3\f\7\f\u009b\n\f\f\f\16\f\u009e\13\f\3\r\3\r\3\r\3\r\3\r\3\r\5\r\u00a6"+
		"\n\r\3\16\3\16\3\16\3\16\3\16\3\16\5\16\u00ae\n\16\3\17\5\17\u00b1\n\17"+
		"\3\17\6\17\u00b4\n\17\r\17\16\17\u00b5\3\20\3\20\3\20\3\20\5\20\u00bc"+
		"\n\20\3\21\3\21\3\21\3\21\3\21\5\21\u00c3\n\21\3\22\3\22\3\23\3\23\3\24"+
		"\6\24\u00ca\n\24\r\24\16\24\u00cb\3\24\3\24\2\2\25\3\3\5\4\7\5\t\6\13"+
		"\7\r\b\17\t\21\n\23\13\25\f\27\r\31\2\33\2\35\16\37\17!\20#\21%\22\'\23"+
		"\3\2\b\6\2&&C\\aac|\4\2\2\u0101\ud802\udc01\3\2\ud802\udc01\3\2\udc02"+
		"\ue001\7\2&&\62;C\\aac|\5\2\13\f\16\17\"\"\u00d6\2\3\3\2\2\2\2\5\3\2\2"+
		"\2\2\7\3\2\2\2\2\t\3\2\2\2\2\13\3\2\2\2\2\r\3\2\2\2\2\17\3\2\2\2\2\21"+
		"\3\2\2\2\2\23\3\2\2\2\2\25\3\2\2\2\2\27\3\2\2\2\2\35\3\2\2\2\2\37\3\2"+
		"\2\2\2!\3\2\2\2\2#\3\2\2\2\2%\3\2\2\2\2\'\3\2\2\2\3)\3\2\2\2\5\66\3\2"+
		"\2\2\7=\3\2\2\2\tD\3\2\2\2\13K\3\2\2\2\r`\3\2\2\2\17f\3\2\2\2\21k\3\2"+
		"\2\2\23y\3\2\2\2\25\u0088\3\2\2\2\27\u0098\3\2\2\2\31\u00a5\3\2\2\2\33"+
		"\u00ad\3\2\2\2\35\u00b0\3\2\2\2\37\u00bb\3\2\2\2!\u00c2\3\2\2\2#\u00c4"+
		"\3\2\2\2%\u00c6\3\2\2\2\'\u00c9\3\2\2\2)*\7t\2\2*+\7g\2\2+,\7c\2\2,-\7"+
		"e\2\2-.\7j\2\2./\7g\2\2/\60\7f\2\2\60\61\7a\2\2\61\62\7g\2\2\62\63\7f"+
		"\2\2\63\64\7i\2\2\64\65\7g\2\2\65\4\3\2\2\2\66\67\7c\2\2\678\7a\2\289"+
		"\7u\2\29:\7v\2\2:;\7c\2\2;<\7t\2\2<\6\3\2\2\2=>\7n\2\2>?\7g\2\2?@\7p\2"+
		"\2@A\7i\2\2AB\7v\2\2BC\7j\2\2C\b\3\2\2\2DE\7t\2\2EF\7c\2\2FG\7p\2\2GH"+
		"\7f\2\2HI\7q\2\2IJ\7o\2\2J\n\3\2\2\2KL\7t\2\2LM\7g\2\2MN\7s\2\2NO\7w\2"+
		"\2OP\7k\2\2PQ\7t\2\2QR\7g\2\2RS\7o\2\2ST\7g\2\2TU\7p\2\2UV\7v\2\2VW\7"+
		"a\2\2WX\7e\2\2XY\7q\2\2YZ\7x\2\2Z[\7g\2\2[\\\7t\2\2\\]\7c\2\2]^\7i\2\2"+
		"^_\7g\2\2_\f\3\2\2\2`a\7p\2\2ab\7g\2\2bc\7x\2\2cd\7g\2\2de\7t\2\2e\16"+
		"\3\2\2\2fg\7v\2\2gh\7k\2\2hi\7o\2\2ij\7g\2\2j\20\3\2\2\2kl\7g\2\2lm\7"+
		"f\2\2mn\7i\2\2no\7g\2\2op\7a\2\2pq\7e\2\2qr\7q\2\2rs\7x\2\2st\7g\2\2t"+
		"u\7t\2\2uv\7c\2\2vw\7i\2\2wx\7g\2\2x\22\3\2\2\2yz\7t\2\2z{\7g\2\2{|\7"+
		"c\2\2|}\7e\2\2}~\7j\2\2~\177\7g\2\2\177\u0080\7f\2\2\u0080\u0081\7a\2"+
		"\2\u0081\u0082\7x\2\2\u0082\u0083\7g\2\2\u0083\u0084\7t\2\2\u0084\u0085"+
		"\7v\2\2\u0085\u0086\7g\2\2\u0086\u0087\7z\2\2\u0087\24\3\2\2\2\u0088\u0089"+
		"\7x\2\2\u0089\u008a\7g\2\2\u008a\u008b\7t\2\2\u008b\u008c\7v\2\2\u008c"+
		"\u008d\7g\2\2\u008d\u008e\7z\2\2\u008e\u008f\7a\2\2\u008f\u0090\7e\2\2"+
		"\u0090\u0091\7q\2\2\u0091\u0092\7x\2\2\u0092\u0093\7g\2\2\u0093\u0094"+
		"\7t\2\2\u0094\u0095\7c\2\2\u0095\u0096\7i\2\2\u0096\u0097\7g\2\2\u0097"+
		"\26\3\2\2\2\u0098\u009c\5\31\r\2\u0099\u009b\5\33\16\2\u009a\u0099\3\2"+
		"\2\2\u009b\u009e\3\2\2\2\u009c\u009a\3\2\2\2\u009c\u009d\3\2\2\2\u009d"+
		"\30\3\2\2\2\u009e\u009c\3\2\2\2\u009f\u00a6\t\2\2\2\u00a0\u00a1\n\3\2"+
		"\2\u00a1\u00a6\6\r\2\2\u00a2\u00a3\t\4\2\2\u00a3\u00a4\t\5\2\2\u00a4\u00a6"+
		"\6\r\3\2\u00a5\u009f\3\2\2\2\u00a5\u00a0\3\2\2\2\u00a5\u00a2\3\2\2\2\u00a6"+
		"\32\3\2\2\2\u00a7\u00ae\t\6\2\2\u00a8\u00a9\n\3\2\2\u00a9\u00ae\6\16\4"+
		"\2\u00aa\u00ab\t\4\2\2\u00ab\u00ac\t\5\2\2\u00ac\u00ae\6\16\5\2\u00ad"+
		"\u00a7\3\2\2\2\u00ad\u00a8\3\2\2\2\u00ad\u00aa\3\2\2\2\u00ae\34\3\2\2"+
		"\2\u00af\u00b1\7/\2\2\u00b0\u00af\3\2\2\2\u00b0\u00b1\3\2\2\2\u00b1\u00b3"+
		"\3\2\2\2\u00b2\u00b4\4\62;\2\u00b3\u00b2\3\2\2\2\u00b4\u00b5\3\2\2\2\u00b5"+
		"\u00b3\3\2\2\2\u00b5\u00b6\3\2\2\2\u00b6\36\3\2\2\2\u00b7\u00b8\7~\2\2"+
		"\u00b8\u00bc\7~\2\2\u00b9\u00ba\7q\2\2\u00ba\u00bc\7t\2\2\u00bb\u00b7"+
		"\3\2\2\2\u00bb\u00b9\3\2\2\2\u00bc \3\2\2\2\u00bd\u00be\7(\2\2\u00be\u00c3"+
		"\7(\2\2\u00bf\u00c0\7c\2\2\u00c0\u00c1\7p\2\2\u00c1\u00c3\7f\2\2\u00c2"+
		"\u00bd\3\2\2\2\u00c2\u00bf\3\2\2\2\u00c3\"\3\2\2\2\u00c4\u00c5\7*\2\2"+
		"\u00c5$\3\2\2\2\u00c6\u00c7\7+\2\2\u00c7&\3\2\2\2\u00c8\u00ca\t\7\2\2"+
		"\u00c9\u00c8\3\2\2\2\u00ca\u00cb\3\2\2\2\u00cb\u00c9\3\2\2\2\u00cb\u00cc"+
		"\3\2\2\2\u00cc\u00cd\3\2\2\2\u00cd\u00ce\b\24\2\2\u00ce(\3\2\2\2\13\2"+
		"\u009c\u00a5\u00ad\u00b0\u00b5\u00bb\u00c2\u00cb\3\b\2\2";
	public static final ATN _ATN =
		new ATNDeserializer().deserialize(_serializedATN.toCharArray());
	static {
		_decisionToDFA = new DFA[_ATN.getNumberOfDecisions()];
		for (int i = 0; i < _ATN.getNumberOfDecisions(); i++) {
			_decisionToDFA[i] = new DFA(_ATN.getDecisionState(i), i);
		}
	}
}