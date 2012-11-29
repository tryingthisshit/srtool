package srt.tool;

import java.util.HashMap;
import java.util.Map;

import srt.ast.AssignStmt;
import srt.ast.Decl;
import srt.ast.DeclRef;
import srt.ast.Expr;
import srt.ast.visitor.impl.DefaultVisitor;

public class SSAVisitor extends DefaultVisitor {
	private Map<String, Integer> index = new HashMap<String, Integer>();

	public SSAVisitor() {
		super(true);
	}
	
	private String getSSAName(String name) {
		if(!index.containsKey(name)){
			index.put(name,0);
		}
		return String.format("%s$%d", name, index.get(name));
	}
	
	private void incrementSSAIndex(String name) {
		int currentIndex = index.containsKey(name) ? index.get(name) : 0;
		index.put(name, currentIndex+1);
	}
	
	@Override
	public Object visit(Decl decl) {
		Decl ndecl = new Decl(getSSAName(decl.getName()), decl.getType(), decl);
		return super.visit(ndecl);
	}
	
	@Override
	public Object visit(DeclRef declRef) {
		DeclRef ndeclRef = new DeclRef(getSSAName(declRef.getName()), declRef);
		return super.visit(ndeclRef);
	}
	
	@Override
	public Object visit(AssignStmt assignment) {
		Expr rhs = assignment.getRhs();
		Expr parsedExpr = (Expr) super.visit(rhs);

		DeclRef d = assignment.getLhs();
		incrementSSAIndex(d.getName());
		DeclRef nd = (DeclRef) this.visit(d);
		
		AssignStmt newAssign = new AssignStmt(nd, parsedExpr, assignment);

		return newAssign;
	}

}
