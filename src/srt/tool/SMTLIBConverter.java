package srt.tool;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import srt.ast.Expr;

public class SMTLIBConverter {
	
	private ExprToSmtlibVisitor exprConverter;
	private StringBuilder query;
	
	public SMTLIBConverter(Set<String> variableNames, List<Expr> transitionExprs, List<Expr> propertyExprs) {
		
		if(propertyExprs.size() == 0)
		{
			throw new IllegalArgumentException("No assertions.");
		}
		
		exprConverter = new ExprToSmtlibVisitor();
		query = new StringBuilder("(set-logic QF_BV)\n" +
				"(define-fun tobv32 ((p Bool)) (_ BitVec 32) (ite p (_ bv1 32) (_ bv0 32)))\n");
		// TODO: Define more functions above (for convenience), as needed.
		
		for(String v : variableNames)
		{
			query.append("(declare-fun " + v + " () (_ BitVec 32))\n");
		}
		
		for(Expr e : transitionExprs)
		{
			query.append("(assert " + exprConverter.visit(e) + ")\n");
		}
		
		StringBuilder assertion = new StringBuilder("(assert (not\n");
		StringBuilder ands = new StringBuilder();
		for(Expr e : propertyExprs){
			ands.insert(0, "  (and " + exprConverter.visit(e) + "\n");
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
	
}
