package org.iwhalecloud;

import com.alibaba.fastjson.JSONObject;
import org.dom4j.Document;
import org.dom4j.DocumentException;
import org.dom4j.DocumentHelper;
import org.dom4j.Element;
import org.iwhalecloud.utils.FileUtils;
import org.iwhalecloud.utils.JSONUtils;
import org.iwhalecloud.utils.ParseUtil;

public class BasicTestDemo {
    public static void main(String[] args) throws Exception {
        String bpMessage = FileUtils.readFile("resource/fk-message.xml");

        Element rootElement = DocumentHelper.parseText(bpMessage).getRootElement();
        Element subElement = rootElement.element("Body").elements().get(0).element("body");
        System.out.println(subElement.getTextTrim());
        Document document = DocumentHelper.parseText(subElement.getTextTrim());
        System.out.println(document.getRootElement().element("baseInfo").element("areaId").getText());
    }


}
