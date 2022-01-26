package org.iwhalecloud;

import com.alibaba.fastjson.JSONObject;
import org.apache.commons.lang3.StringUtils;
import org.dom4j.DocumentHelper;
import org.dom4j.Element;
import org.iwhalecloud.utils.FileUtils;
import org.iwhalecloud.utils.MessageCompareUtils;
import org.iwhalecloud.utils.ParseUtil;

import java.io.File;
import java.io.FileInputStream;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

public class BasicTestDemo {
    public static void main(String[] args) throws Exception {
//        String oldXml = FileUtils.readFile("resource/fk-message.xml");
//        String newXml = FileUtils.readFile("resource/bp-message.xml");
//        Element oldRoot = DocumentHelper.parseText(oldXml).getRootElement();
//        Element newRoot = DocumentHelper.parseText(newXml).getRootElement();
//
////        JSONObject fk = new JSONObject();
////        ParseUtil.dom4j2Json(oldRoot, fk);
////        JSONObject bp = new JSONObject();
////        ParseUtil.dom4j2Json(newRoot, bp);
////
////        List<String> list = new ArrayList<>();
////        MessageCompareUtils.compareForRes(fk, bp, "root",  list);
////        System.out.println(StringUtils.join(list, " "));
//
//        List<String> list = new ArrayList<>();
//        MessageCompareUtils.compareForZd(oldRoot, newRoot, list);
//        System.out.println(StringUtils.join(list, " "));

        Properties properties = new Properties();
        FileInputStream fileInputStream = new FileInputStream(new File("E:\\work\\code\\tool\\oc_parallel_contrast\\src\\main\\resources\\admin.properties"));
        properties.load(fileInputStream);

        for (Map.Entry<Object, Object> entry : properties.entrySet()) {
            System.out.println(entry.getKey());
            System.out.println(entry.getValue());
        }
    }
}

