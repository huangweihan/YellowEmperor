package org.iwhalecloud.task;

import com.alibaba.fastjson.JSONObject;
import org.apache.commons.collections.MapUtils;
import org.dom4j.Document;
import org.dom4j.DocumentException;
import org.dom4j.DocumentHelper;
import org.dom4j.Element;
import org.iwhalecloud.config.InterfaceConfig;
import org.iwhalecloud.constant.FkMessageConstant;
import org.iwhalecloud.constant.InterfaceNameContent;
import org.iwhalecloud.dao.impl.MessageDaoImpl;
import org.iwhalecloud.utils.FileUtils;
import org.iwhalecloud.utils.HttpUtils;
import org.iwhalecloud.utils.JSONUtils;
import org.iwhalecloud.utils.MessageCompareUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.util.Assert;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Component
@EnableAsync
@EnableScheduling
public class MessageTask {

    private static final Logger logger = LoggerFactory.getLogger(MessageTask.class);

    // 编排查询已派发未回单
    private static final String BP_OC_API_INST_SQL = "SELECT * FROM oc_api_inst WHERE state = '10D';";

    // 根据编排的环节id转成成服开对应的环节id。存在一对多映射关系，返回多个去查询只会匹配到一个报文
    private static final String GET_FK_TACHE_ID_SQL = "SELECT fk_tache_id FROM gw_tache_map WHERE bp_tache_id = ?;";

    @Autowired
    @Qualifier("bpJdbcTemplate")
    private JdbcTemplate bpJdbcTemplate;

    @Autowired
    private MessageDaoImpl messageDao;

    @Autowired
    private InterfaceConfig interfaceConfig;

    // 退单、特殊流程、跨pon、小微 -> 特殊编排流程
    // 资源走资源、综调走综调
    @Async
    @Scheduled(cron = "0/10 * * * * ?")
    public void endAlarmMessage() throws Exception {
        logger.info(" =========== 定时任务开启 =========== ");
        // 查询编排 派单未回单 的记录
        // todo 字段过多，等确定用哪些字段要修改SQL
//        List<Map<String, Object>> ocApiInstList = bpJdbcTemplate.queryForList(BP_OC_API_INST_SQL);
//        if (ocApiInstList.isEmpty()) {
//            return;
//        }
//        // 判断当前单是回单还是退单，退单直接回单，不进行报文比较；回单逻辑继续
//        for (Map<String, Object> ocApiInstMap : ocApiInstList) {
//            execute(ocApiInstMap);
//        }

        Map<String, Object> map = new HashMap<>();
        map.put("MSG_CODE", "600003635901#8712110281421284084#600006007408");
        map.put("OC_INST_ID", "8888888888888");

        execute(map);
    }

    private void execute(Map<String, Object> ocApiInstMap) throws Exception {
        // todo 获取编排报文
        String bpMessage = FileUtils.readFile("/resource/fk-message.xml");
        // 根据编排查询到的数据(编排报文) 取出环节id
        String bpTacheId = MapUtils.getString(ocApiInstMap, "TACHE_ID");
        // 获取订单编码
        String msgCode = MapUtils.getString(ocApiInstMap, "MSG_CODE", "");
        String[] fields = msgCode.split("#");
        String orderCode = fields[1];
        // 通过 编排环节id 查询配置表 转换得到 服开环节id
        // -- List<Map<String, Object>> tacheMapList = bpJdbcTemplate.queryForList(GET_FK_TACHE_ID_SQL, bpTacheId);
        List<Map<String, Object>> tacheMapList = new ArrayList<>();
        Map<String, Object> tmp = new HashMap<>();
        tmp.put("fk_tache_id", "4845");
        tacheMapList.add(tmp);
        // todo 判断当前是资源请求还是综调请求
        Map<String, Object> sendOrderMap = new HashMap<>();
        Map<String, Object> backOrderMap = new HashMap<>();
        for (Map<String, Object> tacheMap : tacheMapList) {
            String fkTacheId = MapUtils.getString(tacheMap, "fk_tache_id");
            // todo 根据转换之后的编码得到服开报文
            Map<String, String> sourceMap = judgeRequestSource(bpMessage);
            String type = MapUtils.getString(sourceMap, "type");

            orderCode = "8701908121613540790"; // 测试
            if ("res".equals(type)) {
                sendOrderMap = messageDao.queryResSendOrderMessage(orderCode, fkTacheId);
                backOrderMap = messageDao.queryResBackOrderMessage(orderCode, fkTacheId);
            } else if ("zd".equals(type)) {
                sendOrderMap = messageDao.queryZdSendOrderMessage(orderCode, fkTacheId);
                backOrderMap = messageDao.queryZdBackOrderMessage(orderCode, fkTacheId);
            }

            if (!sendOrderMap.isEmpty()) {
                break;
            }
        }
        if (sendOrderMap.isEmpty() || backOrderMap.isEmpty()) {
            throw new RuntimeException();
        }
        // 服开回单报文
        String fkBackOrderMessage = new String((byte[]) backOrderMap.get( "OUT_XML"), "GBK");
        // 服开派单报文
        String fkSendOrderMessage = new String((byte[]) sendOrderMap.get( "IOM_XML"), "GBK");
        if (!StringUtils.hasText(fkBackOrderMessage)) {
            List<String> tacheIdList = tacheMapList.stream().map(m -> MapUtils.getString(m, "fk_tache_id")).collect(Collectors.toList());
            logger.info("根据 fk_tache_id : [{}] 获取服开报文失败！", tacheIdList);
            throw new RuntimeException("根据 fk_tache_id : [" + tacheIdList+ "] 获取服开报文失败！");
        }
        // 服开报文进行 root.baseInfo.workOrderId 转换，转为编排这边的 workOrderId
        // 获取编排的 workOrderId 取表 oc_api_inst 中的 OC_INST_ID
        String bpWorkOrderId = MapUtils.getString(ocApiInstMap, "OC_INST_ID");
        // 替换的是服开派单报文
        fkBackOrderMessage = replaceMessage(fkSendOrderMessage, bpWorkOrderId);

        // 根据服开请求头并获取请求路径
        // String receiptUrl = getReceiptUrl(fkBackOrderMessage);
        // 发起回单
        // String result = HttpUtils.callWebService(fkBackOrderMessage, receiptUrl);
        // logger.info("回单结果： [{}]", result);

        // 报文比较 todo  XXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX看着办
        Element rootElement = DocumentHelper.parseText(bpMessage).getRootElement();
        Element subElement = rootElement.element("Body").elements().get(0).element("body");
        JSONObject bp = new JSONObject();
        JSONUtils.dom4j2Json(DocumentHelper.parseText(subElement.getText()).getRootElement(), bp);

        JSONObject fk = new JSONObject();
        JSONUtils.dom4j2Json(DocumentHelper.parseText(fkSendOrderMessage).getRootElement(), fk);
        MessageCompareUtils.compare(bp, fk, "root");
    }

    // 获取回单访问路径
    @Deprecated
    private String getReceiptUrl(String fkMessage) throws DocumentException {
        Document document = DocumentHelper.parseText(fkMessage);
        Element rootElement = document.getRootElement();
        String infType = rootElement.element("Body").elements().get(0).element("infType").getText();
        if ("resBackOrderRequest".equals(infType) || "resSurveyOrWaitRequest".equals(infType)) {
            logger.info("=========资源待装/退单接口==========");
            return interfaceConfig.getResToBeInstalledServiceUrl();
        } else if ("resAssignResponse".equals(infType)) {
            logger.info("=========资源配置反馈==========");
            return interfaceConfig.getResourceWebServiceUrl();
        } else if ("ResConfirmRet".equals(infType) || "IOMFORWFM".equals(infType)) {
            logger.info("=========外线资源确认/综调回单==========");
            return interfaceConfig.getIntegratedSchedulWebServiceUrl();
        } else {
            logger.error("未知 infType [{}] 请检查！", infType);
            throw new RuntimeException("infType = " + infType + "无法被识别处理");
        }
    }

    // 获取服开报文
    private Map<String, Object> getFkMessage(List<Map<String, Object>> orderList){
        String fkMessage = "";
        Map<String, Object> hitMap = new HashMap<>();
        for (Map<String, Object> orderMap : orderList) {
            // todo 根据转换之后的编码得到服开报文
            String fkTacheId = MapUtils.getString(orderMap, "fk_tache_id");
            fkMessage = "";
            if (StringUtils.hasText(fkMessage)) {
                // todo 从服开获取的报文要判断订单状态
                String state = MapUtils.getString(orderMap, "state");
                if (StringUtils.hasText(state) && ("10N".equals(state) || "10D".equals(state) || "10R".equals(state))) {
                    // todo 正常情况回单处理
                    hitMap = orderMap;
                } else if ("10L".equals(state) || "10DW".equals(state)) {
                    // todo 锁单，暂时先不考虑
                    logger.error("订单状态为10L，暂不考虑");
                } else {
                    logger.error("未知订单状态 [{}]", state);
                    throw new RuntimeException("未知订单状态 [" + state + "]");
                }
                break;
            }
        }
        return hitMap;
    }

    // 替换派单服开报文中的 workOrderId
    private String replaceMessage(String soapXml, String workOrderId) throws DocumentException {
        Document document = DocumentHelper.parseText(soapXml);
        Element rootElement = document.getRootElement();
        rootElement.element("baseInfo").element("workOrderId").setText(workOrderId);
        return document.getRootElement().asXML();
    }



    // 资源请求 综调请求
    // map -> 通过报文 得到url 以及 请求来源 资源 | 综调
    private Map<String, String> judgeRequestSource(String message) throws DocumentException {
        Assert.hasText(message, "用于判断请求来源的报文不能为空");
        Document document = DocumentHelper.parseText(message);
        Element rootElement = document.getRootElement();
        String infType = rootElement.element("Body").elements().get(0).element("infType").getText();
        Map<String, String> resultMap = new HashMap<>();
        if ("resBackOrderRequest".equals(infType) || "resSurveyOrWaitRequest".equals(infType)) {
            // 资源待装/退单接口
            resultMap.put("url", interfaceConfig.getResToBeInstalledServiceUrl());
            resultMap.put("infType", InterfaceNameContent.RES_BACK_OR_WAIT_ORDER_REQUEST);
            resultMap.put("type", "res");
        } else if ("resAssignResponse".equals(infType)) {
            // 资源配置反馈
            resultMap.put("url", interfaceConfig.getResourceWebServiceUrl());
            resultMap.put("infType", InterfaceNameContent.RES_ASSIGN_RESPONSE);
            resultMap.put("type", "res");
        } else if ("ResConfirmRet".equals(infType) || "IOMFORWFM".equals(infType)) {
            // 外线资源确认/综调回单
            resultMap.put("url", interfaceConfig.getIntegratedSchedulWebServiceUrl());
            resultMap.put("infType", InterfaceNameContent.RES_CONFIRMRET_OR_IOMFORWFM);
            resultMap.put("type", "zd");
        } else {
            logger.error("未知 infType [{}] 请检查！", infType);
            throw new RuntimeException("infType = " + infType + "无法被识别处理");
        }
        return resultMap;
    }
}
