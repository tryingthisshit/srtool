package srt.tool;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import srt.ast.Expr;
import srt.util.AssertionUtil;
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
		
		AssertionUtil assertionUtil = new AssertionUtil(propertyExprs, exprConverter);
		
		query.append(assertionUtil.getAssertionsAndProperties());
		
		query.append("(check-sat)\n");
		query.append(assertionUtil.getGetValue());
		
	}

	public String getQuery() {
		return query.toString();
	}
	
	public List<Integer> getPropertiesThatFailed(String queryResult) {
		List<Integer> res = new ArrayList<Integer>();
		Matcher m = Pattern.compile("(\\d+) false").matcher(queryResult);
		while (m.find()) {
			res.add(Integer.parseInt(m.group(1)));
		}
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
