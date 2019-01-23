package com.example.init;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Scanner;

import org.jsoup.Connection;
import org.jsoup.Connection.Method;
import org.jsoup.Connection.Response;
import org.jsoup.Jsoup;

/**
 * 模拟登录带验证码的教务系统
 * 
 * 2018-2-9
 */
public class JsoupSafeCode {
    private static String url_safecode = "http://jwgl.sdust.edu.cn/verifycode.servlet?0.020974584"; // 验证码
    private static String url_encode = "http://jwgl.sdust.edu.cn/Logon.do?method=logon&flag=sess"; // 加密字符串
    private static String url_Login = "http://jwgl.sdust.edu.cn/Logon.do?method=logon"; // 登录
    private String username = "";
    private String password = "";
    private static String path = JsoupSafeCode.class.getResource("/").getPath().replaceAll("%20", " ") + "safecode.png";
    private static Map<String, String> cookie;

    /**
     * 下载验证码
     * 保存Cookie
     * @throws IOException
     */
    public static void getSafeCode() throws IOException {
        Response response = Jsoup.connect(url_safecode).ignoreContentType(true) // 获取图片需设置忽略内容类型
                .userAgent("Mozilla").method(Method.GET).timeout(3000).execute();
        cookie = response.cookies();
        byte[] bytes = response.bodyAsBytes();
        Util.saveFile(path, bytes);
        System.out.println("保存验证码到：" + path);
    }

    /**
     * 登录教务系统
     */
    public static void initLogin() throws IOException {
        System.out.print("输入验证码：");
        Scanner scan = new Scanner(System.in);
        String code = scan.next();
        try {
            Map<String, String> data = new HashMap<String, String>();
            data.put("view", "1");
            data.put("encoded", getEncoded());
            data.put("RANDOMCODE", code);
            Connection connect = Jsoup.connect(url_Login)
                    .header("Accept",
                            "text/html,application/xhtml+xml,application/xml;q=0.9,image/webp,image/apng,*/*;q=0.8")
                    .userAgent("Mozilla").method(Method.POST).data(data).timeout(3000);
            for (Map.Entry<String, String> entry : cookie.entrySet()) {
                connect.cookie(entry.getKey(), entry.getValue());
            }
            Response response = connect.execute();
            System.out.println(response.parse().text().toString());
        } catch (IOException e) {

        }
    }

    /**
     * 加密参数（依具体环境而定，加密算法一般在JS中获得）
     */
    public static String getEncoded() {
        try {
            Connection connect = Jsoup.connect(url_encode)
                    .header("Accept",
                            "text/html,application/xhtml+xml,application/xml;q=0.9,image/webp,image/apng,*/*;q=0.8")
                    .userAgent("Mozilla").method(Method.POST).timeout(3000);
            for (Map.Entry<String, String> entry : cookie.entrySet()) {
                connect.cookie(entry.getKey(), entry.getValue());
            }
            Response response = connect.execute();
            String dataStr = response.parse().text();
            // 把JS中的加密算法用Java写一遍：
            String scode = dataStr.split("#")[0];
            String sxh = dataStr.split("#")[1];
            String code = "201601060622" + "%%%" + "anyan2014";//账号  密码
            String encoded = "";
            for (int i = 0; i < code.length(); i++) {
                if (i < 20) {
                    encoded = encoded + code.substring(i, i + 1)
                            + scode.substring(0, Integer.parseInt(sxh.substring(i, i + 1)));
                    scode = scode.substring(Integer.parseInt(sxh.substring(i, i + 1)), scode.length());
                } else {
                    encoded = encoded + code.substring(i, code.length());
                    i = code.length();
                }
            }
            return encoded;
        } catch (IOException e) {

        }
        return null;
    }
    
    public static void main(String[] args) throws IOException {
    	getSafeCode();
    	initLogin();
    	
	}

}
