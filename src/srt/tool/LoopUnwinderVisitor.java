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

	@Override
	public Object visit(WhileStmt whileStmt) {
		int bound = whileStmt.getBound() != null ? whileStmt.getBound().getValue() : defaultUnwindBound; 
		
		AssertStmt invariantAssertion = new AssertStmt(InvUtil.getInvariantExpr(whileStmt));
		
		Stmt assumeStmt = new AssumeStmt(new UnaryExpr(UnaryExpr.LNOT, whileStmt.getCondition()));
		Stmt assertStmt = new AssertStmt(new UnaryExpr(UnaryExpr.LNOT, whileStmt.getCondition()));
		
		List<Stmt> stmtList = new LinkedList<Stmt>();
		stmtList.add(invariantAssertion);
		
		if (unwindingAssertions) { 
			stmtList.add(assertStmt);
		}
		stmtList.add(assumeStmt);
		
		Stmt mainStatement = new BlockStmt(stmtList);
		
		while(bound > 0){
			IfStmt ifStatement = getIfStatement(whileStmt, mainStatement);
			List<Stmt> finalList = new LinkedList<Stmt>();
			finalList.add(invariantAssertion);
			finalList.add(ifStatement);
			mainStatement = new BlockStmt(finalList);
			bound--;
		}
		
		return super.visit(mainStatement);
	}
	
	private IfStmt getIfStatement(WhileStmt whileStmt, Stmt baseStmt){
		
		List<Stmt> joinedList = new LinkedList<Stmt>();
		
		joinedList.add(whileStmt.getBody());
		joinedList.add(baseStmt);
		BlockStmt chained = new BlockStmt(joinedList);
		
		IfStmt ifStatement = new IfStmt(whileStmt.getCondition(), chained, new EmptyStmt());
		
		return ifStatement;
		
	}

}
