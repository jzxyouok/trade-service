package com.zyhao.openec.util;
import java.io.UnsupportedEncodingException;
import java.net.Inet4Address;
import java.net.InetAddress;
import java.net.InterfaceAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.net.URLEncoder;
import java.util.Enumeration;
import java.util.List;


public class Utils {

	public static String QRfromGoogle(String chl) throws Exception {
		int widhtHeight = 300;
		String EC_level = "L";
		int margin = 0;
		chl = UrlEncode(chl);
		String QRfromGoogle = "http://chart.apis.google.com/chart?chs=" + widhtHeight + "x" + widhtHeight
				+ "&cht=qr&chld=" + EC_level + "|" + margin + "&chl=" + chl;

		return QRfromGoogle;
	}
	
	// 特殊字符处理
	public static String UrlEncode(String src)  throws UnsupportedEncodingException {
		return URLEncoder.encode(src, "UTF-8").replace("+", "%20");
	}
	
    @SuppressWarnings("rawtypes")
	public
    static  String localIp(){
      String ip = null;
       Enumeration allNetInterfaces;
       try {
           allNetInterfaces = NetworkInterface.getNetworkInterfaces();            
           while (allNetInterfaces.hasMoreElements()) {
               NetworkInterface netInterface = (NetworkInterface) allNetInterfaces.nextElement();
               List<InterfaceAddress> InterfaceAddress = netInterface.getInterfaceAddresses();
               for (InterfaceAddress add : InterfaceAddress) {
                  InetAddress Ip = add.getAddress();
                   if (Ip != null && Ip instanceof Inet4Address) {
                       ip = Ip.getHostAddress();
                   }
               }
           }
       } catch (SocketException e) {
           // TODO Auto-generated catch block        
//           logger.warn("获取本机Ip失败:异常信息:"+e.getMessage());
       }
       return ip;
   }
}
