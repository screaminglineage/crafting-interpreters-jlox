package lox;

abstract class Expr {
  interface Visitor<R> {
    R visitTernaryExpr(Ternary expr);
    R visitBinaryExpr(Binary expr);
    R visitGroupingExpr(Grouping expr);
    R visitLiteralExpr(Literal expr);
    R visitUnaryExpr(Unary expr);
  }

  static class Ternary extends Expr {
    Ternary(Expr predicate, Token operatorQuestion, Expr trueBranch, Token operatorColon, Expr falseBranch) {
      this.predicate = predicate;
      this.trueBranch = trueBranch;
      this.falseBranch = falseBranch;
      this.operatorQuestion = operatorQuestion;
      this.operatorColon = operatorColon;
    }
    @Override
    <R> R accept(Visitor<R> visitor) {
      return visitor.visitTernaryExpr(this);
    }

    final Expr predicate;
    final Expr trueBranch;
    final Expr falseBranch;
    final Token operatorQuestion;
    final Token operatorColon;
  }
  static class Binary extends Expr {
    Binary(Expr left, Token operator, Expr right) {
      this.left = left;
      this.operator = operator;
      this.right = right;
    }

    @Override
    <R> R accept(Visitor<R> visitor) {
      return visitor.visitBinaryExpr(this);
    }

    final Expr left;
    final Token operator;
    final Expr right;
  }
  static class Grouping extends Expr {
    Grouping(Expr expression) {
      this.expression = expression;
    }

    @Override
    <R> R accept(Visitor<R> visitor) {
      return visitor.visitGroupingExpr(this);
    }

    final Expr expression;
  }
  static class Literal extends Expr {
    Literal(Object value) {
      this.value = value;
    }

    @Override
    <R> R accept(Visitor<R> visitor) {
      return visitor.visitLiteralExpr(this);
    }

    final Object value;
  }
  static class Unary extends Expr {
    Unary(Token operator, Expr right) {
      this.operator = operator;
      this.right = right;
    }

    @Override
    <R> R accept(Visitor<R> visitor) {
      return visitor.visitUnaryExpr(this);
    }

    final Token operator;
    final Expr right;
  }

  abstract <R> R accept(Visitor<R> visitor);
}
