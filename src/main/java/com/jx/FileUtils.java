package com.jx;

import java.io.File;
import java.io.IOException;

public class FileUtils {

    public static String getProjectDir() {
        try {
            return new File("").getCanonicalPath();
        } catch (IOException e) {
            throw new RuntimeException("无法获取项目目录", e);
        }
    }
}
