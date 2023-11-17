package synth.egg;

/**
 * can only be used for completed ASTs(ones without non-terminals)
 */
public class Egg {
    public static String simplify(String expr) {
        return Lib.INSTANCE.simplify(expr);
    }

    public static boolean equal(String l, String r) {
        return Lib.INSTANCE.equal(l, r);
    }

}
