package srt.util;

import srt.ast.BinaryExpr;
import srt.ast.Expr;
import srt.ast.ExprList;
import srt.ast.IntLiteral;
import srt.ast.WhileStmt;

public class InvUtil {
	// Returns an Expr that's a conjunction of the invariant list
	// If the list is empty return truth
	public static Expr getInvariantExpr(WhileStmt whileStmt){
		ExprList exprList = whileStmt.getInvariantList();	
		Expr baseExpr = new IntLiteral(1);
		for(Expr e : exprList.getExprs()) {
			baseExpr = new BinaryExpr(BinaryExpr.LAND, e, baseExpr);
		}
		return baseExpr;
	}
}
