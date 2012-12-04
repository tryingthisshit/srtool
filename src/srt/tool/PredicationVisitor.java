package srt.tool;

import java.util.LinkedList;
import java.util.List;

import srt.ast.AssertStmt;
import srt.ast.AssignStmt;
import srt.ast.AssumeStmt;
import srt.ast.BinaryExpr;
import srt.ast.BlockStmt;
import srt.ast.DeclRef;
import srt.ast.Expr;
import srt.ast.HavocStmt;
import srt.ast.IfStmt;
import srt.ast.IntLiteral;
import srt.ast.Program;
import srt.ast.Stmt;
import srt.ast.TernaryExpr;
import srt.ast.UnaryExpr;
import srt.ast.visitor.impl.DefaultVisitor;

public class PredicationVisitor extends DefaultVisitor {
	private final String assumePredicate = "$G";
	
	private int basePredicateCounter = 0;
	private String currentBasePredicate;
		
	private int havocPredicateCounter = 0;

	public PredicationVisitor() {
		super(true);
		currentBasePredicate = getFreshBasePredicate();
	}
	
	/*This is to add the base predicates to the whole program and set them to true*/
	@Override
	public Object visit(Program program) {
		String startingPredicate = currentBasePredicate;
		
		Program visitedProgram = (Program) super.visit(program);
		List<Stmt> newStatementList = new LinkedList<Stmt>(visitedProgram.getBlockStmt().getStmtList().getStatements());
		newStatementList.add(0, new AssignStmt(new DeclRef(startingPredicate), new IntLiteral(1)));
		newStatementList.add(1, new AssignStmt(new DeclRef(assumePredicate), new IntLiteral(1)));
		
		BlockStmt newBlock = new BlockStmt(newStatementList);
		return new Program(visitedProgram.getFunctionName(), visitedProgram.getDeclList(), newBlock, visitedProgram);
	}
	
	
	/*
	 * Transforms
	 * 
	 * if(E) { 
	 *	S 
	 * } else { 
	 *	T 
	 * }
	 * 
	 * to
	 * Q = P && E;
     * R = P && !E;
	 * Pred(S, Q);
	 * Pred(T, R);
	 * 
	 * where Q and R are fresh predicate names
	 */
	@Override
	public Object visit(IfStmt ifStmt) {
		DeclRef currentPredicate = new DeclRef(currentBasePredicate);
		
		String oldBasePredicate = currentBasePredicate;
		currentBasePredicate = getFreshBasePredicate();
		
		DeclRef ifPredicate = new DeclRef(currentBasePredicate);
		Expr ifPredicateAssignment = new BinaryExpr(BinaryExpr.LAND, currentPredicate, ifStmt.getCondition());
		Stmt ifPredicateStatement = new AssignStmt(ifPredicate, ifPredicateAssignment);
		Stmt ifStatement = (Stmt) visit(ifStmt.getThenStmt());
		
		
		currentBasePredicate = getFreshBasePredicate();
		DeclRef elsePredicate = new DeclRef(currentBasePredicate);
		Expr elsePredicateAssignment = new BinaryExpr(BinaryExpr.LAND, currentPredicate,
				new UnaryExpr(UnaryExpr.LNOT, ifStmt.getCondition()));
		Stmt elsePredicateStatement = new AssignStmt(elsePredicate, elsePredicateAssignment);
		Stmt elseStatement = (Stmt) visit(ifStmt.getElseStmt());

		currentBasePredicate = oldBasePredicate;
		
		List<Stmt> statementList = new LinkedList<Stmt>();
		statementList.add(ifPredicateStatement);
		statementList.add(elsePredicateStatement);
		statementList.add(ifStatement);
		statementList.add(elseStatement);		
		
		BlockStmt block = new BlockStmt(statementList);
		return block;
	}

	/*
	 * Transforms
	 * assert(E)
	 * to
	 * assert((G && P) => E)
	 * where G is the global assume predicate and P the current "if" predicate
	 */
	@Override
	public Object visit(AssertStmt assertStmt) {
		Expr condition = new BinaryExpr(BinaryExpr.LAND,
				new DeclRef(assumePredicate),
				new DeclRef(currentBasePredicate));
		Expr implication = new TernaryExpr(condition,
				assertStmt.getCondition(),
				new IntLiteral(1));
		AssertStmt nAssert = new AssertStmt(implication, assertStmt);
		return super.visit(nAssert);
	}

	/*
	 * Transforms
	 * x = E;
	 * to
	 * x = ((G && P) ? E : x)
	 * where G is the global assume predicate and P the current "if" predicate
	 */
	@Override
	public Object visit(AssignStmt assignment) {
		Expr condition = new BinaryExpr(BinaryExpr.LAND,
				new DeclRef(assumePredicate),
				new DeclRef(currentBasePredicate));
		Expr rhs = new TernaryExpr(condition, assignment.getRhs(), assignment.getLhs());
		AssignStmt nAssignment = new AssignStmt(assignment.getLhs(), rhs, assignment);
		return super.visit(nAssignment);
	}

	/*
	 * Transforms
	 * assume(E);
	 * to
	 * G = G && ((G && P) => E)
	 * where G is the global assume predicate and P the current "if" predicate
	 */
	@Override
	public Object visit(AssumeStmt assumeStmt) {
		Expr condition = new BinaryExpr(BinaryExpr.LAND,
				new DeclRef(assumePredicate),
				new DeclRef(currentBasePredicate));
		Expr gImpl = new TernaryExpr(condition, assumeStmt.getCondition(), new IntLiteral(1));
		Expr gAnd = new BinaryExpr(BinaryExpr.LAND, new DeclRef(assumePredicate), gImpl);
		AssignStmt newAssumeAssignment = new AssignStmt(new DeclRef(assumePredicate), gAnd, assumeStmt);
		return super.visit(newAssumeAssignment);
	}

	/*
	 * Transforms
	 * havoc(x);
	 * to
	 * x = (G && P) ? h : x
	 * where G is the global assume predicate, P the current "if" predicate, and "h" a fresh predicate
	 */
	@Override
	public Object visit(HavocStmt havocStmt) {
		BinaryExpr condition = new BinaryExpr(BinaryExpr.LAND,
				new DeclRef(assumePredicate),
				new DeclRef(currentBasePredicate));
		TernaryExpr havocTernary = new TernaryExpr(condition, new DeclRef(getFreshHavocPredicate()), havocStmt.getVariable());
		AssignStmt newHavocAssignment = new AssignStmt(havocStmt.getVariable(), havocTernary , havocStmt);
		return super.visit(newHavocAssignment);
	}
	
	private String getFreshBasePredicate() {
		return String.format("$P%d", basePredicateCounter++);
	}
	
	private String getFreshHavocPredicate() {
		return String.format("$h%d", havocPredicateCounter++);
	}

}
