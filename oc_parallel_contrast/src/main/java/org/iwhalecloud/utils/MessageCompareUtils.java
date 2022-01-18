package org.iwhalecloud.utils;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import org.apache.commons.collections.MapUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.util.Assert;
import org.springframework.util.ObjectUtils;

import java.util.*;
import java.util.stream.Collectors;

public class MessageCompareUtils {

    private MessageCompareUtils(){}

    private static final Map<String, String> attrMap;

    static {
        attrMap = new HashMap<>();
        attrMap.put("itemSpecId", "5");
        attrMap.put("desc", "4");
        attrMap.put("seq", "3");
        attrMap.put("newItemVal", "2");
        attrMap.put("oldItemVal", "1");
    }

    public static void compare(JSONObject oldObj, JSONObject newObj, String path) {
        Set<Map.Entry<String, Object>> oldEntrySet = oldObj.entrySet();

        for (Map.Entry<String, Object> entry : oldEntrySet) {
            String key = entry.getKey();
            Object value = entry.getValue();

            if (value instanceof String) {
                String oldValue = (String) value;
                String newValue = newObj.getString(key);
                if (!ObjectUtils.nullSafeEquals(oldValue, newValue)) {
                    System.out.printf("旧报文\n%s\n新报文\n%s\n节点路径 [%s]\n", displayJsonAsXml(oldValue, key),
                            displayJsonAsXml(newValue, key), (path + "." + key));
                    System.out.println("-----------------------------------------------------");
                }
            } else if (value instanceof JSONObject) {
                JSONObject oldJsonObject = (JSONObject) value;
                JSONObject newJsonObject = newObj.getJSONObject(key);
                compare(oldJsonObject, newJsonObject, path + "." + key);
            } else if (value instanceof JSONArray) {
                JSONArray oldJsonArray = (JSONArray) value;
                for (Object old : oldJsonArray) {
                    JSONObject oldJsonObject = (JSONObject) old;
                    JSONArray newJsonArray = newObj.getJSONArray(key);
                    List<JSONObject> newJsonObjectList = getJsonObject(newJsonArray, oldJsonObject);
                    if (newJsonObjectList.isEmpty()) {
                        // 说明旧报文中的 JsonObject 在新报文中找不到
                        System.out.printf("新报文中缺失节点：\n %s 节点路径：%s\n", displayJsonAsXml(oldJsonObject, key), (path + "." + key));
                        System.out.println("-----------------------------------------------------");
                    } else if (newJsonObjectList.size() > 1) {
                        // 说明新报文中出现了重复的节点
                        System.out.printf("新报文节点重复：旧报文\n %s\n新报文\n %s\n节点路径 %s\n", displayJsonAsXml(oldJsonObject, key),
                                displayJsonAsXml(newJsonObjectList, key), (path + "." + key));
                        System.out.println("-----------------------------------------------------");
                    } else if (!compareJsonObject(oldJsonObject, newJsonObjectList.get(0))) {
                        // 比较 compareJsonObject：true -> 匹配成功  false -> 匹配失败
                        System.out.printf("旧报文 \n%s\n新报文\n%s\n节点路径 %s\n", displayJsonAsXml(oldJsonObject, key),
                                displayJsonAsXml(newJsonObjectList.get(0), key), (path + "." + key));
                        System.out.println("-----------------------------------------------------");
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
            if ("seq".equals(key)) {continue;}
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
        return ObjectUtils.nullSafeEquals(newItemSpecId, oldItemSpecId) ;
    }

    private static String displayJsonAsXml(Object value, String tag){
        Assert.notNull(value, "待转化的 value 不能为空");
        Assert.notNull(tag, "tag 不能为空");
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append("<").append(tag).append(">").append(value).append("</").append(tag).append(">");
        return stringBuilder.toString();
    }

    private static String displayJsonAsXml(JSONObject jsonObject, String parentTag) {
        Assert.notNull(jsonObject, "待转化的JSONObject不能为空");
        Assert.notNull(parentTag, "parentTag不能为空");
        Set<Map.Entry<String, Object>> set = jsonObject.entrySet();
        List<Map.Entry<String, Object>> sortResult = set.stream().sorted((o1, o2) -> {
            String k1 = o1.getKey();
            String k2 = o2.getKey();
            return StringUtils.compare(MapUtils.getString(attrMap, k1, "0"),
                    MapUtils.getString(attrMap, k2, "0"));
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

    private static String displayJsonAsXml(List<JSONObject> objectList, String parentTag) {
        Assert.notNull(objectList, "待转化的objectList不能为空");
        Assert.notNull(parentTag, "parentTag不能为空");
        StringBuilder stringBuilder = new StringBuilder();
        for (JSONObject jsonObject : objectList) {
            stringBuilder.append(displayJsonAsXml(jsonObject, parentTag)).append("\n");
        }
        return stringBuilder.toString();
    }

}