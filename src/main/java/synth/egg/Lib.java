package synth.egg;

import com.sun.jna.Library;
import com.sun.jna.Native;

public interface Lib extends Library {
    Lib INSTANCE = Native.load("egg_synth", Lib.class);
    String simplify(String expr);

    boolean equal(String l, String r);
}
