package com.zyhao.openec.order.controller;

import java.util.List;
import java.util.Optional;

import javax.servlet.http.HttpServletRequest;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.zyhao.openec.order.entity.Orders;
import com.zyhao.openec.order.repository.OrderRepository;
import com.zyhao.openec.order.service.OrderService;

/**
 * 
 * @author zgy_c
 *
 */
@RestController
@RequestMapping(path = "/nologin")
public class OrderNologinController {

	@Autowired
	private OrderRepository orderRepository;
	
	@Autowired
	private OrderService orderService;


	
	/**
	 * 修改订单支付状态
	 * 
	 * @param
	 * @return
	 * @throws Exception
	 */
	@Transactional
	@RequestMapping(path = "/edit/{out_trade_no}", method = RequestMethod.GET)
	public ResponseEntity<List<Orders>> editOrder(
			@PathVariable("out_trade_no") String out_trade_no,
			@RequestParam String status,
			@RequestParam String orderstatus,
			HttpServletRequest request) throws Exception {
        return Optional.ofNullable(orderService.editOrderPayStatus(request,out_trade_no,status,orderstatus))
                .map(orders -> new ResponseEntity(orders,HttpStatus.OK))
                .orElseThrow(() -> new Exception("Could not find getWaitPayOrderDetail"));
	}
	
}
