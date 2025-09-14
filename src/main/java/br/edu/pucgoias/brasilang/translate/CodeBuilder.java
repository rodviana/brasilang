package br.edu.pucgoias.brasilang.translate;

/** Utility to help building C code with proper indentation. */
public class CodeBuilder {

    private final StringBuilder sb = new StringBuilder();
    private int indent = 0;

    public void indent() { indent++; }
    public void outdent() { if (indent > 0) indent--; }

    public void appendLine(String line) {
        sb.append("    ".repeat(indent)).append(line).append("\n");
    }

    public String build() { return sb.toString(); }
}
