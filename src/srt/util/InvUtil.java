package srt.util;

import java.util.LinkedList;
import java.util.List;

import srt.ast.AssertStmt;
import srt.ast.AssumeStmt;
import srt.ast.BinaryExpr;
import srt.ast.BlockStmt;
import srt.ast.Expr;
import srt.ast.IntLiteral;
import srt.ast.Stmt;
import srt.ast.WhileStmt;

public class InvUtil {
	// Returns an Expr that's a conjunction of the invariant list
	// If the list is empty return truth representation (1)
	public static AssumeStmt getInvariantAssume(WhileStmt whileStmt){
		Expr baseExpr = new IntLiteral(1);
		for(Expr e : whileStmt.getInvariantList().getExprs()) {
			baseExpr = new BinaryExpr(BinaryExpr.LAND, e, baseExpr);
		}
		return new AssumeStmt(baseExpr);
	}

	// returns a BlockStmt containing a list of assertions
	// Assertions are based on invariants and use the same
	// nodes as nodes basedOn
	public static Stmt getInvariantAssertions(WhileStmt whileStmt) {
		List<Stmt> assertionList = new LinkedList<Stmt>();
		for (Expr e : whileStmt.getInvariantList().getExprs()) {
			assertionList.add(new AssertStmt(e, e));
		}
		return new BlockStmt(assertionList);
	}
}
