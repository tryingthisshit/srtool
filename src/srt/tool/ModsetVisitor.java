package srt.tool;

import java.util.HashSet;
import java.util.Set;

import srt.ast.AssignStmt;
import srt.ast.visitor.impl.DefaultVisitor;

public class ModsetVisitor extends DefaultVisitor {
	public Set<String> modset = new HashSet<String>();;
	
	public ModsetVisitor() {
		super(false);
	}
	
	/*
	 * A variable X is part of a modset of a statement if it's a variable being
	 * assigned to in an AssignStmt
	 */
	@Override
	public Object visit(AssignStmt assignment) {
		modset.add(assignment.getLhs().getName());
		return super.visit(assignment);
	}
}