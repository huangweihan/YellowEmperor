package org.iwhalecloud.utils;

import org.apache.commons.lang.StringEscapeUtils;

public class ParseUtil {

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

	public static String xmlReplaceKey(String content,String orgKey,String tarKey) throws Exception{
		content = content.replaceAll("\\<"+orgKey+"\\>", "<"+tarKey+">");
		content = content.replaceAll("\\</"+orgKey+"\\>", "</"+tarKey+">");
		return content;
	}

	public static String xmlReplaceE(String content) throws Exception{
		content = content.replaceAll("\\<e\\>", "");
		content = content.replaceAll("\\</e\\>", "");
		return content;
	}

	public static String xmlRoughParseEx(String content, String key) throws Exception {
		StringBuffer result = new StringBuffer("<" + key + ">");
		String xmlPough = xmlRoughParse(content, key);
		result = result.append(xmlPough + "</" + key + ">");
		String afterParse = new String(result);
		return afterParse;
	}
}
