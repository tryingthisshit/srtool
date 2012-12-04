package srt.tool;

import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import srt.ast.AssumeStmt;
import srt.ast.BlockStmt;
import srt.ast.DeclRef;
import srt.ast.EmptyStmt;
import srt.ast.HavocStmt;
import srt.ast.IfStmt;
import srt.ast.IntLiteral;
import srt.ast.Stmt;
import srt.ast.WhileStmt;
import srt.ast.visitor.impl.DefaultVisitor;
import srt.util.InvUtil;

public class LoopAbstractionVisitor extends DefaultVisitor {

	public LoopAbstractionVisitor() {
		super(true);
	}

	
	/*
	 * Applies the loop abstraction transformation
	 * 
	 * S
	 * while(C)
	 * invariant X
	 * {
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
		Stmt invariantAsserts = InvUtil.getInvariantAssertions(whileStmt);
		
		//Assume doesn't need to be based on specific nodes.
		//Hence we just have one assume statement.
		Stmt invariantAssume = InvUtil.getInvariantAssume(whileStmt);

		Set<String> modset = getModset(whileStmt.getBody());
		Stmt havocs = getHavocs(modset);
		
		List<Stmt> modifiedBodyList = new LinkedList<Stmt>();
		modifiedBodyList.add(whileStmt.getBody());
		modifiedBodyList.add(invariantAsserts);
		modifiedBodyList.add(new AssumeStmt(new IntLiteral(0)));
		Stmt modifiedBody = new BlockStmt(modifiedBodyList);
		Stmt ifstmt = new IfStmt(whileStmt.getCondition(), modifiedBody, new EmptyStmt(), whileStmt);
		
		List<Stmt> stmtList = new LinkedList<Stmt>();
		stmtList.add(invariantAsserts);
		stmtList.add(havocs);
		stmtList.add(invariantAssume);
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
