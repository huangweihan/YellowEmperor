package org.iwhalecloud.dao.impl;

import org.iwhalecloud.constant.FkMessageConstant;
import org.iwhalecloud.dao.MessageDao;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

import java.util.Map;

/**
 * 派单报文用来对比，回单报文用来推动下一个环节
 */
@Component
public class MessageDaoImpl implements MessageDao {

    @Autowired
    @Qualifier("fkJdbcTemplate")
    private JdbcTemplate fkJdbcTemplate;

    @Override
    public Map<String, Object> queryResSendOrderMessage(String orderCode, String tacheId) {
         return fkJdbcTemplate.queryForMap(FkMessageConstant.RES_SEND_ORDER_SQL, orderCode, tacheId);
    }

    @Override
    public Map<String, Object> queryResBackOrderMessage(String orderCode, String tacheId) {
        return fkJdbcTemplate.queryForMap(FkMessageConstant.RES_BACK_ORDER_SQL, orderCode, tacheId);
    }

    @Override
    public Map<String, Object> queryZdSendOrderMessage(String orderCode, String tacheId) {
        return fkJdbcTemplate.queryForMap(FkMessageConstant.ZD_SEND_ORDER_SQL, orderCode, tacheId);
    }

    @Override
    public Map<String, Object> queryZdBackOrderMessage(String orderCode, String tacheId) {
        return fkJdbcTemplate.queryForMap(FkMessageConstant.ZD_BACK_ORDER_SQL, orderCode, tacheId);
    }
}
