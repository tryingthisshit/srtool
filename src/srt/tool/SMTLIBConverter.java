package srt.tool;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import srt.ast.Expr;
import srt.util.Names;

public class SMTLIBConverter {
	
	private ExprToSmtlibVisitor exprConverter;
	private StringBuilder query;
	
	public SMTLIBConverter(Set<String> variableNames, List<Expr> transitionExprs, List<Expr> propertyExprs) {
		
		if(propertyExprs.size() == 0)
		{
			throw new IllegalArgumentException("No assertions.");
		}
		
		exprConverter = new ExprToSmtlibVisitor();
		query = new StringBuilder("(set-logic QF_BV)\n");
		// TODO: Define more functions above (for convenience), as needed.
		query.append(insertMethods());
		
		
		for(String v : variableNames)
		{
			query.append("(declare-fun " + v + " () (_ BitVec 32))\n");
		}

		for(Expr e : transitionExprs)
		{
			query.append(String.format("(assert (%s %s))\n", Names.toBoolFunction, exprConverter.visit(e)));
		}
		
		StringBuilder assertion = new StringBuilder("(assert (not\n");
		StringBuilder ands = new StringBuilder();
		for(Expr e : propertyExprs){
			ands.insert(0, String.format("  (and (%s %s)\n", Names.toBoolFunction, exprConverter.visit(e)));
			ands.append(")");
		}
		assertion.append(ands);
		assertion.append("))\n");
		query.append(assertion);

		// TODO: Declare variables, add constraints, add properties to check
		// here.

		
		query.append("(check-sat)\n");
		
	}

	public String getQuery() {
		return query.toString();
	}
	
	public List<Integer> getPropertiesThatFailed(String queryResult) {
		List<Integer> res = new ArrayList<Integer>();
		
		return res;
	}
	
	private String insertMethods() {
		StringBuilder builder = new StringBuilder();
		
		String toBVec = String.format("(define-fun %s ((p Bool)) (_ BitVec 32) (ite p (_ bv1 32) (_ bv0 32)))\n", Names.toBVectorFunction);
		builder.append(toBVec);
		
		String toBool = String.format("(define-fun %s (( p (_ BitVec 32) )) (Bool) (not (= p #x00000000)))\n", Names.toBoolFunction);
		builder.append(toBool);
		
		return builder.toString();
	}
	
}
