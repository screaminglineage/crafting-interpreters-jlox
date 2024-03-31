package lox;

import java.util.ArrayList;
import java.util.List;

// RPNPrinter from https://craftinginterpreters.com/representing-code.html#challenges
// Only implemented for arithmetic operations
// Doesnt differentiate between unary or binary minus
class RPNPrinter implements Expr.Visitor<String> {
    String print(Expr expr) {
        return expr.accept(this);
    }

    private final List<String> operatorStack = new ArrayList<>();

    @Override
    public String visitBinaryExpr(Expr.Binary expr) {
        return toRPN(expr.operator.lexeme, expr.left, expr.right);
    }

    @Override
    public String visitGroupingExpr(Expr.Grouping expr) {
        return toRPN("group", expr.expression);
    }

    @Override
    public String visitLiteralExpr(Expr.Literal expr) {
        if (expr.value == null) return "nil";
        return expr.value.toString();
    }

    @Override
    public String visitUnaryExpr(Expr.Unary expr) {
        return toRPN(expr.operator.lexeme, expr.right);
    }

    int precedence(String operator) {
        return switch (operator) {
            case "+", "-" -> 1;
            case "*", "/" -> 2;
            case "group" -> 3;
            default -> 0;
        };
    }


    String toRPN(String operator, Expr... exprs) {
        StringBuilder builder = new StringBuilder();
        for (Expr expr : exprs) {
            builder.append(expr.accept(this)).append(" ");
        }
        while (!operatorStack.isEmpty() && precedence(operatorStack.getLast()) >= precedence(operator)) {
            builder.append(operatorStack.removeLast()).append(" ");
        }
        operatorStack.add(operator);

        while (!operatorStack.isEmpty()) {
            builder.append(operatorStack.removeLast()).append(" ");
        }

        return builder.toString();
    }

    public static void main(String[] args) {
        Expr expression = new Expr.Binary(
                new Expr.Binary(
                        new Expr.Literal(1),
                        new Token(TokenType.PLUS, "+", null, 1),
                        new Expr.Literal(2)),
                new Token(TokenType.STAR, "*", null, 1),
                new Expr.Binary(
                        new Expr.Literal(4),
                        new Token(TokenType.MINUS, "-", null, 1),
                        new Expr.Literal(3)));

        System.out.println(new RPNPrinter().print(expression));
    }
}
