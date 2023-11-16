package synth.egg;

public class Egg {
    public static String simplify(String expr) {
        return Lib.INSTANCE.simplify(expr);
    }

    public static boolean equal(String l, String r) {
        return Lib.INSTANCE.equal(l, r);
    }

}
