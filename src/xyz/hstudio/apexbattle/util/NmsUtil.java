package xyz.hstudio.apexbattle.util;

import lombok.Getter;
import xyz.hstudio.apexbattle.nms.*;

public class NmsUtil {

    @Getter
    private static INMS instance;

    public static void init() {
        switch (VersionUtil.getVersion()) {
            case v1_8_R3:
                instance = new v1_8_R3();
                return;
            case v1_9_R2:
                instance = new v1_9_R2();
                return;
            case v1_10_R1:
                instance = new v1_10_R1();
                return;
            case v1_11_R1:
                instance = new v1_11_R1();
                return;
            case v1_12_R1:
                instance = new v1_12_R1();
                return;
            default:
                instance = null;
        }
    }
}