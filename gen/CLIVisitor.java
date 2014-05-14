// Generated from /home/krikar/dev/mbt/GW3/gw-cli/src/main/antlr4/org/graphwalker/cli/CLI.g4 by ANTLR 4.x
import org.antlr.v4.runtime.misc.NotNull;
import org.antlr.v4.runtime.tree.ParseTreeVisitor;

/**
 * This interface defines a complete generic visitor for a parse tree produced
 * by {@link CLIParser}.
 *
 * @param <T> The return type of the visit operation. Use {@link Void} for
 * operations with no return type.
 */
public interface CLIVisitor<T> extends ParseTreeVisitor<T> {
	/**
	 * Visit a parse tree produced by {@link CLIParser#booleanAndExpression}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitBooleanAndExpression(@NotNull CLIParser.BooleanAndExpressionContext ctx);
	/**
	 * Visit a parse tree produced by {@link CLIParser#edge_coverage}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitEdge_coverage(@NotNull CLIParser.Edge_coverageContext ctx);
	/**
	 * Visit a parse tree produced by {@link CLIParser#random}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitRandom(@NotNull CLIParser.RandomContext ctx);
	/**
	 * Visit a parse tree produced by {@link CLIParser#vertex_coverage}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitVertex_coverage(@NotNull CLIParser.Vertex_coverageContext ctx);
	/**
	 * Visit a parse tree produced by {@link CLIParser#reached_edge}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitReached_edge(@NotNull CLIParser.Reached_edgeContext ctx);
	/**
	 * Visit a parse tree produced by {@link CLIParser#reached_vertex}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitReached_vertex(@NotNull CLIParser.Reached_vertexContext ctx);
	/**
	 * Visit a parse tree produced by {@link CLIParser#logicalExpression}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitLogicalExpression(@NotNull CLIParser.LogicalExpressionContext ctx);
	/**
	 * Visit a parse tree produced by {@link CLIParser#primaryExpression}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitPrimaryExpression(@NotNull CLIParser.PrimaryExpressionContext ctx);
	/**
	 * Visit a parse tree produced by {@link CLIParser#requirement_coverage}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitRequirement_coverage(@NotNull CLIParser.Requirement_coverageContext ctx);
	/**
	 * Visit a parse tree produced by {@link CLIParser#never}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitNever(@NotNull CLIParser.NeverContext ctx);
	/**
	 * Visit a parse tree produced by {@link CLIParser#time}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitTime(@NotNull CLIParser.TimeContext ctx);
	/**
	 * Visit a parse tree produced by {@link CLIParser#a_star}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitA_star(@NotNull CLIParser.A_starContext ctx);
	/**
	 * Visit a parse tree produced by {@link CLIParser#length}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitLength(@NotNull CLIParser.LengthContext ctx);
	/**
	 * Visit a parse tree produced by {@link CLIParser#value}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitValue(@NotNull CLIParser.ValueContext ctx);
	/**
	 * Visit a parse tree produced by {@link CLIParser#stopCondition}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitStopCondition(@NotNull CLIParser.StopConditionContext ctx);
	/**
	 * Visit a parse tree produced by {@link CLIParser#generator}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitGenerator(@NotNull CLIParser.GeneratorContext ctx);
	/**
	 * Visit a parse tree produced by {@link CLIParser#parse}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitParse(@NotNull CLIParser.ParseContext ctx);
}