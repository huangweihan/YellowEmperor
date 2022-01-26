package org.iwhalecloud.utils;

import org.apache.commons.io.IOUtils;
import org.apache.http.Consts;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.impl.conn.PoolingHttpClientConnectionManager;
import org.apache.http.protocol.BasicHttpContext;
import org.apache.http.protocol.HttpContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;

@SuppressWarnings(value = "all")
public class HttpUtils {

    private static final Logger log = LoggerFactory.getLogger(HttpUtils.class);

    private static final PoolingHttpClientConnectionManager hcm = new PoolingHttpClientConnectionManager();

    private static HttpClient httpClient;
    static {
        hcm.setMaxTotal(200);
        hcm.setDefaultMaxPerRoute(200);
        HttpClientBuilder builder = HttpClientBuilder.create();
        builder.setConnectionManager(hcm);
        httpClient = builder.build();
    }

    /**
     * http请求
     * @param requestParam 请求参数(JSON格式)
     * @param targetUrl          请求路径
     * @return 请求结果
     */
    public static String callHttpService(String requestParam, String targetUrl) {
        String requestResult = null;
        try {
            HttpPost httpPost = new HttpPost(targetUrl);
            httpPost.setHeader("Accept-Encoding", "UTF-8");
            httpPost.setHeader("Content-Type", "application/json;charset=UTF-8");
            httpPost.setEntity(new StringEntity(requestParam, ContentType.create("application/json", Consts.UTF_8)));
            RequestConfig requestConfig = RequestConfig.custom().setConnectTimeout(30000).setSocketTimeout(30000).setStaleConnectionCheckEnabled(true).build();
            httpPost.setConfig(requestConfig);
            HttpContext httpContext = new BasicHttpContext();
            log.info(" ===== http请求参数[{}] ==== ", requestParam);
            HttpResponse response = httpClient.execute(httpPost, httpContext);
            // 响应结果处理
            HttpEntity httpEntity = response.getEntity();
            InputStream inputStream = httpEntity.getContent();
            requestResult = IOUtils.toString(inputStream);
            log.info(" ===== http请求结果[{}] ==== ", requestResult);
        } catch (Exception e) {
            log.error("请求参数[{}] - 请求路径[{}] - 请求异常[{}]", requestParam, targetUrl, e);
        }
        return requestResult;
    }

    /**
     * wb请求
     * @param requestParam 请求参数
     * @param targetUrl 请求路径
     * @return 请求结果
     */
    public static String callWebService(String requestParam, String targetUrl) {
        String requestResult = "";
        PrintWriter out = null;
        ByteArrayOutputStream bos = null;
        BufferedInputStream bis = null;
        try {
            // 创建url连接
            URL url = new URL(targetUrl);
            // 打开连接
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("POST");
            connection.setDoOutput(true);
            connection.setDoInput(true);
            // 设置请求参数
            connection.setRequestProperty("Content-Type", "text/xml;charset=UTF-8");
            connection.setRequestProperty("Transfer-Encoding", "gzip,deflate");
            connection.setRequestProperty("connection", "close");
            connection.setRequestProperty("user-agent",
                    "Jakarta Commons-HttpClient/3.1");
            connection.setRequestProperty("SOAPAction", "");
            log.info(" ===== wb请求参数[{}] ==== ", requestParam);
            // 建立实际连接
            connection.connect();
            // 获取URLConnection对象对应的输出流
            out = new PrintWriter(connection.getOutputStream());
            // 删除参数
            out.print(requestParam);
            // flush输出流的缓冲
            out.flush();
            // 获取对应的数据字节流
            bos = new ByteArrayOutputStream();
            byte[] buf = new byte[1024];
            int c = 0;
            // 如果获取的结果是成功的
            if (connection.getResponseCode() == 200) {
                // 读取数据
                bis = new BufferedInputStream(connection.getInputStream());
            } else {
                // 如果不是200，那就是出问题,获取错误流
                bis = new BufferedInputStream(connection.getErrorStream());
            }
            while ((c = bis.read(buf)) != -1) {
                bos.write(buf, 0, c);
                bos.flush();
            }
            // 获取数据
            requestResult = bos.toString();
            requestResult = requestResult.replaceAll("<\\?xml version=\"1.0\" encoding=\"GB2312\"\\?>", "");
            requestResult = requestResult.replaceAll("<\\?xml version=\"1.0\" encoding=\"UTF-8\"\\?>", "");
            requestResult = ParseUtil.xmlRoughParse(requestResult, "root");
            log.info(" ===== wb请求结果[{}] ==== ", requestResult);
            return requestResult;
        } catch (Exception e) {
            log.error("Exception error =========>HttpRequestUtils.httpWebService.error===============>{}",e.getMessage());
        } finally {
            try {
                if (out != null) {
                    out.close();
                }
                if (bos != null) {
                    bos.close();
                }
                if (bis != null) {
                    bis.close();
                }
            } catch (IOException ex) {
                ex.printStackTrace();
                log.error("IOException error-======>HttpRequestUtils.httpWebService.error===============>{}",ex.getMessage());
            }
        }
        return requestResult;
    }

}
