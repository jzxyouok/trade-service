package com.zyhao.openec.order.pojo;

import java.io.Serializable;

public class SysConfigInfo implements Serializable {

	/** serialVersionUID. */
	private static final long serialVersionUID = 1L;

	/** sysconfiginfo_id. */
	private Long sysconfiginfoId;

	/** appid. */
	private String appid;

	/** appkey. */
	private String appkey;

	/** appsecret. */
	private String appsecret;

	/** front_notify_redirect_uri. */
	private String frontNotifyRedirectUri;

	/** back_notify_redirect_uri. */
	private String backNotifyRedirectUri;

	/** config_type. */
	private String configType;

	/** mechid. */
	private String mechid;

	/** channel_id. */
	private String channelId;

	/** business_id. */
	private String businessId;

	/** store_id. */
	private String storeId;

	/** remart. */
	private String remart;

	/** create_at. */
	private Long createAt;

	/**
	 * Constructor.
	 */
	public SysConfigInfo() {
	}

	/**
	 * Set the sysconfiginfo_id.
	 * 
	 * @param sysconfiginfoId
	 *            sysconfiginfo_id
	 */
	public void setSysconfiginfoId(Long sysconfiginfoId) {
		this.sysconfiginfoId = sysconfiginfoId;
	}

	/**
	 * Get the sysconfiginfo_id.
	 * 
	 * @return sysconfiginfo_id
	 */
	public Long getSysconfiginfoId() {
		return this.sysconfiginfoId;
	}

	/**
	 * Set the appid.
	 * 
	 * @param appid
	 *            appid
	 */
	public void setAppid(String appid) {
		this.appid = appid;
	}

	/**
	 * Get the appid.
	 * 
	 * @return appid
	 */
	public String getAppid() {
		return this.appid;
	}

	/**
	 * Set the appkey.
	 * 
	 * @param appkey
	 *            appkey
	 */
	public void setAppkey(String appkey) {
		this.appkey = appkey;
	}

	/**
	 * Get the appkey.
	 * 
	 * @return appkey
	 */
	public String getAppkey() {
		return this.appkey;
	}

	/**
	 * Set the appsecret.
	 * 
	 * @param appsecret
	 *            appsecret
	 */
	public void setAppsecret(String appsecret) {
		this.appsecret = appsecret;
	}

	/**
	 * Get the appsecret.
	 * 
	 * @return appsecret
	 */
	public String getAppsecret() {
		return this.appsecret;
	}

	/**
	 * Set the front_notify_redirect_uri.
	 * 
	 * @param frontNotifyRedirectUri
	 *            front_notify_redirect_uri
	 */
	public void setFrontNotifyRedirectUri(String frontNotifyRedirectUri) {
		this.frontNotifyRedirectUri = frontNotifyRedirectUri;
	}

	/**
	 * Get the front_notify_redirect_uri.
	 * 
	 * @return front_notify_redirect_uri
	 */
	public String getFrontNotifyRedirectUri() {
		return this.frontNotifyRedirectUri;
	}

	/**
	 * Set the back_notify_redirect_uri.
	 * 
	 * @param backNotifyRedirectUri
	 *            back_notify_redirect_uri
	 */
	public void setBackNotifyRedirectUri(String backNotifyRedirectUri) {
		this.backNotifyRedirectUri = backNotifyRedirectUri;
	}

	/**
	 * Get the back_notify_redirect_uri.
	 * 
	 * @return back_notify_redirect_uri
	 */
	public String getBackNotifyRedirectUri() {
		return this.backNotifyRedirectUri;
	}

	/**
	 * Set the config_type.
	 * 
	 * @param configType
	 *            config_type
	 */
	public void setConfigType(String configType) {
		this.configType = configType;
	}

	/**
	 * Get the config_type.
	 * 
	 * @return config_type
	 */
	public String getConfigType() {
		return this.configType;
	}

	/**
	 * Set the mechid.
	 * 
	 * @param mechid
	 *            mechid
	 */
	public void setMechid(String mechid) {
		this.mechid = mechid;
	}

	/**
	 * Get the mechid.
	 * 
	 * @return mechid
	 */
	public String getMechid() {
		return this.mechid;
	}

	/**
	 * Set the channel_id.
	 * 
	 * @param channelId
	 *            channel_id
	 */
	public void setChannelId(String channelId) {
		this.channelId = channelId;
	}

	/**
	 * Get the channel_id.
	 * 
	 * @return channel_id
	 */
	public String getChannelId() {
		return this.channelId;
	}

	/**
	 * Set the business_id.
	 * 
	 * @param businessId
	 *            business_id
	 */
	public void setBusinessId(String businessId) {
		this.businessId = businessId;
	}

	/**
	 * Get the business_id.
	 * 
	 * @return business_id
	 */
	public String getBusinessId() {
		return this.businessId;
	}

	/**
	 * Set the store_id.
	 * 
	 * @param storeId
	 *            store_id
	 */
	public void setStoreId(String storeId) {
		this.storeId = storeId;
	}

	/**
	 * Get the store_id.
	 * 
	 * @return store_id
	 */
	public String getStoreId() {
		return this.storeId;
	}

	/**
	 * Set the remart.
	 * 
	 * @param remart
	 *            remart
	 */
	public void setRemart(String remart) {
		this.remart = remart;
	}

	/**
	 * Get the remart.
	 * 
	 * @return remart
	 */
	public String getRemart() {
		return this.remart;
	}

	/**
	 * Set the create_at.
	 * 
	 * @param createAt
	 *            create_at
	 */
	public void setCreateAt(Long createAt) {
		this.createAt = createAt;
	}

	/**
	 * Get the create_at.
	 * 
	 * @return create_at
	 */
	public Long getCreateAt() {
		return this.createAt;
	}

	@Override
	public String toString() {
		return "SysConfigInfo [sysconfiginfoId=" + sysconfiginfoId + ", appid=" + appid + ", appkey=" + appkey
				+ ", appsecret=" + appsecret + ", frontNotifyRedirectUri=" + frontNotifyRedirectUri
				+ ", backNotifyRedirectUri=" + backNotifyRedirectUri + ", configType=" + configType + ", mechid="
				+ mechid + ", channelId=" + channelId + ", businessId=" + businessId + ", storeId=" + storeId
				+ ", remart=" + remart + ", createAt=" + createAt + "]";
	}


}