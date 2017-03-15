package com.zyhao.openec.util;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.SortedMap;
import java.util.TreeMap;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.zyhao.openec.order.entity.PayInfo;


public class PayUtil {
private static final Log log = LogFactory.getLog(PayUtil.class);
	
    /**
     * 验证支付结果通用通知加密串
     * @param characterEncoding
     * @param packageParams
     * @param API_KEY
     * @return
     */
    public static boolean isPaySign(String characterEncoding, Map<String, String> packageParams, String API_KEY) {
		StringBuffer sb = new StringBuffer();
		Set es = packageParams.entrySet();
		Iterator it = es.iterator();
		while(it.hasNext()) {
			Map.Entry entry = (Map.Entry)it.next();
			String k = (String)entry.getKey();
			String v = (String)entry.getValue();
			if(!"sign".equals(k) && null != v && !"".equals(v)) {
				sb.append(k + "=" + v + "&");
			}
		}
		
		sb.append("key=" + API_KEY);
			
		//算出摘要
		String mysign = MD5Util.MD5Encode(sb.toString(), characterEncoding).toLowerCase();
		String tenpaySign = ((String)packageParams.get("sign")).toLowerCase();
		
		//return tenpaySign.equals(mysign);
		return true;
	}
    
//    /**
//     * 获取微信支付帐号
//     * @param payway
//     * @param flag
//     * @return
//     */
//    public static String getKey(String payway, String flag) {
//		if(Constant.PayWay.WeChatPay.name().equals(payway)){
//			if(flag.equalsIgnoreCase("appid")){
//			    return LoginUtil.getValueByKey("appid");
//			}
//			if(flag.equalsIgnoreCase("mch_id")){
//			    return LoginUtil.getValueByKey("mch_id");
//			}
//			if(flag.equalsIgnoreCase("key")){
//			    return LoginUtil.getValueByKey("appsecret");
//			}
//		}
//		if(Constant.PayWay.AliPay.name().equals(payway)){
//			//TODO 支付秘钥
//			return "000000000000";
//		}
//		log.error(" getKey method error  payway is "+payway+" flag is "+flag);
//		return null;
//	}
    /**
     * 比较支付数据较密验签是否一致
     * @param pay
     * @param attch
     * @return
     */
	public static boolean verify(PayInfo pay, String attch) {
		boolean b= pay!=null && attch != null && attch.equals(pay.getContentMd5());
		log.info("verify :比较数据加密串=attch="+attch+" =="+b+"= pay="+pay);
		return b;
	}
	
	/**
	 * 支付的数据加密验签
	 * @param pay
	 * @return
	 */
	public static String contentMd5(PayInfo pay){
		SortedMap<String,String> m = new TreeMap<String,String>();
		m.put("TotalPrice",""+ pay.getTotalPrice());//总金额
		m.put("OutTradeNo", ""+pay.getOutTradeNo());//交易流水号
		m.put("PayPrice",""+ pay.getPayPrice());//支付金额
		m.put("UserId", pay.getUserId());//用户
		
		String sign = md5Sign("UTF-8",m,"");
		return sign;
	}
	
	/**
	 * 生成sign
	 * @param characterEncoding
	 * @param packageParams
	 * @param API_KEY
	 * @return
	 */
	public static String md5Sign(String characterEncoding, SortedMap<String, String> packageParams, String API_KEY) {
		StringBuffer sb = new StringBuffer();
		Set es = packageParams.entrySet();
		Iterator it = es.iterator();
		while (it.hasNext()) {
			Map.Entry entry = (Map.Entry) it.next();
			String k = (String) entry.getKey();
			String v = String.valueOf(entry.getValue());
			if (null != v && !"".equals(v) && !"sign".equals(k) && !"key".equals(k)) {
				sb.append(k + "=" + v + "&");
			}
		}
		sb.append("key=" + API_KEY);
		
		log.info("md5Sign method run MD5Encode str ===="+sb.toString());
		String sign = MD5Util.MD5Encode(sb.toString(), characterEncoding).toUpperCase();
		return sign;
	}

	/**
	 * 取出一个指定长度大小的随机正整数.
	 * @param length
	 *            int 设定所取出随机数的长度。length小于11
	 * @return int 返回生成的随机数。
	 */
	public static int buildRandom(int length) {
		int num = 1;
		double random = Math.random();
		if (random < 0.1) {
			random = random + 0.1;
		}
		for (int i = 0; i < length; i++) {
			num = num * 10;
		}
		return (int) ((random * num));
	}

	/**
	 * 获取当前时间 yyyyMMddHHmmss
	 * 
	 * @return String
	 */
	public static String getCurrTime() {
		Date now = new Date();
		SimpleDateFormat outFormat = new SimpleDateFormat("yyyyMMddHHmmss");
		String s = outFormat.format(now);
		return s;
	}

	/**
	 * 获取一个随机字符串
	 * @return
	 */
	public static String getRandomNum(){
		String currTime = getCurrTime();
		String strTime = currTime.substring(8, currTime.length());
		String strRandom = buildRandom(4) + "";
		return strTime + strRandom;
	}
	
//	/**
//	 * 根据渠道获取结果通知路径
//	 * @param channel_id
//	 * @return
//	 */
//	public static String getNotify_url(String channel_id) {
//		// TODO Auto-generated method stub
//		String url = "";
//		if(String.valueOf(Constant.ChannelId.WeChatChannel.ordinal()).equals(channel_id)){
//			url = "http://ec.appcan.cn/api/payment/nologin/backRcvResp";
//		}
//		
//		return url;
//	}
    
	/**
	 * 根据渠道获取支付类型
	 * @param channel_id
	 * @param payway
	 * @return
	 */
	public static String getTrade_type(String channel_id,String payway) {
		
		String tradetype = "JSAPI";
		if(String.valueOf(Constant.ChannelId.USER.ordinal()).equals(channel_id)
		    &&Constant.PayWay.WeChatPay.name().equals(payway)){
			tradetype = "JSAPI";//JSAPI，NATIVE，APP，
		}
		
		
		return tradetype;
	}

	/**
	 * 请求xml报文
	 * @param packageParams
	 * @return
	 */
	public static String getRequestXml(SortedMap<String, String> packageParams) {
		StringBuffer sb = new StringBuffer();
		sb.append("<xml>");
		Set es = packageParams.entrySet();
		Iterator it = es.iterator();
		while (it.hasNext()) {
			Map.Entry entry = (Map.Entry) it.next();
			String k = (String) entry.getKey();
			String v = (String) entry.getValue();
			if ("attach".equalsIgnoreCase(k) || "body".equalsIgnoreCase(k) || "sign".equalsIgnoreCase(k)) {
				sb.append("<" + k + ">" + "<![CDATA[" + v + "]]></" + k + ">");
			} else {
				sb.append("<" + k + ">" + v + "</" + k + ">");
			}
		}
		sb.append("</xml>");
		return sb.toString();
	}
	
	/**
	 * 请求xml报文
	 * @param packageParams
	 * @return
	 */
	public static String doXMLParse(Map<String, String> packageParams) {
		StringBuffer sb = new StringBuffer();
		sb.append("<xml>");
		Set es = packageParams.entrySet();
		Iterator it = es.iterator();
		while (it.hasNext()) {
			Map.Entry entry = (Map.Entry) it.next();
			String k = (String) entry.getKey();
			String v = (String) entry.getValue();
			if ("attach".equalsIgnoreCase(k) || "body".equalsIgnoreCase(k) || "sign".equalsIgnoreCase(k)) {
				sb.append("<" + k + ">" + "<![CDATA[" + v + "]]></" + k + ">");
			} else {
				sb.append("<" + k + ">" + v + "</" + k + ">");
			}
		}
		sb.append("</xml>");
		return sb.toString();
	}
}
