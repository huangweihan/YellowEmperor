package org.iwhalecloud;

import com.alibaba.fastjson.JSONObject;
import org.apache.commons.lang3.StringUtils;
import org.dom4j.DocumentHelper;
import org.dom4j.Element;
import org.iwhalecloud.utils.FileUtils;
import org.iwhalecloud.utils.MessageCompareUtils;
import org.iwhalecloud.utils.ParseUtil;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

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

        LocalDateTime localDateTime = LocalDateTime.parse("2022-01-21T16:36:52");
        String format = localDateTime.format(DateTimeFormatter.ofPattern("yyyy-MM-dd hh:mm:ss"));
        System.out.println(format);

    }}

