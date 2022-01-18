package org.iwhalecloud.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.PropertySource;
import org.springframework.stereotype.Component;

@Component
@PropertySource(value = "classpath:/META-INF/interface.properties")
public class InterfaceConfig {

    // 资源待装/退单接口
    @Value("${restobeinstalledservice.url}")
    private String resToBeInstalledServiceUrl;

    // 资源配置反馈接口
    @Value("${resourcewebservice.url}")
    private String ResourceWebServiceUrl;

    // 综调回单/外线资源确认反馈接口
    @Value("${integratedschedulwebservice.url}")
    private String IntegratedSchedulWebServiceUrl;

    // 小文件查询接口
    @Value("${queryfileinfobyapiinstid.url}")
    private String queryFileInfoByApiInstIdUrl;

    public String getResToBeInstalledServiceUrl() {
        return resToBeInstalledServiceUrl;
    }

    public String getResourceWebServiceUrl() {
        return ResourceWebServiceUrl;
    }

    public String getIntegratedSchedulWebServiceUrl() {
        return IntegratedSchedulWebServiceUrl;
    }

    public String getQueryFileInfoByApiInstIdUrl(){
        return queryFileInfoByApiInstIdUrl;
    }
}
