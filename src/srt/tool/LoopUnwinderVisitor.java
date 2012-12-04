package srt.tool;

import java.util.LinkedList;
import java.util.List;

import srt.ast.AssertStmt;
import srt.ast.AssumeStmt;
import srt.ast.BlockStmt;
import srt.ast.EmptyStmt;
import srt.ast.IfStmt;
import srt.ast.Stmt;
import srt.ast.UnaryExpr;
import srt.ast.WhileStmt;
import srt.ast.visitor.impl.DefaultVisitor;
import srt.util.InvUtil;

public class LoopUnwinderVisitor extends DefaultVisitor {

	private final boolean unwindingAssertions;
	private final int defaultUnwindBound;

	public LoopUnwinderVisitor(boolean unwindingAssertions,
			int defaultUnwindBound) {
		super(true);
		this.unwindingAssertions = unwindingAssertions;
		this.defaultUnwindBound = defaultUnwindBound;
	}

	/*
	 * Applies the loop unwinding transformation
	 * 
	 * S
	 * while(C)
	 * invariant X
	 * bound N
	 * {
	 * 	B;
	 * }
	 * T;
	 * 
	 * transforms to
	 * 
	 * S;
	 * assert(X);
	 * if (C) {
	 * 	B;
	 *  assert(X);
	 *  if (C) {
	 *  	B;
	 *  	.
	 *  	. If happens N times in total
	 *  	.
	 *  	assert(X)
	 *  	if (C) {
	 *  		B;
	 *  		assert(!C) - only included if we want to maintain soundness
	 *  		assume(!C);
	 *  	}
	 *  }
	 * }
	 * 
	 */
	@Override
	public Object visit(WhileStmt whileStmt) {
		List<Stmt> stmtList = new LinkedList<Stmt>();
		
		Stmt invariantAssertions = InvUtil.getInvariantAssertions(whileStmt);
		stmtList.add(invariantAssertions);
		
		if (unwindingAssertions) {
			//This will be an assertion that fails if we go out of bounds while unwinding the loop
			//We base it on the whileStmt so it reports the error at that point.
			Stmt assertStmt = new AssertStmt(new UnaryExpr(UnaryExpr.LNOT, whileStmt.getCondition()), whileStmt);
			stmtList.add(assertStmt);
		}
		
		
		Stmt assumeStmt = new AssumeStmt(new UnaryExpr(UnaryExpr.LNOT, whileStmt.getCondition()));
		stmtList.add(assumeStmt);
		
		Stmt mainStatement = new BlockStmt(stmtList);
		
		int bound = whileStmt.getBound() != null ? whileStmt.getBound().getValue() : defaultUnwindBound; 
		while(bound > 0){
			Stmt ifStatement = createIfStatement(whileStmt, mainStatement);
			List<Stmt> wrapList = new LinkedList<Stmt>();
			wrapList.add(invariantAssertions);
			wrapList.add(ifStatement);
			mainStatement = new BlockStmt(wrapList);
			bound--;
		}
		
		return super.visit(mainStatement);
	}
	
	private Stmt createIfStatement(WhileStmt whileStmt, Stmt baseStmt){
		
		List<Stmt> joinedList = new LinkedList<Stmt>();
		
		joinedList.add(whileStmt.getBody());
		joinedList.add(baseStmt);
		Stmt chained = new BlockStmt(joinedList);
		
		Stmt ifStatement = new IfStmt(whileStmt.getCondition(), chained, new EmptyStmt());
		
		return ifStatement;
	}

}
