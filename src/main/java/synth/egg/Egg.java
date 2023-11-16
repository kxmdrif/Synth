package synth.egg;

public class Egg {
    public static int add(int a, int b) {
        return Lib.INSTANCE.add(a, b);
    }

    public static void hello() {
        Lib.INSTANCE.hello();
    }

    public static String simplify(String expr) {
        return Lib.INSTANCE.simplify(expr);
    }
}
