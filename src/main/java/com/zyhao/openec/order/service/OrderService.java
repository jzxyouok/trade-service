package com.zyhao.openec.order.service;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import javax.servlet.http.HttpServletRequest;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.client.loadbalancer.LoadBalanced;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestTemplate;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.zyhao.openec.entity.Store;
import com.zyhao.openec.order.entity.Inventory;
import com.zyhao.openec.order.entity.OrderItem;
import com.zyhao.openec.order.entity.Orders;
import com.zyhao.openec.order.entity.PayInfo;
import com.zyhao.openec.order.entity.RefundOrderItem;
import com.zyhao.openec.order.entity.RefundOrders;
import com.zyhao.openec.order.pojo.BigOrder;
import com.zyhao.openec.order.pojo.SellerOrder;
import com.zyhao.openec.order.repository.OrderItemRepository;
import com.zyhao.openec.order.repository.OrderRepository;
import com.zyhao.openec.order.repository.PayInfoRepository;
import com.zyhao.openec.order.repository.RefundOrderRepository;
import com.zyhao.openec.pojo.MachineCode;
import com.zyhao.openec.pojo.RepEntity;
import com.zyhao.openec.pojo.ReturnObj;
import com.zyhao.openec.util.Constant;
import com.zyhao.openec.util.UniqueCodeUtil;

/**
 * 
 * Title:OrderService
 * Desc: 支付服务功能
 * @author Administrator
 * @date 2016年10月14日 下午3:29:38
 */
@Service
public class OrderService {
	private final Log log = LogFactory.getLog(OrderService.class);
//	private OAuth2RestTemplate oAuth2RestTemplate;
    private RestTemplate restTemplate;
    public static String platform_seller = "seller";//seller-物业平台 user-用户平台 employee-运营平台
	public static String platform_user = "user";//seller-物业平台 user-用户平台 employee-运营平台
    @Autowired
    public OrderService(
//    		@LoadBalanced OAuth2RestTemplate oAuth2RestTemplate,
        @LoadBalanced RestTemplate normalRestTemplate) {
//        this.oAuth2RestTemplate = oAuth2RestTemplate;
        this.restTemplate = normalRestTemplate;
    }
	@Autowired
	private OrderRepository orderRepository;
	@Autowired
	private RefundOrderRepository refundOrderRepository;
	@Autowired
	private OrderItemRepository orderItemRepository;
	@Autowired
	private UniqueCodeUtil uniqueCodeUtil;
	@Autowired
	private PayInfoRepository payInfoRepository;
	@Autowired
	private MachineCode machineCode;
	@Autowired
	private PaymentServiceV1 paymentService;
	
	@Autowired  
    HttpServletRequest request;
	/**
	 * 认证平台
	 * @return
	 */
	public Map<String,String[]> getAuthenticatedUser() {
		return request.getParameterMap();
	}
	public String getTradeOutNo(String channelId) throws Exception {
		String getPayInfoCode = uniqueCodeUtil.getNextCode(machineCode,channelId);
		log.info("getTradeOutNo is "+getPayInfoCode);
		return getPayInfoCode;
	}

	public String createPayInfo(BigOrder reqOrder) throws Exception {
//		HttpHeaders headers = new HttpHeaders();
//		MediaType type = MediaType.parseMediaType("application/json; charset=UTF-8");
//		headers.setContentType(type);
//		
//		JSONObject json = new JSONObject();
//		json.put("outTradeNo", reqOrder.getTradeOutNo());
//		json.put("totalPrice", reqOrder.getTotalPrice());
//		json.put("payPrice", reqOrder.getTotalPrice());
//		json.put("userId", reqOrder.getMemberId());
//		json.put("payType","1");//1-现金支付 2-货到付款
//		json.put("channelId", reqOrder.getChannelId());
//		json.put("payStatus", "0");
//		json.put("businessId",reqOrder.getBusinessId());
		int size = reqOrder.getSellerOrders().size();
		String sellerId = ",";
		for(int i=0;i<size;i++){
			SellerOrder o = reqOrder.getSellerOrders().get(0);
			if(o.getSellerId() != null && o.getSellerId() != ""){
				sellerId = sellerId+o.getSellerId() ;
			}
		    
		}
//		json.put("sellerId",sellerId.substring(sellerId.indexOf(",")+1));
//        log.info("createPayInfo method call order method param is "+json);
        
//	    HttpEntity<String> formEntity = new HttpEntity<String>(json.toString(), headers);
//		String createPayInfo = restTemplate.postForObject("http://payment-service/v1/createPayInfo",formEntity, String.class);		
    
		PayInfo pay = new PayInfo();
		pay.setSellerId(sellerId.substring(sellerId.indexOf(",")+1));
		pay.setOutTradeNo(reqOrder.getTradeOutNo());
		pay.setTotalPrice(reqOrder.getTotalPrice());
		pay.setPayPrice(reqOrder.getTotalPrice());
		pay.setUserId(reqOrder.getMemberId());
		pay.setPayType("1");//1-现金支付 2-货到付款
		pay.setChannelId(reqOrder.getChannelId());
		pay.setPayStatus("0");
		pay.setBusinessId(reqOrder.getBusinessId());
		
		log.info("createPayInfo method call order method param is "+pay);
		
		ReturnObj createPayInfo = paymentService.createPayInfo(pay);
		log.info("createPayInfo is "+createPayInfo);
		if(Constant.success.equals(createPayInfo.getCode())){
			return String.valueOf(createPayInfo.getData());
		}else{
			throw new Exception(createPayInfo.getMsg());
		}
		
	}
	
	/**
	 * 生成订单
	 * @throws Exception 
	 */
	@Transactional
	public BigOrder createOrder(BigOrder bigOrder,List<Orders>orders,List<OrderItem> orderItems) throws Exception{
		//保存订单信息
		if(orders != null){
	        orderRepository.save(orders);
		}
		//保存订单项信息
	    if(orderItems != null){
		    orderItemRepository.save(orderItems);
	    }
		//保存支付信息
	    if(bigOrder != null){
		    createPayInfo(bigOrder);
	    }
		return bigOrder;
	}

	public String getOrderCode() {
		return uniqueCodeUtil.getUniqueCode(machineCode);
	}

	/**
	 * 申请退单
	 * @param refundOrders
	 * @return
	 */
	public RepEntity createRefundOrder(HttpServletRequest request,RefundOrders refundOrders) {
		RepEntity resp = new RepEntity();
		try{
			Map<String,String[]> userId = getAuthenticatedUser();
			
			Long currTime = System.currentTimeMillis();
			
			/** 退单号. */
			refundOrders.setRefundOrderCode(""+currTime);
			
			/** 订单提交时间. */
			refundOrders.setCreateAt(currTime);
			
			/** 用户id. */
			refundOrders.setMemberId(userId.get("Session_id")[0]);
			
			/** 最后变更时间. */
			refundOrders.setModifyAt(currTime);

			/** 退单状态. */
			refundOrders.setStatus("0");
			
			/** 是否对账. */
			refundOrders.setIsBilled("F");
			
			/** 商户订单号(通过原订单号查询回来). */
			Orders reOrder = orderRepository.findByMemberIdAndOrderCode(userId.get("Session_id")[0], refundOrders.getOrderCode());
			if(reOrder == null){
				resp.setStatus("-1");
				resp.setMsg("原订单查询失败,请检查订单号");
				return resp;
			}
			refundOrders.setOutTradeNo(reOrder.getOutTradeNo()); 
			
			/** 退单金额(分)(通过SKU获取商品金额). */
			
			List<RefundOrderItem> refundOrderItems = refundOrders.getRefundOrderItems();
			
			List<String> skus = refundOrderItems.stream().map(refundOrderItem -> refundOrderItem.getSku()).collect(Collectors.toList());
			
			
			
			Inventory[] inventorys = getInventoryBySKUS(new ArrayList(Arrays.asList(skus.toArray())));
			
			//设置价格
			int realSellPrice = 0;
			for (Inventory _inventory : inventorys) {
				for(RefundOrderItem refundOrderItem:refundOrderItems){
					if(_inventory.getSku().equals(refundOrderItem.getSku())){
						//设置价格
						refundOrderItem.setPrice(_inventory.getPrice());
						
						//设置图片
						refundOrderItem.setProductPic(_inventory.getPictures());
						
						//设置规格
						refundOrderItem.setSpecifications(_inventory.getSpecs());
						
						//设置退货价格
						realSellPrice += _inventory.getPrice()*refundOrderItem.getGoodsCount();
					}
				}
			}
			
			//设置退款总价
			refundOrders.setRealSellPrice(realSellPrice);
			
			resp.setStatus("0");
			resp.setMsg("退单申请成功");
			resp.setData(refundOrderRepository.save(refundOrders));
			
			return resp;
			
		}catch(Exception e){
			e.printStackTrace();
			resp.setStatus("-1");
			resp.setMsg("退单申请失败");
			return resp;
		}

	}
	
	/**
	 * 通过SKU数组获取库存集合
	 */
	public Inventory[] getInventoryBySKUS(List<String> reqAry) throws Exception{
		
		HttpHeaders headers = new HttpHeaders();
		MediaType type = MediaType.parseMediaType("application/json; charset=UTF-8");
		headers.setContentType(type);				
		HttpEntity<List<String>> formEntity = new HttpEntity<List<String>>(reqAry, headers);		
		Inventory[] inventoryAry = restTemplate.postForObject("http://inventory-service/v1/batch",formEntity, Inventory[].class);
		
		return inventoryAry;
	}
	
	/**
	 * 退单列表
	 * @param 
	 * @return
	 */
	public RepEntity getRefundList(HttpServletRequest request,int page,int size,String type) {
		RepEntity resp = new RepEntity();
		try{
			Map<String,String[]> userId = getAuthenticatedUser();
			Pageable pageable = new PageRequest(page, size);
			Page<RefundOrders> refundOrderList = refundOrderRepository.findByMemberIdAndType(userId.get("Session_id")[0],type,pageable);
			resp.setData(refundOrderList);
			resp.setMsg("退单列表获取成功");
			resp.setStatus("0");
			return resp;
			
		}catch(Exception e){
			e.printStackTrace();
			resp.setMsg("退单列表获取失败");
			resp.setStatus("-1");
			return resp;
		}

	}

	/**
	 * 退单详情
	 * @param 
	 * @return
	 */
	public RepEntity getRefundDetail(HttpServletRequest request,String refundCode){
		RepEntity resp = new RepEntity();
		try{
			Map<String,String[]> userId = getAuthenticatedUser();
		
			RefundOrders refundOrder =  refundOrderRepository.findByMemberIdAndRefundOrderCode(userId.get("Session_id")[0],refundCode);
		
			resp.setData(refundOrder);
			resp.setMsg("退单详情获取成功");
			resp.setStatus("0");
			return resp;
		
		}catch(Exception e){
			e.printStackTrace();
			resp.setMsg("退单详情获取失败");
			resp.setStatus("-1");
			return resp;
		}
	}
	
	/**
	 * 退单审核
	 * @param 
	 * @return
	 */
	public RepEntity modifyRefundStatus(HttpServletRequest request,String refundCode,String status,String refundOpinion){
		RepEntity resp = new RepEntity();
		try{
			Map<String,String[]> user = getAuthenticatedUser();
			String userId = user.get("Session_id")[0];
			
			RefundOrders refundOrder =  refundOrderRepository.findByMemberIdAndRefundOrderCode(userId,refundCode);
			
			refundOrder.setStatus(status);
			
			refundOrder.setRefundOpinion(refundOpinion);
			
			RefundOrders _refundOrder = refundOrderRepository.save(refundOrder);
			
			resp.setData(_refundOrder);
			resp.setMsg("退单审核成功");
			resp.setStatus("0");
			return resp;	
		}catch(Exception e){
			e.printStackTrace();
			resp.setMsg("退单审核失败");
			resp.setStatus("-1");
			return resp;	
		}

	}
	
	/**
	 * 订单列表(按状态,排除删除态和待支付态)
	 * @param page
	 * @param size
	 * @return
	 */
	public RepEntity getOrderList(HttpServletRequest request,int page, int size,String status) {
		page = page - 1;
		RepEntity resp = new RepEntity();
		try{
			Map<String,String[]> user = getAuthenticatedUser();
			String userId = user.get("Session_id")[0];
			String businessId = user.get("Session_businessId")[0];
			Sort sort = new Sort(Sort.Direction.DESC, "createAt");
			Pageable pageable = new PageRequest(page, size,sort);
			log.info("====content======status="+status+" businessId="+businessId+" userId="+userId);
			
			if(status.equals(PaymentServiceV1.status_4) || status.equals(PaymentServiceV1.status_6)){
				
				List<String> statusIn = new ArrayList<String>();
				statusIn.add(PaymentServiceV1.status_4);
				statusIn.add(PaymentServiceV1.status_6);
				Page<Orders> orderList = orderRepository.findByMemberIdAndBusinessIdAndStatusIn(userId,businessId,statusIn,pageable);
				List<Orders> content = orderList.getContent();
				log.info("====content======"+content);
				for(Orders o :content){
					o.setOrderItems(orderItemRepository.findByOrderCode(o.getOrderCode()));
					o.setPayInfo(payInfoRepository.findByOutTradeNo(o.getOutTradeNo()));
				}
				
				resp.setMsg("订单列表查询成功");
				resp.setStatus("0");
				resp.setData(orderList);
				
				return resp;
			}
			
			Page<Orders> orderList = orderRepository.findByMemberIdAndStatusAndBusinessId(userId,status,businessId,pageable);
			List<Orders> content = orderList.getContent();
			log.info("====content======"+content);
			for(Orders o :content){
				o.setOrderItems(orderItemRepository.findByOrderCode(o.getOrderCode()));
				o.setPayInfo(payInfoRepository.findByOutTradeNo(o.getOutTradeNo()));
			}
			resp.setMsg("订单列表查询成功");
			resp.setStatus("0");
			resp.setData(orderList);
			
			return resp;
		}catch(Exception e){
			e.printStackTrace();
			resp.setMsg("订单列表查询失败");
			resp.setStatus("-1");
			return resp;
		}	
	}
	
	/**
	 * 待支付订单列表
	 * @param page
	 * @param size
	 * @return
	 */
	public RepEntity getWaitPayOrderList(HttpServletRequest request,String outTradeNos) {
		RepEntity resp = new RepEntity();

		try{
			//当前用户待支付列表
			String[] _outTradeNos = outTradeNos.split(",");

			List<List<Orders>> orderList = new LinkedList<List<Orders>>();
			
			for (String outTradeNo : _outTradeNos) {
				orderList.add(getWaitPayOrderByOutTradeNo(request,outTradeNo));
			}
			
//			List<List<Orders>> orderList = waitPayInfoList.stream().map(payInfoMap -> getWaitPayOrderByOutTradeNo(payInfoMap)).collect(Collectors.toList());
//			
			resp.setStatus("0");
			resp.setMsg("订单列表查询成功");
			resp.setData(orderList);

			
			return resp;
		}catch(Exception e){
			e.printStackTrace();
			resp.setStatus("-1");
			resp.setMsg("订单列表查询失败");
			return resp;
		}

		
	}
	
	public List<Orders> getWaitPayOrderByOutTradeNo(HttpServletRequest request,String outTradeNo){
		try{	
			Map<String,String[]> user = getAuthenticatedUser();
			String userId = user.get("Session_id")[0];
		    return orderRepository.findByMemberIdAndOutTradeNo(userId,outTradeNo);
		}catch(Exception e){
			e.printStackTrace();
			return null;
		}
	}
	
	
	/**
	 * 待支付订单详情
	 * @param page
	 * @param size
	 * @return
	 */
	public RepEntity getWaitPayOrderDetail(HttpServletRequest request,String outTradeNo) {
		RepEntity resp = new RepEntity();
		try{
			Map<String,String[]> user = getAuthenticatedUser();
			String userId = user.get("Session_id")[0];
			List<Orders> orders = orderRepository.findByMemberIdAndOutTradeNo(userId,outTradeNo);
			resp.setStatus("0");
			resp.setMsg("查询成功");
			resp.setData(orders);
			
			return resp;
		}catch(Exception e){
			e.printStackTrace();
			resp.setStatus("-1");
			resp.setMsg("详情查询失败");
			return resp;
		}

	}
	
	
	/**
	 * 订单详情
	 * @param page
	 * @param size
	 * @return
	 */
	public RepEntity getOrderByOrderCode(String orderCode){
		RepEntity resp = new RepEntity();
		Map<String,String[]> authenticatedUser = getAuthenticatedUser();
		/**
		 * 判断平台
		 */
		String platform = authenticatedUser.get("platform")!=null?authenticatedUser.get("platform")[0]:"";

		if(platform_user.equalsIgnoreCase(platform)){
			String userId = authenticatedUser.get("Session_id")[0];
			Orders order = orderRepository.findByMemberIdAndOrderCode(userId,orderCode);
			if(order == null){
				resp.setStatus("-1");
				resp.setMsg("该订单不存在或已被删除");
				
				return resp;
			}
			order.setOrderItems(orderItemRepository.findByOrderCode(order.getOrderCode()));
			order.setPayInfo(payInfoRepository.findByOutTradeNo(order.getOutTradeNo()));
			resp.setStatus("0");
			resp.setMsg("详情查询成功");
			resp.setData(order);
			
			return resp;
			
			
		}else if(platform_seller.equalsIgnoreCase(platform)){
			String sellerId = request.getParameter("sellerId");
			log.info("queryOrderList user.get(Session_businessId)[0] = "+authenticatedUser.get("Session_businessId")[0]+" sellerId = "+sellerId);
			
			Orders order = orderRepository.findByBusinessIdAndOrderCode(authenticatedUser.get("Session_businessId")[0], orderCode);
			if(order == null){
				resp.setStatus("-1");
				resp.setMsg("该订单不存在或已被删除");
				return resp;
			}
			order.setOrderItems(orderItemRepository.findByOrderCode(order.getOrderCode()));
			order.setPayInfo(payInfoRepository.findByOutTradeNo(order.getOutTradeNo()));
			resp.setStatus("0");
			resp.setMsg("详情查询成功");
			resp.setData(order);
			return resp;
		}else{
			Orders order = orderRepository.findOne(orderCode);
			if(order == null){
				resp.setStatus("-1");
				resp.setMsg("该订单不存在或已被删除");
				return resp;
			}
			order.setOrderItems(orderItemRepository.findByOrderCode(order.getOrderCode()));
			order.setPayInfo(payInfoRepository.findByOutTradeNo(order.getOutTradeNo()));
			resp.setStatus("0");
			resp.setMsg("详情查询成功");
			resp.setData(order);
			return resp;
		}
	}

	/**
	 * 修改订单状态
	 * @param orderCode
	 * @param status
	 * @return
	 */
	public RepEntity editOrderStatus(HttpServletRequest request,String orderCode, String status) {
		RepEntity resp = new RepEntity();
		try{
			Map<String,String[]> user = getAuthenticatedUser();
			String userId = user.get("Session_id")[0];
			Orders order = orderRepository.findByMemberIdAndOrderCode(userId,orderCode);
			
			/**
			 * 3 状态为确认收货 , 5状态为取消订单
			 */
			if(!(status.equals("3") || status.equals("5"))){
				
				resp.setStatus("-1");
				resp.setMsg("订单状态修改失败,状态不允许");
				
				return resp;
				
			}
			
			order.setStatus(status);
			orderRepository.save(order);
			
			resp.setStatus("0");
			resp.setMsg("订单状态修改成功");
			resp.setData(order);
			
			return resp;
			
			
		}catch(Exception e){
			e.printStackTrace();
			resp.setStatus("-1");
			resp.setMsg("订单状态修改失败");
			return resp;
		}

		
	}

	
	/**
	 * 根据订单号删除订单
	 * @param orderCode
	 * @return
	 */
	public Object deleteOrder(HttpServletRequest request,String orderCode) {
		RepEntity resp = new RepEntity();
		try{
			Map<String,String[]> user = getAuthenticatedUser();
			String userId = user.get("Session_id")[0];
			Orders order = orderRepository.findByMemberIdAndOrderCode(userId,orderCode);
			
			if(order == null || order.getStatus().equals("100")){
				resp.setStatus("-1");
				resp.setMsg("该订单不存在或已被删除");
				
				return resp;
			}
			
			order.setStatus("100");
			orderRepository.save(order);
			
			resp.setStatus("0");
			resp.setMsg("订单删除成功");
			resp.setData(order);
			
			return resp;
						
		}catch(Exception e){
			e.printStackTrace();
			resp.setStatus("-1");
			resp.setMsg("订单删除失败");
			return resp;
		}
	}

	/**
	 * 修改订单支付状态
	 * @param out_trade_no
	 * @param status
	 * @param orderstatus
	 * @param mustModifyOrderStatus
	 * @param mustNotifyInventory
	 * @return
	 * @throws Exception
	 */
	public Object editOrderPayStatus(String out_trade_no, String status,String orderstatus,boolean mustModifyOrderStatus,boolean mustNotifyInventory) throws Exception {
		List<Inventory> reqAry = new ArrayList<Inventory>();
		List<Orders> orders = orderRepository.findByOutTradeNo(out_trade_no);
		for (Orders order : orders) {
			if(mustModifyOrderStatus){//若该订单状态，由支付的前台通知和后台通知来判断
				//若支付失败，订单状态为待支付
				if(!PaymentServiceV1.status_0.equals(order.getStatus())
						&& status.equals(PaymentServiceV1.status_6) ){
					//待确认不必须是待支付
				}else{
					order.setStatus(status);
				}
				
				order.setPayStatus(orderstatus);
			}
			log.info("editOrderPayStatus=======editOrderPayStatus is "+order);
			if(order.getOrderCode() != null){
				List<OrderItem> findByOrderCode = orderItemRepository.findByOrderCode(order.getOrderCode());
				log.info("editOrderPayStatus=======OrderItem is "+findByOrderCode);
				for(OrderItem oi:findByOrderCode){
					Inventory inventory = new Inventory();
					inventory.setSku(oi.getSku());
					inventory.setAmount(oi.getGoodsCount());
					inventory.setPrice(oi.getPrice());
					reqAry.add(inventory);
				}
			}
		}
		
		if(mustModifyOrderStatus){//若该订单状态，由支付的前台通知和后台通知来判断
			orderRepository.save(orders);
		}
		if(mustNotifyInventory){//如果不必须通知库存,由支付的前台通知和后台通知来判断
			HttpHeaders headers = new HttpHeaders();
			MediaType type = MediaType.parseMediaType("application/json; charset=UTF-8");
			headers.setContentType(type);				
			ObjectMapper map = new ObjectMapper();
			String s = map.writeValueAsString(reqAry);
			log.info("===================="+s);
			HttpEntity<String> formEntity = new HttpEntity<String>(s, headers);
			restTemplate.postForObject("http://inventory-service/v1/minusInventory",formEntity, Object.class);
		
		}
		
		return orders;
	}

	public RepEntity getRefundListByStatus(HttpServletRequest request,int page, int size, String type, String status) {
		RepEntity resp = new RepEntity();
		try{
			Map<String,String[]> user = getAuthenticatedUser();
			String userId = user.get("Session_id")[0];
			Pageable pageable = new PageRequest(page, size);
			Page<RefundOrders> refundOrderList = refundOrderRepository.findByMemberIdAndTypeAndStatus(userId,status,type,pageable);
			resp.setData(refundOrderList);
			resp.setMsg("退单列表获取成功");
			resp.setStatus("0");
			return resp;
			
		}catch(Exception e){
			e.printStackTrace();
			resp.setMsg("退单列表获取失败");
			resp.setStatus("-1");
			return resp;
		}
	}

	public RepEntity setIsRemind(HttpServletRequest request,String orderCode) {
		RepEntity resp = new RepEntity();
		try{
			Map<String,String[]> user = getAuthenticatedUser();
			String userId = user.get("Session_id")[0];
			Orders order = orderRepository.findByMemberIdAndOrderCode(userId, orderCode);
			order.setIsRemind("1");
			orderRepository.save(order);
			
			resp.setStatus("0");
			resp.setMsg("订单状态修改成功");
			resp.setData(order);
			
			return resp;
			
			
		}catch(Exception e){
			e.printStackTrace();
			resp.setStatus("-1");
			resp.setMsg("订单状态修改失败");
			return resp;
		}

	}

	public String getSellerName(String sellerId) {
		HttpHeaders headers = new HttpHeaders();
		MediaType type = MediaType.parseMediaType("application/json; charset=UTF-8");
		headers.setContentType(type);
		
		JSONObject json = new JSONObject();
		json.put("id",sellerId);
		
        log.info("getSellerName method call order method param is "+json);
        
	    HttpEntity<String> formEntity = new HttpEntity<String>(json.toString(), headers);
		Store store = restTemplate.postForObject("http://merchant-shop-service/nologin/findStoreById",formEntity, Store.class);		
		log.info("getSellerName is "+store);
		return store.getStoreName();
	}

	/**
	 * 订单列表(按状态,排除删除态和待支付态)
	 * @param page
	 * @param size
	 * @return
	 */
	public RepEntity getOrderList(int page, int size,String status,String sellerId,String businessId) {
		RepEntity resp = new RepEntity();
		try{
            Sort sort = new Sort(Sort.Direction.DESC, "createAt");
			Pageable pageable = new PageRequest(page, size,sort );
			List<String> statusIn = new ArrayList<String>();
			statusIn.add(status);
            if(status.equals(PaymentServiceV1.status_4) || status.equals(PaymentServiceV1.status_6)){
				statusIn.add(PaymentServiceV1.status_4);
				statusIn.add(PaymentServiceV1.status_6);
            }
			Page<Orders> orderList = orderRepository.findBySellerIdAndStatusInAndBusinessId(sellerId,statusIn,businessId,pageable);
			if(orderList.getContent() != null && orderList.getContent().size() > 0){
				List<Orders> content = orderList.getContent();
				for(Orders o:content){
					o.setOrderItems(orderItemRepository.findByOrderCode(o.getOrderCode()));
					o.setPayInfo(payInfoRepository.findByOutTradeNo(o.getOutTradeNo()));
				}
			}
			resp.setMsg("订单列表查询成功");
			resp.setStatus("0");
			resp.setData(orderList);
			
			return resp;
		}catch(Exception e){
			e.printStackTrace();
			resp.setMsg("订单列表查询失败");
			resp.setStatus("-1");
			return resp;
		}	
	}
}
