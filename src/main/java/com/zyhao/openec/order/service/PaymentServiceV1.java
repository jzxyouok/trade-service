package com.zyhao.openec.order.service;

import java.io.BufferedOutputStream;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.SortedMap;
import java.util.TreeMap;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang3.time.DateFormatUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.client.loadbalancer.LoadBalanced;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
//import org.springframework.security.oauth2.client.OAuth2RestTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestTemplate;

import com.zyhao.openec.order.entity.PayInfo;
import com.zyhao.openec.order.pojo.PayPoJo;
import com.zyhao.openec.order.repository.PayInfoRepository;
import com.zyhao.openec.pojo.RepEntity;
import com.zyhao.openec.pojo.ReturnObj;
import com.zyhao.openec.pojo.SysConfigInfo;
import com.zyhao.openec.util.CommonUtil;
import com.zyhao.openec.util.Constant;
import com.zyhao.openec.util.DateUtil;
import com.zyhao.openec.util.EpayCore;
import com.zyhao.openec.util.EpayMD5;
import com.zyhao.openec.util.HttpUtil;
import com.zyhao.openec.util.O2MUtil;
import com.zyhao.openec.util.PayUtil;
import com.zyhao.openec.util.QrcbConstant;
import com.zyhao.openec.util.Utils;
import com.zyhao.openec.util.XMLUtil;

/**
 * 
 * Title:PaymentServiceV1 
 * Desc: 支付服务功能
 * @author Administrator
 * @date 2016年10月14日 下午3:29:38
 */
@Service
public class PaymentServiceV1 {
	private final static Log log = LogFactory.getLog(PaymentServiceV1.class);
//	private OAuth2RestTemplate oAuth2RestTemplate;
	private RestTemplate restTemplate;
	@Autowired
	private QrcbConstant qrcbConstant;
	@Autowired
	private PayPoJo payPojo;
	public static String status_0 = "0";//0-待支付 1-已支付待发货  2-待收货 3-已签收 4-交易完成5-交易取消
	public static String status_4 = "4";//0-待支付 1-已支付待发货  2-待收货 3-已签收 4-交易完成5-交易取消
	public static String status_6 = "6";//0-待支付 1-已支付待发货  2-待收货 3-已签收 4-交易完成5-交易取消 6-前台通知支付确认
	
    @Autowired
    public PaymentServiceV1(
//    	@LoadBalanced OAuth2RestTemplate oAuth2RestTemplate,
        @LoadBalanced RestTemplate normalRestTemplate
        ) {
//        this.oAuth2RestTemplate = oAuth2RestTemplate;
        this.restTemplate = normalRestTemplate;
    }
	@Autowired
	private PayInfoRepository payInfoDao;
	@Autowired
	private OrderService orderService;
	
    public ReturnObj createPayInfo(PayInfo pay) throws Exception {
		ReturnObj returnObj = new ReturnObj();
		if(pay == null){
			log.error(" createPayInfo method has an error :"+DateUtil.getDefaultDate()+" pay is null,return error ");
		    returnObj.setCode(Constant.error);
		    returnObj.setMsg(" pay is null ");
		    return returnObj;
		}
		try{
			
		    log.info("createPayInfo method params pay is "+pay.toString());
		    //生成数据验签
		    String contentMd5 = PayUtil.contentMd5(pay);
		    pay.setContentMd5(contentMd5);
		    
		    savePayInfo(pay);
		    returnObj.setCode(Constant.success);
		    returnObj.setMsg(" save pay info success");
		    return returnObj;
		}catch(Exception ex){
			//------异常处理--------
			ex.printStackTrace();
			log.error(ex);
		}
		returnObj.setCode(Constant.error);
	    returnObj.setMsg(" cannot save pay info");
	    return returnObj;
    }
	
	/**
	 * 前台通知
	 * @param pay
	 * @return
	 * @throws Exception 
	 */
	public ReturnObj frontNotify(HttpServletRequest request,PayInfo pay) throws Exception {
	
		log.info(" frontNotify method begin update params is "+pay.toString());
		
		Map<String, String[]> authenticatedUser = getAuthenticatedUser();
		String authenticatedUserId = authenticatedUser.get("Session_id")[0];
		
		log.info(" frontNotify method find payinfo ByOutTradeNo error, find payinfo authenticatedUserId="+authenticatedUserId+" pay.getOutTradeNo()="+pay.getOutTradeNo());
		
		
//		if(authenticatedUserId != null && !"".equals(String.valueOf(authenticatedUserId))){
			PayInfo findByOutTradeNo = payInfoDao.findByOutTradeNo(pay.getOutTradeNo());
			if(findByOutTradeNo == null){
				log.error(" frontNotify method find payinfo ByOutTradeNo error,cannot find payinfo ");
				ReturnObj returnObj = new ReturnObj();
				returnObj.setCode(Constant.error);
				returnObj.setMsg("cannot find payinfo from db");
				return returnObj;
			}
			
			
			
			if(findByOutTradeNo.getPayWay().equals(Constant.PayWay.QrcbPay.name())){
				Map<String,String> requestParams = O2MUtil.Split(pay.getDetail().replace("?", "&"));
				requestParams.put("outTradeNo", null);
				
				String key = getSysConfigInfo(findByOutTradeNo.getBusinessId(),findByOutTradeNo.getPayWay()).getAppkey();
				String value = EpayCore.createLinkString(EpayCore.paraFilter(requestParams));
				String sign = EpayMD5.sign(value,key,"UTF-8");
				log.error(" frontNotify method find payinfo ByOutTradeNo error,验证数据加解密失败 key="+key+" requestParams="+requestParams);

				if(!EpayMD5.validateSign(value, sign, key, "UTF-8"))//验证数据加解密
				{
					log.error(" frontNotify method find payinfo ByOutTradeNo error,验证数据加解密失败 requestParams="+requestParams);

					ReturnObj returnObj = new ReturnObj();
				    returnObj.setCode(Constant.error);
				    returnObj.setMsg("验证数据加解密失败");
					return returnObj;
			    }
				
			}
			
			
			
			
			
		    findByOutTradeNo.setDetail(pay.getDetail());
		    findByOutTradeNo.setTradeNo(pay.getTradeNo());
		    findByOutTradeNo.setFrontNotifyStatus(pay.getFrontNotifyStatus());
		    payInfoDao.saveAndFlush(findByOutTradeNo);
		    
		    boolean modifyOrderStatus = Boolean.valueOf(payPojo.getMustUseFrontNotifyForOrderStatus());
		    boolean notifyInventory = Boolean.valueOf(payPojo.getMustUseFrontNotifyForInventory());
		    String status = status_6;
			String orderstatus = pay.getPayStatus();
		    orderService.editOrderPayStatus(pay.getOutTradeNo(),status,orderstatus, modifyOrderStatus, notifyInventory);
		    
		    ReturnObj returnObj = new ReturnObj();
		    returnObj.setCode(Constant.success);
		    returnObj.setMsg("success");
			return returnObj;
//		}else{
//			log.error(" frontNotify method find payinfo userid is diffrent ");
//			ReturnObj returnObj = new ReturnObj();
//			returnObj.setCode(Constant.error);
//			returnObj.setMsg("payinfo userid is diffrent");
//			return returnObj;
//		}
	}
	
	/**
	 * 认证平台
	 * @return
	 */
//	public User getAuthenticatedUser() {
//        return oAuth2RestTemplate.getForObject("http://user-service/uaa/v1/me", User.class);
//	}
	@Autowired  
    HttpServletRequest request;
	/**
	 * 认证平台
	 * @return
	 */
	public Map<String,String[]> getAuthenticatedUser() {
		return request.getParameterMap();
	}
	
	/**
	 * 保存，需要认证
	 * @param pay
	 * @return
	 */
	@Transactional
	public PayInfo savePayInfo(PayInfo pay) {
		log.info(" savePayInfo method login begin auth ");
		
		return payInfoDao.save(pay);
	}
	
	/**
	 * 更新支付，不需要认证
	 * @param pay
	 * @return
	 */
	@Transactional
	public PayInfo updatePayInfo(PayInfo pay) {
		log.info(" updatePayInfo method begin pay="+pay.toString());
		return payInfoDao.saveAndFlush(pay);
	}
	/**
	 * 支付结果通知返回给支付平台结果
	 * @param payInfo
	 * @param response
	 * @return
	 */
	public String backRcvResp(PayInfo payInfo, HttpServletResponse response) {
		String resXml = null;
		if(Constant.PayWay.WeChatPay.name().equals(payInfo.getPayWay())){//微信
		    resXml = "<xml><return_code><![CDATA[SUCCESS]]></return_code><return_msg><![CDATA[OK]]></return_msg></xml> ";
		
			//处理业务完毕
			try {
				if(resXml != null){ //-----需要输出
					BufferedOutputStream out = new BufferedOutputStream(response.getOutputStream());
					out.write(resXml.getBytes());
					out.flush();
					out.close();
				}
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		if(Constant.PayWay.QrcbPay.name().equals(payInfo.getPayWay())){//
			return "success";//default answer
		}
		
		
		
		return "success";//default answer
	}
	
	/**
	 * 查询支付单
	 * @param outTradeNo
	 * @return
	 */
	public PayInfo getpayInfoByOutTradeNo(String outTradeNo){
		return payInfoDao.findByOutTradeNo(outTradeNo);
	}
	
	/**
	 * 微信统一预下单接口
	 * @param out_trade_no
	 * @param channel_id
	 * @param pay_way
	 * @param body
	 * @return
	 * @throws Exception 
	 */
	public ReturnObj getPrePay(String out_trade_no,
			String channel_id,String businessId,String pay_way,String body,String openid) throws Exception{
//		String appid = PayUtil.getKey(pay_way,"appid");
//        String mch_id = PayUtil.getKey(pay_way,"mch_id"); // 商业号
//        String key = PayUtil.getKey(pay_way,"key"); // key
        
//        String appid = ""+DataDicUtil.getAllFields(businessId).get("AppID");
//        String mch_id = ""+DataDicUtil.getAllFields(businessId).get("MechID"); // 商业号
//        String key = ""+DataDicUtil.getAllFields(businessId).get("ApiKey"); // key
        SysConfigInfo sysConfigInfo = getSysConfigInfo(businessId,pay_way);
        String appid = sysConfigInfo.getAppid();
        String mch_id = sysConfigInfo.getMechid();
        String key = sysConfigInfo.getAppkey();
        
        String nonce_str = PayUtil.getRandomNum();
        String spbill_create_ip = Utils.localIp();// Constant.CREATE_IP;
        
        String trade_type = PayUtil.getTrade_type(channel_id,pay_way);
        
        SortedMap<String,String> packageParams = new TreeMap<String,String>();
        log.info(" getPrePay method packageParams packageParams="+packageParams.toString());
		try {
			
			//根据大out_trade_no获取金额
		    PayInfo pay =payInfoDao.findByOutTradeNo(out_trade_no);
//		    String notify_url = DataDicUtil.getAllFields(businessId).get("BackNotifyRedirectUri")
//		    		+"/"+channel_id+"/"+pay_way+"/"+pay.getContentMd5();
		    String notify_url = sysConfigInfo.getBackNotifyRedirectUri()+"/"+channel_id+"/"+pay_way+"/"+pay.getContentMd5();

//			if(pay == null){
//				log.error(" getPrePay method cannot find order out_trade_no="+out_trade_no);
//				
//				ReturnObj returnObj = new ReturnObj();
//				returnObj.setCode(Constant.error);
//				returnObj.setMsg("cannot find order");
//				return returnObj;
//			}
			Integer price = pay.getPayPrice();
			//微信支付已分为单位，库里的单位是元 * 100
			//long  order_price = CommonUtil.longMultiply(price, 100);
			
			packageParams.put("appid", appid);//公众账号ID 微信分配的公众账号ID（企业号corpid即为此appId） 
			packageParams.put("mch_id", mch_id);//商户号  微信支付分配的商户号
			packageParams.put("nonce_str", nonce_str);//随机字符串，不长于32位
			packageParams.put("body", body);//商品或支付单简要描述
			packageParams.put("out_trade_no", out_trade_no);//商户系统内部的订单号,32个字符内、可包含字母, 其他说明见商户订单号
			packageParams.put("total_fee", String.valueOf(price));//订单总金额，单位为分
			packageParams.put("spbill_create_ip", spbill_create_ip);//APP和网页支付提交用户端ip，Native支付填调用微信支付API的机器IP。
			packageParams.put("notify_url", notify_url);//接收微信支付异步通知回调地址，通知url必须为直接可访问的url，不能携带参数。
			packageParams.put("trade_type", trade_type);//交易类型  取值如下：JSAPI，NATIVE，APP，
			packageParams.put("attach", pay.getContentMd5());//数据包
			if("JSAPI".equals(trade_type)){
				packageParams.put("openid", openid);//trade_type=JSAPI，此参数必传，用户在商户appid下的唯一标识。
			}
			String sign = PayUtil.md5Sign("UTF-8", packageParams,key);
			packageParams.put("sign", sign);//签名
			
			String requestXML = PayUtil.getRequestXml(packageParams);
			log.info("getPrePay method  request weixin pay param is:"+requestXML);
			
			String resXml = HttpUtil.postData(sysConfigInfo.getUfdoderUrl(), requestXML);
			log.info("getPrePay method  response weixin result is :"+resXml);
			if(resXml==null){
				
				ReturnObj returnObj = new ReturnObj();
				returnObj.setCode(Constant.error);
				returnObj.setMsg("请求微信服务器失败,需要检查服务器是否开放请求地址");
				return returnObj;
			}
			
			SortedMap<String,String> map = XMLUtil.doXMLParse(resXml);
			
			String return_code = (String) map.get("return_code");//返回的状态值
			String result_code = (String) map.get("result_code");//返回的处理结果值 俩个都是success的时候才会返回二维码的code url
			if(return_code.equalsIgnoreCase("SUCCESS")){
				if(result_code.equalsIgnoreCase("SUCCESS")){
					
					ReturnObj returnObj = new ReturnObj();
					returnObj.setCode(Constant.success);
					returnObj.setMsg("success");
					
					String prepay_sign = PayUtil.md5Sign("UTF-8", map,key);
					if(!prepay_sign.equals(map.get("sign"))){
						returnObj.setCode(Constant.error);
						returnObj.setMsg(" weixin prepay sign is error");
						return returnObj;
					}else{
						
						TreeMap<String,String> dataMap = new TreeMap<String,String>();
						dataMap.put("appId",appid);
						dataMap.put("timeStamp",""+System.currentTimeMillis()/1000);//时间戳，自1970年以来的秒数
						dataMap.put("nonceStr",PayUtil.getRandomNum());
						dataMap.put("package", "prepay_id="+map.get("prepay_id"));
						dataMap.put("signType", "MD5");
						String md5Sign = PayUtil.md5Sign("UTF-8", dataMap,key);
						dataMap.put("paySign",md5Sign);
						log.info("getPrePay method  response dataMap==="+dataMap);
						returnObj.setData(dataMap);
					}
					returnObj.setMsg(out_trade_no);
					return returnObj;
				}else{
					String err_code_des = (String) map.get("err_code_des");
					ReturnObj returnObj = new ReturnObj();
					returnObj.setCode(Constant.error);
					returnObj.setMsg(err_code_des);
					returnObj.setMsg(out_trade_no);
					return returnObj;
				}
			}else{
				String return_msg = (String) map.get("return_msg");
				
				log.info("getPrePay method  response weixin error:"+map);
				ReturnObj returnObj = new ReturnObj();
				returnObj.setCode(Constant.error);
				returnObj.setMsg("调用微信支付出现错误:"+return_msg);
				return returnObj;
			}
		} catch (Exception e) {
			log.info("getPrePay method request weixin 统一下单出现错误:"+e);
			e.printStackTrace();
			ReturnObj returnObj = new ReturnObj();
			returnObj.setCode(Constant.error);
			returnObj.setMsg("调取微信统一下单出现错误");
			return returnObj;
		}
		
		
//		<xml><return_code><![CDATA[SUCCESS]]></return_code>
//		<return_msg><![CDATA[OK]]></return_msg>
//		<appid><![CDATA[wx8a52391c7e8c5617]]></appid>
//		<mch_id><![CDATA[1263302901]]></mch_id>
//		<nonce_str><![CDATA[cNFNoO1QKQsvLfvK]]></nonce_str>
//		<sign><![CDATA[6EAF85C231515F72C33272EBE1B564DC]]></sign>
//		<result_code><![CDATA[FAIL]]></result_code>
//		<err_code><![CDATA[OUT_TRADE_NO_USED]]></err_code>
//		<err_code_des><![CDATA[商户订单号重复]]></err_code_des>
//		</xml>

		
	}
	public ReturnObj getPrePayQrcb(String out_trade_no,
			String channel_id,String pay_way,String body,String businessId) throws Exception{
		ReturnObj returnObj = new ReturnObj();
		
		//根据大out_trade_no获取金额
	    PayInfo pay =payInfoDao.findByOutTradeNo(out_trade_no);
//	    String notify_url = DataDicUtil.getAllFields(channel_id).get("BackNotifyRedirectUri")
//	    		+"/"+channel_id+"/"+pay_way+"/"+pay.getContentMd5();
//	
	    SysConfigInfo sysConfigInfo = getSysConfigInfo(businessId,pay_way);
	    String notify_url = sysConfigInfo.getBackNotifyRedirectUri()+channel_id+"/"+pay_way+"/"+pay.getContentMd5();
	    String partner = sysConfigInfo.getMechid();//"12039175";
	    String return_url = sysConfigInfo.getFrontNotifyRedirectUri()+out_trade_no;
	    String key = sysConfigInfo.getAppkey();
	    
	    
	    
	  //  String notify_url = qrcbConstant.getNotify_url()+channel_id+"/"+pay_way+"/"+pay.getContentMd5();//"https://sqyx.qrcb.com.cn/api/payment/nologin/backRcvResp/"+channel_id+"/"+pay_way+"/"+pay.getContentMd5();
		Integer price = pay.getPayPrice();
		double total_price = CommonUtil.doubleDivide(price, 100,2);
		String url = sysConfigInfo.getUfdoderUrl()+qrcbConstant.getUrl();//"https://epay.qrcb.com.cn:50080/epaygate/mb/Wirelesspaygate.htm";
		String show_url = qrcbConstant.getShow_url();//"https://sqyx.qrcb.com.cn";
		
		String ip = "127.0.0.1";
		
		
		String time_start = DateFormatUtils.format(new Date(), "yyyyMMdd HH:mm:ss");
	    //	String time_expire = "";
		//String transport_fee = "";
		//String product_fee = "";
		String attach = pay.getContentMd5();
		
		//String trade_details = partner+"^"+total_price+"^"+0+"^"+body+"^"+out_trade_no+"^0002^1"; //商户号^交易金额^手续费金额^商品描述^订单号^交易类型^是否计算手续费
		
		
		SortedMap<String,String> map = new TreeMap<String,String>();
		map.put("service","pay_service");
		map.put("service_version","1.0");
		map.put("input_charset","UTF-8");
		map.put("sign_type","MD5");
		//map.put("sign","");
		map.put("partner",partner);
		map.put("out_trade_no",out_trade_no);
		map.put("subject",body);
		map.put("show_url",show_url);
		map.put("body",body);
		map.put("total_fee",""+total_price);
		map.put("fee_type","1");
		map.put("spbill_create_ip",ip);
		map.put("time_start",time_start);//yyyyMMdd hh:mm:ss
		map.put("trade_mode","0002");
		map.put("attach",attach);
		map.put("notify_url",notify_url);
		map.put("return_url",return_url);
		map.put("trans_channel","mb");//pc-电脑 mb-手机
		//map.put("batch_num","1");//交易笔数  
		//map.put("trade_details",trade_details);//第一笔交易#第二笔交易#  商户号^交易金额^手续费金额^商品描述^订单号^交易类型^是否计算手续费
		
		
		
		String value = EpayCore.createLinkString(EpayCore.paraFilter(map));
		String sign = EpayMD5.sign(value,key,"UTF-8");
		map.put("sign", sign);//签名
		
		//String requestXML = PayUtil.doXMLParse(map);
		//log.info("getPrePayQrcb method  request ty pay param is:"+requestXML);
		
		 //待请求参数数组
        List<String> keys = new ArrayList<String>(map.keySet());

        StringBuffer sbHtml = new StringBuffer();

        sbHtml.append("<form id=\"epaysubmit\" name=\"epaysubmit\" action=\"" + url
                         + "\" method=\"post\">");

        for (int i = 0; i < keys.size(); i++) {
            String name = (String) keys.get(i);
            String v = (String) map.get(name);

            sbHtml.append("<input type=\"hidden\" name=\"" + name + "\" value=\"" + v + "\"/>");
        }
        //submit按钮控件请不要含有name属性
        sbHtml.append("<input type=\"submit\" value=\"确定\" style=\"display:none;\"></form>");
        sbHtml.append("<script>document.getElementById('epaysubmit').submit();</script>");
     
		returnObj.setCode(Constant.success);
		//String data = "{\"url\":\""+url+"\",\"params\":\""+sbHtml.toString()+"\"}";
		returnObj.setData(sbHtml.toString());
		returnObj.setMsg(out_trade_no);
		return returnObj;
	}
	/**
	 * 支付成功,通知订单服务
	 * @param payInfo
	 * @throws Exception 
	 */
	public void notifyOrder(PayInfo payInfo) throws Exception {
		//restTemplate.getForObject("http://order-service/nologin/edit/"+id +"?status=4"+"&orderstatus="+payInfo.getPayStatus(),Object.class);		
		
		boolean modifyOrderStatus = Boolean.valueOf(payPojo.getMustUseBackNotifyForOrderStatus());
		boolean notifyInventory = Boolean.valueOf(payPojo.getMustUseBackNotifyForInventory());
	    String status = status_4;
		String orderstatus = payInfo.getPayStatus();
	    orderService.editOrderPayStatus(payInfo.getOutTradeNo(),status,orderstatus, modifyOrderStatus, notifyInventory);
	    
	}
	
	
	public SysConfigInfo getSysConfigInfo(String businessId,String payWay) {
//		ResponseEntity<Object> forEntity = restTemplate.getForEntity("http://customer-service/uaa/nologin/getSysConfigInfo?Session_businessId="+businessId+"&BusinessPayWay="+payWay,Object.class,Object.class);
		try{
			ResponseEntity<RepEntity> forEntity = restTemplate.getForEntity("http://customer-service/uaa/nologin/getSysConfigInfo?Session_businessId="+businessId+"&BusinessPayWay="+payWay,RepEntity.class);
            List<SysConfigInfo> sysConfigInfo = forEntity.getBody().getSysConfigInfo();	
            if(sysConfigInfo != null && sysConfigInfo.size() > 0){
            	return sysConfigInfo.get(0);
            }
		}catch(Exception ex){
			log.info("==========================1111111111111111111111===============");
			
		}

		
		
//		RepEntity body = forEntity.getBody();
//		if("0".equals(body.getStatus())){
//			return (SysConfigInfo)body.getData();
//		}
		return null;
	}
	
	public PayInfo getpayInfoByOutTradeNoAndChannelId(String out_trade_no, String channel_id) {
		PayInfo pay = payInfoDao.findByOutTradeNoAndChannelId(out_trade_no,channel_id);
		return pay;
	}

	public Page<PayInfo> getpayInfoNoPayment(String id, Pageable pageRequest) {
		return payInfoDao.findByPayStatusAndUserId("0",id,pageRequest);
	}
	
	public PayInfo getpayInfoById(String id) {
		PayInfo pay = payInfoDao.findOne(id);
		return pay;
	}
	
//	public static void main(String[] args) {
//		double doubleMultiply = CommonUtil.doubleDivide(1000, 120,2);
//		System.out.println(doubleMultiply);
//		String format = DateFormatUtils.format(new Date(), "yyyyMMdd HH:mm:ss");
//		System.out.println(format);
//        
//		ReturnObj returnObj = new ReturnObj();
//		
//	
//	    String channel_id = "1";
//	    String pay_way = "QrcbPay";
//	    String getContentMd5 = "1";
//	    String body = "订单描述测试";
//		String notify_url = "http://127.0.0.1:8080/test/jsp/pay/notify_url.jsp";
//		String out_trade_no = ""+new Date().getTime();
//		double total_price = 0.01;
//		
//		String partner = "12039175";
//		String show_url = "https://www.baidu.com/img/baidu_jgylogo3.gif";
//		String ip = "127.0.0.1";
//		String url = "https://epay.qrcb.com.cn:50080/epaygate/mb/Wirelesspaygate.htm";
//		
//		String time_start = DateFormatUtils.format(new Date(), "yyyyMMdd HH:mm:ss");
//	    //	String time_expire = "";
//		//String transport_fee = "";
//		//String product_fee = "";
//		String attach = getContentMd5;
//		String return_url = "http://sqyx.qrcb.com.cn";
//	//	String trade_details = partner+"^"+total_price+"^"+0+"^"+body+"^"+out_trade_no+"^0002^1"; //商户号^交易金额^手续费金额^商品描述^订单号^交易类型^是否计算手续费
//		String key = "pkh6pvqaw0l1ryrvnikan38iecxkdfew";
//		
//		SortedMap<String,String> map = new TreeMap<String,String>();
//		map.put("service","pay_service");
//		map.put("input_charset","UTF-8");
//		map.put("sign_type","MD5");
//		map.put("deliveryaddr","山东省青岛市市南区香港中路100号");
//		map.put("partner",partner);
//		map.put("out_trade_no",out_trade_no);
//		map.put("subject","这是一个测试订单");
//		map.put("show_url",show_url);
//		map.put("body",body);
//		map.put("total_fee",""+total_price);
//		map.put("fee_type","1");
//		map.put("spbill_create_ip",ip);
//		map.put("time_start",time_start);//yyyyMMdd hh:mm:ss
//		map.put("trade_mode","0002");
//		map.put("attach",attach);
//		map.put("notify_url",notify_url);
//		map.put("return_url",return_url);
//		map.put("trans_channel","mb");//pc-电脑 mb-手机
//	//	map.put("batch_num","1");//交易笔数  
//	//	map.put("trade_details",trade_details);//第一笔交易#第二笔交易#  商户号^交易金额^手续费金额^商品描述^订单号^交易类型^是否计算手续费
//		
//		
//		
//		String value = EpayCore.createLinkString(EpayCore.paraFilter(map));
//		String sign = EpayMD5.sign(value,key,"UTF-8");
//		
//		
//		 //除去数组中的空值和签名参数
//        Map<String, String> sPara = EpayCore.paraFilter(map);
//        
//        String prestr = EpayCore.createLinkString(sPara); 
//        String mysign = EpayMD5.sign(prestr,key,"UTF-8");
//        
//        
//        log.info(sign+" getPrePayQrcb method  request ty pay mysign is:"+mysign);
//        
//        
//        
//		map.put("sign", sign);//签名
//		
//		String requestXML = EpayCore.createLinkString(map);//PayUtil.doXMLParse(map);
//		log.info("getPrePayQrcb method  request ty pay param is:"+requestXML);
//		
//		returnObj.setCode(Constant.success);
//		String data = "{\"url\":\""+url+"\",\"params\":\""+requestXML+"\"}";
//		returnObj.setData(data );
//		
//		log.info(returnObj.getData().toString());
//		
//	}
	
}
