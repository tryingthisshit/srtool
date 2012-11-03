package srt.tool;

import srt.ast.BinaryExpr;
import srt.ast.DeclRef;
import srt.ast.Expr;
import srt.ast.IntLiteral;
import srt.ast.TernaryExpr;
import srt.ast.UnaryExpr;
import srt.ast.visitor.impl.DefaultVisitor;

public class ExprToSmtlibVisitor extends DefaultVisitor {
	
	public ExprToSmtlibVisitor() {
		super(false);
	}

	@Override
	public String visit(BinaryExpr expr) {
		String operator = null;
		switch(expr.getOperator())
		{
			case BinaryExpr.ADD:
				operator = "(bvadd %s %s)";
				break;
			case BinaryExpr.BAND:
				break;
			case BinaryExpr.BOR:
				break;
			case BinaryExpr.BXOR:
				break;
			case BinaryExpr.DIVIDE:
				break;
			case BinaryExpr.LSHIFT:
				break;
			case BinaryExpr.MOD:
				break;
			case BinaryExpr.MULTIPLY:
				break;
			case BinaryExpr.RSHIFT:
				break;
			case BinaryExpr.SUBTRACT:
				break;
				
			case BinaryExpr.LAND:
				break;
			case BinaryExpr.LOR:
				break;
			
			case BinaryExpr.GEQ:
				break;
			case BinaryExpr.GT:
				break;
			case BinaryExpr.LEQ:
				break;
			case BinaryExpr.LT:
				break;
			case BinaryExpr.NEQUAL:
				break;
			case BinaryExpr.EQUAL:
				operator = "(= %s %s)";
				break;
			default:
				throw new IllegalArgumentException("Invalid binary operator");
		}
		
		return String.format(operator, visit(expr.getLhs()), visit(expr.getRhs()));
		
	}

	@Override
	public String visit(DeclRef declRef) {
		return declRef.getName();
	}

	@Override
	public String visit(IntLiteral intLiteral) {
		/* We need to convert given number to a String Hex representation.
		 * For example 2 = #x00000002
		 */
		String hex = Integer.toHexString(intLiteral.getValue());
		StringBuilder hexString = new StringBuilder("#x00000000");
		hexString.replace(hexString.length()-hex.length(), hexString.length(), hex);
		return hexString.toString();
	}

	@Override
	public String visit(TernaryExpr ternaryExpr) {
		return null;
	}

	@Override
	public String visit(UnaryExpr unaryExpr) {
		String operator = null;
		switch(unaryExpr.getOperator())
		{
		case UnaryExpr.UMINUS:
			operator = "(bvneg %s)";
			break;
		case UnaryExpr.UPLUS:
			break;
		case UnaryExpr.LNOT:
			break;
		case UnaryExpr.BNOT:
			break;
		default:
			throw new IllegalArgumentException("Invalid binary operator");
		}
		
		return String.format(operator, visit(unaryExpr.getOperand()));
	}
	
	
	/* Overridden just to make return type String. 
	 * @see srt.ast.visitor.DefaultVisitor#visit(srt.ast.Expr)
	 */
	@Override
	public String visit(Expr expr) {
		return (String) super.visit(expr);
	}
	
	

}
