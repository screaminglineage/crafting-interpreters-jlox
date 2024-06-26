package tool;

import java.io.IOException;
import java.io.PrintWriter;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.List;

public class GenerateAst {
    public static void main(String[] args) throws IOException {
        if (args.length != 1) {
            System.out.println("Usage: generate_ast [output_directory]");
            System.exit(64);
        }
        String outputDir = args[0];
        defineAst(outputDir, "Expr", Arrays.asList(
                "Binary : Expr left, Token operator, Expr right",
                "Grouping : Expr expression",
                "Literal : Object value",
                "Unary : Token operator, Expr right",
                "Variable : Token name",
                "Assign : Token name, Expr value"
        ));
        defineAst(outputDir, "Stmt", Arrays.asList(
                "Expression : Expr expression",
                "Print : Expr expression",
                "Var : Token name, Expr initializer",
                "Block : List<Stmt> statements"

        ));
    }

    private static void defineAst(String outputDir, String baseName, List<String> types) throws IOException {
        String path = STR."\{outputDir}/\{baseName}.java";
        PrintWriter writer = new PrintWriter(path, StandardCharsets.UTF_8);
        writer.println("package lox;");
        writer.println();
        writer.println("import java.util.List;");
        writer.println();
        writer.println(STR."abstract class \{baseName} {");

        defineVisitor(writer, baseName, types);

        for (String type: types) {
            String classname = type.split(":")[0].trim();
            String fields = type.split(":")[1].trim();
            defineType(writer, baseName, classname, fields);
        }

        // The base accept() method.
        writer.println();
        writer.println("  abstract <R> R accept(Visitor<R> visitor);");

        writer.println("}");
        writer.close();
    }

    private static void defineType(PrintWriter writer, String baseName, String className, String fieldList) {
        writer.println(STR."  static class \{className} extends \{baseName} {");

        // Constructor.
        writer.println(STR."    \{className}(\{fieldList}) {");

        // Store parameters in fields.
        String[] fields = fieldList.split(", ");
        for (String field : fields) {
            String name = field.split(" ")[1];
            writer.println(STR."      this.\{name} = \{name};");
        }

        writer.println("    }");

        // Visitor pattern.
        writer.println();
        writer.println("    @Override");
        writer.println("    <R> R accept(Visitor<R> visitor) {");
        writer.println(STR."      return visitor.visit\{className}\{baseName}(this);");
        writer.println("    }");

        // Fields.
        writer.println();
        for (String field : fields) {
            writer.println(STR."    final \{field};");
        }

        writer.println("  }");
    }

    private static void defineVisitor(PrintWriter writer, String baseName, List<String> types) {
        writer.println("  interface Visitor<R> {");

        for (String type : types) {
            String typeName = type.split(":")[0].trim();
            writer.println(STR."    R visit\{typeName}\{baseName}(\{typeName} \{baseName.toLowerCase()});");
        }
        writer.println("  }");
    }
}

