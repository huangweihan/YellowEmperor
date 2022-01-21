package org.iwhalecloud.dao.impl;

import org.iwhalecloud.constant.FkMessageConstant;
import org.iwhalecloud.dao.MessageDao;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

import java.util.Map;

/**
 * 派单报文用来对比，回单报文用来推动下一个环节
 */
@Component
public class MessageDaoImpl implements MessageDao {

    private static final Logger logger = LoggerFactory.getLogger(MessageDaoImpl.class);

    @Autowired
    @Qualifier("fkJdbcTemplate")
    private JdbcTemplate fkJdbcTemplate;

    @Override
    public Map<String, Object> queryResSendOrderMessage(String orderCode, String tacheId) {
        try {
            return fkJdbcTemplate.queryForMap(FkMessageConstant.RES_SEND_ORDER_SQL, orderCode, tacheId);
        } catch (EmptyResultDataAccessException ex) {
            logger.info("orderCode[{}] tacheId[{}]查询服开报文为空", orderCode, tacheId);
        } catch (Exception ex) {
            logger.error("查询服开回单报文异常：\n{}", ex.getMessage());
        }
        return null;
    }

    @Override
    public Map<String, Object> queryResBackOrderMessage(String orderCode, String tacheId) {
        try {
            return fkJdbcTemplate.queryForMap(FkMessageConstant.RES_BACK_ORDER_SQL, orderCode, tacheId);
        } catch (EmptyResultDataAccessException ex) {
            logger.info("orderCode[{}] tacheId[{}]查询服开报文为空", orderCode, tacheId);
        } catch (Exception ex) {
            logger.error("查询服开回单报文异常：\n{}", ex.getMessage());
        }
        return null;
    }

    @Override
    public Map<String, Object> queryZdSendOrderMessage(String orderCode, String tacheId) {
        try {
            return fkJdbcTemplate.queryForMap(FkMessageConstant.ZD_SEND_ORDER_SQL, orderCode, tacheId);
        } catch (EmptyResultDataAccessException ex) {
            logger.info("orderCode[{}] tacheId[{}]查询服开报文为空", orderCode, tacheId);
        } catch (Exception ex) {
            logger.error("查询综调派单报文异常：\n{}", ex.getMessage());
        }
        return null;
    }

    @Override
    public Map<String, Object> queryZdBackOrderMessage(String orderCode, String tacheId) {
        try {
            return fkJdbcTemplate.queryForMap(FkMessageConstant.ZD_BACK_ORDER_SQL, orderCode, tacheId);
        } catch (EmptyResultDataAccessException ex) {
            logger.info("orderCode[{}] tacheId[{}]查询服开报文为空", orderCode, tacheId);
        } catch (Exception ex) {
            logger.error("查询综调回单报文异常：\n{}", ex.getMessage());
        }
        return null;
    }
}
