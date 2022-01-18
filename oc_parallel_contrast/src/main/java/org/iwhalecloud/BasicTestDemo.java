package org.iwhalecloud;

import com.alibaba.fastjson.JSONObject;
import org.dom4j.DocumentHelper;
import org.iwhalecloud.utils.*;

public class BasicTestDemo {
    public static void main(String[] args) throws Exception {
        JSONObject bp = new JSONObject();
        JSONUtils.dom4j2Json(DocumentHelper.parseText(FileUtils.readFile("in/newXml.xml")).getRootElement(), bp);

        JSONObject fk = new JSONObject();
        JSONUtils.dom4j2Json(DocumentHelper.parseText(FileUtils.readFile("in/oldXml.xml")).getRootElement(), fk);
        MessageCompareUtils.compare(fk, bp, "root");
    }


}
