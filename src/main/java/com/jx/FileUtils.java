package com.jx;

import java.io.File;
import java.io.IOException;

/**
 * @author jx on 2018/4/12.
 */

public class FileUtils {

    public static String getProjectDir(){
        File directory = new File("");// 参数为空
        String courseFile = null;
        try {
            courseFile = directory.getCanonicalPath();
        } catch (IOException e) {
            e.printStackTrace();
        }
        System.out.println(courseFile);
        return courseFile;
    }


}
