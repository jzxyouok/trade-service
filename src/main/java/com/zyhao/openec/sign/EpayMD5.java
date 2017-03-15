package com.zyhao.openec.sign;
import java.io.UnsupportedEncodingException;
import java.security.SignatureException;
import org.apache.commons.codec.digest.DigestUtils;
/**
 *类名：EpayMD5  				  <br/>
 *详细：EpayMD5签名类                                     <br/>
 *版本：1.0               		  <br/>				
 *日期：20130510				      <br/>
 *说明：根据参数和字符集获取签名          <br/> 
 **/
public class EpayMD5 {

	/**
     * 签名字符串
     * @param val 需要签名的字符串
     * @param key 密钥
     * @param input_charset 编码格式
     * @return 签名结果
     */
    public static String sign(String val, String key, String input_charset) {
    	val = val + key;
        return DigestUtils.md5Hex(getContentBytes(val, input_charset));
    }
    
    /**
     * 验证签名字符串
     * @param val 需要签名的字符串
     * @param sign 签名结果
     * @param key 密钥
     * @param input_charset 编码格式
     * @return true:验证通过，false:验证失败
     */
    public static boolean validateSign(String val, String sign, String key, String input_charset) {
    	val = val + key;
    	String mysign = DigestUtils.md5Hex(getContentBytes(val, input_charset));
    	if(mysign.equals(sign)) {
    		return true;
    	}else{
    		return false;
    	}
    }

    /**
     * @param content
     * @param charset
     * @return
     * @throws SignatureException
     * @throws UnsupportedEncodingException 
     */
    private static byte[] getContentBytes(String content, String charset) {
        if (charset == null || "".equals(charset)) {
            return content.getBytes();
        }
        if(!"utf-8".equalsIgnoreCase(charset))
        	new  UnsupportedEncodingException();
        try {
            return content.getBytes(charset);
        } catch (UnsupportedEncodingException e) {
            throw new RuntimeException("MD5签名出现错误,编码集不对,错误编码集是:" + charset);
        }
    }
}