package com.veelur.sync.elasticsearch.util;

import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.http.HttpServletRequest;
import java.net.Inet4Address;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.util.Enumeration;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @author 余海
 * @version 1.0
 * @description ip帮助类
 * @create 2018-06-26 下午4:27
 */
public class IpUtils {

    private static final Logger logger = LoggerFactory.getLogger(IpUtils.class);

    private static String serverIp;


    /**
     * 获取requestip
     *
     * @param request
     * @return
     */
    public static String getRequestIpAddress(HttpServletRequest request) {
        if (request == null) return "";
        String ip = request.getHeader("X-Forwarded-For");

        if (ip == null || ip.length() == 0 || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("X-Real-IP");
        }
        if (ip == null || ip.length() == 0 || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("Proxy-Client-IP");
        }
        if (ip == null || ip.length() == 0 || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("WL-Proxy-Client-IP");
        }
        if (ip == null || ip.length() == 0 || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("HTTP_CLIENT_IP");
        }
        if (ip == null || ip.length() == 0 || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("HTTP_X_FORWARDED_FOR");
        }
        if (ip == null || ip.length() == 0 || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getRemoteAddr();
        }
        String p[] = ip.split("[,]", 10);
        if (p.length == 0) {
            return ip;
        }
        if (p.length == 1) {
            return p[0];
        }
        if ("unknown".equalsIgnoreCase(p[0])) {
            return p[1];
        }
        return p[0];
    }

    /**
     * 获取服务器ip
     *
     * @return
     */
    public static String getServerIpAddress() {
        if (null == serverIp) {
            synchronized (IpUtils.class) {
                if (null == serverIp) {
                    serverIp = getIpAddress();
                }
            }
        }
        return serverIp;
    }

    /**
     * 获取mac地址
     *
     * @return
     */
    private static String getMacAddress() {
        try {
            Enumeration<NetworkInterface> allNetInterfaces = NetworkInterface.getNetworkInterfaces();
            byte[] mac = null;
            while (allNetInterfaces.hasMoreElements()) {
                NetworkInterface netInterface = (NetworkInterface) allNetInterfaces.nextElement();
                if (netInterface.isLoopback() || netInterface.isVirtual() || !netInterface.isUp()) {
                    continue;
                } else {
                    mac = netInterface.getHardwareAddress();
                    if (mac != null) {
                        StringBuilder sb = new StringBuilder();
                        for (int i = 0; i < mac.length; i++) {
                            sb.append(String.format("%02X%s", mac[i], (i < mac.length - 1) ? "-" : ""));
                        }
                        if (sb.length() > 0) {
                            return sb.toString();
                        }
                    }
                }
            }
        } catch (Exception e) {
            logger.error("MAC地址获取失败:" + e.getMessage(), e);
        }
        return "";
    }

    /**
     * 获取ip地址
     *
     * @return
     */
    private static String getIpAddress() {
        String preIp = "";
        try {
            Enumeration<NetworkInterface> allNetInterfaces = NetworkInterface.getNetworkInterfaces();
            InetAddress ip = null;
            while (allNetInterfaces.hasMoreElements()) {
                NetworkInterface netInterface = (NetworkInterface) allNetInterfaces.nextElement();
                if (netInterface.isLoopback() || netInterface.isVirtual() || !netInterface.isUp()) {
                    continue;
                } else {
                    Enumeration<InetAddress> addresses = netInterface.getInetAddresses();
                    while (addresses.hasMoreElements()) {
                        ip = addresses.nextElement();
                        if (ip != null && ip instanceof Inet4Address) {
                            String lip = ip.getHostAddress().substring(ip.getHostAddress().lastIndexOf("."));
                            if (!"0".equals(lip) && "1".equals(lip)) {
                                return ip.getHostAddress();
                            }
                            if (!"127.0.0.1".equals(ip.getHostAddress())) {
                                preIp = ip.getHostAddress();
                            }
                        }
                    }
                }
            }
        } catch (Exception e) {
            logger.error("IP地址获取失败:" + e.getMessage(), e);
        }
        return preIp;
    }


    /**
     * 判断是否为内网ip地址
     * 10.0.0.0 - 10.255.255.255
     * 172.16.0.0 - 172.31.255.255
     * 192.168.0.0 - 192.168.255.255
     *
     * @param ip
     * @return
     */
    public static boolean isInnerIP(String ip) {
        if (StringUtils.isEmpty(ip)) {
            return false;
        }

        if (ip.equals("127.0.0.1")) {
            return true;
        }

        //合起来写
        String pattern = "((192\\.168|172\\.([1][6-9]|[2]\\d|3[01]))"
                + "(\\.([2][0-4]\\d|[2][5][0-5]|[01]?\\d?\\d)){2}|"
                + "^(\\D)*10(\\.([2][0-4]\\d|[2][5][0-5]|[01]?\\d?\\d)){3})";

        Pattern reg = Pattern.compile(pattern);
        Matcher match = reg.matcher(ip);

        return match.find();

    }
}

