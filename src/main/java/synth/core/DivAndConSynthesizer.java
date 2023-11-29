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
        ASTNode initResult = doInit(cfg, examples);
        if (initResult != null) {
            return new Program(initResult);
        }
        if (checkInfeasibleExamples()) {
            return null;
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

    /**
     * eval_example_1|eval_example_|...|eval_example_n -> Index Of AstNode
     */
    private Map<String, Integer> exprEquivalentClass;
    private Map<String, Integer> predEquivalentClass;
    private CFG cfg;
    private List<Example> examples;
    private List<ASTNode> exprList;
    private List<ASTNode> predList;
    private final String[] starts = {"1", "2", "3", "x", "y", "z"};
    private int[] growExprByExprPtrMax;
    private int[] growPredByExprPtrMax;
    private int[] growPredBy1PredPtrMax;
    private int[] growPredBy2PredPtrMax;

    private ASTNode doInit(CFG cfg, List<Example> examples) {
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
            if (satisfy(startSymbol)) {
                return startSymbol;
            }
            ASTNode divExpr = checkAndSynthesisDiv(startSymbol);
            if (divExpr != null) {
                return divExpr;
            }
            checkAndAddExpr(startSymbol);
        }
        return null;
    }

    private boolean checkInfeasibleExamples() {
        // check conflict examples
        Map<String, Integer> map = new HashMap<>();
        for (Example example : this.examples) {
            String key = example.getInput().get("x")
                    + "|" + example.getInput().get("y")
                    + "|" + example.getInput().get("z");
            Integer val = map.get(key);
            if (val == null) {
                map.put(key, example.getOutput());
            } else {
                if (val != example.getOutput()) {
                    return true;
                }
            }
        }
        // check out of range examples
        for (Example example : this.examples) {
            int x = example.getInput().get("x");
            int y = example.getInput().get("y");
            int z = example.getInput().get("z");
            // min value among x,y,x,1,2,3
            int min = Math.min(x, Math.min(y, Math.min(z, 1)));
            int output = example.getOutput();
            if ((min == 0 || min == 1) && output < min) {
                return true;
            }
        }
        return false;
    }

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
            checkAndAddExpr(newExpr);
        }
        return null;
    }

    private ASTNode checkAndSynthesisDiv(ASTNode expr) {
        // use the same encoding way as predEquivalentClass
        String satBitMap = getSatExamples(expr);
        if (!checkDiv(satBitMap)) {
            return null;
        }
        ASTNode divPred = findDivPred(satBitMap);
        if (divPred == null) {
            return null;
        }
        List<Example> unSatExamples = new ArrayList<>();
        for (int i = 0; i < satBitMap.length(); ++i) {
            if (satBitMap.charAt(i) == '0') {
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

    private ASTNode findDivPred(String bitMap) {
        Integer predIdx = this.predEquivalentClass.get(bitMap);
        if (predIdx == null) {
            return null;
        }
        return this.predList.get(predIdx);
    }

    private String getSatExamples(ASTNode expr) {
        StringBuilder bitMap = new StringBuilder();
        for (Example example : this.examples) {
            if (Interpreter.evaluateExpr(expr, example.getInput()) == example.getOutput()) {
                bitMap.append('1');
            } else {
                bitMap.append('0');
            }
        }
        return bitMap.toString();
    }
    private boolean checkDiv(String bitMap) {
        int count = 0;
        for (char c : bitMap.toCharArray()) {
            if (c == '1') {
                ++count;
            }
        }
        return count > bitMap.length() / 2;
    }

    private void growPredByExpr(ASTNode left, ASTNode right) {
        // need copy or not? If later process does not modify, we do not need to copy to save space.
        List<ASTNode> newPredList = List.of(
                new ASTNode(new Terminal("Lt"), List.of(left, right)),
                new ASTNode(new Terminal("Lt"), List.of(right, left)),
                new ASTNode(new Terminal("Eq"), List.of(left, right))
        );
        for (ASTNode newPred : newPredList) {
            checkAndAddPred(newPred);
        }
    }

    private void growPredBy2Pred(ASTNode left, ASTNode right) {
        String[] ops = {"And", "Or"};
        for (String op : ops) {
            ASTNode newPred = new ASTNode(new Terminal(op), List.of(left, right));
            checkAndAddPred(newPred);
        }
    }

    private void growPredBy1Pred(ASTNode pred) {
        String[] ops = {"Not"};
        for (String op : ops) {
            ASTNode newPred = new ASTNode(new Terminal(op), List.of(pred));
            checkAndAddPred(newPred);
        }
    }

    /**
     * @param expr expression
     */
    private void checkAndAddExpr(ASTNode expr) {
        List<String> outputs = this.examples.stream()
                .map(i -> Interpreter.evaluateExpr(expr, i.getInput()))
                .map(i -> Integer.toString(i))
                .collect(Collectors.toList());
        String key = String.join("|", outputs);
        if (this.exprEquivalentClass.containsKey(key)) {
            int originalIdx = this.exprEquivalentClass.get(key);
            if (expr.size() < this.exprList.get(originalIdx).size()) {
                this.exprList.set(originalIdx, expr);
            }
        } else {
            this.exprEquivalentClass.put(key, this.exprList.size());
            this.exprList.add(expr);
        }
    }

    /**
     * @param pred predicate
     */
    private void checkAndAddPred(ASTNode pred) {
        List<String> outputs = this.examples.stream()
                .map(i -> Interpreter.evaluatePred(pred, i.getInput()))
                .map(i -> (i ? "1" : "0"))
                .collect(Collectors.toList());
        String key = String.join("", outputs);
        if (this.predEquivalentClass.containsKey(key)) {
            int originalIdx = this.predEquivalentClass.get(key);
            if (pred.size() < this.predList.get(originalIdx).size()) {
                this.predList.set(originalIdx, pred);
            }
        } else {
            this.predEquivalentClass.put(key, this.predList.size());
            this.predList.add(pred);
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
