package synth.core;

import org.junit.Assert;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.Timeout;
import synth.cfg.CFG;
import synth.cfg.NonTerminal;
import synth.cfg.Production;
import synth.cfg.Terminal;
import synth.util.FileUtils;
import synth.util.Parser;

import java.util.*;
import java.util.concurrent.TimeUnit;

public class BenchmarkTest {
    @Rule
    public Timeout timeout = new Timeout(1, TimeUnit.MINUTES);
    @Test
    public void benchmarkTest1() {
        final String path = "benchmark/benchmark1.txt";
        runBenchmark(path);
    }

    @Test
    public void benchmarkTest2() {
        final String path = "benchmark/benchmark2.txt";
        runBenchmark(path);
    }

    private void runBenchmark(String path) {
        List<Example> examples = loadExamples(path);
        CFG cfg = buildCFG();
        ISynthesizer synthesizer = new DivAndConSynthesizer();
        Program program = synthesizer.synthesize(cfg, examples);
        System.out.print("PROGRAM: ");
        System.out.println(program);
        Assert.assertTrue(verify(program, examples));
    }

    private boolean verify(Program program, List<Example> examples) {
        if (program == null) {
            return true;
        }
        for (Example example : examples) {
            if (Interpreter.evaluate(program, example.getInput()) != example.getOutput()) {
                return false;
            }
        }
        return true;
    }

    private List<Example> loadExamples(String examplesFilePath) {
        List<String> lines = FileUtils.readLinesFromFile(examplesFilePath);
        return Parser.parseAllExamples(lines);
    }
    private CFG buildCFG() {
        NonTerminal startSymbol = new NonTerminal("E");
        Map<NonTerminal, List<Production>> symbolToProductions = new HashMap<>();
        {
            NonTerminal retSymbol = new NonTerminal("E");
            List<Production> prods = new ArrayList<>();
            prods.add(new Production(new NonTerminal("E"), new Terminal("Ite"), List.of(new NonTerminal("B"), new NonTerminal("E"), new NonTerminal("E"))));
            prods.add(new Production(new NonTerminal("E"), new Terminal("Add"), List.of(new NonTerminal("E"), new NonTerminal("E"))));
            prods.add(new Production(new NonTerminal("E"), new Terminal("Multiply"), List.of(new NonTerminal("E"), new NonTerminal("E"))));
            prods.add(new Production(new NonTerminal("E"), new Terminal("x"), Collections.emptyList()));
            prods.add(new Production(new NonTerminal("E"), new Terminal("y"), Collections.emptyList()));
            prods.add(new Production(new NonTerminal("E"), new Terminal("z"), Collections.emptyList()));
            prods.add(new Production(new NonTerminal("E"), new Terminal("1"), Collections.emptyList()));
            prods.add(new Production(new NonTerminal("E"), new Terminal("2"), Collections.emptyList()));
            prods.add(new Production(new NonTerminal("E"), new Terminal("3"), Collections.emptyList()));
            symbolToProductions.put(retSymbol, prods);
        }
        {
            NonTerminal retSymbol = new NonTerminal("B");
            List<Production> prods = new ArrayList<>();
            prods.add(new Production(new NonTerminal("B"), new Terminal("Lt"), List.of(new NonTerminal("E"), new NonTerminal("E"))));
            prods.add(new Production(new NonTerminal("B"), new Terminal("Eq"), List.of(new NonTerminal("E"), new NonTerminal("E"))));
            prods.add(new Production(new NonTerminal("B"), new Terminal("And"), List.of(new NonTerminal("B"), new NonTerminal("B"))));
            prods.add(new Production(new NonTerminal("B"), new Terminal("Or"), List.of(new NonTerminal("B"), new NonTerminal("B"))));
            prods.add(new Production(new NonTerminal("B"), new Terminal("Not"), List.of(new NonTerminal("B"))));
            symbolToProductions.put(retSymbol, prods);
        }
        return new CFG(startSymbol, symbolToProductions);
    }
}
