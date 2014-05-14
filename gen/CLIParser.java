// Generated from /home/krikar/dev/mbt/GW3/gw-cli/src/main/antlr4/org/graphwalker/cli/CLI.g4 by ANTLR 4.x
import org.antlr.v4.runtime.atn.*;
import org.antlr.v4.runtime.dfa.DFA;
import org.antlr.v4.runtime.*;
import org.antlr.v4.runtime.misc.*;
import org.antlr.v4.runtime.tree.*;
import java.util.List;
import java.util.Iterator;
import java.util.ArrayList;

@SuppressWarnings({"all", "warnings", "unchecked", "unused", "cast"})
public class CLIParser extends Parser {
	protected static final DFA[] _decisionToDFA;
	protected static final PredictionContextCache _sharedContextCache =
		new PredictionContextCache();
	public static final int
		T__9=1, T__8=2, T__7=3, T__6=4, T__5=5, T__4=6, T__3=7, T__2=8, T__1=9, 
		T__0=10, Identifier=11, INTEGER=12, OR=13, AND=14, LPAREN=15, RPAREN=16, 
		WHITESPACE=17;
	public static final String[] tokenNames = {
		"<INVALID>", "'reached_edge'", "'a_star'", "'length'", "'random'", "'requirement_coverage'", 
		"'never'", "'time'", "'edge_coverage'", "'reached_vertex'", "'vertex_coverage'", 
		"Identifier", "INTEGER", "OR", "AND", "'('", "')'", "WHITESPACE"
	};
	public static final int
		RULE_parse = 0, RULE_logicalExpression = 1, RULE_booleanAndExpression = 2, 
		RULE_primaryExpression = 3, RULE_stopCondition = 4, RULE_generator = 5, 
		RULE_value = 6, RULE_random = 7, RULE_a_star = 8, RULE_edge_coverage = 9, 
		RULE_vertex_coverage = 10, RULE_reached_edge = 11, RULE_reached_vertex = 12, 
		RULE_length = 13, RULE_time = 14, RULE_requirement_coverage = 15, RULE_never = 16;
	public static final String[] ruleNames = {
		"parse", "logicalExpression", "booleanAndExpression", "primaryExpression", 
		"stopCondition", "generator", "value", "random", "a_star", "edge_coverage", 
		"vertex_coverage", "reached_edge", "reached_vertex", "length", "time", 
		"requirement_coverage", "never"
	};

	@Override
	public String getGrammarFileName() { return "CLI.g4"; }

	@Override
	public String[] getTokenNames() { return tokenNames; }

	@Override
	public String[] getRuleNames() { return ruleNames; }

	@Override
	public String getSerializedATN() { return _serializedATN; }

	@Override
	public ATN getATN() { return _ATN; }

	public CLIParser(TokenStream input) {
		super(input);
		_interp = new ParserATNSimulator(this,_ATN,_decisionToDFA,_sharedContextCache);
	}
	public static class ParseContext extends ParserRuleContext {
		public TerminalNode EOF() { return getToken(CLIParser.EOF, 0); }
		public GeneratorContext generator() {
			return getRuleContext(GeneratorContext.class,0);
		}
		public TerminalNode RPAREN() { return getToken(CLIParser.RPAREN, 0); }
		public StopConditionContext stopCondition() {
			return getRuleContext(StopConditionContext.class,0);
		}
		public TerminalNode LPAREN() { return getToken(CLIParser.LPAREN, 0); }
		public ParseContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_parse; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof CLIListener ) ((CLIListener)listener).enterParse(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof CLIListener ) ((CLIListener)listener).exitParse(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof CLIVisitor ) return ((CLIVisitor<? extends T>)visitor).visitParse(this);
			else return visitor.visitChildren(this);
		}
	}

	public final ParseContext parse() throws RecognitionException {
		ParseContext _localctx = new ParseContext(_ctx, getState());
		enterRule(_localctx, 0, RULE_parse);
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(34); generator();
			setState(35); match(LPAREN);
			setState(36); stopCondition();
			setState(37); match(RPAREN);
			setState(38); match(EOF);
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	public static class LogicalExpressionContext extends ParserRuleContext {
		public BooleanAndExpressionContext booleanAndExpression(int i) {
			return getRuleContext(BooleanAndExpressionContext.class,i);
		}
		public List<BooleanAndExpressionContext> booleanAndExpression() {
			return getRuleContexts(BooleanAndExpressionContext.class);
		}
		public List<TerminalNode> OR() { return getTokens(CLIParser.OR); }
		public TerminalNode OR(int i) {
			return getToken(CLIParser.OR, i);
		}
		public LogicalExpressionContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_logicalExpression; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof CLIListener ) ((CLIListener)listener).enterLogicalExpression(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof CLIListener ) ((CLIListener)listener).exitLogicalExpression(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof CLIVisitor ) return ((CLIVisitor<? extends T>)visitor).visitLogicalExpression(this);
			else return visitor.visitChildren(this);
		}
	}

	public final LogicalExpressionContext logicalExpression() throws RecognitionException {
		LogicalExpressionContext _localctx = new LogicalExpressionContext(_ctx, getState());
		enterRule(_localctx, 2, RULE_logicalExpression);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(40); booleanAndExpression();
			setState(45);
			_errHandler.sync(this);
			_la = _input.LA(1);
			while (_la==OR) {
				{
				{
				setState(41); match(OR);
				setState(42); booleanAndExpression();
				}
				}
				setState(47);
				_errHandler.sync(this);
				_la = _input.LA(1);
			}
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	public static class BooleanAndExpressionContext extends ParserRuleContext {
		public List<TerminalNode> AND() { return getTokens(CLIParser.AND); }
		public PrimaryExpressionContext primaryExpression(int i) {
			return getRuleContext(PrimaryExpressionContext.class,i);
		}
		public TerminalNode AND(int i) {
			return getToken(CLIParser.AND, i);
		}
		public List<PrimaryExpressionContext> primaryExpression() {
			return getRuleContexts(PrimaryExpressionContext.class);
		}
		public BooleanAndExpressionContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_booleanAndExpression; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof CLIListener ) ((CLIListener)listener).enterBooleanAndExpression(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof CLIListener ) ((CLIListener)listener).exitBooleanAndExpression(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof CLIVisitor ) return ((CLIVisitor<? extends T>)visitor).visitBooleanAndExpression(this);
			else return visitor.visitChildren(this);
		}
	}

	public final BooleanAndExpressionContext booleanAndExpression() throws RecognitionException {
		BooleanAndExpressionContext _localctx = new BooleanAndExpressionContext(_ctx, getState());
		enterRule(_localctx, 4, RULE_booleanAndExpression);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(48); primaryExpression();
			setState(53);
			_errHandler.sync(this);
			_la = _input.LA(1);
			while (_la==AND) {
				{
				{
				setState(49); match(AND);
				setState(50); primaryExpression();
				}
				}
				setState(55);
				_errHandler.sync(this);
				_la = _input.LA(1);
			}
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	public static class PrimaryExpressionContext extends ParserRuleContext {
		public LogicalExpressionContext logicalExpression() {
			return getRuleContext(LogicalExpressionContext.class,0);
		}
		public StopConditionContext stopCondition() {
			return getRuleContext(StopConditionContext.class,0);
		}
		public PrimaryExpressionContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_primaryExpression; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof CLIListener ) ((CLIListener)listener).enterPrimaryExpression(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof CLIListener ) ((CLIListener)listener).exitPrimaryExpression(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof CLIVisitor ) return ((CLIVisitor<? extends T>)visitor).visitPrimaryExpression(this);
			else return visitor.visitChildren(this);
		}
	}

	public final PrimaryExpressionContext primaryExpression() throws RecognitionException {
		PrimaryExpressionContext _localctx = new PrimaryExpressionContext(_ctx, getState());
		enterRule(_localctx, 6, RULE_primaryExpression);
		try {
			setState(61);
			switch (_input.LA(1)) {
			case LPAREN:
				enterOuterAlt(_localctx, 1);
				{
				setState(56); match(LPAREN);
				setState(57); logicalExpression();
				setState(58); match(RPAREN);
				}
				break;
			case T__9:
			case T__7:
			case T__5:
			case T__4:
			case T__3:
			case T__2:
			case T__1:
			case T__0:
			case OR:
			case AND:
			case RPAREN:
				enterOuterAlt(_localctx, 2);
				{
				setState(60); stopCondition();
				}
				break;
			default:
				throw new NoViableAltException(this);
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	public static class StopConditionContext extends ParserRuleContext {
		public LengthContext length() {
			return getRuleContext(LengthContext.class,0);
		}
		public Reached_edgeContext reached_edge() {
			return getRuleContext(Reached_edgeContext.class,0);
		}
		public NeverContext never() {
			return getRuleContext(NeverContext.class,0);
		}
		public TimeContext time() {
			return getRuleContext(TimeContext.class,0);
		}
		public TerminalNode Identifier() { return getToken(CLIParser.Identifier, 0); }
		public TerminalNode RPAREN() { return getToken(CLIParser.RPAREN, 0); }
		public Requirement_coverageContext requirement_coverage() {
			return getRuleContext(Requirement_coverageContext.class,0);
		}
		public Vertex_coverageContext vertex_coverage() {
			return getRuleContext(Vertex_coverageContext.class,0);
		}
		public Edge_coverageContext edge_coverage() {
			return getRuleContext(Edge_coverageContext.class,0);
		}
		public Reached_vertexContext reached_vertex() {
			return getRuleContext(Reached_vertexContext.class,0);
		}
		public TerminalNode LPAREN() { return getToken(CLIParser.LPAREN, 0); }
		public TerminalNode INTEGER() { return getToken(CLIParser.INTEGER, 0); }
		public StopConditionContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_stopCondition; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof CLIListener ) ((CLIListener)listener).enterStopCondition(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof CLIListener ) ((CLIListener)listener).exitStopCondition(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof CLIVisitor ) return ((CLIVisitor<? extends T>)visitor).visitStopCondition(this);
			else return visitor.visitChildren(this);
		}
	}

	public final StopConditionContext stopCondition() throws RecognitionException {
		StopConditionContext _localctx = new StopConditionContext(_ctx, getState());
		enterRule(_localctx, 8, RULE_stopCondition);
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(106);
			switch ( getInterpreter().adaptivePredict(_input,3,_ctx) ) {
			case 1:
				{
				setState(63); edge_coverage();
				setState(64); match(LPAREN);
				setState(65); match(INTEGER);
				setState(66); match(RPAREN);
				}
				break;
			case 2:
				{
				}
				break;
			case 3:
				{
				setState(69); vertex_coverage();
				setState(70); match(LPAREN);
				setState(71); match(INTEGER);
				setState(72); match(RPAREN);
				}
				break;
			case 4:
				{
				}
				break;
			case 5:
				{
				setState(75); reached_edge();
				setState(76); match(LPAREN);
				setState(77); match(Identifier);
				setState(78); match(RPAREN);
				}
				break;
			case 6:
				{
				}
				break;
			case 7:
				{
				setState(81); reached_vertex();
				setState(82); match(LPAREN);
				setState(83); match(Identifier);
				setState(84); match(RPAREN);
				}
				break;
			case 8:
				{
				}
				break;
			case 9:
				{
				setState(87); length();
				setState(88); match(LPAREN);
				setState(89); match(INTEGER);
				setState(90); match(RPAREN);
				}
				break;
			case 10:
				{
				}
				break;
			case 11:
				{
				setState(93); time();
				setState(94); match(LPAREN);
				setState(95); match(INTEGER);
				setState(96); match(RPAREN);
				}
				break;
			case 12:
				{
				}
				break;
			case 13:
				{
				setState(99); requirement_coverage();
				setState(100); match(LPAREN);
				setState(101); match(INTEGER);
				setState(102); match(RPAREN);
				}
				break;
			case 14:
				{
				}
				break;
			case 15:
				{
				setState(105); never();
				}
				break;
			}
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	public static class GeneratorContext extends ParserRuleContext {
		public A_starContext a_star() {
			return getRuleContext(A_starContext.class,0);
		}
		public RandomContext random() {
			return getRuleContext(RandomContext.class,0);
		}
		public GeneratorContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_generator; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof CLIListener ) ((CLIListener)listener).enterGenerator(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof CLIListener ) ((CLIListener)listener).exitGenerator(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof CLIVisitor ) return ((CLIVisitor<? extends T>)visitor).visitGenerator(this);
			else return visitor.visitChildren(this);
		}
	}

	public final GeneratorContext generator() throws RecognitionException {
		GeneratorContext _localctx = new GeneratorContext(_ctx, getState());
		enterRule(_localctx, 10, RULE_generator);
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(111);
			switch (_input.LA(1)) {
			case T__6:
				{
				setState(108); random();
				}
				break;
			case LPAREN:
				{
				}
				break;
			case T__8:
				{
				setState(110); a_star();
				}
				break;
			default:
				throw new NoViableAltException(this);
			}
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	public static class ValueContext extends ParserRuleContext {
		public TerminalNode Identifier() { return getToken(CLIParser.Identifier, 0); }
		public TerminalNode INTEGER() { return getToken(CLIParser.INTEGER, 0); }
		public ValueContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_value; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof CLIListener ) ((CLIListener)listener).enterValue(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof CLIListener ) ((CLIListener)listener).exitValue(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof CLIVisitor ) return ((CLIVisitor<? extends T>)visitor).visitValue(this);
			else return visitor.visitChildren(this);
		}
	}

	public final ValueContext value() throws RecognitionException {
		ValueContext _localctx = new ValueContext(_ctx, getState());
		enterRule(_localctx, 12, RULE_value);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(113);
			_la = _input.LA(1);
			if ( !(_la==Identifier || _la==INTEGER) ) {
			_errHandler.recoverInline(this);
			}
			consume();
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	public static class RandomContext extends ParserRuleContext {
		public RandomContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_random; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof CLIListener ) ((CLIListener)listener).enterRandom(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof CLIListener ) ((CLIListener)listener).exitRandom(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof CLIVisitor ) return ((CLIVisitor<? extends T>)visitor).visitRandom(this);
			else return visitor.visitChildren(this);
		}
	}

	public final RandomContext random() throws RecognitionException {
		RandomContext _localctx = new RandomContext(_ctx, getState());
		enterRule(_localctx, 14, RULE_random);
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(115); match(T__6);
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	public static class A_starContext extends ParserRuleContext {
		public A_starContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_a_star; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof CLIListener ) ((CLIListener)listener).enterA_star(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof CLIListener ) ((CLIListener)listener).exitA_star(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof CLIVisitor ) return ((CLIVisitor<? extends T>)visitor).visitA_star(this);
			else return visitor.visitChildren(this);
		}
	}

	public final A_starContext a_star() throws RecognitionException {
		A_starContext _localctx = new A_starContext(_ctx, getState());
		enterRule(_localctx, 16, RULE_a_star);
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(117); match(T__8);
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	public static class Edge_coverageContext extends ParserRuleContext {
		public Edge_coverageContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_edge_coverage; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof CLIListener ) ((CLIListener)listener).enterEdge_coverage(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof CLIListener ) ((CLIListener)listener).exitEdge_coverage(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof CLIVisitor ) return ((CLIVisitor<? extends T>)visitor).visitEdge_coverage(this);
			else return visitor.visitChildren(this);
		}
	}

	public final Edge_coverageContext edge_coverage() throws RecognitionException {
		Edge_coverageContext _localctx = new Edge_coverageContext(_ctx, getState());
		enterRule(_localctx, 18, RULE_edge_coverage);
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(119); match(T__2);
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	public static class Vertex_coverageContext extends ParserRuleContext {
		public Vertex_coverageContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_vertex_coverage; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof CLIListener ) ((CLIListener)listener).enterVertex_coverage(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof CLIListener ) ((CLIListener)listener).exitVertex_coverage(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof CLIVisitor ) return ((CLIVisitor<? extends T>)visitor).visitVertex_coverage(this);
			else return visitor.visitChildren(this);
		}
	}

	public final Vertex_coverageContext vertex_coverage() throws RecognitionException {
		Vertex_coverageContext _localctx = new Vertex_coverageContext(_ctx, getState());
		enterRule(_localctx, 20, RULE_vertex_coverage);
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(121); match(T__0);
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	public static class Reached_edgeContext extends ParserRuleContext {
		public Reached_edgeContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_reached_edge; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof CLIListener ) ((CLIListener)listener).enterReached_edge(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof CLIListener ) ((CLIListener)listener).exitReached_edge(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof CLIVisitor ) return ((CLIVisitor<? extends T>)visitor).visitReached_edge(this);
			else return visitor.visitChildren(this);
		}
	}

	public final Reached_edgeContext reached_edge() throws RecognitionException {
		Reached_edgeContext _localctx = new Reached_edgeContext(_ctx, getState());
		enterRule(_localctx, 22, RULE_reached_edge);
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(123); match(T__9);
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	public static class Reached_vertexContext extends ParserRuleContext {
		public Reached_vertexContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_reached_vertex; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof CLIListener ) ((CLIListener)listener).enterReached_vertex(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof CLIListener ) ((CLIListener)listener).exitReached_vertex(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof CLIVisitor ) return ((CLIVisitor<? extends T>)visitor).visitReached_vertex(this);
			else return visitor.visitChildren(this);
		}
	}

	public final Reached_vertexContext reached_vertex() throws RecognitionException {
		Reached_vertexContext _localctx = new Reached_vertexContext(_ctx, getState());
		enterRule(_localctx, 24, RULE_reached_vertex);
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(125); match(T__1);
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	public static class LengthContext extends ParserRuleContext {
		public LengthContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_length; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof CLIListener ) ((CLIListener)listener).enterLength(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof CLIListener ) ((CLIListener)listener).exitLength(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof CLIVisitor ) return ((CLIVisitor<? extends T>)visitor).visitLength(this);
			else return visitor.visitChildren(this);
		}
	}

	public final LengthContext length() throws RecognitionException {
		LengthContext _localctx = new LengthContext(_ctx, getState());
		enterRule(_localctx, 26, RULE_length);
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(127); match(T__7);
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	public static class TimeContext extends ParserRuleContext {
		public TimeContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_time; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof CLIListener ) ((CLIListener)listener).enterTime(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof CLIListener ) ((CLIListener)listener).exitTime(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof CLIVisitor ) return ((CLIVisitor<? extends T>)visitor).visitTime(this);
			else return visitor.visitChildren(this);
		}
	}

	public final TimeContext time() throws RecognitionException {
		TimeContext _localctx = new TimeContext(_ctx, getState());
		enterRule(_localctx, 28, RULE_time);
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(129); match(T__3);
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	public static class Requirement_coverageContext extends ParserRuleContext {
		public Requirement_coverageContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_requirement_coverage; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof CLIListener ) ((CLIListener)listener).enterRequirement_coverage(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof CLIListener ) ((CLIListener)listener).exitRequirement_coverage(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof CLIVisitor ) return ((CLIVisitor<? extends T>)visitor).visitRequirement_coverage(this);
			else return visitor.visitChildren(this);
		}
	}

	public final Requirement_coverageContext requirement_coverage() throws RecognitionException {
		Requirement_coverageContext _localctx = new Requirement_coverageContext(_ctx, getState());
		enterRule(_localctx, 30, RULE_requirement_coverage);
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(131); match(T__5);
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	public static class NeverContext extends ParserRuleContext {
		public NeverContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_never; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof CLIListener ) ((CLIListener)listener).enterNever(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof CLIListener ) ((CLIListener)listener).exitNever(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof CLIVisitor ) return ((CLIVisitor<? extends T>)visitor).visitNever(this);
			else return visitor.visitChildren(this);
		}
	}

	public final NeverContext never() throws RecognitionException {
		NeverContext _localctx = new NeverContext(_ctx, getState());
		enterRule(_localctx, 32, RULE_never);
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(133); match(T__4);
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	public static final String _serializedATN =
		"\3\u0430\ud6d1\u8206\uad2d\u4417\uaef1\u8d80\uaadd\3\23\u008a\4\2\t\2"+
		"\4\3\t\3\4\4\t\4\4\5\t\5\4\6\t\6\4\7\t\7\4\b\t\b\4\t\t\t\4\n\t\n\4\13"+
		"\t\13\4\f\t\f\4\r\t\r\4\16\t\16\4\17\t\17\4\20\t\20\4\21\t\21\4\22\t\22"+
		"\3\2\3\2\3\2\3\2\3\2\3\2\3\3\3\3\3\3\7\3.\n\3\f\3\16\3\61\13\3\3\4\3\4"+
		"\3\4\7\4\66\n\4\f\4\16\49\13\4\3\5\3\5\3\5\3\5\3\5\5\5@\n\5\3\6\3\6\3"+
		"\6\3\6\3\6\3\6\3\6\3\6\3\6\3\6\3\6\3\6\3\6\3\6\3\6\3\6\3\6\3\6\3\6\3\6"+
		"\3\6\3\6\3\6\3\6\3\6\3\6\3\6\3\6\3\6\3\6\3\6\3\6\3\6\3\6\3\6\3\6\3\6\3"+
		"\6\3\6\3\6\3\6\3\6\3\6\5\6m\n\6\3\7\3\7\3\7\5\7r\n\7\3\b\3\b\3\t\3\t\3"+
		"\n\3\n\3\13\3\13\3\f\3\f\3\r\3\r\3\16\3\16\3\17\3\17\3\20\3\20\3\21\3"+
		"\21\3\22\3\22\3\22\2\2\23\2\4\6\b\n\f\16\20\22\24\26\30\32\34\36 \"\2"+
		"\3\3\2\r\16\u008b\2$\3\2\2\2\4*\3\2\2\2\6\62\3\2\2\2\b?\3\2\2\2\nl\3\2"+
		"\2\2\fq\3\2\2\2\16s\3\2\2\2\20u\3\2\2\2\22w\3\2\2\2\24y\3\2\2\2\26{\3"+
		"\2\2\2\30}\3\2\2\2\32\177\3\2\2\2\34\u0081\3\2\2\2\36\u0083\3\2\2\2 \u0085"+
		"\3\2\2\2\"\u0087\3\2\2\2$%\5\f\7\2%&\7\21\2\2&\'\5\n\6\2\'(\7\22\2\2("+
		")\7\2\2\3)\3\3\2\2\2*/\5\6\4\2+,\7\17\2\2,.\5\6\4\2-+\3\2\2\2.\61\3\2"+
		"\2\2/-\3\2\2\2/\60\3\2\2\2\60\5\3\2\2\2\61/\3\2\2\2\62\67\5\b\5\2\63\64"+
		"\7\20\2\2\64\66\5\b\5\2\65\63\3\2\2\2\669\3\2\2\2\67\65\3\2\2\2\678\3"+
		"\2\2\28\7\3\2\2\29\67\3\2\2\2:;\7\21\2\2;<\5\4\3\2<=\7\22\2\2=@\3\2\2"+
		"\2>@\5\n\6\2?:\3\2\2\2?>\3\2\2\2@\t\3\2\2\2AB\5\24\13\2BC\7\21\2\2CD\7"+
		"\16\2\2DE\7\22\2\2Em\3\2\2\2Fm\3\2\2\2GH\5\26\f\2HI\7\21\2\2IJ\7\16\2"+
		"\2JK\7\22\2\2Km\3\2\2\2Lm\3\2\2\2MN\5\30\r\2NO\7\21\2\2OP\7\r\2\2PQ\7"+
		"\22\2\2Qm\3\2\2\2Rm\3\2\2\2ST\5\32\16\2TU\7\21\2\2UV\7\r\2\2VW\7\22\2"+
		"\2Wm\3\2\2\2Xm\3\2\2\2YZ\5\34\17\2Z[\7\21\2\2[\\\7\16\2\2\\]\7\22\2\2"+
		"]m\3\2\2\2^m\3\2\2\2_`\5\36\20\2`a\7\21\2\2ab\7\16\2\2bc\7\22\2\2cm\3"+
		"\2\2\2dm\3\2\2\2ef\5 \21\2fg\7\21\2\2gh\7\16\2\2hi\7\22\2\2im\3\2\2\2"+
		"jm\3\2\2\2km\5\"\22\2lA\3\2\2\2lF\3\2\2\2lG\3\2\2\2lL\3\2\2\2lM\3\2\2"+
		"\2lR\3\2\2\2lS\3\2\2\2lX\3\2\2\2lY\3\2\2\2l^\3\2\2\2l_\3\2\2\2ld\3\2\2"+
		"\2le\3\2\2\2lj\3\2\2\2lk\3\2\2\2m\13\3\2\2\2nr\5\20\t\2or\3\2\2\2pr\5"+
		"\22\n\2qn\3\2\2\2qo\3\2\2\2qp\3\2\2\2r\r\3\2\2\2st\t\2\2\2t\17\3\2\2\2"+
		"uv\7\6\2\2v\21\3\2\2\2wx\7\4\2\2x\23\3\2\2\2yz\7\n\2\2z\25\3\2\2\2{|\7"+
		"\f\2\2|\27\3\2\2\2}~\7\3\2\2~\31\3\2\2\2\177\u0080\7\13\2\2\u0080\33\3"+
		"\2\2\2\u0081\u0082\7\5\2\2\u0082\35\3\2\2\2\u0083\u0084\7\t\2\2\u0084"+
		"\37\3\2\2\2\u0085\u0086\7\7\2\2\u0086!\3\2\2\2\u0087\u0088\7\b\2\2\u0088"+
		"#\3\2\2\2\7/\67?lq";
	public static final ATN _ATN =
		new ATNDeserializer().deserialize(_serializedATN.toCharArray());
	static {
		_decisionToDFA = new DFA[_ATN.getNumberOfDecisions()];
		for (int i = 0; i < _ATN.getNumberOfDecisions(); i++) {
			_decisionToDFA[i] = new DFA(_ATN.getDecisionState(i), i);
		}
	}
}