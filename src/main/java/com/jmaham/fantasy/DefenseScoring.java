package com.jmaham.fantasy;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

public class DefenseScoring {
    public static final double ptsPerSack = 1.0;
    public static final double ptsPerInt = 2.0;
    public static final double ptsPerFumbleRecovery = 2.0;
    public static final double ptsPerTd = 6.0;
    public static final double ptsPerSafety = 2.0;
    public static final Map<String, Double> ptsAgainst;
    static {
        ptsAgainst = Map.of("0", 10.0, "1To6", 7.0, "7To13", 4.0, "14To20", 1.0, "21To27", 0.0, "28To34", -1.0, "35+", -4.0);
    }
}
