package synth.core;

import synth.cfg.CFG;
import synth.cfg.NonTerminal;

import java.util.*;

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
        // TODO: implement this method
        ASTNode start = new ASTNode(new NonTerminal("E"), Collections.emptyList());
        Queue<ASTNode> workList = new ArrayDeque<>();
        workList.offer(start);
        while (!workList.isEmpty()) {
            ASTNode ast = workList.poll();
            if (checkComplete(ast) && satisfy(cfg, ast, examples)) {
                return new Program(ast);
            }
            workList.addAll(expand(cfg, ast));
        }
        throw new RuntimeException("To be implemented");
    }

    public List<ASTNode> expand(CFG cfg, ASTNode root) {
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

    public boolean satisfy(CFG cfg, ASTNode root, List<Example> examples) {
        Program program = new Program(root);
        for (Example example : examples) {
            if (Interpreter.evaluate(program, example.getInput()) != example.getOutput()) {
                return false;
            }
        }
        return true;
    }
}