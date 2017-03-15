package com.zyhao.openec.order.controller;

import java.util.Map;
import java.util.Optional;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.zyhao.openec.order.entity.PayInfo;
import com.zyhao.openec.order.pojo.ReturnObj;
import com.zyhao.openec.order.service.PaymentServiceV1;
import com.zyhao.openec.pojo.MachineCode;
import com.zyhao.openec.util.Constant;
import com.zyhao.openec.util.DateUtil;
import com.zyhao.openec.util.PayUtil;
import com.zyhao.openec.util.UniqueCodeUtil;


/**
 * 
 * Title:PaymentController 
 * Desc: 支付功能
 * @author Administrator
 * @date 2016年10月14日 上午11:26:23
 */
@RestController
@RequestMapping("/v1")
public class PaymentController {
	private final Log log = LogFactory.getLog(PaymentController.class);
	@Autowired
	private PaymentServiceV1 paymentService;
	@Autowired
	private UniqueCodeUtil uniqueCodeUtil;
	@Autowired
	private MachineCode machineCode;
	
	@Autowired
    public PaymentController(PaymentServiceV1 paymentService) {
        this.paymentService = paymentService;
    }
	

	/**
	 * 生成支付信息
	 * @param pay
	 * @return
	 * @throws Exception
	 */
	@Transactional
	@RequestMapping(path = "/createPayInfo", method = RequestMethod.POST)
    public ResponseEntity createPayInfo(@Validated @RequestBody PayInfo pay) throws Exception {
		ReturnObj returnObj = new ReturnObj();
		if(pay == null){
			log.error(" createPayInfo method has an error :"+DateUtil.getDefaultDate()+" pay is null,return error ");
		    returnObj.setCode(Constant.error);
		    returnObj.setMsg(" pay is null ");
		    return Optional.ofNullable(returnObj)
		    		.map(varname -> new ResponseEntity<>(varname, HttpStatus.OK))
		    		.get();
		}
		log.info("createPayInfo method params pay is "+pay.toString());
		   
	    return Optional.ofNullable(paymentService.createPayInfo(pay))
	    		.map(varname -> new ResponseEntity<>(varname, HttpStatus.OK))
	    		.get();
    }
	
	/**
	 * 获取支付的编号
	 * @param channelId
	 * @param payWay
	 * @param request
	 * @param response
	 * @return
	 * @throws Exception
	 */
	@RequestMapping(path = "/getPayInfoCode",method=RequestMethod.GET)
    public ResponseEntity getPayInfoCode(
    		@RequestParam("channel_id") String channelId,
    		HttpServletRequest request,HttpServletResponse response) throws Exception {
		
		return Optional.ofNullable(uniqueCodeUtil.getNextCode(machineCode,channelId))
                .map(varname -> new ResponseEntity<>(varname, HttpStatus.OK))
                .orElseThrow(() -> new Exception("Could not find user"));
	}
	
	/**
	 * 待支付
	 * @param channelId
	 * @param payWay
	 * @param request
	 * @param response
	 * @return
	 * @throws Exception
	 */
	@RequestMapping(path = "/getPayInfo/noPayment",method=RequestMethod.GET)
    public ResponseEntity noPayment(
    		@RequestParam("page") int page,
    		@RequestParam("size") int size,
    		HttpServletRequest request,
    		HttpServletResponse response) throws Exception {
		Map<String,String[]> authenticatedUser = paymentService.getAuthenticatedUser();
		
		String[] strings = authenticatedUser.get("Session_id");
		
		Pageable pageRequest = new PageRequest(page,size);
		
		Page<PayInfo> getpayInfoNoPayment = paymentService.getpayInfoNoPayment(strings[0],pageRequest);
//		log.info("getpayInfoNoPayment is "+getpayInfoNoPayment.getContent());
//		RepEntity rep = new RepEntity();
//		rep.setStatus(""+HttpStatus.OK.ordinal());
//		rep.setData(getpayInfoNoPayment.getContent());
//		rep.setTotalElements(getpayInfoNoPayment.getTotalElements());
//		rep.setTotalPages(Long.valueOf(getpayInfoNoPayment.getTotalPages()));
		return Optional.ofNullable(getpayInfoNoPayment)
                .map(varname -> new ResponseEntity<>(varname, HttpStatus.OK))
                .orElseThrow(() -> new Exception("Could not find user"));
	}

}
