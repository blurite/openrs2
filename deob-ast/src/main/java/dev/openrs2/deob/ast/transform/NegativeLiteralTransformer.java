package dev.openrs2.deob.ast.transform;

import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.expr.UnaryExpr;
import dev.openrs2.deob.ast.util.ExprUtils;

public final class NegativeLiteralTransformer extends Transformer {
	@Override
	public void transform(CompilationUnit unit) {
		unit.findAll(UnaryExpr.class).forEach(expr -> {
			var operand = expr.getExpression();
			if (!ExprUtils.isIntegerOrLongLiteral(operand)) {
				return;
			}

			var op = expr.getOperator();
			if (op == UnaryExpr.Operator.PLUS) {
				expr.replace(operand);
			} else if (op == UnaryExpr.Operator.MINUS) {
				expr.replace(ExprUtils.negate(operand));
			}
		});
	}
}
