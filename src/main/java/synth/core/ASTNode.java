package synth.core;

import synth.cfg.Symbol;
import synth.egg.Egg;

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

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ASTNode astNode = (ASTNode) o;
        return Egg.equal(this.toEggExpr(), astNode.toEggExpr());
    }

    // todo: more efficient
    //  Not sure correct??
    @Override
    public int hashCode() {
        return Egg.simplify(this.toEggExpr()).length();
    }

}
