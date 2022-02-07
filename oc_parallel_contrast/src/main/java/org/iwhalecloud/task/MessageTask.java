package org.iwhalecloud.task;

import com.alibaba.fastjson.JSONObject;
import org.apache.commons.collections.MapUtils;
import org.dom4j.Document;
import org.dom4j.DocumentException;
import org.dom4j.DocumentHelper;
import org.dom4j.Element;
import org.iwhalecloud.config.InterfaceConfig;
import org.iwhalecloud.constant.InterfaceNameContent;
import org.iwhalecloud.constant.SoapConstant;
import org.iwhalecloud.dao.impl.MessageDaoImpl;
import org.iwhalecloud.utils.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.util.Assert;
import org.springframework.util.ObjectUtils;
import org.springframework.util.StringUtils;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

@Component
@EnableScheduling
public class MessageTask {

    private static final Logger logger = LoggerFactory.getLogger(MessageTask.class);

    // 编排查询已派发未回单
    private static final String BP_OC_API_INST_SQL = "SELECT ID,TACHE_ID,MSG_CODE FROM oc_api_inst WHERE state in ('10D', '10RD')  ";

    // 根据编排的环节id转成成服开对应的环节id。存在一对多映射关系，返回多个去查询只会匹配到一个报文
    private static final String GET_FK_TACHE_ID_SQL = "SELECT fk_tache_id,bp_tache_name,port_type FROM gw_tache_map WHERE bp_tache_id = ?;";

    // 报文差异对比结果存储
    private static final String MSG_DIFF_SQL = "INSERT INTO GW_MSG_DIFF (order_id,work_order_id,fk_xml,bp_xml,diff,create_date) VALUES(?,?,?,?,?,now())";

    @Autowired
    @Qualifier("bpJdbcTemplate")
    private JdbcTemplate bpJdbcTemplate;

    @Autowired
    private MessageDaoImpl messageDao;

    @Autowired
    private InterfaceConfig interfaceConfig;

     @Scheduled(cron = "${time.task.cron}")
    public void endAlarmMessage() throws Exception {
        logger.info(" =========== 定时任务开启 =========== ");
        // 查询编排 派单未回单 的记录
        List<Map<String, Object>> ocApiInstList = bpJdbcTemplate.queryForList(BP_OC_API_INST_SQL);
        if (ocApiInstList.isEmpty()) {
            logger.info("查询oc_api_inst为空，跳过本次处理");
            logger.info(" =========== 定时任务结束 =========== ");
            return;
        }
        for (Map<String, Object> ocApiInstMap : ocApiInstList) {
            try {
                execute(ocApiInstMap);
            } catch (Exception e) {
                logger.error(e.getMessage());
            }
        }
        logger.info(" =========== 定时任务结束 =========== ");
    }

    private void execute(Map<String, Object> ocApiInstMap) throws Exception {
        // 获取编排的 workOrderId， 取表 oc_api_inst 中的 ID
        String bpWorkOrderId = MapUtils.getString(ocApiInstMap, "ID");
        // 检验
        Map<String, Object> checkMap = null;
        try {
            String checkSQl = "SELECT state,level,update_time FROM gw_bp_response_logs WHERE  work_order_id = ?  LIMIT 1";
            checkMap = bpJdbcTemplate.queryForMap(checkSQl, bpWorkOrderId);
        } catch (DataAccessException e) {
            logger.info("bpWorkOrderId[{}] 查询gw_bp_response_logs为空", bpWorkOrderId);
        }

        // 校验订单是否需要继续比较
        if (!checkOrderContinue(checkMap, bpWorkOrderId)) {
            return;
        }

        // 获取编排报文
        String bpMessage = getBpMessage(bpWorkOrderId);
        // 根据编排查询到的数据(编排报文) 取出环节id
        String bpTacheId = MapUtils.getString(ocApiInstMap, "TACHE_ID");
        // 获取订单编码
        String msgCode = MapUtils.getString(ocApiInstMap, "MSG_CODE");
        String[] fields = msgCode.split("#");
        String orderCode = fields[1];

        // 通过 编排环节id 查询配置表 转换得到 服开环节id
        List<Map<String, Object>> tacheMapList = bpJdbcTemplate.queryForList(GET_FK_TACHE_ID_SQL, bpTacheId);
        if (tacheMapList.isEmpty()) {
            logger.error("根据编排环节id:{} 找不到对应的服开环节id", bpTacheId);
            return;
        }

        // 判断当前是否需要跳过
        boolean checkOrderDealContinue = false;
        String bpTacheName = null;
        String[] array = {"网元施工", "网元平台施工", "网元平台施工_拆"};
        String jumpTacheName = null;
        for (Map<String, Object> map : tacheMapList) {
            bpTacheName = MapUtils.getString(map, "bp_tache_name", "");
            for (String tacheName : array) {
                if (ObjectUtils.nullSafeEquals(tacheName, bpTacheName)) {
                    checkOrderDealContinue = true;
                    jumpTacheName = tacheName;
                    break;
                }
            }
        }

        if (checkOrderDealContinue) {
            logger.info("当前环节是：{}跳过处理", jumpTacheName);
            return;
        }

        Map<String, String> messageMap = getFkMessage(tacheMapList, orderCode);

        // 服开派单报文
        String fkSendOrderMessage = MapUtils.getString(messageMap, "IOM_XML", "");
        // 服开回单报文
        String fkBackOrderMessage = MapUtils.getString(messageMap, "OUT_XML", "");

        // 服开回单和派单报文如果为空，那么不进行回单以及报文比对
        if (!StringUtils.hasText(fkSendOrderMessage) && !StringUtils.hasText(fkSendOrderMessage)) {

            logger.info("服开派单报文为空，不进行回单以及报文比对！");
            Calendar calendar = Calendar.getInstance();
            calendar.setTime(new Date());
            if (checkMap == null) {
                // 第一次
                String failSql = "insert into gw_bp_response_logs (work_order_id, level, update_time, create_date) values (?,?,?,now());";
                calendar.add(Calendar.SECOND, 30);
                bpJdbcTemplate.update(failSql, bpWorkOrderId, 1, calendar.getTime());
                return;
            }

            int level = MapUtils.getIntValue(checkMap, "level");
            calendar.add(Calendar.SECOND, Math.min(level * 30, 60));
            // calendar.add(Calendar.MINUTE, Math.min(level * 5, 30));
            String failSql = "update gw_bp_response_logs set level = level + 1,update_time = ? where work_order_id = ?";
            bpJdbcTemplate.update(failSql, calendar.getTime(), bpWorkOrderId);
            return;
        }

        // 服开派单报文进行 转为编排这边的 workOrderId
        String requestType = MapUtils.getString(messageMap, "type");
        // 订单状态
        String state = MapUtils.getString(messageMap, "state");
        if ("10R".equals(state)) {
            // 退单
            fkBackOrderMessage = replaceMessage(fkBackOrderMessage, bpWorkOrderId, requestType);
            if ("res".equals(requestType)) {
                fkBackOrderMessage = addSoapHead(fkBackOrderMessage, SoapConstant.RES_QUIT_ORDER_SOAP_HEAD);
                HttpUtils.callWebService(fkBackOrderMessage, interfaceConfig.getResToBeInstalledServiceUrl());
            } else {
                fkBackOrderMessage = addSoapHead(fkBackOrderMessage, SoapConstant.ZD_QUIT_ORDER_SOAP_HEAD);
                HttpUtils.callWebService(fkBackOrderMessage, interfaceConfig.getIntegratedSchedulWebServiceUrl());
            }
            logger.info("退单成功！");
            return;
        }

        fkBackOrderMessage = replaceMessage(fkBackOrderMessage, bpWorkOrderId, requestType);
        // 服开派单没有协议头
        fkBackOrderMessage = addSoapHead(fkBackOrderMessage, MapUtils.getString(messageMap, "soapHead"));

        String receiptUrl = MapUtils.getString(messageMap, "url");
        // 发起回单
        String result = HttpUtils.callWebService(fkBackOrderMessage, receiptUrl);
        logger.info(" ===== 回单结果： [{}] ==== ", result);

        if (checkMap != null) {
            String updateBackSuccessSql = "DELETE FROM gw_bp_response_logs WHERE work_order_id = ?";
            bpJdbcTemplate.update(updateBackSuccessSql, bpWorkOrderId);
        }

        Assert.notNull(bpMessage, "编排对比报文不能为空");
        Assert.notNull(fkSendOrderMessage, "服开对比报文不能为空");
        // 报文对比
        dealWithCompare(bpMessage, fkSendOrderMessage, MapUtils.getString(messageMap, "type"));
    }

    // 报文对比
    private void dealWithCompare(String bpMessage, String fkMessage, String type) throws DocumentException {
        // 判断当前是综调请求还是资源请求
        String workOrderId = null;
        String orderId = null;
        String msgDiff = null;

        if ("res".equals(type)) {
            // 报文比较
            Element rootElement = DocumentHelper.parseText(bpMessage).getRootElement();
            Element subElement = rootElement.element("Body").elements().get(0).element("body");
            JSONObject bp = new JSONObject();
            Element bpEle = DocumentHelper.parseText(subElement.getTextTrim()).getRootElement();
            JSONUtils.dom4j2Json(bpEle, bp);

            JSONObject fk = new JSONObject();
            JSONUtils.dom4j2Json(DocumentHelper.parseText(fkMessage).getRootElement(), fk);
            // 资源
            List<String> diffResultList = new ArrayList<>();
            MessageCompareUtils.compareForRes(fk, bp, "root", diffResultList);
            msgDiff = org.apache.commons.lang3.StringUtils.join(diffResultList, "");
            orderId = bpEle.element("baseInfo").elementText("orderId");
            workOrderId = bpEle.element("baseInfo").elementText("workOrderId");
        } else if ("zd".equals(type)) {
            // 综调
            List<String> diffResultList = new ArrayList<>();
            Element rootElement = DocumentHelper.parseText(bpMessage).getRootElement();
            Element bpElement = rootElement.element("Body").elements().get(0).element("body");
            bpElement = DocumentHelper.parseText(bpElement.getTextTrim()).getRootElement();
            Element fkElement = DocumentHelper.parseText(fkMessage).getRootElement();

            MessageCompareUtils.compareForZd(fkElement, bpElement, diffResultList);

            msgDiff = org.apache.commons.lang3.StringUtils.join(diffResultList, "");
            List<Element> elements = bpElement.element("OM_SERVICE_ORDER").elements("OM_SO_ATTR");

            for (Element element : elements) {
                if ("WORK_ORDER_ID".equals(element.attribute("ATTR_CODE").getValue())) {
                    workOrderId = element.attribute("ATTR_VALUE").getValue();
                }
                if ("ORDER_ID".equals(element.attribute("ATTR_CODE").getValue())) {
                    orderId = element.attribute("ATTR_VALUE").getValue();
                }
            }
        }
        if (!StringUtils.hasText(msgDiff)) {
            logger.info("==== 报文比较结果一致 ====");
            return;
        }
        logger.info("==== 报文比较存在差异 =====");
        bpJdbcTemplate.update(MSG_DIFF_SQL, orderId, workOrderId, fkMessage, bpMessage, msgDiff);
    }

    // 添加 Soap头
    private String addSoapHead(String fkBackOrderMessage, String soapHead) {
        Element rootElement = null;
        try {
            rootElement = DocumentHelper.parseText(soapHead).getRootElement();
            Element element = rootElement.element("Body").elements().get(0);
            Element bodyElement = element.element("body");
            if (bodyElement != null) {
                // 说明当前是 <body>
                bodyElement.addCDATA(fkBackOrderMessage);
            } else {
                // 说明当前是 <xml>
                Element xmlElement = element.element("xml");
                xmlElement.addCDATA(fkBackOrderMessage);
            }
            return rootElement.asXML();
        } catch (DocumentException e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * 获取服开派单报文和回单报文
     *
     * @param tacheMapList 编排环节id可能对应多个服开环节id，但是只会匹配到一个服开报文
     * @param orderCode    订单编码
     * @return 报文
     */
    private Map<String, String> getFkMessage(List<Map<String, Object>> tacheMapList, String orderCode) {
        Map<String, String> messageMap = new HashMap<>();
        Map<String, Object> sendOrderMap = new HashMap<>();
        Map<String, Object> backOrderMap = new HashMap<>();
        for (Map<String, Object> tacheMap : tacheMapList) {
            String fkTacheId = MapUtils.getString(tacheMap, "fk_tache_id");
            String portType = MapUtils.getString(tacheMap, "port_type");
            Map<String, String> requestMap = judgeRequestSource(portType);
            String type = MapUtils.getString(requestMap, "type");
            // 根据转换之后的编码得到服开报文
            // 服开回单报文获取可能为空，暂时跳过，不做回单
            if (ObjectUtils.nullSafeEquals("res", type)) {
                sendOrderMap = messageDao.queryResSendOrderMessage(orderCode, fkTacheId);
                backOrderMap = messageDao.queryResBackOrderMessage(orderCode, fkTacheId);
            } else if (ObjectUtils.nullSafeEquals("zd", type)) {
                sendOrderMap = messageDao.queryZdSendOrderMessage(orderCode, fkTacheId);
                backOrderMap = messageDao.queryZdBackOrderMessage(orderCode, fkTacheId);
            }
            if (sendOrderMap != null) {
                messageMap.put("port_type", MapUtils.getString(tacheMap, "port_type"));
                messageMap.put("url", MapUtils.getString(requestMap, "url"));
                messageMap.put("type", type);
                messageMap.put("soapHead", MapUtils.getString(requestMap, "soapHead"));
                messageMap.put("state", MapUtils.getString(sendOrderMap, "state"));
                try {
                    // 服开派单报文
                    messageMap.put("IOM_XML", new String((byte[]) sendOrderMap.get("IOM_XML"), "GBK"));
                    // 服开回单报文(当状态是10R，回单报文为空)
                    messageMap.put("OUT_XML", new String((byte[]) backOrderMap.get("OUT_XML"), "GBK"));
                } catch (Exception e) {
                    e.printStackTrace();
                }
                break;
            }
        }
        return messageMap;
    }

    /**
     * 替换派单服开报文中的 workOrderId
     */
    private String replaceMessage(String xml, String workOrderId, String type) {
        try {
            Document document = DocumentHelper.parseText(xml);
            Element rootElement = document.getRootElement();
            if ("res".equals(type)) {
                rootElement.element("baseInfo").element("workOrderId").setText(workOrderId);
                return document.getRootElement().asXML();
            } else if ("zd".equals(type)) {
                rootElement.selectSingleNode("IDA_SVR_OPEN//INPUT_XMLDATA//LineOrder//OrderInfos//OrderInfo//BssOrderId").setText(workOrderId);
                return document.getRootElement().asXML();
            }
        } catch (DocumentException e) {
            e.printStackTrace();
        }
        return null;
    }

    // 根据映射表得出当前请求类型
    private Map<String, String> judgeRequestSource(String portType) {
        Assert.hasText(portType, "用于判断请求来源的不能为空");
        Map<String, String> resultMap = new HashMap<>();

        if ("IOMFORWFM".equals(portType)) {
            // 综调回单
            resultMap.put("url", interfaceConfig.getIntegratedSchedulWebServiceUrl());
            resultMap.put("infType", InterfaceNameContent.IOMFORWFM);
            resultMap.put("type", "zd");
            resultMap.put("soapHead", SoapConstant.IOM_BACK_SOAP_HEAD);
        } else if ("ResConfirmRet".equals(portType)) {
            // 外线资源确认回单
            resultMap.put("url", interfaceConfig.getIntegratedSchedulWebServiceUrl());
            resultMap.put("infType", InterfaceNameContent.RES_CONFIRMRET);
            resultMap.put("type", "zd");
            resultMap.put("soapHead", SoapConstant.OUTERRES_BACK_SOAP_HEAD);
        } else if ("resAssignResponse".equals(portType)) {
            // 资源回单
            resultMap.put("url", interfaceConfig.getResourceWebServiceUrl());
            resultMap.put("infType", InterfaceNameContent.RES_ASSIGN_RESPONSE);
            resultMap.put("type", "res");
            resultMap.put("soapHead", SoapConstant.RES_BACK_SOAP_HEAD);
        } else {
            logger.error("未知 funcCode [{}] 请检查！", portType);
            throw new RuntimeException("funcCode = " + portType + "无法被识别处理");
        }
        return resultMap;
    }

    /**
     * 获取编码派单报文
     *
     * @param apiInstId apiInstId
     * @return 报文
     */
    private String getBpMessage(String apiInstId) {
        JSONObject jsonObject = new JSONObject();
        jsonObject.put("apiInstId", apiInstId);
        String requestResult = HttpUtils.callHttpService(jsonObject.toJSONString(), interfaceConfig.getQueryFileInfoByApiInstIdUrl());
        JSONObject resultJsonObject = JSONObject.parseObject(requestResult);
        return resultJsonObject.getJSONObject("result").getString("requestInfo");
    }

    private boolean checkOrderContinue(Map<String, Object> checkMap, String bpWorkOrderId) throws ParseException {
        if (checkMap != null && !checkMap.isEmpty()) {
            String state = MapUtils.getString(checkMap, "state");
            // 状态为1才能继续判断，为0说明当前订单出了问题，不再继续下面环节
            if (!"1".equals(state)) {
                logger.info("bpWorkOrderId[{}] 异常状态不做处理", bpWorkOrderId);
                return false;
            }
            int level = MapUtils.getIntValue(checkMap, "level");
            if (level > 4) {
                String updateSql = "update gw_bp_response_logs set state = 0 where work_order_id = ?;";
                logger.info("bpWorkOrderId[{}] 异常次数已到达5次，标记为异常并不再做处理", bpWorkOrderId);
                bpJdbcTemplate.update(updateSql, bpWorkOrderId);
                return false;
            }

            String updateTime = MapUtils.getString(checkMap, "update_time");
            LocalDateTime localDateTime = LocalDateTime.parse(updateTime);
            String format = localDateTime.format(DateTimeFormatter.ofPattern("yyyy-MM-dd hh:mm:ss"));
            Calendar curCalendar = Calendar.getInstance();
            curCalendar.setTime(new Date());
            Calendar updateCalendar = Calendar.getInstance();
            updateCalendar.setTime(new SimpleDateFormat("yyyy-MM-dd hh:mm:ss").parse(format));

            if ((curCalendar.getTimeInMillis() < updateCalendar.getTimeInMillis())) {
                // 时间还没到，跳过当前
                logger.info("bpWorkOrderId[{}] 休眠时间还没到，继续等待", bpWorkOrderId);
                return false;
            }
            // 时间到了，放行
        }
        return true;
    }
}
