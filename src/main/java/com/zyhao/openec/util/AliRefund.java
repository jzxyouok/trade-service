package com.zyhao.openec.util;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

public class AliRefund {
	static Log log = LogFactory.getLog(AliRefund.class);
	
	//商户的私钥,需要PKCS8格式，RSA公私钥生成：https://doc.open.alipay.com/doc2/detail.htm?spm=a219a.7629140.0.0.nBDxfy&treeId=58&articleId=103242&docType=1
	public static String private_key = "MIICdQIBADANBgkqhkiG9w0BAQEFAASCAl8wggJbAgEAAoGBAJhQccxe0uKjh/MDTdSIh2oXiY2JPuIWlja/FkdsbUvN1AalYYsyMwn3umsMgPzRnqnLi1HKynTK3f2+HVPptu/I1arnhMyYfCJGtejqVqamwa8ombGrA8kG12vPx5gTUPlbTWJo3i26mEW9tSlGYIq2GT5VoduN8CRHP4ALY4NjAgMBAAECgYApY8rYv3fNEKUz21T+CS/LG6RMatxEseCV9e1G8Wbt73vLZouQOM73yXLF/jra+NeFyoMMce/8NtiGSHBNyjfkm7rcm2jfBHE5+v7qNII5qjAkKUYcHn4vbnATjw2/N+p3fQ9/2A9WbCDvmlI/XVFyalY7EM1SPshlPPmHm/9/4QJBAMcpqxgqmWAWMF1AyCRgoTUNrG9cMhbWE1ZGVRq8ygbVBAGqfuqH6LleW8YDL1AhYp9DDxta5KCZQLa6ofvTNBMCQQDDyCPi/uoMXWZ5P+yWOwvdHTbl4mKw94Ds55iUy5SvGJDG2xhmIJZYRkthrrQXq7RGYQnwZlgOSqCAQAhfWz1xAkAlnOf0G2915xrhMa/atukbFOcA7P+Eso9aVtQRxZ+95JLqIaHwdxRrlbdtUQKpdnSisU50ExlkfyzqlZqr7XpDAkARLJxhTEVRwlu75Sym3RUkV2y6a8qGhZ+sqIBNBTLMzVXd69hPFSq7Iad5MS8hKHHO/rI1gXTWBBviRDu3ybQxAkAKXRNB9nJt+IdwwgjeLW7iShB4XWGQuh1DQfHHl+Huqu3dkBZfL3KRk9OM3lBu3t20UgacsUtwdT5BQebHwg3B";
	// 支付宝的公钥,查看地址：https://b.alipay.com/order/pidAndKey.htm
	public static String alipay_public_key  = "MIGfMA0GCSqGSIb3DQEBAQUAA4GNADCBiQKBgQCYUHHMXtLio4fzA03UiIdqF4mNiT7iFpY2vxZHbG1LzdQGpWGLMjMJ97prDID80Z6py4tRysp0yt39vh1T6bbvyNWq54TMmHwiRrXo6lampsGvKJmxqwPJBtdrz8eYE1D5W01iaN4tuphFvbUpRmCKthk+VaHbjfAkRz+AC2ODYwIDAQAB";
	//退款异步通知页面路径 需http://格式的完整路径，不能加?id=123这类自定义参数，必须外网可以正常访问
	public static String refund_url = "";
	public static String partner_key = "khrf6zjenrdns6gxdwveilghoaowe59f";
	
//	
//	public static void main(String[] args) {
//		try {
//			///singleTradeQuery();
//			alipayTradeQuery();
//		} catch (Exception e) {
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//		}
//	}
	public static void singleTradeQuery() throws Exception {
		String partner = "2088401182898775";
		String input_charset = "utf-8";
		String out_trade_no = "1586c8e0e1d0364";
        String key = private_key;
		String sign_type = "RSA";
		//---------------------------------------------
		Map<String, String> sParaTemp = new HashMap<String, String>();
		sParaTemp.put("service", "single_trade_query");
		sParaTemp.put("partner", partner);
        sParaTemp.put("_input_charset",input_charset);
		//sParaTemp.put("trade_no",out_trade_no);
		sParaTemp.put("out_trade_no",out_trade_no);
		
		String sHtmlText = buildRequestHttp(sParaTemp,sign_type,key,input_charset);
        String url = "https://mapi.alipay.com/gateway.do?"+sHtmlText;
        String result = HttpUtil.postLzsz(url,"");
        log.info(result);
	}
	
	public static void alipayTradeQuery() throws Exception {
		String app_id = "2016042001316516";
		String method = "alipay.trade.query";// (统一收单线下交易查询)
		String out_trade_no = "1586c8e0e1d0364";
		String key = private_key;
		String sign_type = "RSA";
		String charset = "utf-8";
		String timestamp = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new java.util.Date());

		//---------------------------------------------
		Map<String, String> sParaTemp = new HashMap<String, String>();
		
		sParaTemp.put("app_id", app_id);
		sParaTemp.put("method",method);
		sParaTemp.put("charset",charset);
		sParaTemp.put("sign_type",sign_type);
		sParaTemp.put("timestamp",timestamp );
		sParaTemp.put("version","1.0");
		sParaTemp.put("biz_content","");
		sParaTemp.put("out_trade_no",out_trade_no);
		//sParaTemp.put("trade_no",out_trade_no);
		
		String sHtmlText = buildRequestHttp(sParaTemp,sign_type,key,charset);
		sHtmlText = sHtmlText.replaceAll(" ", "%20");////重要
		
		String url = "https://mapi.alipay.com/gateway.do?"+sHtmlText;
        String result = HttpUtil.postLzsz(url,"");
        log.info(result);
	}
	
	
    public static void refund() throws Exception {
    	String url = "https://mapi.alipay.com/gateway.do?";
    	String partner = "2088401182898775";
    	String key = private_key;
    	
    	String channelId = "2";
    	String tradeNo = "";//原付款支付宝交易号
    	String refundFee = "";//退款总金额;
    	String refundReason = "";//退款原因
    	String sign_type = "RSA";
        String input_charset = "utf-8";
        //----------------基本参数-------------------------
    	Map<String, String> sParaTemp = new HashMap<String, String>();
		sParaTemp.put("service", "refund_fastpay_by_platform_nopwd");
		sParaTemp.put("partner",partner);
		sParaTemp.put("_input_charset","utf-8");
		sParaTemp.put("notify_url", refund_url);
		//UniqueCodeComponent uniqueEncodeBack = UniqueEncode.getObject();
		String batchNo = "eeeeeeeeeeeeeee";//uniqueEncodeBack.getNextCode(channelId);
		sParaTemp.put("batch_no", batchNo);
		sParaTemp.put("refund_date", new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new java.util.Date()));
		sParaTemp.put("batch_num", "1");
		try {
			sParaTemp.put("detail_data",new String((tradeNo+"^"+refundFee+"^"+refundReason).getBytes("ISO-8859-1"), "UTF-8"));
		} catch (Exception e) {
			e.printStackTrace();
		}
		//-----------------建立请求------------------------
		String sHtmlText = buildRequestHttp(sParaTemp,sign_type,key,input_charset);
		sHtmlText = sHtmlText.replaceAll(" ", "%20");////重要
		log.info( " 建立请求参数：" +url+ sHtmlText);
		String result = HttpUtil.postLzsz(url+sHtmlText,"");
		//-----------------请求响应处理------------------------
		log.info(result);
		
	}
    
    
    /** 
     * 除去数组中的空值和签名参数
     * @param sArray 签名参数组
     * @return 去掉空值与签名参数后的新签名参数组
     */
    public static Map<String, String> paraFilter(Map<String, String> sArray) {

        Map<String, String> result = new HashMap<String, String>();

        if (sArray == null || sArray.size() <= 0) {
            return result;
        }

        for (String key : sArray.keySet()) {
            String value = sArray.get(key);
            if (value == null || value.equals("") || key.equalsIgnoreCase("sign")
                || key.equalsIgnoreCase("sign_type")) {
                continue;
            }
            result.put(key, value);
        }

        return result;
    }
    
    /** 
     * 把数组所有元素排序，并按照“参数=参数值”的模式用“&”字符拼接成字符串
     * @param params 需要排序并参与字符拼接的参数组
     * @return 拼接后字符串
     */
    public static String createLinkString(Map<String, String> params) {

        List<String> keys = new ArrayList<String>(params.keySet());
        Collections.sort(keys);

        String prestr = "";
        for (int i = 0; i < keys.size(); i++) {
            String key = keys.get(i);
            String value = params.get(key);

            if (i == keys.size() - 1) {//拼接时，不包括最后一个&字符
                prestr = prestr + key + "=" + value;
            } else {
                prestr = prestr + key + "=" + value + "&";
            }
        }

        return prestr;
    }
    
    /**
     * 生成签名结果
     * @param sPara 要签名的数组
     * @return 签名结果字符串
     */
	public static String buildRequestMysign(Map<String, String> sPara,String sign_type,String key,String input_charset) {
    	String prestr = createLinkString(sPara); //把数组所有元素，按照“参数=参数值”的模式用“&”字符拼接成字符串
    	log.info("prestr="+prestr);
        String mysign = "";
        if(sign_type.equals("RSA") ){
        	mysign = RSA.sign(prestr,key,input_charset);
        }
        if(sign_type.equals("MD5") ) {
        	mysign = MD5.sign(prestr,key,input_charset);
        }
        log.info("mysign="+mysign);
        return mysign;
    }
	
    /**
     * 生成要请求给支付宝的参数数组
     * @param sParaTemp 请求前的参数数组
     * @return 要请求的参数数组
     */
    private static Map<String, String> buildRequestPara(Map<String, String> sParaTemp,String sign_type,String key,String input_charset) {
        //除去数组中的空值和签名参数
        Map<String, String> sPara = paraFilter(sParaTemp);
        //生成签名结果
        String mysign = buildRequestMysign(sPara,sign_type, key, input_charset);

        //签名结果与签名方式加入请求提交参数组中
        sPara.put("sign", mysign);
        sPara.put("sign_type",sign_type);

        return sPara;
    }
    
    public static String buildRequestHttp(Map<String, String> sParaTemp,String sign_type,String key,String input_charset) throws Exception{
        //待请求参数数组
        Map<String, String> sPara = buildRequestPara(sParaTemp,sign_type,key,input_charset);
        List<String> keys = new ArrayList<String>(sPara.keySet());

        Collections.sort(keys);
        
        String getStr = "";
        for (int i = 0; i < keys.size(); i++) {
            String name = (String) keys.get(i);
            String value = (String) sPara.get(name);
            if(i==0){
            	getStr+=name+"="+value;
            }else{
            	getStr+="&"+name+"="+value;
            }
        } 
        log.info("--------------buildRequestHttp="+getStr);
        return getStr;
    }
    
    
    
}
