package com.zyhao.openec.util;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Map;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.json.JSONObject;

import com.zyhao.openec.order.entity.PayInfo;

public class WechatPayUtil {
	private static final Log log = LogFactory.getLog(WechatPayUtil.class);
	
	/**
	 * 将支付结果通用通知转成bean
	 * @param map
	 * @param pay
	 * @return
	 * @throws Exception
	 */
    public static PayInfo convertMapToBean(Map<String,String> map,PayInfo pay) throws Exception {
		
		JSONObject json = new JSONObject();
		if(pay == null){
			log.error(" convertWechatMapToBean method run error :"+String.valueOf(map.get("out_trade_no")));
		    throw new Exception("cannot find pay info by out_trade_no "+String.valueOf(map.get("out_trade_no")));
		}
		
		if(map.containsKey("return_code")){
		    pay.setReturnCode(String.valueOf(map.get("return_code")));
		}
		if(map.containsKey("return_msg")){
		    pay.setReturnMsg(String.valueOf(map.get("return_msg")));
		}
		
		if(map.containsKey("appid")){
			pay.setSellerId(String.valueOf(map.get("appid")));
		}
		if(map.containsKey("mch_id")){
		    pay.setSellerNum(String.valueOf(map.get("mch_id")));
		}
		if(map.containsKey("device_info")){
		    pay.setDeviceInfo(String.valueOf(map.get("device_info")));
		}
		if(map.containsKey("nonce_str")){
			json.put("nonce_str",String.valueOf(map.get("nonce_str")));
		}
		if(map.containsKey("sign")){
		    pay.setSign(String.valueOf(map.get("sign")));
		}
		if(map.containsKey("result_code")){
		    pay.setResultCode(String.valueOf(map.get("result_code")));
		}
		if(map.containsKey("err_code")){
		    pay.setErrCode(String.valueOf(map.get("err_code")));
		}
		if(map.containsKey("err_code_des")){
		    pay.setErrCodeDes(String.valueOf(map.get("err_code_des")));
		}
		if(map.containsKey("openid")){
			pay.setBuyerId(String.valueOf(map.get("openid")));
		}
		if(map.containsKey("is_subscribe")){
			json.put("is_subscribe",String.valueOf(map.get("is_subscribe")));
		}
		if(map.containsKey("trade_type")){
		    pay.setTradeType(String.valueOf(map.get("trade_type")));
		}
		if(map.containsKey("bank_type")){
		    pay.setBankNo(String.valueOf(map.get("bank_type")));
		}
		if(map.containsKey("total_fee")){
			json.put("total_fee",String.valueOf(map.get("total_fee")));
		}
		if(map.containsKey("settlement_total_fee")){
			json.put("settlement_total_fee",String.valueOf(map.get("settlement_total_fee")));
		}
		if(map.containsKey("fee_type")){
			json.put("fee_type",String.valueOf(map.get("fee_type")));
		}
		if(map.containsKey("cash_fee")){
			json.put("cash_fee",String.valueOf(map.get("cash_fee")));
		}
		if(map.containsKey("cash_fee_type")){
			json.put("cash_fee_type",String.valueOf(map.get("cash_fee_type")));
		}
		if(map.containsKey("coupon_fee")){
			json.put("coupon_fee",String.valueOf(map.get("coupon_fee")));
		}
		if(map.containsKey("coupon_count")){
			json.put("coupon_count",String.valueOf(map.get("coupon_count")));
		}
		if(map.containsKey("transaction_id")){
		    pay.setTradeNo(String.valueOf(map.get("transaction_id")));
		}
		if(map.containsKey("attach")){
		    pay.setAttch(String.valueOf(map.get("attach")));
		}
		if(map.containsKey("time_end")){
            SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMddHHmmss");                
            Date date = sdf.parse(String.valueOf(map.get("time_end"))); 
		    pay.setGmtPayment(date.getTime());//20140903131540
		}
		pay.setExt(json.toString());
		return pay;
	}
//    
//    /**
//     * 验证支付结果通用通知加密串
//     * @param characterEncoding
//     * @param packageParams
//     * @param API_KEY
//     * @return
//     */
//    public static boolean isTenpaySign(String characterEncoding, Map<Object, Object> packageParams, String API_KEY) {
//		StringBuffer sb = new StringBuffer();
//		Set es = packageParams.entrySet();
//		Iterator it = es.iterator();
//		while(it.hasNext()) {
//			Map.Entry entry = (Map.Entry)it.next();
//			String k = (String)entry.getKey();
//			String v = (String)entry.getValue();
//			if(!"sign".equals(k) && null != v && !"".equals(v)) {
//				sb.append(k + "=" + v + "&");
//			}
//		}
//		
//		sb.append("key=" + API_KEY);
//			
//		//算出摘要
//		String mysign = MD5Util.MD5Encode(sb.toString(), characterEncoding).toLowerCase();
//		String tenpaySign = ((String)packageParams.get("sign")).toLowerCase();
//		
//		return tenpaySign.equals(mysign);
//		//return true;
//	}
//    
//    /**
//     * 获取微信支付帐号
//     * @param channel_id
//     * @param flag
//     * @return
//     */
//    public static String getKey(String channel_id, String flag) {
//		if(Constent.ChannelId.WeChatChannel.name().equals(channel_id)){
//			if(flag.equalsIgnoreCase("appid")){
//			    return Constent.APP_ID;
//			}
//			if(flag.equalsIgnoreCase("mch_id")){
//			    return Constent.MCH_ID;
//			}
//			if(flag.equalsIgnoreCase("key")){
//			    return Constent.API_KEY;
//			}
//		}
//		log.error(" getKey method error  channel_id is "+" flag is "+flag);
//		return null;
//	}
//    /**
//     * 比较支付数据较密验签是否一致
//     * @param pay
//     * @param attch
//     * @return
//     */
//	public static boolean verify(PayInfo pay, String attch) {
//		return pay!=null && attch != null && attch.equals(pay.getContentMd5());
//	}
//	
//	/**
//	 * 支付的数据加密验签
//	 * @param pay
//	 * @return
//	 */
//	public static String contentMd5(PayInfo pay){
//		SortedMap<Object,Object> m = new TreeMap<Object,Object>();
//		m.put("TotalPrice", pay.getTotalPrice());//总金额
//		m.put("OutTradeNo", pay.getOutTradeNo());//交易流水号
//		m.put("PayPrice", pay.getPayPrice());//支付金额
//		m.put("UserId", pay.getUserId());//用户
//		
//		String sign = createSign("UTF-8",m,getKey(pay.getChannelId(),"key"));
//		return sign;
//	}
//	
//	/**
//	 * 生成sign
//	 * @param characterEncoding
//	 * @param packageParams
//	 * @param API_KEY
//	 * @return
//	 */
//	public static String createSign(String characterEncoding, SortedMap<Object, Object> packageParams, String API_KEY) {
//		StringBuffer sb = new StringBuffer();
//		Set es = packageParams.entrySet();
//		Iterator it = es.iterator();
//		while (it.hasNext()) {
//			Map.Entry entry = (Map.Entry) it.next();
//			String k = (String) entry.getKey();
//			String v = String.valueOf(entry.getValue());
//			if (null != v && !"".equals(v) && !"sign".equals(k) && !"key".equals(k)) {
//				sb.append(k + "=" + v + "&");
//			}
//		}
//		sb.append("key=" + API_KEY);
//		String sign = MD5Util.MD5Encode(sb.toString(), characterEncoding).toUpperCase();
//		return sign;
//	}
//	
//	
//
//	/**
//	 * 取出一个指定长度大小的随机正整数.
//	 * @param length
//	 *            int 设定所取出随机数的长度。length小于11
//	 * @return int 返回生成的随机数。
//	 */
//	public static int buildRandom(int length) {
//		int num = 1;
//		double random = Math.random();
//		if (random < 0.1) {
//			random = random + 0.1;
//		}
//		for (int i = 0; i < length; i++) {
//			num = num * 10;
//		}
//		return (int) ((random * num));
//	}
//
//	/**
//	 * 获取当前时间 yyyyMMddHHmmss
//	 * 
//	 * @return String
//	 */
//	public static String getCurrTime() {
//		Date now = new Date();
//		SimpleDateFormat outFormat = new SimpleDateFormat("yyyyMMddHHmmss");
//		String s = outFormat.format(now);
//		return s;
//	}
//
//	/**
//	 * 获取一个随机字符串
//	 * @return
//	 */
//	public static String getRandomNum(){
//		String currTime = getCurrTime();
//		String strTime = currTime.substring(8, currTime.length());
//		String strRandom = buildRandom(4) + "";
//		return strTime + strRandom;
//	}
//	
//	/**
//	 * 根据渠道获取结果通知路径
//	 * @param channel_id
//	 * @return
//	 */
//	public static String getNotify_url(String channel_id) {
//		// TODO Auto-generated method stub
//		String url = "";
//		if(Constent.ChannelId.WeChatChannel.name().equals(channel_id)){
//			url = "http://www.baidu.com";
//		}
//		
//		return url;
//	}
//    
//	/**
//     * 根据渠道获取支付类型
//     * @param channel_id
//     * @return
//     */
//	public static String getTrade_type(String channel_id) {
//		
//		String tradetype = "";
//		if(Constent.ChannelId.WeChatChannel.name().equals(channel_id)){
//			tradetype = "NATIVE";//JSAPI，NATIVE，APP，
//		}
//		return tradetype;
//	}
//
//	/**
//	 * 请求xml报文
//	 * @param packageParams
//	 * @return
//	 */
//	public static String getRequestXml(SortedMap<Object, Object> packageParams) {
//		StringBuffer sb = new StringBuffer();
//		sb.append("<xml>");
//		Set es = packageParams.entrySet();
//		Iterator it = es.iterator();
//		while (it.hasNext()) {
//			Map.Entry entry = (Map.Entry) it.next();
//			String k = (String) entry.getKey();
//			String v = (String) entry.getValue();
//			if ("attach".equalsIgnoreCase(k) || "body".equalsIgnoreCase(k) || "sign".equalsIgnoreCase(k)) {
//				sb.append("<" + k + ">" + "<![CDATA[" + v + "]]></" + k + ">");
//			} else {
//				sb.append("<" + k + ">" + v + "</" + k + ">");
//			}
//		}
//		sb.append("</xml>");
//		return sb.toString();
//	}
}
