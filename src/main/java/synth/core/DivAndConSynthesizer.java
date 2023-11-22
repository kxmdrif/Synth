package synth.core;

import synth.cfg.CFG;
import synth.cfg.Terminal;

import java.util.*;
import java.util.stream.Collectors;

public class DivAndConSynthesizer implements ISynthesizer {

    /**
     * Synthesize a program f(x, y, z) based on a context-free grammar and examples
     *
     * @param cfg      the context-free grammar
     * @param examples a list of examples
     * @return the program or null to indicate synthesis failure
     */
    @Override
    public Program synthesize(CFG cfg, List<Example> examples) {
        doInit(cfg, examples);

        // Check the initial exprList. New generated expr will be checked at once.
        for (int i = 0; i < exprList.size(); ++i) {
            ASTNode expr = exprList.get(i);
            if (satisfy(expr)) {
                return new Program(expr);
            }
            ASTNode divExpr = checkAndSynthesisDiv(expr);
            if (divExpr != null) {
                return new Program(divExpr);
            }
        }

        while (true) {
            growPred();
            ASTNode result = growExpr();
            if (result != null) {
                return new Program(result);
            }
        }
//        return null;
    }

    private void doInit(CFG cfg, List<Example> examples) {
        this.exprEquivalentClass = new HashMap<>();
        this.predEquivalentClass = new HashMap<>();
        this.cfg = cfg;
        this.examples = examples;
        this.exprList = new ArrayList<>();
        this.predList = new ArrayList<>();
        this.growExprByExprPtrMax = new int[]{0, 0};
        this.growPredByExprPtrMax = new int[]{0, 0};
        this.growPredBy1PredPtrMax = new int[]{0};
        this.growPredBy2PredPtrMax = new int[]{0, 0};


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
    private CFG cfg;
    private List<Example> examples;
    private List<ASTNode> exprList;
    private List<ASTNode> predList;
    private final String[] starts = {"1", "2", "3", "x", "y", "z"};
    private int[] growExprByExprPtrMax;
    private int[] growPredByExprPtrMax;
    private int[] growPredBy1PredPtrMax;
    private int[] growPredBy2PredPtrMax;

    // invoke secondly
    private ASTNode growExpr() {
        int exprSize = exprList.size();
        for (int i = 0; i < exprSize; ++i) {
            for (int j = i; j < exprSize; ++j) {
                if (i < growExprByExprPtrMax[0] && j < growExprByExprPtrMax[1]) {
                    continue;
                }
                ASTNode result = growExprByExpr(exprList.get(i), exprList.get(j));
                if (result != null) {
                    return result;
                }
            }
        }
        growExprByExprPtrMax[0] = growExprByExprPtrMax[1] = exprSize;
        return null;
    }

    //invoke firstly
    private void growPred() {
        int exprSize = exprList.size();
        for (int i = 0; i < exprSize; ++i) {
            for (int j = i; j < exprSize; ++j) {
                if (i < growPredByExprPtrMax[0] && j < growPredByExprPtrMax[1]) {
                    continue;
                }
                growPredByExpr(exprList.get(i), exprList.get(j));
            }
        }
        growPredByExprPtrMax[0] = growPredByExprPtrMax[1] = exprSize;

        // memorize size to avoid infinite loop as we change the list size
        int predSize = predList.size();
        for (int i = 0; i < predSize; ++i) {
            if (i < growPredBy1PredPtrMax[0]) {
                continue;
            }
            growPredBy1Pred(predList.get(i));
        }
        growPredBy1PredPtrMax[0] = predSize;

        for (int i = 0; i < predSize; ++i) {
            //start from i + 1 because And(pred[i], pred[i]) = pred[i], Or(pred[i], pred[i]) = pred[i]
            for (int j = i + 1; j < predSize; ++j) {
                if (i < growPredBy2PredPtrMax[0] && j < growPredBy2PredPtrMax[1]) {
                    continue;
                }
                growPredBy2Pred(predList.get(i), predList.get(j));
            }
        }
        growPredBy2PredPtrMax[0] = growPredBy2PredPtrMax[1] = predSize;
    }

    private ASTNode growExprByExpr(ASTNode left, ASTNode right) {
        String[] ops = {"Add", "Multiply"};
        for (String op : ops) {
            ASTNode newExpr = new ASTNode(new Terminal(op), List.of(left, right));
            if (satisfy(newExpr)) {
                return newExpr;
            }
            ASTNode divExpr = checkAndSynthesisDiv(newExpr);
            if (divExpr != null) {
                return divExpr;
            }
            if (!checkExprExistsAndUpdateEqClass(newExpr)) {
                exprList.add(newExpr);
            }
        }
        return null;
    }

    private ASTNode checkAndSynthesisDiv(ASTNode expr) {
        byte[] satBitMap = getSatExamples(expr);
        if (!checkDiv(satBitMap)) {
            return null;
        }
        ASTNode divPred = findDivPred(satBitMap);
        if (divPred == null) {
            return null;
        }
        List<Example> unSatExamples = new ArrayList<>();
        for (int i = 0; i < satBitMap.length; ++i) {
            if (satBitMap[i] == 0) {
                unSatExamples.add(this.examples.get(i));
            }
        }
        DivAndConSynthesizer divAndConSynthesizer = new DivAndConSynthesizer();
        Program unSatPart = divAndConSynthesizer.synthesize(this.cfg, unSatExamples);
        if (unSatPart == null) {
            return null;
        }
        return new ASTNode(new Terminal("Ite"), List.of(
                divPred,
                expr,
                unSatPart.getRoot()
        ));
    }

    private ASTNode findDivPred(byte[] bitMap) {
        for (ASTNode pred : this.predList) {
            boolean canDiv = true;
            for (int i = 0; i < bitMap.length; ++i) {
                boolean satPred = Interpreter.evaluatePred(pred, this.examples.get(i).getInput());
                byte sat = (byte) (satPred ? 1 : 0);
                if (sat != bitMap[i]) {
                    canDiv = false;
                    break;
                }
            }
            if (canDiv) {
                return pred;
            }
        }
        return null;
    }

    private byte[] getSatExamples(ASTNode expr) {
        byte[] bitMap = new byte[this.examples.size()];
        for (int i = 0; i < this.examples.size(); ++i) {
            Example example = examples.get(i);
            if (Interpreter.evaluateExpr(expr, example.getInput()) == example.getOutput()) {
                bitMap[i] = 1;
            } else {
                bitMap[i] = 0;
            }
        }
        return bitMap;
    }
    private boolean checkDiv(byte[] bitMap) {
        int count = 0;
        for (byte b : bitMap) {
            if (b == 1) {
                ++count;
            }
        }
        return count > bitMap.length / 2;
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


    private boolean satisfy(ASTNode root) {
        for (Example example : this.examples) {
            if (Interpreter.evaluateExpr(root, example.getInput()) != example.getOutput()) {
                return false;
            }
        }
        return true;
    }
}
