package org.iwhalecloud.constant;

public class SoapConstant {
    private SoapConstant(){}

    // 统一资源回单soap头
    public static String RES_BACK_SOAP_HEAD = "<soapenv:Envelope\n" +
            "    xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\"\n" +
            "    xmlns:xsd=\"http://www.w3.org/2001/XMLSchema\"\n" +
            "    xmlns:soapenv=\"http://schemas.xmlsoap.org/soap/envelope/\"\n" +
            "    xmlns:com=\"http://common.ws.yn.oss.ztesoft.com\">\n" +
            "    <soapenv:Header/>\n" +
            "    <soapenv:Body>\n" +
            "        <com:rscService soapenv:encodingStyle=\"http://schemas.xmlsoap.org/soap/encoding/\">\n" +
            "            <infType xsi:type=\"soapenc:string\" xmlns:soapenc=\"http://schemas.xmlsoap.org/soap/encoding/\">resAssignResponse</infType>\n" +
            "            <head xsi:type=\"soapenc:string\"\n" +
            "                xmlns:soapenc=\"http://schemas.xmlsoap.org/soap/encoding/\">\n" +
            "            </head>\n" +
            "            <body xsi:type=\"soapenc:string\"\n" +
            "                xmlns:soapenc=\"http://schemas.xmlsoap.org/soap/encoding/\">\n" +
            "\t\t\t</body>\n" +
            "\t\t</com:rscService>\n" +
            "\t</soapenv:Body>\n" +
            "</soapenv:Envelope>";

    // 外线资源确认回单接口 Soap 头
    public static final String OUTERRES_BACK_SOAP_HEAD = "<soapenv:Envelope\n" +
            "    xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\"\n" +
            "    xmlns:xsd=\"http://www.w3.org/2001/XMLSchema\"\n" +
            "    xmlns:soapenv=\"http://schemas.xmlsoap.org/soap/envelope/\"\n" +
            "    xmlns:com=\"http://common.ws.yn.oss.ztesoft.com\">\n" +
            "    <soapenv:Header/>\n" +
            "    <soapenv:Body>\n" +
            "        <com:commonInterface soapenv:encodingStyle=\"http://schemas.xmlsoap.org/soap/encoding/\">\n" +
            "            <infType xsi:type=\"soapenc:string\" xmlns:soapenc=\"http://schemas.xmlsoap.org/soap/encoding/\">ResConfirmRet</infType>\n" +
            "            <xml xsi:type=\"soapenc:string\"\n" +
            "                xmlns:soapenc=\"http://schemas.xmlsoap.org/soap/encoding/\">\n" +
            "            </xml>\n" +
            "        </com:commonInterface>\n" +
            "    </soapenv:Body>\n" +
            "</soapenv:Envelope>\t";

    // 综调回单 Soap 头
    public static final String IOM_BACK_SOAP_HEAD = "<soapenv:Envelope\n" +
            "    xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\"\n" +
            "    xmlns:xsd=\"http://www.w3.org/2001/XMLSchema\"\n" +
            "    xmlns:soapenv=\"http://schemas.xmlsoap.org/soap/envelope/\"\n" +
            "    xmlns:com=\"http://common.ws.yn.oss.ztesoft.com\">\n" +
            "    <soapenv:Header/>\n" +
            "    <soapenv:Body>\n" +
            "        <com:commonInterface soapenv:encodingStyle=\"http://schemas.xmlsoap.org/soap/encoding/\">\n" +
            "            <infType xsi:type=\"soapenc:string\" xmlns:soapenc=\"http://schemas.xmlsoap.org/soap/encoding/\">IOMFORWFM</infType>\n" +
            "            <xml xsi:type=\"soapenc:string\"\n" +
            "                xmlns:soapenc=\"http://schemas.xmlsoap.org/soap/encoding/\">\n" +
            "\t\t\t</xml>\n" +
            "\t\t</com:commonInterface>\n" +
            "\t</soapenv:Body>\n" +
            "</soapenv:Envelope>";

    // 资源退单 Soap 头
    public static final String RES_QUIT_ORDER_SOAP_HEAD = "<soapenv:Envelope\n" +
            "    xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\"\n" +
            "    xmlns:xsd=\"http://www.w3.org/2001/XMLSchema\"\n" +
            "    xmlns:soapenv=\"http://schemas.xmlsoap.org/soap/envelope/\"\n" +
            "    xmlns:com=\"http://common.ws.yn.oss.ztesoft.com\">\n" +
            "    <soapenv:Header/>\n" +
            "    <soapenv:Body>\n" +
            "        <com:rscService soapenv:encodingStyle=\"http://schemas.xmlsoap.org/soap/encoding/\">\n" +
            "            <infType xsi:type=\"soapenc:string\"\n" +
            "                xmlns:soapenc=\"http://schemas.xmlsoap.org/soap/encoding/\">resAssignResponse\n" +
            "            </infType>\n" +
            "            <head xsi:type=\"soapenc:string\"\n" +
            "                xmlns:soapenc=\"http://schemas.xmlsoap.org/soap/encoding/\">\n" +
            "            </head>\n" +
            "            <body xsi:type=\"soapenc:string\"\n" +
            "                xmlns:soapenc=\"http://schemas.xmlsoap.org/soap/encoding/\">\n" +
            "\t\t\t</body>\n" +
            "\t\t</com:rscService>\n" +
            "\t</soapenv:Body>\n" +
            "</soapenv:Envelope>\n" ;

    // 综调退单 Soap 头
    public static final String ZD_QUIT_ORDER_SOAP_HEAD = "<soapenv:Envelope xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\" xmlns:xsd=\"http://www.w3.org/2001/XMLSchema\" xmlns:soapenv=\"http://schemas.xmlsoap.org/soap/envelope/\" xmlns:com=\"http://common.ws.yn.oss.ztesoft.com\">\n" +
            "   <soapenv:Header/>\n" +
            "   <soapenv:Body>\n" +
            "      <com:commonInterface soapenv:encodingStyle=\"http://schemas.xmlsoap.org/soap/encoding/\">\n" +
            "         <infType xsi:type=\"soapenc:string\" xmlns:soapenc=\"http://schemas.xmlsoap.org/soap/encoding/\">IOMFORWFM</infType>\n" +
            "         <xml xsi:type=\"soapenc:string\" xmlns:soapenc=\"http://schemas.xmlsoap.org/soap/encoding/\"></xml>\n" +
            "      </com:commonInterface>\n" +
            "   </soapenv:Body>\n" +
            "</soapenv:Envelope>";
}
