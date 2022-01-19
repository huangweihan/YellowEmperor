package org.iwhalecloud.utils;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import net.sf.json.JSONSerializer;
import net.sf.json.xml.XMLSerializer;
import org.apache.commons.lang.StringEscapeUtils;
import org.apache.commons.lang3.StringUtils;
import org.dom4j.*;
import java.util.List;

public class ParseUtil {

	/**
	 * xml转json
	 */
	public static JSONObject parseXmlToJson(String xmlStr) throws DocumentException {
		Document doc = DocumentHelper.parseText(xmlStr);
		JSONObject json = new JSONObject();
		dom4j2Json(doc.getRootElement(), json);
		return json;
	}

	/**
	 * xml转json （具体的报文解析--无数据的节点将不会被展示）
	 */
	public static void dom4j2Json(Element element, JSONObject json) {
		//如果是属性
		for (Object o : element.attributes()) {
			Attribute attr = (Attribute) o;
			if (!StringUtils.isEmpty(attr.getValue())) {
				json.put("@" + attr.getName(), attr.getValue());
			}
		}
		List<Element> chdEl = element.elements();
		//如果没有子元素,只有一个值
		if (chdEl.isEmpty() && !StringUtils.isEmpty(element.getText())) {
			json.put(element.getName(), element.getText());
		}
		//有子元素
		for (Element e : chdEl) {
			//子元素也有子元素
			if (!e.elements().isEmpty()) {
				JSONObject childJson = new JSONObject();
				//反复迭代，寻找子元素
				dom4j2Json(e, childJson);
				Object o = json.get(e.getName());
				if (o != null) {
					JSONArray jsonTemp = null;
					//如果此元素已存在,则转为jsonArray
					if (o instanceof JSONObject) {
						JSONObject jsono = (JSONObject) o;
						json.remove(e.getName());
						jsonTemp = new JSONArray();
						jsonTemp.add(jsono);
						jsonTemp.add(childJson);
					}
					if (o instanceof JSONArray) {
						jsonTemp = (JSONArray) o;
						jsonTemp.add(childJson);
					}
					json.put(e.getName(), jsonTemp);
				} else {
					if (!childJson.isEmpty()) {
						json.put(e.getName(), childJson);
					}
				}
			} else {
				//子元素没有子元素
				for (Object o : element.attributes()) {
					Attribute attr = (Attribute) o;
					if (!StringUtils.isEmpty(attr.getValue())) {
						json.put("@" + attr.getName(), attr.getValue());
					}
				}
				if (!e.getText().isEmpty()) {
					json.put(e.getName(), e.getText());
				}
			}
		}
	}

	public static String xmlRoughParse(String content,String key)throws Exception{
		String result = null;
		content = StringEscapeUtils.unescapeHtml(content);
		content = content.replaceAll("&lt;", "<").replaceAll("&gt;", ">");
		content = content.replaceAll("\\<!\\[CDATA\\[", "").replaceAll("\\]\\]\\>", "");
		content = content.replaceAll("\\<\\?xml version=\"1.0\" encoding=\"UTF-8\"\\?\\>", "");
		content = content.replaceAll("\\<\\?xml version=\"1.0\" encoding=\"GBK\"\\?\\>", "");

		content = content.replaceAll("\\r\\n", "");
		content = content.replaceAll("\\n", "");
		content = content.trim();
		content = content.replaceAll("\\<\\s+", "<");
		content = content.replaceAll("\\</\\s+", "</");
		content = content.replaceAll("\\s+\\>", ">");
		content = content.replaceAll("\\>\\s+", ">");
		content = content.replaceAll("\\s+\\<", "<");
		content = content.replaceAll("\\s+", " ");

		if(key==null || "".equals(key)){
			return content;
		}
		int startIndex = content.indexOf("<"+key+">");
		int endIndex = content.indexOf("</"+key+">");
		if(startIndex < 0 || endIndex <= 0 || endIndex <= startIndex ){
			return result;
		}
		result = content.substring(startIndex+key.length()+2, endIndex);
		return result;
	}
}
