package com.jx;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.util.ArrayList;
import java.util.List;

/**
 * @author jx on 2018/4/12.
 */

class FileUtils {

    /**
     * 从txt文件读取
     */
    public static List<String> readFile(String path) {
        List<String> stringList = new ArrayList<String>();
        FileInputStream fis = null;
        InputStreamReader isr = null;
        BufferedReader br = null;
        try {
            String str = "";
            fis = new FileInputStream(path);
            // 从文件系统中的某个文件中获取字节
            isr = new InputStreamReader(fis);
            br = new BufferedReader(isr);
            while ((str = br.readLine()) != null) {
                stringList.add(str);
            }
        } catch (FileNotFoundException e) {
            System.out.println("找不到指定文件!");
        } catch (IOException e) {
            System.out.println("读取文件失败!");
        } finally {
            try {
                br.close();
                isr.close();
                fis.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return stringList;
    }

    /**
     * 生成.json格式文件
     */
    public static boolean createJsonFile(String jsonString, String fullPath) {
        boolean flag = true;
        try {
            File file = new File(fullPath);
            if (!file.getParentFile().exists()) {
                file.getParentFile().mkdirs();
            }
            if (file.exists()) {
                file.delete();
            }
            file.createNewFile();

            // 格式化json字符串
            jsonString = JsonFormatTool.formatJson(jsonString);
            Writer write = new OutputStreamWriter(new FileOutputStream(file), "UTF-8");
            write.write(jsonString);
            write.flush();
            write.close();
        } catch (Exception e) {
            flag = false;
            e.printStackTrace();
        }
        return flag;
    }
}
