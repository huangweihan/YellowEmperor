package org.iwhalecloud;

import com.alibaba.fastjson.JSONObject;
import org.apache.commons.lang3.StringUtils;
import org.dom4j.DocumentHelper;
import org.iwhalecloud.utils.*;

import java.util.ArrayList;
import java.util.List;

public class BasicTestDemo {
    public static void main(String[] args) throws Exception {
        JSONObject bp = ParseUtil.parseXmlToJson(FileUtils.readFile("in/newXml.xml"));
        JSONObject fk = ParseUtil.parseXmlToJson(FileUtils.readFile("in/oldXml.xml"));

//        JSONObject fk = new JSONObject();
//        JSONUtils.dom4j2Json(DocumentHelper.parseText(FileUtils.readFile("in/oldXml.xml")).getRootElement(), fk);

        List<String> list = new ArrayList<>();
        MessageCompareUtils.compare(fk, bp, "root", list);
        System.out.println(StringUtils.join(list, ""));
    }


}
