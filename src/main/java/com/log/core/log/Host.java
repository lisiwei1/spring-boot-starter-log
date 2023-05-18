package com.log.core.log;

/**
 * @Author lsw
 * @Date 2023/5/6 13:16
 */
public class Host {

    // 服务名称
    private String serverName;

    // 主机IP
    private String hostIp;

    // 主机名称
    private String HostName;


    public Host(String serverName, String hostIp, String hostName) {
        this.serverName = serverName;
        this.hostIp = hostIp;
        HostName = hostName;
    }

}
