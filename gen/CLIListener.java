// Generated from /home/krikar/dev/mbt/GW3/gw-cli/src/main/antlr4/org/graphwalker/cli/CLI.g4 by ANTLR 4.x
import org.antlr.v4.runtime.misc.NotNull;
import org.antlr.v4.runtime.tree.ParseTreeListener;

/**
 * This interface defines a complete listener for a parse tree produced by
 * {@link CLIParser}.
 */
public interface CLIListener extends ParseTreeListener {
	/**
	 * Enter a parse tree produced by {@link CLIParser#booleanAndExpression}.
	 * @param ctx the parse tree
	 */
	void enterBooleanAndExpression(@NotNull CLIParser.BooleanAndExpressionContext ctx);
	/**
	 * Exit a parse tree produced by {@link CLIParser#booleanAndExpression}.
	 * @param ctx the parse tree
	 */
	void exitBooleanAndExpression(@NotNull CLIParser.BooleanAndExpressionContext ctx);
	/**
	 * Enter a parse tree produced by {@link CLIParser#edge_coverage}.
	 * @param ctx the parse tree
	 */
	void enterEdge_coverage(@NotNull CLIParser.Edge_coverageContext ctx);
	/**
	 * Exit a parse tree produced by {@link CLIParser#edge_coverage}.
	 * @param ctx the parse tree
	 */
	void exitEdge_coverage(@NotNull CLIParser.Edge_coverageContext ctx);
	/**
	 * Enter a parse tree produced by {@link CLIParser#random}.
	 * @param ctx the parse tree
	 */
	void enterRandom(@NotNull CLIParser.RandomContext ctx);
	/**
	 * Exit a parse tree produced by {@link CLIParser#random}.
	 * @param ctx the parse tree
	 */
	void exitRandom(@NotNull CLIParser.RandomContext ctx);
	/**
	 * Enter a parse tree produced by {@link CLIParser#vertex_coverage}.
	 * @param ctx the parse tree
	 */
	void enterVertex_coverage(@NotNull CLIParser.Vertex_coverageContext ctx);
	/**
	 * Exit a parse tree produced by {@link CLIParser#vertex_coverage}.
	 * @param ctx the parse tree
	 */
	void exitVertex_coverage(@NotNull CLIParser.Vertex_coverageContext ctx);
	/**
	 * Enter a parse tree produced by {@link CLIParser#reached_edge}.
	 * @param ctx the parse tree
	 */
	void enterReached_edge(@NotNull CLIParser.Reached_edgeContext ctx);
	/**
	 * Exit a parse tree produced by {@link CLIParser#reached_edge}.
	 * @param ctx the parse tree
	 */
	void exitReached_edge(@NotNull CLIParser.Reached_edgeContext ctx);
	/**
	 * Enter a parse tree produced by {@link CLIParser#reached_vertex}.
	 * @param ctx the parse tree
	 */
	void enterReached_vertex(@NotNull CLIParser.Reached_vertexContext ctx);
	/**
	 * Exit a parse tree produced by {@link CLIParser#reached_vertex}.
	 * @param ctx the parse tree
	 */
	void exitReached_vertex(@NotNull CLIParser.Reached_vertexContext ctx);
	/**
	 * Enter a parse tree produced by {@link CLIParser#logicalExpression}.
	 * @param ctx the parse tree
	 */
	void enterLogicalExpression(@NotNull CLIParser.LogicalExpressionContext ctx);
	/**
	 * Exit a parse tree produced by {@link CLIParser#logicalExpression}.
	 * @param ctx the parse tree
	 */
	void exitLogicalExpression(@NotNull CLIParser.LogicalExpressionContext ctx);
	/**
	 * Enter a parse tree produced by {@link CLIParser#primaryExpression}.
	 * @param ctx the parse tree
	 */
	void enterPrimaryExpression(@NotNull CLIParser.PrimaryExpressionContext ctx);
	/**
	 * Exit a parse tree produced by {@link CLIParser#primaryExpression}.
	 * @param ctx the parse tree
	 */
	void exitPrimaryExpression(@NotNull CLIParser.PrimaryExpressionContext ctx);
	/**
	 * Enter a parse tree produced by {@link CLIParser#requirement_coverage}.
	 * @param ctx the parse tree
	 */
	void enterRequirement_coverage(@NotNull CLIParser.Requirement_coverageContext ctx);
	/**
	 * Exit a parse tree produced by {@link CLIParser#requirement_coverage}.
	 * @param ctx the parse tree
	 */
	void exitRequirement_coverage(@NotNull CLIParser.Requirement_coverageContext ctx);
	/**
	 * Enter a parse tree produced by {@link CLIParser#never}.
	 * @param ctx the parse tree
	 */
	void enterNever(@NotNull CLIParser.NeverContext ctx);
	/**
	 * Exit a parse tree produced by {@link CLIParser#never}.
	 * @param ctx the parse tree
	 */
	void exitNever(@NotNull CLIParser.NeverContext ctx);
	/**
	 * Enter a parse tree produced by {@link CLIParser#time}.
	 * @param ctx the parse tree
	 */
	void enterTime(@NotNull CLIParser.TimeContext ctx);
	/**
	 * Exit a parse tree produced by {@link CLIParser#time}.
	 * @param ctx the parse tree
	 */
	void exitTime(@NotNull CLIParser.TimeContext ctx);
	/**
	 * Enter a parse tree produced by {@link CLIParser#a_star}.
	 * @param ctx the parse tree
	 */
	void enterA_star(@NotNull CLIParser.A_starContext ctx);
	/**
	 * Exit a parse tree produced by {@link CLIParser#a_star}.
	 * @param ctx the parse tree
	 */
	void exitA_star(@NotNull CLIParser.A_starContext ctx);
	/**
	 * Enter a parse tree produced by {@link CLIParser#length}.
	 * @param ctx the parse tree
	 */
	void enterLength(@NotNull CLIParser.LengthContext ctx);
	/**
	 * Exit a parse tree produced by {@link CLIParser#length}.
	 * @param ctx the parse tree
	 */
	void exitLength(@NotNull CLIParser.LengthContext ctx);
	/**
	 * Enter a parse tree produced by {@link CLIParser#value}.
	 * @param ctx the parse tree
	 */
	void enterValue(@NotNull CLIParser.ValueContext ctx);
	/**
	 * Exit a parse tree produced by {@link CLIParser#value}.
	 * @param ctx the parse tree
	 */
	void exitValue(@NotNull CLIParser.ValueContext ctx);
	/**
	 * Enter a parse tree produced by {@link CLIParser#stopCondition}.
	 * @param ctx the parse tree
	 */
	void enterStopCondition(@NotNull CLIParser.StopConditionContext ctx);
	/**
	 * Exit a parse tree produced by {@link CLIParser#stopCondition}.
	 * @param ctx the parse tree
	 */
	void exitStopCondition(@NotNull CLIParser.StopConditionContext ctx);
	/**
	 * Enter a parse tree produced by {@link CLIParser#generator}.
	 * @param ctx the parse tree
	 */
	void enterGenerator(@NotNull CLIParser.GeneratorContext ctx);
	/**
	 * Exit a parse tree produced by {@link CLIParser#generator}.
	 * @param ctx the parse tree
	 */
	void exitGenerator(@NotNull CLIParser.GeneratorContext ctx);
	/**
	 * Enter a parse tree produced by {@link CLIParser#parse}.
	 * @param ctx the parse tree
	 */
	void enterParse(@NotNull CLIParser.ParseContext ctx);
	/**
	 * Exit a parse tree produced by {@link CLIParser#parse}.
	 * @param ctx the parse tree
	 */
	void exitParse(@NotNull CLIParser.ParseContext ctx);
}