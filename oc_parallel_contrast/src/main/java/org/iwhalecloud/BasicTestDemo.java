package org.iwhalecloud;

import org.apache.commons.lang3.StringUtils;
import org.dom4j.Document;
import org.dom4j.DocumentHelper;
import org.dom4j.Element;
import org.iwhalecloud.utils.FileUtils;
import org.iwhalecloud.utils.MessageCompareUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.context.annotation.Bean;

import java.util.ArrayList;
import java.util.List;

public class BasicTestDemo {
    public static void main(String[] args) throws Exception {
        String content = FileUtils.readFile("in/oldXml.xml");
        String content2 = FileUtils.readFile("in/newXml.xml");
        Document document = DocumentHelper.parseText(content);
        Element rootElement = document.getRootElement();

        Document document2 = DocumentHelper.parseText(content2);
        Element rootElement2 = document2.getRootElement();
        List<String> list = new ArrayList<>();
        MessageCompareUtils.compare(rootElement, rootElement2, list);

        System.out.println(StringUtils.join(list, "\n"));
    }
}

