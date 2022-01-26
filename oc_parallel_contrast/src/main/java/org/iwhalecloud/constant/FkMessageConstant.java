package org.iwhalecloud.constant;

public class FkMessageConstant {

    private FkMessageConstant(){}

    // 服开 - 资源派单报文查询
    public static final String RES_SEND_ORDER_SQL = "SELECT t3.iom_xml AS IOM_XML,t2.work_order_state AS STATE FROM om_order t1,wo_work_order t2,inf_iom_log t3 " +
            "WHERE t1.order_code = ? AND t2.base_order_id = t1.id AND t2.tache_define_id =?  AND t3.work_order_id = t2.id AND t3.srv_type = '1002' AND ROWNUM = 1";

    // 服开 - 资源回单报文查询
    public static final String RES_BACK_ORDER_SQL = "SELECT t3.out_xml AS OUT_XML FROM om_order t1, wo_work_order t2, inf_iom_log t3 " +
            "WHERE t1.order_code = ? AND t2.base_order_id = t1.id AND t2.tache_define_id = ?  AND t3.work_order_id = t2.id AND t3.srv_type = '1006'  AND ROWNUM = 1";

    // 服开 - 综调派单报文查询
    public static final String ZD_SEND_ORDER_SQL = "SELECT t3.iom_xml AS IOM_XML,t2.work_order_state AS STATE FROM om_order t1,wo_work_order t2,inf_iom_log t3 " +
            "WHERE t1.order_code = ? AND t2.base_order_id = t1.id AND t2.tache_define_id = ? AND t3.work_order_id = t2.id AND t3.system_flag = 'ZDS'  AND ROWNUM = 1";

    // 服开 - 综调回单报文查询
    public static final String ZD_BACK_ORDER_SQL = "SELECT t3.iom_xml AS OUT_XML FROM om_order t1,wo_work_order t2,inf_iom_log t3 " +
            "WHERE t1.order_code = ? AND t2.base_order_id = t1.id AND t2.tache_define_id = ?  AND t3.work_order_id = t2.id AND t3.srv_type = '1110' AND ROWNUM = 1";

}
