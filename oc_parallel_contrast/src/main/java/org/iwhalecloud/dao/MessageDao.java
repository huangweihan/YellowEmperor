package org.iwhalecloud.dao;

import java.util.Map;

public interface MessageDao {

    // 服开系统 - 资源派单报文查询
    Map<String, Object> queryResSendOrderMessage(String orderCode, String tacheId);

    // 服开系统 - 资源回单报文查询
    Map<String, Object> queryResBackOrderMessage(String orderCode, String tacheId);

    // 服开系统 - 综调派单报文查询
    Map<String, Object> queryZdSendOrderMessage(String orderCode, String tacheId);

    // 服开系统 - 资源回单报文查询
    Map<String, Object> queryZdBackOrderMessage(String orderCode, String tacheId);
}
