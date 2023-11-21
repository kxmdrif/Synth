package synth.core;

import com.microsoft.z3.AST;
import synth.cfg.CFG;
import synth.cfg.Terminal;

import java.util.*;
import java.util.stream.Collectors;

public class OptimizedSynthesizer implements ISynthesizer {

    /**
     * Synthesize a program f(x, y, z) based on a context-free grammar and examples
     *
     * @param cfg      the context-free grammar
     * @param examples a list of examples
     * @return the program or null to indicate synthesis failure
     */
    @Override
    public Program synthesize(CFG cfg, List<Example> examples) {
        doInit(examples);
        while (true) {
            growPred();
            growExpr();
            for (ASTNode expr : exprList) {
                if (satisfy(expr, examples)) {
                    return new Program(expr);
                }
            }
        }
//        return null;
    }

    private void doInit(List<Example> examples) {
        this.exprEquivalentClass = new HashMap<>();
        this.predEquivalentClass = new HashMap<>();
        this.examples = examples;
        this.exprList = new ArrayList<>();
        this.predList = new ArrayList<>();
        for (String start : this.starts) {
            ASTNode startSymbol = new ASTNode(new Terminal(start), Collections.emptyList());
            if (!checkExprExistsAndUpdateEqClass(startSymbol)) {
                exprList.add(startSymbol);
            }
        }
    }

    /**
     * eval_example_1|eval_example_|...|eval_example_n -> AstNode
     */
    private Map<String, ASTNode> exprEquivalentClass;
    private Map<String, ASTNode> predEquivalentClass;
    private List<Example> examples;
    private List<ASTNode> exprList;
    private List<ASTNode> predList;
    private final String[] starts = {"1", "2", "3", "x", "y", "z"};

    // invoke secondly
    private void growExpr() {
        int exprSize = exprList.size();
        int predSize = predList.size();
        for (int i = 0; i < exprSize; ++i) {
            for (int j = 0; j < exprSize; ++j) {
                growExprByExpr(exprList.get(i), exprList.get(j));
            }
        }
        for (int i = 0; i < exprSize; ++i) {
            for (int j = 0; j < exprSize; ++j) {
                for (int k = 0; k < predSize; ++k) {
                    growExprByPred(predList.get(k), exprList.get(i), exprList.get(j));
                }
            }
        }
    }

    private void growExprByExpr(ASTNode left, ASTNode right) {
        String[] ops = {"Add", "Multiply"};
        for (String op : ops) {
            ASTNode newExpr = new ASTNode(new Terminal(op), List.of(left, right));
            if (!checkExprExistsAndUpdateEqClass(newExpr)) {
                exprList.add(newExpr);
            }
        }
    }

    //todo temporary ignore Ite and use divide and conquer
    private void growExprByPred(ASTNode pred, ASTNode left, ASTNode right) {
        List<ASTNode> newExprList = List.of(
                new ASTNode(new Terminal("Ite"), List.of(pred, left, right)),
                new ASTNode(new Terminal("Ite"), List.of(pred, left, right))
        );
        for (ASTNode newExpr : newExprList) {
            if (!checkExprExistsAndUpdateEqClass(newExpr)) {
                exprList.add(newExpr);
            }
        }
    }

    //invoke firstly
    //todo memorize the iter last time to avoid starting from 0
    private void growPred() {
        int exprSize = exprList.size();
        for (int i = 0; i < exprSize; ++i) {
            for (int j = i + 1; j < exprSize; ++j) {
                growPredByExpr(exprList.get(i), exprList.get(j));
            }
        }
        // memorize size to avoid infinite loop as we change the list size
        int predSize = predList.size();
        for (int i = 0; i < predSize; ++i) {
            growPredBy1Pred(predList.get(i));
        }
        for (int i = 0; i < predSize; ++i) {
            for (int j = 0; j < predSize; ++j) {
                growPredBy2Pred(predList.get(i), predList.get(j));
            }
        }
    }

    private void growPredByExpr(ASTNode left, ASTNode right) {
        // need copy or not? If later process does not modify, we do not need to copy to save space.
        List<ASTNode> newPredList = List.of(
                new ASTNode(new Terminal("Lt"), List.of(left, right)),
                new ASTNode(new Terminal("Lt"), List.of(right, left)),
                new ASTNode(new Terminal("Eq"), List.of(left, right))
        );
        for (ASTNode newPred : newPredList) {
            if (!checkPredExistsAndUpdateEqClass(newPred)) {
                predList.add(newPred);
            }
        }
    }

    private void growPredBy2Pred(ASTNode left, ASTNode right) {
        String[] ops = {"And", "Or"};
        for (String op : ops) {
            ASTNode newPred = new ASTNode(new Terminal(op), List.of(left, right));
            if (!checkPredExistsAndUpdateEqClass(newPred)) {
                predList.add(newPred);
            }
        }
    }

    private void growPredBy1Pred(ASTNode pred) {
        String[] ops = {"Not"};
        for (String op : ops) {
            ASTNode newPred = new ASTNode(new Terminal(op), List.of(pred));
            if (!checkPredExistsAndUpdateEqClass(newPred)) {
                predList.add(newPred);
            }
        }
    }

    /**
     * @param expr
     * @return true if contains and update the expr to the simpler one (based on size).
     * false if not contains and add it to the map.
     */
    private boolean checkExprExistsAndUpdateEqClass(ASTNode expr) {
        List<String> outputs = this.examples.stream()
                .map(i -> Interpreter.evaluateExpr(expr, i.getInput()))
                .map(i -> Integer.toString(i))
                .collect(Collectors.toList());
        String key = String.join("|", outputs);
        if (this.exprEquivalentClass.containsKey(key)) {
            ASTNode original = this.exprEquivalentClass.get(key);
            if (expr.size() < original.size()) {
                this.exprEquivalentClass.put(key, expr);
            }
            return true;
        } else {
            this.exprEquivalentClass.put(key, expr);
            return false;
        }
    }

    /**
     * @param pred
     * @return true if contains and update the pred to the simpler one (based on size).
     * false if not contains and add it to the map.
     */
    private boolean checkPredExistsAndUpdateEqClass(ASTNode pred) {
        List<String> outputs = this.examples.stream()
                .map(i -> Interpreter.evaluatePred(pred, i.getInput()))
                .map(i -> (i ? "1" : "0"))
                .collect(Collectors.toList());
        String key = String.join("|", outputs);
        if (this.predEquivalentClass.containsKey(key)) {
            ASTNode original = this.predEquivalentClass.get(key);
            if (pred.size() < original.size()) {
                this.predEquivalentClass.put(key, pred);
            }
            return true;
        } else {
            this.predEquivalentClass.put(key, pred);
            return false;
        }
    }


    private boolean satisfy(ASTNode root, List<Example> examples) {
        Program program = new Program(root);
        for (Example example : examples) {
            if (Interpreter.evaluate(program, example.getInput()) != example.getOutput()) {
                return false;
            }
        }
        return true;
    }
}
