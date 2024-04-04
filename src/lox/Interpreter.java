package lox;

class Interpreter implements Expr.Visitor<Object> {
    void interpret(Expr expression) {
        try {
            Object result = expression.accept(this);
            System.out.println(stringify(result));
        } catch (RuntimeError error) {
            Lox.runtimeError(error);
        }
    }

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
                if (isStringOperands(left, right)) yield stringCompare(left, right) > 0;
                if (isNumberOperands(left, right)) yield (double) left > (double) right;
                throw new RuntimeError(expr.operator, "Operands must both be numbers or strings");
            }
            case TokenType.GREATER_EQUAL -> {
                if (isStringOperands(left, right)) yield stringCompare(left, right) >= 0;
                if (isNumberOperands(left, right)) yield (double) left >= (double) right;
                throw new RuntimeError(expr.operator, "Operands must both be numbers or strings");
            }
            case TokenType.LESS -> {
                if (isStringOperands(left, right)) yield stringCompare(left, right) < 0;
                if (isNumberOperands(left, right)) yield (double) left < (double) right;
                throw new RuntimeError(expr.operator, "Operands must both be numbers or strings");
            }
            case TokenType.LESS_EQUAL -> {
                if (isStringOperands(left, right)) yield stringCompare(left, right) <= 0;
                if (isNumberOperands(left, right)) yield (double) left <= (double) right;
                throw new RuntimeError(expr.operator, "Operands must both be numbers or strings");
            }
            case TokenType.BANG_EQUAL -> {
                checkNumberOperands(expr.operator, left, right);
                yield !isEqual(left, right);
            }
            case TokenType.EQUAL_EQUAL -> {
                if (isStringOperands(left, right)) yield stringCompare(left, right) == 0;
                if (isNumberOperands(left, right)) yield (double) left == (double) right;
                throw new RuntimeError(expr.operator, "Operands must both be numbers or strings");
            }
            case TokenType.PLUS -> {
                if (isNumberOperands(left, right)) yield (double) left + (double) right;
                if (left instanceof String) yield left + stringify(right);
                if (right instanceof String) yield stringify(left) + right;
                throw new RuntimeError(expr.operator, "Operands must both be numbers or at least one should be a string.");
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
                if ((double) right == 0) throw new RuntimeError(expr.operator, "Cannot divide by 0.");
                yield (double) left / (double) right;
            }
            case TokenType.COMMA -> right;
            default -> null; // Unreachable
        };
    }

    @Override
    public Object visitTernaryExpr(Expr.Ternary expr) {
        Object falseValue = expr.falseBranch.accept(this);
        Object trueValue = expr.trueBranch.accept(this);
        Object predicate = expr.predicate.accept(this);
        return (isTruthy(predicate)) ? trueValue : falseValue;
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

    private String stringify(Object object) {
        if (object == null) return "nil";
        if (object instanceof Double) {
            String text = object.toString();
            if (text.endsWith(".0")) {
                text = text.substring(0, text.length() - 2);
            }
            return text;
        }
        return object.toString();
    }

    private int stringCompare(Object a, Object b) {
        return ((String) a).compareTo((String) b);
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

    private boolean isNumberOperands(Object left, Object right) {
        return (left instanceof Double && right instanceof Double);
    }

    private boolean isStringOperands(Object left, Object right) {
        return (left instanceof String && right instanceof String);
    }
}
