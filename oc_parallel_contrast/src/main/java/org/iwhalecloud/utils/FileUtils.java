package org.iwhalecloud.utils;

import org.apache.commons.io.IOUtils;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.net.URL;

public class FileUtils {
    private FileUtils(){}

    public static String readFile(String filePath){
        URL url = Thread.currentThread().getContextClassLoader().getResource("");
        try {
            String path = url.getPath();
            return IOUtils.toString(new FileReader(path + "/" + filePath));
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }
}
