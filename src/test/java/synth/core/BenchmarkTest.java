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
    @Test
    public void benchmarkTest3() {
        final String path = "benchmark/benchmark3.txt";
        runBenchmark(path);
    }
    @Test
    public void benchmarkTest4() {
        final String path = "benchmark/benchmark4.txt";
        runBenchmark(path);
    }
    @Test
    public void benchmarkTest5() {
        final String path = "benchmark/benchmark5.txt";
        runBenchmark(path);
    }
    @Test
    public void benchmarkTest6() {
        final String path = "benchmark/benchmark6.txt";
        runBenchmark(path);
    }
    @Test
    public void benchmarkTest7() {
        final String path = "benchmark/benchmark7.txt";
        runBenchmark(path);
    }
    @Test
    public void benchmarkTest8() {
        final String path = "benchmark/benchmark8.txt";
        runBenchmark(path);
    }
    @Test
    public void benchmarkTest9() {
        final String path = "benchmark/benchmark9.txt";
        runBenchmark(path);
    }
    @Test
    public void benchmarkTest10() {
        final String path = "benchmark/benchmark10.txt";
        runBenchmark(path);
    }
    @Test
    public void benchmarkTest11() {
        final String path = "benchmark/benchmark11.txt";
        runBenchmark(path);
    }
    @Test
    public void benchmarkTest12() {
        final String path = "benchmark/benchmark12.txt";
        runBenchmark(path);
    }
    @Test
    public void benchmarkTest13() {
        final String path = "benchmark/benchmark13.txt";
        runBenchmark(path);
    }
    @Test
    public void benchmarkTest14() {
        final String path = "benchmark/benchmark14.txt";
        runBenchmark(path);
    }
    @Test
    public void benchmarkTest15() {
        final String path = "benchmark/benchmark15.txt";
        runBenchmark(path);
    }
    @Test
    public void benchmarkTest16() {
        final String path = "benchmark/benchmark16.txt";
        runBenchmark(path);
    }
    @Test
    public void benchmarkTest17() {
        final String path = "benchmark/benchmark17.txt";
        runBenchmark(path);
    }
    @Test
    public void benchmarkTest18() {
        final String path = "benchmark/benchmark18.txt";
        runBenchmark(path);
    }
    @Test
    public void benchmarkTest19() {
        final String path = "benchmark/benchmark19.txt";
        runBenchmark(path);
    }
    @Test
    public void benchmarkTest20() {
        final String path = "benchmark/benchmark20.txt";
        runBenchmark(path);
    }
    @Test
    public void benchmarkTest21() {
        final String path = "benchmark/benchmark21.txt";
        runBenchmark(path);
    }
    @Test
    public void benchmarkTest22() {
        final String path = "benchmark/benchmark22.txt";
        runBenchmark(path);
    }
    @Test
    public void benchmarkTest23() {
        final String path = "benchmark/benchmark23.txt";
        runBenchmark(path);
    }
    @Test
    public void benchmarkTest24() {
        final String path = "benchmark/benchmark24.txt";
        runBenchmark(path);
    }
    @Test
    public void benchmarkTest25() {
        final String path = "benchmark/benchmark25.txt";
        runBenchmark(path);
    }
    @Test
    public void benchmarkTest26() {
        final String path = "benchmark/benchmark26.txt";
        runBenchmark(path);
    }
    @Test
    public void benchmarkTest27() {
        final String path = "benchmark/benchmark27.txt";
        runBenchmark(path);
    }
    @Test
    public void benchmarkTest28() {
        final String path = "benchmark/benchmark28.txt";
        runBenchmark(path);
    }
    @Test
    public void benchmarkTest29() {
        final String path = "benchmark/benchmark29.txt";
        runBenchmark(path);
    }
    @Test
    public void benchmarkTest30() {
        final String path = "benchmark/benchmark30.txt";
        runBenchmark(path);
    }
    @Test
    public void benchmarkTest31() {
        final String path = "benchmark/benchmark31.txt";
        runBenchmark(path);
    }
    @Test
    public void benchmarkTest32() {
        final String path = "benchmark/benchmark32.txt";
        runBenchmark(path);
    }
    @Test
    public void benchmarkTest33() {
        final String path = "benchmark/benchmark33.txt";
        runBenchmark(path);
    }
    @Test
    public void benchmarkTest34() {
        final String path = "benchmark/benchmark34.txt";
        runBenchmark(path);
    }
    @Test
    public void benchmarkTest35() {
        final String path = "benchmark/benchmark35.txt";
        runBenchmark(path);
    }
    @Test
    public void benchmarkTest36() {
        final String path = "benchmark/benchmark36.txt";
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
