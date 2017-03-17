
package com.zyhao.openec.order.controller;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Optional;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import com.zyhao.openec.order.entity.PayInfo;
import com.zyhao.openec.order.pojo.PayPoJo;
import com.zyhao.openec.order.service.PaymentServiceV1;
import com.zyhao.openec.pojo.ReturnObj;
import com.zyhao.openec.sign.EpayMD5;
import com.zyhao.openec.util.AliPayUtil;
import com.zyhao.openec.util.Constant;
import com.zyhao.openec.util.DateUtil;
import com.zyhao.openec.util.EpayCore;
import com.zyhao.openec.util.PayUtil;
import com.zyhao.openec.util.WechatPayUtil;
import com.zyhao.openec.util.XMLUtil;

@RestController
@RequestMapping("/nologin")
public class PaymentNoLoginController {
	private final Log log = LogFactory.getLog(PaymentNoLoginController.class);
	@Autowired
	private PaymentServiceV1 paymentService;
	
	@Autowired
    public PaymentNoLoginController(PaymentServiceV1 paymentService) {
        this.paymentService = paymentService;
    }
	
	/**
	 * 微信前台通知调用
	 * @param pay
	 * @return
	 * @throws Exception
	 */
	@RequestMapping(path = "/frontNotify", method = RequestMethod.POST)
    public ResponseEntity frontNotify(
    		@Validated @RequestBody PayInfo pay,
    		HttpServletRequest request) throws Exception {
		
		if(pay == null){
			//返回失败信息
			log.error(" frontNotify method has an error :"+DateUtil.getDefaultDate()+" pay is null,return error ");
			ReturnObj returnObj = new ReturnObj();
		    returnObj.setCode(Constant.error);
		    returnObj.setMsg(" pay is null ");
		    return Optional.ofNullable(returnObj)
		    		.map(varname -> new ResponseEntity<>(varname, HttpStatus.OK))
		    		.get();
		}
		log.info("frontNotify method params is "+pay.toString());
		try{
			ReturnObj returnObj = paymentService.frontNotify(request,pay);
			return Optional.ofNullable(returnObj)
	               .map(varname -> new ResponseEntity<>(varname, HttpStatus.OK))
	               .get();
		}catch(Exception ex){
			//------异常处理--------
			ex.printStackTrace();
			log.error(ex);
		}
		ReturnObj returnObj = new ReturnObj();
		returnObj.setCode(Constant.error);
	    returnObj.setMsg("error");
	    return Optional.ofNullable(returnObj)
	    		.map(varname -> new ResponseEntity<>(varname, HttpStatus.OK))
	    		.get();
    }
	
	/**
	 * 后台异步通知
	 * @param request
	 * @param response
	 * @return
	 * @throws Exception
	 */
	@RequestMapping(path = "/backRcvResp/{channel_id}/{pay_way}/{attch}")
    public ResponseEntity backRcvResp(
    		@PathVariable("channel_id") String channel_id,
    		@PathVariable("pay_way") String pay_way,
    		@PathVariable("attch") String attch,
    		HttpServletRequest request,HttpServletResponse response) throws Exception {
		log.info(" backRcvResp method is called! [start]"+DateUtil.getDefaultDate());
		
		//1.获取请求参数,输出JSON日志 
		//------BEGINE-----------
		JSONObject requestParamsJson = new JSONObject();
		String postRequestParams = getPostRequestParams(request);
		Map<String, String> getRequestParams = getGetRequestParams(request);
		requestParamsJson.put("postParams", postRequestParams);
		requestParamsJson.put("getParams", getRequestParams);
		requestParamsJson.put("channel_id",channel_id);
		requestParamsJson.put("pay_way",pay_way);
		requestParamsJson.put("attch",attch);
		log.info(DateUtil.getDefaultDate()+" backRcvResp method is called! all params are "+requestParamsJson);
		
		//2.根据支付方式进行参数解析,转成PayInfo Bean：微信支付通知参数是xml报文，支付宝是get请求的参数
		PayInfo payInfo = null;
		String returnStr = null;
		try{
	    	log.info("backRcvResp method run WeChatPay: pay_way="+pay_way+(pay_way.equals("WeChatPay")));

			PayInfo pay = null;
		    switch (pay_way){
		    case "WeChatPay" : 

		    	Map<String,String> requestParams = XMLUtil.doXMLParse(postRequestParams);
		    	log.info("backRcvResp method run WeChatPay: out_trade_no="+String.valueOf(requestParams.get("out_trade_no")));

		    	pay = paymentService.getpayInfoByOutTradeNo(String.valueOf(requestParams.get("out_trade_no")));
				log.info(" backRcvResp method pay is "+pay+" pay.getBusinessId() is"+pay.getBusinessId());
				
//		    	String key = ""+DataDicUtil.getAllFields(pay.getBusinessId()).get("ApiKey");
		    	String key =paymentService.getSysConfigInfo(pay.getBusinessId()).getAppkey();
				log.info(" backRcvResp method pay is "+pay+" key is"+key);

		    	if(pay==null){
			    	requestParamsJson.put("code", "error");
			    	requestParamsJson.put("msg", "the db cannot find PayInfo");
			    	log.error("backRcvResp method run error:数据库支付信息不存在! out_trade_no="+String.valueOf(getRequestParams.get("out_trade_no")));
			    	//----END---------
					returnStr = "";//paymentService.backRcvResp(payInfo,response);
					log.info(" backRcvResp method run [end] requestParamsJson="+requestParamsJson +" returnStr="+returnStr);
			        return Optional.ofNullable(returnStr)
			                .map(varname -> new ResponseEntity<>(varname, HttpStatus.OK))
			                .get();
			    }else{
					//----------处理微信支付通知业务--------------
					log.info(" backRcvResp method WechatPayUtil.getKey is "+key+" channel_id is"+channel_id);
					if(PayUtil.isPaySign("utf-8",requestParams,key))//验证数据加解密
					{
						log.info(" backRcvResp method 比较验签成功:WechatPayUtil.getKey is "+key+" channel_id is"+channel_id);

                        payInfo = WechatPayUtil.convertMapToBean(requestParams,pay);
                        
					}else{
						requestParamsJson.put("code", "error");
				    	requestParamsJson.put("msg", "the wechat Sign compares faild");
						log.error(" backRcvResp method request error:比较验签失败  out_trade_no="+String.valueOf(requestParams.get("out_trade_no")));
					
						//----END---------
						returnStr = paymentService.backRcvResp(payInfo,response);
						log.info(" backRcvResp method run [end] requestParamsJson="+requestParamsJson +" returnStr="+returnStr);
				        return Optional.ofNullable(returnStr)
				                .map(varname -> new ResponseEntity<>(varname, HttpStatus.OK))
				                .get();
					
					}
			    }
				break;
		    case "AliPay":
		    	pay = paymentService.getpayInfoByOutTradeNo(String.valueOf(getRequestParams.get("out_trade_no")));
			    if(pay==null){
			    	requestParamsJson.put("code", "error");
			    	requestParamsJson.put("msg", "the db cannot find PayInfo");
			    	log.error("backRcvResp method run error:数据库支付信息不存在! out_trade_no="+String.valueOf(getRequestParams.get("out_trade_no")));
			    	//----END---------
					returnStr = paymentService.backRcvResp(payInfo,response);
					log.info(" backRcvResp method run [end] requestParamsJson="+requestParamsJson +" returnStr="+returnStr);
			        return Optional.ofNullable(returnStr)
			                .map(varname -> new ResponseEntity<>(varname, HttpStatus.OK))
			                .get();
			    }else{
			    	key = "";
					//----------处理支付宝支付通知业务--------------
					log.debug(" backRcvResp method AliPayUtil.getKey is "+key+" channel_id is"+channel_id);
					if(PayUtil.isPaySign("utf-8",getRequestParams,key))//验证数据加解密
					{
					    payInfo = AliPayUtil.convertMapToBean(getRequestParams,pay);
					}else{
						requestParamsJson.put("code", "error");
				    	requestParamsJson.put("msg", "the ali Sign compares faild");
						log.error(" backRcvResp method request error:比较验签失败  out_trade_no="+String.valueOf(getRequestParams.get("out_trade_no")));
						//----END---------
						returnStr = paymentService.backRcvResp(payInfo,response);
						log.info(" backRcvResp method run [end] requestParamsJson="+requestParamsJson +" returnStr="+returnStr);
				        return Optional.ofNullable(returnStr)
				                .map(varname -> new ResponseEntity<>(varname, HttpStatus.OK))
				                .get();
					}
			    }
			    break;
		    case "QrcbPay":
		    	pay = paymentService.getpayInfoByOutTradeNo(String.valueOf(getRequestParams.get("out_trade_no")));
		    	key = "pkh6pvqaw0l1ryrvnikan38iecxkdfew";
		    	
			    if(pay==null){
			    	requestParamsJson.put("code", "error");
			    	requestParamsJson.put("msg", "the db cannot find PayInfo");
			    	log.error("backRcvResp method run error:数据库支付信息不存在! out_trade_no="+String.valueOf(getRequestParams.get("out_trade_no")));
			    	//----END---------
					returnStr = "";//paymentService.backRcvResp(payInfo,response);
					log.info(" backRcvResp method run [end] requestParamsJson="+requestParamsJson +" returnStr="+returnStr);
			        return Optional.ofNullable(returnStr)
			                .map(varname -> new ResponseEntity<>(varname, HttpStatus.OK))
			                .get();
			    }else{
					//----------处理支付宝支付通知业务--------------
					log.debug(" backRcvResp method QrcbPayUtil.getKey is "+key+" channel_id is"+channel_id);
					String value = EpayCore.createLinkString(EpayCore.paraFilter(getRequestParams));
					String sign = EpayMD5.sign(value,key,"UTF-8");
					
					if(EpayMD5.validateSign(value, sign, key, "UTF-8"))//验证数据加解密
					{
					    payInfo = AliPayUtil.convertMapToBeanQrcb(getRequestParams,pay);
					}else{
						requestParamsJson.put("code", "error");
				    	requestParamsJson.put("msg", "the ali Sign compares faild");
						log.error(" backRcvResp method request error:比较验签失败  out_trade_no="+String.valueOf(getRequestParams.get("out_trade_no")));
						//----END---------
						returnStr = paymentService.backRcvResp(payInfo,response);
						log.info(" backRcvResp method run [end] requestParamsJson="+requestParamsJson +" returnStr="+returnStr);
				        return Optional.ofNullable(returnStr)
				                .map(varname -> new ResponseEntity<>(varname, HttpStatus.OK))
				                .get();
					}
			    }
		    	break;
		    }
		}catch(Exception ex){
			//------异常处理--------
			ex.printStackTrace();
			requestParamsJson.put("code", "error");
	    	requestParamsJson.put("msg", ex.getMessage());
			log.error(" backRcvResp method convert to payinfo exception",ex);
			//----END---------
			returnStr = paymentService.backRcvResp(payInfo,response);
			log.info(" backRcvResp method run [end] requestParamsJson="+requestParamsJson +" returnStr="+returnStr);
	        return Optional.ofNullable(returnStr)
	                .map(varname -> new ResponseEntity<>(varname, HttpStatus.OK))
	                .get();
		}
		
		
		//3.处理PayInfo,验证通知结果的参数加解密,异常处理
		try{
	        if(!PayUtil.verify(payInfo,attch)){
	        	payInfo.setPayStatus("4");//支付状态（默认-0，成功-1，失败-2，3-异常,4-成功，但数据比较不一致）
	        }else{
	        	
	        	payInfo.setPayStatus("1");//支付状态（默认-0，成功-1，失败-2，3-异常,4-成功，但数据比较不一致） 
	        }
	        paymentService.updatePayInfo(payInfo);
		}catch(Exception ex){
			//------异常处理--------
			log.error("backRcvResp method 修改支付单失败!payInfo="+payInfo.toString());
			ex.printStackTrace();
			requestParamsJson.put("code", "error");
	    	requestParamsJson.put("msg", ex.getMessage());
			log.error(" backRcvResp method save payinfo exception",ex);
			//----END---------
			returnStr = paymentService.backRcvResp(payInfo,response);
			log.info(" backRcvResp method run [end] requestParamsJson="+requestParamsJson +" returnStr="+returnStr);
	        return Optional.ofNullable(returnStr)
	                .map(varname -> new ResponseEntity<>(varname, HttpStatus.OK))
	                .get();
		}
		//TODO 4.调用订单接口
		try{
		    paymentService.notifyOrder(payInfo);
		}catch(Exception ex){
			ex.printStackTrace();
			log.error("backRcvResp method 通知订单失败!payInfo="+payInfo.toString());
		}
	    // 5.返回	 
		//----END---------
		returnStr = paymentService.backRcvResp(payInfo,response);
		log.info(" backRcvResp method run [end] requestParamsJson="+requestParamsJson +" returnStr="+returnStr);
        return Optional.ofNullable(returnStr)
                .map(varname -> new ResponseEntity<>(varname, HttpStatus.OK))
                .get();
    }
	
	/**
	 * 获取支付结果的post请求参数
	 * @param request
	 * @return
	 * @throws Exception
	 */
	public static String getPostRequestParams(HttpServletRequest request) throws Exception {
		InputStream inputStream ;
		StringBuffer sb = new StringBuffer();
		inputStream = request.getInputStream();
		String s ;
		BufferedReader in = new BufferedReader(new InputStreamReader(inputStream, "UTF-8"));
		while ((s = in.readLine()) != null){
			sb.append(s);
		}
		in.close();
		inputStream.close();
		return sb.toString();
	}
	/**
	 * 获取支付结果的get请求参数
	 * @param request
	 * @return
	 */
	private Map<String,String> getGetRequestParams(HttpServletRequest request) {
		Map<String,String> params = new HashMap<String,String>();
		Map requestParams = request.getParameterMap();
		for (Iterator iter = requestParams.keySet().iterator(); iter.hasNext();){
			String name = (String) iter.next();
			String[] values = (String[]) requestParams.get(name);
			String valueStr = "";
			for (int i = 0; i < values.length; i++){
				valueStr = (i == values.length - 1) ? valueStr + values[i] : valueStr + values[i] + ",";
			}
			params.put(name, valueStr);
		}
		return params;
	}
	
	/***
	 * 统一预支付接口
	 * @param out_trade_no
	 * @param channel_id
	 * @param pay_way
	 * @param body
	 * @param request
	 * @param response
	 * @return
	 * @throws Exception
	 */
	@Transactional
	@RequestMapping(path = "/unionPrePay",method=RequestMethod.POST)
    public ResponseEntity getPrePay(@RequestBody PayPoJo payPojo) throws Exception {
        String out_trade_no = payPojo.getOutTradeNo();
		String pay_way = payPojo.getPayWay();
		String body = payPojo.getBody();
		
		log.info(" getPrePay method params is payinfo="+payPojo.toString());
		ReturnObj returnObj = new ReturnObj();
		returnObj.setCode(Constant.error);
		returnObj.setMsg("channel not exit");
		log.info(" getPrePay method getpayInfoByOutTradeNo payinfo="+payPojo.toString());
		//查询支付信息
		PayInfo getpayInfoByOutTradeNo = paymentService.getpayInfoByOutTradeNo(out_trade_no);
		String channel_id = getpayInfoByOutTradeNo.getChannelId();
		String businessId = getpayInfoByOutTradeNo.getBusinessId();
		log.info(" getPrePay method getpayInfoByOutTradeNo getpayInfoByOutTradeNo="+getpayInfoByOutTradeNo.toString());
		if(getpayInfoByOutTradeNo == null 
				|| getpayInfoByOutTradeNo.getOutTradeNo() == null){
			returnObj.setCode(Constant.error);
			returnObj.setMsg("no find payinfo channel_id="+channel_id+" out_trade_no="+out_trade_no);
			return Optional.ofNullable(returnObj)
	                .map(varname -> new ResponseEntity<>(varname, HttpStatus.NOT_FOUND))
	                .get();
		}
		//更新支付表
		getpayInfoByOutTradeNo.setPayWay(pay_way);
		log.info(" getPrePay method updatePayInfo updatePayInfo="+getpayInfoByOutTradeNo.toString());
		paymentService.updatePayInfo(getpayInfoByOutTradeNo);
		log.info(" getPrePay method updatePayInfo updatePayInfo="+getpayInfoByOutTradeNo.toString());
		//根据支付方式进行接口调用
		if(Constant.PayWay.WeChatPay.name().equals(pay_way)){//微信
			String openid = paymentService.getAuthenticatedUser().get("openidLogin_"+businessId)[0];
			returnObj = paymentService.getPrePay(out_trade_no,channel_id,businessId,pay_way,body,openid);
			log.info("getPrePay method run PrePaStr ===="+returnObj.getData());
			return Optional.ofNullable(returnObj)
	               .map(varname -> new ResponseEntity<>(varname, HttpStatus.OK))
	               .get();
		}
        if(Constant.PayWay.QrcbPay.name().equals(pay_way)){//农商行
			
			returnObj = paymentService.getPrePayQrcb(out_trade_no,channel_id,pay_way,body);
			log.info("getPrePay method run PrePaStr ===="+returnObj.getData());
			return Optional.ofNullable(returnObj)
	               .map(varname -> new ResponseEntity<>(varname, HttpStatus.OK))
	               .get();
		}
		if(Constant.PayWay.AliPay.name().equals(pay_way)){//阿里
			returnObj.setCode(Constant.success);
			returnObj.setMsg(out_trade_no);
			return Optional.ofNullable(returnObj)
	               .map(varname -> new ResponseEntity<>(varname, HttpStatus.OK))
	               .get();
		}
		
		return Optional.ofNullable(returnObj)
                .map(varname -> new ResponseEntity<>(varname, HttpStatus.NOT_FOUND))
                .get();
	}

}

	