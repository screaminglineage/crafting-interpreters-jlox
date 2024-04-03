package lox;

import java.util.List;

class Parser {
    private static class ParseError extends RuntimeException {}
    private final List<Token> tokens;
    private int current = 0;

    Parser(List<Token> tokens) {
        this.tokens = tokens;
    }

    Expr parse() {
        try {
            return expression();
        } catch (ParseError error) {
            return null;
        }
    }

//     ternary -
    private Expr expression() {
        return comma();
    }

    private Expr comma() {
        Expr left = ternary();
        while (match(TokenType.COMMA)) {
            Token operator = previous();
            Expr right = ternary();
            left = new Expr.Binary(left, operator, right);
        }
        return left;
    }

    private Expr ternary() {
        Expr first = equality();
        if (match(TokenType.QUESTION)) {
            Token operatorQuestion = previous();
            Expr middle = expression();
            Token operatorColon = consume(TokenType.COLON, "Expect ':' after expression");
            Expr last = equality();
            return new Expr.Ternary(first, operatorQuestion, middle, operatorColon, last);
        }
        return first;
    }

    private Expr equality() {
        if (match(TokenType.BANG_EQUAL, TokenType.EQUAL_EQUAL)) {
            Token operator = previous();
            Expr _ = comparison();
            throw error(operator, "Expect left hand operand");
        }
        Expr left = comparison();
        while (match(TokenType.BANG_EQUAL, TokenType.EQUAL_EQUAL)) {
            Token operator = previous();
            Expr right = comparison();
            left = new Expr.Binary(left, operator, right);
        }
        return left;
    }

    private Expr comparison() {
        if (match(TokenType.GREATER, TokenType.LESS, TokenType.GREATER_EQUAL, TokenType.LESS_EQUAL)) {
            Token operator = previous();
            Expr _ = term();
            throw error(operator, "Expect left hand operand");
        }
        Expr left = term();
        while (match(TokenType.GREATER, TokenType.LESS, TokenType.GREATER_EQUAL, TokenType.LESS_EQUAL)) {
            Token operator = previous();
            Expr right = term();
            left = new Expr.Binary(left, operator, right);
        }
        return left;
    }

    private Expr term() {
        if (match(TokenType.PLUS, TokenType.MINUS)) {
            Token operator = previous();
            Expr _ = factor();
            throw error(operator, "Expect left hand operand");
        }
        Expr left = factor();
        while (match(TokenType.PLUS, TokenType.MINUS)) {
            Token operator = previous();
            Expr right = factor();
            left = new Expr.Binary(left, operator, right);
        }
        return left;
    }

    private Expr factor() {
        if (match(TokenType.SLASH, TokenType.STAR)) {
            Token operator = previous();
            Expr _ = unary();
            throw error(operator, "Expect left hand operand");
        }
        Expr left = unary();
        while (match(TokenType.SLASH, TokenType.STAR)) {
            Token operator = previous();
            Expr right = unary();
            left = new Expr.Binary(left, operator, right);
        }
        return left;
    }

    private Expr unary() {
        if (match(TokenType.BANG, TokenType.MINUS)) {
            Token operator = previous();
            Expr right = unary();
            return new Expr.Unary(operator, right);
        }
        return primary();
    }

    private Expr primary() {
        if (match(TokenType.FALSE)) return new Expr.Literal(false);
        if (match(TokenType.TRUE)) return new Expr.Literal(true);
        if (match(TokenType.NIL)) return new Expr.Literal(null);
        
        if (match(TokenType.NUMBER, TokenType.STRING)) {
            return new Expr.Literal(previous().literal);
        }
        if (match(TokenType.LEFT_PAREN)) {
            Expr expr = expression();
            consume(TokenType.RIGHT_PAREN, "Expect ')' after expression");
            return new Expr.Grouping(expr);
        }
        throw error(peek(), "Expect expression");
    }

    private boolean match(TokenType... types) {
        for (TokenType type : types) {
            if (check(type)) {
                advance();
                return true;
            }
        }
        return false;
    }

    private Token consume(TokenType type, String message) {
        if (check(type)) return advance();
        throw error(peek(), message);
    }

    private boolean check(TokenType type) {
        if (isAtEnd()) return false;
        return peek().type == type;
    }

    private Token advance() {
        if (!isAtEnd()) current++;
        return previous();
    }

    private boolean isAtEnd() {
        return peek().type == TokenType.EOF;
    }

    private Token peek() {
        return tokens.get(current);
    }

    private Token previous() {
        return tokens.get(current - 1);
    }

    private ParseError error(Token token, String message) {
        Lox.error(token, message);
        return new ParseError();
    }

    private void synchronize() {
        advance();

        while (!isAtEnd()) {
            if (previous().type == TokenType.SEMICOLON) return;

            switch (peek().type) {
                case CLASS:
                case FUN:
                case VAR:
                case FOR:
                case IF:
                case WHILE:
                case PRINT:
                case RETURN:
                    return;
            }
            advance();
        }
    }
}
