package srt.util;

import java.util.List;

import srt.ast.Expr;
import srt.tool.ExprToSmtlibVisitor;

public class AssertionUtil {
	final List<Expr> propertyExprs;
	final ExprToSmtlibVisitor exprConverter;
	
	
	public AssertionUtil(List<Expr> propertyExprs, ExprToSmtlibVisitor exprConverter) {
		this.propertyExprs = propertyExprs;
		this.exprConverter = exprConverter;
	}

	public String getAssertionsAndProperties() {
		StringBuilder builder = new StringBuilder();
		
		for (int i = 0; i < propertyExprs.size(); i++) {
			builder.append(String.format("(declare-fun %s () Bool)\n", getPredicate(i)));
		}
		
		for (int i = 0; i < propertyExprs.size(); i++) {
			builder.append(String.format("(assert (= %s (%s %s)))\n",
					getPredicate(i),
					Names.toBoolFunction,
					exprConverter.visit(propertyExprs.get(i))));
		}
		builder.append("(assert (not\n");
		
		StringBuilder ands = new StringBuilder();
		
		for (int i = 0; i < propertyExprs.size(); i++) {
			ands.insert(0, String.format(" (and %s", getPredicate(i)));
			ands.append(")");
		}
		builder.append(ands);
		builder.append("))\n");
		
		return builder.toString();
	}
	
	public String getGetValue() {
		StringBuilder builder = new StringBuilder("(get-value (");
		
		for (int i = 0; i < propertyExprs.size(); i++) {
			builder.append(String.format("%s ", getPredicate(i)));
		}
		builder.append("))\n");
		return builder.toString();
	}
	
	private String getPredicate(int i){
		return String.format("$prop%d", i);
	}

}
