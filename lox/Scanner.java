import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Scanner {
    private static final Map<String, TokenType> keywords;
    static {
        keywords = new HashMap<>();
        keywords.put("and", TokenType.AND);
        keywords.put("class", TokenType.CLASS);
        keywords.put("else", TokenType.ELSE);
        keywords.put("false", TokenType.FALSE);
        keywords.put("fun", TokenType.FUN);
        keywords.put("for", TokenType.FOR);
        keywords.put("if", TokenType.IF);
        keywords.put("nil", TokenType.NIL);
        keywords.put("or", TokenType.OR);
        keywords.put("print", TokenType.PRINT);
        keywords.put("return", TokenType.RETURN);
        keywords.put("super", TokenType.SUPER);
        keywords.put("this", TokenType.THIS);
        keywords.put("true", TokenType.TRUE);
        keywords.put("var", TokenType.VAR);
        keywords.put("while", TokenType.WHILE);
    }
    private final String source;
    private final List<Token> tokens = new ArrayList<>();
    private int start = 0;
    private int current = 0;
    private int line = 1;
    Scanner(String source) {
        this.source = source;
    }

    List<Token> scanTokens() {
        while (!isAtEnd()) {
            start = current;
            scanToken();
        }
        tokens.add(new Token(TokenType.EOF, "", null, line));
        return tokens;
    }

    private void scanToken() {
        char c = advance();
        switch (c) {
            case '(': addToken(TokenType.LEFT_PAREN); break;
            case ')': addToken(TokenType.RIGHT_PAREN); break;
            case '{': addToken(TokenType.LEFT_BRACE); break;
            case '}': addToken(TokenType.RIGHT_BRACE); break;
            case ',': addToken(TokenType.COMMA); break;
            case '.': addToken(TokenType.DOT); break;
            case '-': addToken(TokenType.MINUS); break;
            case '+': addToken(TokenType.PLUS); break;
            case ';': addToken(TokenType.SEMICOLON); break;
            case '*': addToken(TokenType.STAR); break;
            case '!': addToken(match('=')? TokenType.BANG_EQUAL: TokenType.BANG); break;
            case '=': addToken(match('=')? TokenType.EQUAL_EQUAL: TokenType.EQUAL); break;
            case '>': addToken(match('=')? TokenType.GREATER_EQUAL: TokenType.GREATER); break;
            case '<': addToken(match('=')? TokenType.LESS_EQUAL: TokenType.LESS); break;
            case '/': {
                if (match('/')) {
                    while (peek() != '\n' && !isAtEnd()) advance();
                } else if (match('*')) {
                    multiline_comment();
                } else {
                    addToken(TokenType.SLASH);
                }
                break;
            }

            case ' ':
            case '\r':
            case '\t':
                break;
            case '\n': line++; break;

            case '"': string(); break;

            default: {
                if (isDigit(c)) {
                    number();
                } else if (isAlphaNumeric(c)) {
                  identifier();
                } else {
                    Lox.error(line, "Unexpected character."); break;
                }
            }
        }
    }

    private boolean isAtEnd() {
        return current >= source.length();
    }

    private char advance() {
        return source.charAt(current++);
    }


    private boolean isDigit(char ch) {
        return '0' <= ch && ch <= '9';
    }

    private boolean isAlpha(char ch) {
        return ('a' <= ch && ch <= 'z')
                || ('A' <= ch && ch <= 'A')
                || ch == '_';
    }

    private boolean isAlphaNumeric(char ch) {
        return isAlpha(ch) || isDigit(ch);
    }

    private boolean match(char expected) {
        if (isAtEnd()) return false;
        if (source.charAt(current) != expected) return false;
        current++;
        return true;
    }

    private char peek() {
        if (isAtEnd()) return '\0';
        return source.charAt(current);
    }

    private char peekNext() {
        if (current + 1 >= source.length()) return '\0';
        return source.charAt(current+1);
    }

    private void string() {
        while (peek() != '"' && !isAtEnd()) {
            if (peek() == '\n') line++;
            advance();
        }

        // TODO: causes an exception due to unterminated string
        if (isAtEnd()) {
            Lox.error(line, "Unterminated string.");
        }

        advance();

        // TODO: support escape sequences by converting them to actual values here
        String value = source.substring(start+1, current-1);
        addToken(TokenType.STRING, value);

    }

    private void number() {
        while(isDigit(peek())) advance();

        if (peek() == '.' && isDigit(peekNext())) {
            advance();
            while (isDigit(peek())) advance();
        }

        String value = source.substring(start, current);
        // TODO: catch exception and show error
        double num = Double.parseDouble(value);
        addToken(TokenType.NUMBER, num);
    }

    private void identifier() {
        while(isAlphaNumeric(peek())) advance();
        String text = source.substring(start, current);
        TokenType token = keywords.get(text);
        if (token == null) token = TokenType.IDENTIFIER;
        addToken(token);
    }

    private void multiline_comment() {
        int depth = 1;
        while(!isAtEnd() && depth > 0) {
            if (peek() == '*' && peekNext() == '/') depth--;
            else if (peek() == '/' && peekNext() == '*') depth++;
            if (peek() == '\n') line++;
            advance();
        }

        // TODO: causes an exception due to unterminated comment
        if (isAtEnd()) Lox.error(line, "Unterminated comment.");
        advance();
    }

    private void addToken(TokenType type) {
        addToken(type, null);
    }

    private void addToken(TokenType type, Object literal) {
        String text = source.substring(start, current);
        tokens.add(new Token(type, text, literal, line));
    }

}
