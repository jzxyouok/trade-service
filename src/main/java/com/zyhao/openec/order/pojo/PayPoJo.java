package com.zyhao.openec.order.pojo;

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

	@Override
	public String toString() {
		return "PayPoJo [outTradeNo=" + outTradeNo + ", channelId=" + channelId + ", payWay=" + payWay + ", openId="
				+ openId + ", body=" + body + "]";
	}
	
}
