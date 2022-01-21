package org.iwhalecloud.utils;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import lombok.val;
import org.apache.commons.collections.MapUtils;
import org.apache.commons.lang3.StringUtils;
import org.dom4j.Attribute;
import org.dom4j.Element;
import org.dom4j.Node;
import org.springframework.util.Assert;
import org.springframework.util.ObjectUtils;

import java.util.*;
import java.util.List;
import java.util.stream.Collectors;

public class MessageCompareUtils {

    private MessageCompareUtils() {
    }

    private static final Map<String, String> ATTR_MAP;

    static {
        ATTR_MAP = new HashMap<>();
        ATTR_MAP.put("itemSpecId", "5");
        ATTR_MAP.put("desc", "4");
        ATTR_MAP.put("seq", "3");
        ATTR_MAP.put("newItemVal", "2");
        ATTR_MAP.put("oldItemVal", "1");
    }

    /**
     * 综调 - 报文比较
     * 报文比较结构(节点)一致，顺序一致
     */
    public static void compareForZd(Element oldNode, Element newNode, List<String> list) {
        // 当前节点的名称、文本内容和属性
        String curOldNodeValue = oldNode.getTextTrim();
        String curNewNodeValue = newNode.getTextTrim();

        // 判断属性值
        if (!curOldNodeValue.equals(curNewNodeValue)) {
            list.add(assemblyDiffForZdMadeInChina(oldNode, newNode, "报文存在差异(属性)"));
        }

        // 判断属性值
        List<Attribute> oldAttributes = oldNode.attributes();
        for (Attribute oldAttribute : oldAttributes) {
            String name = oldAttribute.getName();
            String oldValue = getAttributeVal(oldAttribute);
            Attribute newAttribute = newNode.attribute(name);
            String newValue = getAttributeVal(newAttribute);
            if (!ObjectUtils.nullSafeEquals(oldValue, newValue)) {
                list.add(assemblyDiffForZdMadeInChina(oldNode, newNode, "报文存在差异(标签)"));
                // 一个标签内的属性比较出现问题，直接把整个标签打印出来，并且接下来的属性就不用做比较了。
                break;
            }
        }

        // 递归遍历当前节点所有的子节点 -> 一级
        List<Element> oldElements = oldNode.elements();
        List<Element> newElements = newNode.elements();

        for (int i = 0; i < oldElements.size(); i++) {
            Element newEle = null;
            Element oldEle  = oldElements.get(i);;

            if (oldElements.size() == newElements.size()) {
                // 当两报文的size相等时，按照报文格式结构以及顺序一致比较
                newEle = newElements.get(i);
                compareForZd(oldEle, newEle, list);
                continue;
            }
            newEle = lookNodeByCount(oldEle, newNode.elements());
            if (newEle == null) {
                // 对应的服开报文不存在该节点
                list.add(assemblyDiffForZdMadeInChina(oldEle, null, "对应的编排报文不存在当前节点"));
                continue;
            }
            compareForZd(oldEle, newEle, list);
        }
    }

    private static Element lookNodeByCount(Element oldE, List<Element> elements){
        int max = (1 + oldE.attributes().size()) / 2;
        int count = 0;
        Element candidate = null;
        for (Element bpE : elements) {
            count = valueHitExplore(oldE, bpE);
            if (max < count) {
                candidate = bpE;
                max = count;
            }
        }
        return candidate;
    }

    private static int valueHitExplore(Element fk, Element bp){
        int count = 0;
        String fkName = fk.getName();
        String bpName = bp.getName();
        if (!fkName.equals(bpName)) {
            return -1;
        }
        String fkText = fk.getText();
        String bpText = bp.getText();
        if (fkText.equals(bpText)) {
            count++;
        }
        List<Attribute> fkAttributes = fk.attributes();
        for (Attribute fkAttribute : fkAttributes) {
            String fkAttributeName = fkAttribute.getName();
            String fkAttributeValue = fkAttribute.getValue();

            Attribute attribute = bp.attribute(fkAttributeName);
            if (attribute != null && StringUtils.equals(fkAttributeValue, attribute.getValue())) {
                count++;
            }
        }
        return count;
    }

    /**
     * 资源 - 报文比较
     * 报文比较结构(节点)不一致，顺序不一致
     */
    public static void compareForRes(JSONObject oldObj, JSONObject newObj, String path, List<String> list) {
        Set<Map.Entry<String, Object>> oldEntrySet = oldObj.entrySet();
        for (Map.Entry<String, Object> entry : oldEntrySet) {
            String key = entry.getKey();
            Object value = entry.getValue();

            if (value instanceof String) {
                String oldValue = (String) value;
                String newValue = newObj.getString(key);
                if (!ObjectUtils.nullSafeEquals(oldValue, newValue)) {
                    list.add(assemblyDiffForResMadeInChina(oldValue, newValue, key, path));
                }
            } else if (value instanceof JSONObject) {
                JSONObject oldJsonObject = (JSONObject) value;
                JSONObject newJsonObject = newObj.getJSONObject(key);
                compareForRes(oldJsonObject, newJsonObject, path + "." + key, list);
            } else if (value instanceof JSONArray) {
                JSONArray oldJsonArray = (JSONArray) value;
                for (Object old : oldJsonArray) {
                    JSONObject oldJsonObject = (JSONObject) old;
                    JSONArray newJsonArray = newObj.getJSONArray(key);
                    List<JSONObject> newJsonObjectList = getJsonObject(newJsonArray, oldJsonObject);
                    if (newJsonObjectList.isEmpty()) {
                        // 说明旧报文中的 JsonObject 在新报文中找不到
                        list.add(assemblyDiffForResMadeInChina(oldJsonObject, null, key, path));
                    } else if (newJsonObjectList.size() > 1) {
                        // 说明新报文中出现了重复的节点
                        list.add(assemblyDiffForResMadeInChina(oldJsonObject, newJsonObjectList, key, path));
                    } else if (!compareJsonObject(oldJsonObject, newJsonObjectList.get(0))) {
                        // 比较 compareJsonObject：true -> 匹配成功  false -> 匹配失败
                        list.add(assemblyDiffForResMadeInChina(oldJsonObject, newJsonObjectList.get(0), key, path));
                    }
                }
            } else {
                System.out.println(value);
                throw new RuntimeException("异常类型未被解析");
            }
        }
    }

    private static boolean compareJsonObject(JSONObject oldObj, JSONObject newObj) {
        Set<Map.Entry<String, Object>> oldEntrySet = oldObj.entrySet();

        for (Map.Entry<String, Object> entry : oldEntrySet) {
            String key = entry.getKey();
            if ("seq".equals(key)) {
                continue;
            }
            Object oldValue = entry.getValue();
            if (!ObjectUtils.nullSafeEquals(oldValue, newObj.getString(key))) {
                return false;
            }
        }
        return true;
    }

    private static List<JSONObject> getJsonObject(JSONArray jsonArray, JSONObject jsonObject) {
        List<JSONObject> jsonObjects = new ArrayList<>();
        for (Object obj : jsonArray) {
            if (findBpJsonObject((JSONObject) obj, jsonObject)) {
                jsonObjects.add((JSONObject) obj);
            }
        }
        return jsonObjects;
    }

    // 前提 itemSpecId 在新和旧报文中保持一致
    private static boolean findBpJsonObject(JSONObject newJsonObject, JSONObject oldJsonObject) {
        String newItemSpecId = newJsonObject.getString("itemSpecId");
        String oldItemSpecId = oldJsonObject.getString("itemSpecId");
        return ObjectUtils.nullSafeEquals(newItemSpecId, oldItemSpecId);
    }

    public static String assemblyDiffForZdMadeInChina(Element fk, Element bp, String title) {
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append(title).append("\n");
        stringBuilder.append("服开报文\n").append(madeInChina(fk)).append("\n");
        if (bp != null) {
            stringBuilder.append("\n编排报文\n").append(madeInChina(bp)).append("\n");
        }
        String path = fk.getPath();
        if (path.startsWith("/")) {
            path = path.substring(1);
        }
        path = path.replaceAll("/", ".");
        stringBuilder.append("\n节点路径：").append(path);
        stringBuilder.append("\n==========================\n");
        return stringBuilder.toString();
    }

    private static String madeInChina(Element element) {
        StringBuilder stringBuilder = new StringBuilder();
        List<Attribute> attributes = element.attributes();
        String key = element.getName();
        String value = element.getTextTrim();
        stringBuilder.append("<").append(key);
        stringBuilder.append(StringUtils.join(attributes.stream().map(Node::asXML).collect(Collectors.toList()), ""));
        if (StringUtils.isEmpty(value)) {
            stringBuilder.append("/>");
        } else {
            stringBuilder.append(">").append(value).append("<").append(key).append("/>");
        }
        return stringBuilder.toString();
    }

    public static String assemblyDiffForResMadeInChina(Object fkObj, Object bpObj, String key, String path) {
        StringBuilder stringBuilder = new StringBuilder();
        // 错误类型
        // (1)节点比较差异 (2)节点重复 (3)节点丢失
        stringBuilder.append("服开报文\n");
        if (fkObj instanceof String) {
            stringBuilder.append("<").append(key).append(">").append(fkObj).append("</").append(key).append(">").append("\n");
        } else {
            JSONObject fk = (JSONObject) fkObj;
            stringBuilder.append(displayJsonAsXml(fk, "orderItem"));
        }
        String title = "- 报文节点差异";
        if (bpObj == null) {
            title = "- 报文节点缺失";
        }
        if (bpObj instanceof String) {
            stringBuilder.append("\n编排报文\n");
            stringBuilder.append("<").append(key).append(">").append(bpObj).append("</").append(key).append(">").append("\n");
        }
        if (bpObj instanceof List) {
            title = "- 报文节点重复";
            List<JSONObject> jsonObjectList = (List<JSONObject>) bpObj;
            stringBuilder.append("\n编排报文\n");
            for (JSONObject jsonObject : jsonObjectList) {
                stringBuilder.append(displayJsonAsXml(jsonObject, "orderItem"));
            }
        }
        if (bpObj instanceof JSONObject) {
            stringBuilder.append("\n编排报文\n");
            JSONObject jsonObject = (JSONObject) bpObj;
            stringBuilder.append(displayJsonAsXml(jsonObject, "orderItem"));
        }
        stringBuilder.insert(0, title + "\n");
        stringBuilder.append("\n节点路径：").append(path).append(".").append(key);
        stringBuilder.append("\n==========================\n");
        return stringBuilder.toString();
    }

    private static String displayJsonAsXml(JSONObject jsonObject, String parentTag) {
        Assert.notNull(jsonObject, "待转化的JSONObject不能为空");
        Assert.notNull(parentTag, "parentTag不能为空");
        Set<Map.Entry<String, Object>> set = jsonObject.entrySet();
        List<Map.Entry<String, Object>> sortResult = set.stream().sorted((o1, o2) -> {
            String k1 = o1.getKey();
            String k2 = o2.getKey();
            return StringUtils.compare(MapUtils.getString(ATTR_MAP, k2, "0"),
                    MapUtils.getString(ATTR_MAP, k1, "0"));
        }).collect(Collectors.toList());

        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append("<").append(parentTag).append(">\n");

        for (Map.Entry<String, Object> entry : sortResult) {
            String key = entry.getKey();
            Object value = entry.getValue();
            stringBuilder.append("\t").append("<").append(key).append(">")
                    .append(value)
                    .append("<").append(key).append("/>\n");
        }
        stringBuilder.append("</").append(parentTag).append("/>\n");
        return stringBuilder.toString();
    }

    private static String getAttributeVal(Attribute attribute){
        if (attribute == null) {
            return "";
        }
        String value = attribute.getValue();
        if (StringUtils.isEmpty(value)) {
            return value;
        }
        return value.trim();
    }

}