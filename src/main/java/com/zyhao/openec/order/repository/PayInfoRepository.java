package com.zyhao.openec.order.repository;

import java.util.List;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import com.zyhao.openec.order.entity.PayInfo;

@Repository
public interface PayInfoRepository extends JpaRepository<PayInfo, String> {
	
	public PayInfo findByOutTradeNo(String outTradeNo);
	public PayInfo findByOutTradeNoAndChannelId(String outTradeNo,String channelId);
	public List<PayInfo> findByPayStatus(String string);
	public Page<PayInfo> findByPayStatusAndUserId(String string,String id,Pageable pageRequest);
	public PayInfo findByOutTradeNoAndUserId(String outTradeNo, String authenticatedUserId);
}
