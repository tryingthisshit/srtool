package srt.tool;

import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import srt.ast.AssertStmt;
import srt.ast.AssumeStmt;
import srt.ast.BinaryExpr;
import srt.ast.BlockStmt;
import srt.ast.DeclRef;
import srt.ast.EmptyStmt;
import srt.ast.Expr;
import srt.ast.ExprList;
import srt.ast.HavocStmt;
import srt.ast.IfStmt;
import srt.ast.IntLiteral;
import srt.ast.Stmt;
import srt.ast.WhileStmt;
import srt.ast.visitor.impl.DefaultVisitor;
import srt.parser.SimpleCParser.assumeStmt_return;
import srt.util.InvUtil;

public class LoopAbstractionVisitor extends DefaultVisitor {

	public LoopAbstractionVisitor() {
		super(true);
	}

	
	/*
	 * Applies the loop abstraction transformation
	 * 
	 * S
	 * invariant X
	 * while(C){
	 * 	B;
	 * }
	 * T;
	 * 
	 * transforms to
	 * 
	 * S;
	 * assert(X);
	 * havoc(modset(B));
	 * assume(X);
	 * if(C){
	 * 	B;
	 *  assert(X);
	 *  assume(false);
	 * }
	 * 
	 */
	@Override
	public Object visit(WhileStmt whileStmt) {
		List<Stmt> stmtList = new LinkedList<Stmt>();
		Expr invariantExpr = InvUtil.getInvariantExpr(whileStmt);
		Stmt invariantAssert = new AssertStmt(invariantExpr);
		Stmt invariantAssume = new AssumeStmt(invariantExpr);
		
		Set<String> modset = getModset(whileStmt.getBody());
		Stmt havocs = getHavocs(modset);
		
		stmtList.add(invariantAssert);
		stmtList.add(havocs);
		stmtList.add(invariantAssume);
		
		List<Stmt> modifiedBodyList = new LinkedList<Stmt>();
		modifiedBodyList.add(whileStmt.getBody());
		modifiedBodyList.add(invariantAssert);
		modifiedBodyList.add(new AssumeStmt(new IntLiteral(0)));
		Stmt modifiedBody = new BlockStmt(modifiedBodyList);
		Stmt ifstmt = new IfStmt(whileStmt.getCondition(), modifiedBody, new EmptyStmt());
		stmtList.add(ifstmt);
		
		BlockStmt block = new BlockStmt(stmtList);
		return super.visit(block);
	}
	
	private Set<String> getModset(Stmt stmt) {
		ModsetVisitor visitor = new ModsetVisitor();
		visitor.visit(stmt);
		return visitor.modset;
	}
	
	private Stmt getHavocs(Set<String> variableNames) {
		List<Stmt> stmtList = new LinkedList<Stmt>();
		for (String varName : variableNames) {
			stmtList.add(new HavocStmt(new DeclRef(varName)));
		}
		
		return new BlockStmt(stmtList);
	}
}
