package synth.core;

import synth.cfg.CFG;
import synth.cfg.NonTerminal;
import synth.cfg.Production;
import synth.cfg.Terminal;

import java.util.*;
import java.util.stream.Collectors;

public class TopDownEnumSynthesizer implements ISynthesizer {

    /**
     * Synthesize a program f(x, y, z) based on a context-free grammar and examples
     *
     * @param cfg      the context-free grammar
     * @param examples a list of examples
     * @return the program or null to indicate synthesis failure
     */
    @Override
    public Program synthesize(CFG cfg, List<Example> examples) {
        int MAX_CAPACITY = 5000000;
        ASTNode start = new ASTNode(new NonTerminal("E"), Collections.emptyList());
        Queue<ASTNode> workList = new ArrayDeque<>();
        workList.offer(start);
        while (!workList.isEmpty()) {
            if (workList.size() > MAX_CAPACITY) {
                return null;
            }
            ASTNode ast = workList.poll();
            if (checkComplete(ast) && satisfy(ast, examples)) {
                return new Program(ast);
            }
            workList.addAll(expand(cfg, ast));
        }
        return null;

    }

    public ASTNode copyAST(ASTNode src) {
        List<ASTNode> children = new ArrayList<>();
        String rootName = src.getSymbol().getName();
        for (ASTNode child : src.getChildren()) {
            children.add(copyAST(child));
        }
        if (src.getSymbol().isTerminal()) {
            return new ASTNode(new Terminal(rootName), children);
        } else {
            return new ASTNode(new NonTerminal(rootName), children);
        }
    }

    public boolean checkLeaf(ASTNode node) {
        return node.getChildren().isEmpty();
    }

    /**
     * Replace non-terminal nodes once a time (non-terminal symbols must be leaf nodes)
     */
    public List<ASTNode> expand(CFG cfg, ASTNode root) {
        ASTNode nodeToExpand = findNodeToExpand(root);
        if (nodeToExpand == null) {
            return new ArrayList<>();
        }
        List<Production> prods = cfg.getProductions((NonTerminal) nodeToExpand.getSymbol());
        List<ASTNode> replaceList = new ArrayList<>();
        for (Production prod : prods) {
            List<ASTNode> children = prod.getArgumentSymbols().stream()
                    .map(i -> new ASTNode(i, new ArrayList<>())).collect(Collectors.toList());
            replaceList.add(new ASTNode(prod.getOperator(), children));
        }
        List<ASTNode> expanded = new ArrayList<>();
        for (ASTNode rep : replaceList) {
            ASTNode copy = copyAST(root);
            ASTNode src = findNodeToExpand(copy);
            expanded.add(replace(copy, src, rep));
        }
        return expanded;
    }

    public ASTNode replace(ASTNode root, ASTNode src, ASTNode dest) {
        if (root == src) {
            return dest;
        }
        for (int i = 0; i < root.getChildren().size(); ++i) {
            ASTNode child = root.getChild(i);
            ASTNode repSub = replace(child, src, dest);
            if (repSub != child) {
                root.getChildren().set(i, repSub);
                break;
            }
        }
        return root;
    }

    //null means no node to expand
    public ASTNode findNodeToExpand(ASTNode root) {
        if (checkLeaf(root)) {
            if (root.getSymbol().isTerminal()) {
                return null;
            } else if (root.getSymbol().isNonTerminal()) {
                return root;
            }
        }
        for (ASTNode child : root.getChildren()) {
            ASTNode exp = findNodeToExpand(child);
            if (exp != null) {
                return exp;
            }
        }
        return null;
    }

    public boolean checkComplete(ASTNode root) {
        if (root == null) return true;
        if (root.getSymbol().isNonTerminal()) return false;
        for (ASTNode child : root.getChildren()) {
            if (!checkComplete(child)) {
                return false;
            }
        }
        return true;

    }

    public boolean satisfy(ASTNode root, List<Example> examples) {
        Program program = new Program(root);
        for (Example example : examples) {
            if (Interpreter.evaluate(program, example.getInput()) != example.getOutput()) {
                return false;
            }
        }
        return true;
    }
}
