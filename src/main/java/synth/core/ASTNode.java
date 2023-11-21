package synth.core;

import synth.cfg.NonTerminal;
import synth.cfg.Symbol;
import synth.cfg.Terminal;
import synth.egg.Egg;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class ASTNode {
    private final Symbol symbol;
    private final List<ASTNode> children;

    public ASTNode(Symbol symbol, List<ASTNode> children) {
        this.symbol = symbol;
        this.children = children;
    }

    public Symbol getSymbol() {
        return symbol;
    }

    public List<ASTNode> getChildren() {
        return children;
    }

    public ASTNode getChild(int index) {
        return children.get(index);
    }

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        builder.append(symbol);
        String separator = "";
        if (!children.isEmpty()) {
            builder.append("(");
            for (ASTNode child : children) {
                builder.append(separator);
                separator = ", ";
                builder.append(child);
            }
            builder.append(")");
        }
        return builder.toString();
    }
     public String toEggExpr() {
        if (children.isEmpty()) return symbol.getName();
        StringBuilder sb = new StringBuilder();
        sb.append("(");
        sb.append(symbol.getName());
        for (ASTNode child : children) {
            sb.append(" ").append(child.toEggExpr());
        }
        sb.append(")");
        return sb.toString();
     }

     public int size() {
        if (children.isEmpty()) return 1;
        int size = 1;
        for (ASTNode child : children) {
            size += child.size();
        }
        return size;
     }

    public ASTNode copy() {
        List<ASTNode> children = new ArrayList<>();
        String rootName = this.getSymbol().getName();
        for (ASTNode child : this.getChildren()) {
            children.add(child.copy());
        }
        if (this.getSymbol().isTerminal()) {
            return new ASTNode(new Terminal(rootName), children);
        } else {
            return new ASTNode(new NonTerminal(rootName), children);
        }
    }

}
