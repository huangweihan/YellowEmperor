package org.iwhalecloud.utils;

import org.apache.commons.io.IOUtils;

import java.io.FileReader;
import java.net.URL;

@SuppressWarnings(value = "all")
public class FileUtils {

    private FileUtils(){}

    public static String readFile(String filePath){
        try {
            URL url = Thread.currentThread().getContextClassLoader().getResource("");
            String path = url.getPath();
            return IOUtils.toString(new FileReader(path + "/" + filePath));
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }
}
