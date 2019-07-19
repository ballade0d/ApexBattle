package xyz.hstudio.apexbattle.util;

import lombok.Getter;
import xyz.hstudio.apexbattle.nms.INMS;
import xyz.hstudio.apexbattle.nms.v1_12_R1;
import xyz.hstudio.apexbattle.nms.v1_13_R2;
import xyz.hstudio.apexbattle.nms.v1_14_R1;

public class NmsUtil {

    @Getter
    private static INMS instance;

    public static void init() {
        switch (VersionUtil.getVersion()) {
            case v1_12_R1:
                instance = new v1_12_R1();
                return;
            case v1_13_R2:
                instance = new v1_13_R2();
                return;
            case v1_14_R1:
                instance = new v1_14_R1();
                return;
            default:
                instance = null;
        }
    }
}