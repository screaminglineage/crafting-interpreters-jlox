package lox;

class Interpreter implements Expr.Visitor<Object> {
    @Override
    public Object visitLiteralExpr(Expr.Literal expr) {
        return expr.value;
    }

    @Override
    public Object visitGroupingExpr(Expr.Grouping expr) {
        return expr.expression.accept(this);
    }

    @Override
    public Object visitUnaryExpr(Expr.Unary expr) {
        Object value = expr.right.accept(this);
        return switch (expr.operator.type) {
            case TokenType.MINUS -> {
                checkNumberOperand(expr.operator, value);
                yield -(double) value;
            }
            case TokenType.BANG -> !isTruthy(value);
            default -> null; // Unreachable
        };
    }

    public Object visitBinaryExpr(Expr.Binary expr) {
        Object left = expr.left.accept(this);
        Object right = expr.right.accept(this);

        return switch (expr.operator.type) {
            case TokenType.GREATER -> {
                checkNumberOperands(expr.operator, left, right);
                yield (double) left > (double) right;
            }
            case TokenType.GREATER_EQUAL -> {
                checkNumberOperands(expr.operator, left, right);
                yield (double) left >= (double) right;
            }
            case TokenType.LESS -> {
                checkNumberOperands(expr.operator, left, right);
                yield (double) left < (double) right;
            }
            case TokenType.LESS_EQUAL -> {
                checkNumberOperands(expr.operator, left, right);
                yield (double) left <= (double) right;
            }
            case TokenType.BANG_EQUAL -> {
                checkNumberOperands(expr.operator, left, right);
                yield !isEqual(left, right);
            }
            case TokenType.EQUAL_EQUAL -> {
                checkNumberOperands(expr.operator, left, right);
                yield isEqual(left, right);
            }
            case TokenType.MINUS -> {
                checkNumberOperands(expr.operator, left, right);
                yield (double) left - (double) right;
            }
            case TokenType.STAR -> {
                checkNumberOperands(expr.operator, left, right);
                yield (double) left * (double) right;
            }
            case TokenType.SLASH -> {
                checkNumberOperands(expr.operator, left, right);
                yield (double) left / (double) right;
            }
            case TokenType.PLUS -> {
                if (left instanceof Double && right instanceof Double) {
                    yield (double) left + (double) right;
                }
                if (left instanceof String && right instanceof String) {
                    yield left + (String) right;
                }
                throw new RuntimeError(expr.operator, "Operands must both be numbers or strings");
            }
            default -> null; // Unreachable
        };
    }


    private boolean isTruthy(Object value) {
        if (value == null) return false;
        if (value instanceof Boolean) return (boolean) value;
        return true;
    }

    private boolean isEqual(Object a, Object b) {
        if (a == null && b == null) return true;
        if (a == null) return false;
        return a.equals(b);
    }

    private void checkNumberOperand(Token operator, Object operand) {
        if (!(operand instanceof Double)) {
            throw new RuntimeError(operator, "Operand must be a number.");
        }
    }

    private void checkNumberOperands(Token operator, Object left, Object right) {
        if (left instanceof Double && right instanceof Double) return;
        throw new RuntimeError(operator, "Operands must be a numbers.");
    }
}
