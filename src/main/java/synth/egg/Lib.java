package synth.egg;

import com.sun.jna.Library;
import com.sun.jna.Native;

public interface Lib extends Library {
    Lib INSTANCE = Native.load("egg_synth", Lib.class);
    int add(int left, int right);
    void hello();

    String simplify(String expr);
}
