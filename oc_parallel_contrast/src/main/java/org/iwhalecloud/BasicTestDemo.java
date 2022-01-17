package org.iwhalecloud;

import com.alibaba.fastjson.JSONObject;
import org.apache.commons.io.IOUtils;
import org.dom4j.Document;
import org.dom4j.DocumentException;
import org.dom4j.DocumentHelper;
import org.dom4j.Element;
import org.iwhalecloud.utils.FileUtils;
import org.iwhalecloud.utils.JSONUtils;
import org.iwhalecloud.utils.MessageCompareUtils;
import org.iwhalecloud.utils.ParseUtil;

public class BasicTestDemo {
    public static void main(String[] args) throws Exception {
        String bpMessage = FileUtils.readFile("in/oldXml.xml");
        System.out.println(addSoap("999999999999", bpMessage));
    }

    private static String addSoap(String fkBackOrderMessage, String bpMessage) {
        // todo 暂时使用编排报文 soap 头
        try {
            Element rootElement = DocumentHelper.parseText(bpMessage).getRootElement();
            Element element = rootElement.element("Body").elements().get(0);
            Element body = element.element("body");

            body.addCDATA(fkBackOrderMessage);
            System.out.println();
        } catch (DocumentException e) {
            e.printStackTrace();
        }
        return null;
    }

}
