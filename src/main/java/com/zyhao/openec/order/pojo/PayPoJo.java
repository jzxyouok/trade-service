package com.zyhao.openec.order.pojo;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Service;

@ConfigurationProperties(prefix="PayConstant")
@Service
public class PayPoJo {
	
	/** 商户订单号. */
	private String outTradeNo;
	
	/** 渠道id. */
	private String channelId;
	
	/** 支付方式:默认0-无 1-微信 2-支付宝 3-银联. */
	private String payWay;

	/** 微信openId. */
	private String openId;

	/** 商品描述. */
	private String body;
	
	/**用前台通知订单修改状态*/
	private String mustUseFrontNotifyForOrderStatus;
	/**用前台通知库存修改*/
	private String mustUseFrontNotifyForInventory;
	/**用后台通知订单修改状态*/
	private String mustUseBackNotifyForOrderStatus;
	/**用后台通知库存修改*/
	private String mustUseBackNotifyForInventory;
	

	public String getOutTradeNo() {
		return outTradeNo;
	}

	public void setOutTradeNo(String outTradeNo) {
		this.outTradeNo = outTradeNo;
	}

	public String getChannelId() {
		return channelId;
	}

	public void setChannelId(String channelId) {
		this.channelId = channelId;
	}

	public String getPayWay() {
		return payWay;
	}

	public void setPayWay(String payWay) {
		this.payWay = payWay;
	}

	public String getOpenId() {
		return openId;
	}

	public void setOpenId(String openId) {
		this.openId = openId;
	}

	public String getBody() {
		return body;
	}

	public void setBody(String body) {
		this.body = body;
	}

	public String getMustUseFrontNotifyForOrderStatus() {
		return mustUseFrontNotifyForOrderStatus;
	}

	public void setMustUseFrontNotifyForOrderStatus(String mustUseFrontNotifyForOrderStatus) {
		this.mustUseFrontNotifyForOrderStatus = mustUseFrontNotifyForOrderStatus;
	}

	public String getMustUseFrontNotifyForInventory() {
		return mustUseFrontNotifyForInventory;
	}

	public void setMustUseFrontNotifyForInventory(String mustUseFrontNotifyForInventory) {
		this.mustUseFrontNotifyForInventory = mustUseFrontNotifyForInventory;
	}

	public String getMustUseBackNotifyForOrderStatus() {
		return mustUseBackNotifyForOrderStatus;
	}

	public void setMustUseBackNotifyForOrderStatus(String mustUseBackNotifyForOrderStatus) {
		this.mustUseBackNotifyForOrderStatus = mustUseBackNotifyForOrderStatus;
	}

	public String getMustUseBackNotifyForInventory() {
		return mustUseBackNotifyForInventory;
	}

	public void setMustUseBackNotifyForInventory(String mustUseBackNotifyForInventory) {
		this.mustUseBackNotifyForInventory = mustUseBackNotifyForInventory;
	}

	@Override
	public String toString() {
		return "PayPoJo [outTradeNo=" + outTradeNo + ", channelId=" + channelId + ", payWay=" + payWay + ", openId="
				+ openId + ", body=" + body + ", mustUseFrontNotifyForOrderStatus=" + mustUseFrontNotifyForOrderStatus
				+ ", mustUseFrontNotifyForInventory=" + mustUseFrontNotifyForInventory
				+ ", mustUseBackNotifyForOrderStatus=" + mustUseBackNotifyForOrderStatus
				+ ", mustUseBackNotifyForInventory=" + mustUseBackNotifyForInventory + "]";
	}

	
}
