package com.zyhao.openec.order.repository;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.repository.PagingAndSortingRepository;

import com.zyhao.openec.order.entity.Orders;



/**
 * 
 * @author zgy_c
 *
 */
public interface OrderRepository extends PagingAndSortingRepository<Orders, String> {
	
	
	/**
	 * 通过支付流水号查询订单,(可作为待支付订单详情)
	 * @param memberId
	 * @param outTradeNo
	 * @return
	 */
	List<Orders> findByMemberIdAndOutTradeNo(String memberId,String outTradeNo);
	
	
	/**
	 * 通过订单号查询订单详情
	 * @param memberId
	 * @param orderCode
	 * @return
	 */
	Orders findByMemberIdAndOrderCode(String memberId,String orderCode);
	
	/**
	 * 按状态查询订单列表
	 * @param memberId
	 * @param status
	 * @param pageable
	 * @return
	 */
	Page<Orders> findByMemberIdAndStatus(String memberId,String status, Pageable pageable);

	
	/**
	 * 按交易流水号查询订单列表,(可作为待支付订单详情,pageable方式)
	 * @param memberId
	 * @param status
	 * @param pageable
	 * @return
	 */
	Page<Orders> findByMemberIdAndOutTradeNo(String memberId,String outTradeNo, Pageable pageable);
	
//	/**
//	 * 查询某人全部订单(pageable方式)
//	 * @param memberId
//	 * @param status
//	 * @param pageable
//	 * @return
//	 */
//	Page<Orders>
	
	/**
	 * 批量返回多条订单数据
	 * @param outTradeNos
	 * @return
	 */
	public List<Orders> findByOutTradeNoIn(List<String> outTradeNos);
	
	/**
	 * 排除某状态查询订单列表
	 * @param memberId
	 * @param status
	 * @param pageable
	 * @return
	 */
	public Page<Orders> findByMemberIdAndStatusNotIn(String memberId,List<String> status, Pageable pageable);


	Page<Orders> findByMemberIdAndChannelIdAndStatusNotIn(String userId,String channelId, List<String> statusNot, Pageable pageable);


	List<Orders> findByOutTradeNo(String out_trade_no);


	Page<Orders> findByMemberIdAndBusinessIdAndStatusNotIn(String userId, String businessId, List<String> statusNot,
			Pageable pageable);


	Page<Orders> findByMemberIdAndStatusAndBusinessId(String userId, String status, String businessId,
			Pageable pageable);

	Page<Orders> findBySellerId(String sellerId,Pageable pageable);

	Orders findByOrderCodeAndSellerId(String id,String sellerId);
	
	Page<Orders> findBySellerIdAndStatusNotIn(String id, List<String> status, Pageable pageable);

	Page<Orders> findBySellerIdAndStatus(String id, String status, Pageable pageable);

	/**
	 * 通过订单号查询订单详情
	 * @param memberId
	 * @param orderCode
	 * @return
	 */
	Orders findBySellerIdAndOrderCode(String sellerId,String orderCode);

	Orders findByBusinessIdAndOrderCode(String businessId, String orderCode);


	Page<Orders> findBySellerIdAndStatusAndBusinessId(String sellerId, String status, String businessId,
			Pageable pageable);


	Page<Orders> findByMemberIdAndBusinessId(String userId, String businessId, Pageable pageable);


	Page<Orders> findByMemberIdAndBusinessIdAndStatusIn(String userId, String businessId, List<String> statusIn,Pageable pageable);


	Page<Orders> findBySellerIdAndStatusInAndBusinessId(String sellerId, List<String> statusIn, String businessId,
			Pageable pageable);
	
}

