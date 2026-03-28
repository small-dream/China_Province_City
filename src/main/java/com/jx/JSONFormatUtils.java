package com.jx;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import java.io.FileWriter;
import java.io.IOException;

/**
 * @author jx on 2018/10/29.
 */

public class JSONFormatUtils {

    public static <T> void jsonWriter(T data, String filePath) {
        Gson gson = new GsonBuilder().setPrettyPrinting().create();
        try (FileWriter writer = new FileWriter(filePath)) {
            gson.toJson(data, writer);
        } catch (IOException e) {
            throw new RuntimeException("写入 JSON 文件失败: " + filePath, e);
        }
    }
}
