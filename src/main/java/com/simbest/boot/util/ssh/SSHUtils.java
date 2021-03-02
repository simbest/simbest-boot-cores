package com.simbest.boot.util.ssh;

import com.jcraft.jsch.ChannelExec;
import com.jcraft.jsch.JSch;
import com.jcraft.jsch.JSchException;
import com.jcraft.jsch.Session;
import com.simbest.boot.util.BootAppFileReader;
import lombok.*;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.IOUtils;

import java.io.IOException;
import java.io.InputStream;

/**
 * <strong>Title : SSHUtils</strong><br>
 * <strong>Description :JAVA使用JSch进行SSH连接Linux并执行命令工具类 </strong><br>
 * <strong>Create on : 2021/2/23</strong><br>
 * <strong>Modify on : 2021/2/23</strong><br>
 * <strong>Copyright (C) Ltd.</strong><br>
 *
 * @author LJW lijianwu@simbest.com.cn
 * @version <strong>V1.0.0</strong><br>
 * <strong>修改历史:</strong><br>
 * 修改人 修改日期 修改描述<br>
 * -------------------------------------------<br>
 */
@Slf4j
public class SSHUtils {

    private static final String ENCODING = "UTF-8";

    public static Session getJSchSession(DestHost destHost) {
        JSch jsch = new JSch();
        Session session = null;
        try {
            session = jsch.getSession(destHost.getUsername(), destHost.getHost(), destHost.getPort());
            session.setPassword(destHost.getPassword());
            session.setConfig("StrictHostKeyChecking", "no");  // 第一次访问服务器时不用输入yes
            session.setTimeout(destHost.getTimeout());
            session.connect();
        } catch (JSchException e) {
            e.printStackTrace();
        }
        return session;
    }

    /**
     * 免密码方式登录
     * @param destHost
     * @param priKey   密钥
     * @return
     */
    public static Session getJSchSessionByNoPwd(DestHost destHost,String keyFilePath) {
        JSch jsch = new JSch();
        Session session = null;
        try {
            //添加私钥
            jsch.addIdentity(null, BootAppFileReader.getClasspathFileToString(keyFilePath).getBytes(), null,null);
            session = jsch.getSession(destHost.getUsername(), destHost.getHost(), destHost.getPort());
            //session.setPassword(destHost.getPassword());
            session.setConfig("StrictHostKeyChecking", "no");  // 第一次访问服务器时不用输入yes
            session.setTimeout(destHost.getTimeout());
            session.connect();
        } catch (JSchException e) {
            e.printStackTrace();
        }
        return session;
    }


    public static String execCommandByJSch(DestHost destHost, String command) {
        return execCommandByJSch(destHost, command, ENCODING);
    }

    public static String execCommandByJSch(DestHost destHost, String command, String resultEncoding) {
        Session session = getJSchSession(destHost);
        String result = execCommandByJSch(session, command, resultEncoding);
        session.disconnect();
        return result;
    }

    public static String execCommandByJSch(Session session, String command) {
        return execCommandByJSch(session, command, ENCODING);
    }

    public static String execCommandByJSch(Session session, String command, String resultEncoding) {
        ChannelExec channelExec;
        InputStream in;
        String result = null;
        try {
            channelExec = (ChannelExec) session.openChannel("exec");
            in = channelExec.getInputStream();
            channelExec.setCommand(command);
            channelExec.setErrStream(System.err);
            channelExec.connect();
            result = IOUtils.toString(in, resultEncoding);
            channelExec.disconnect();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (JSchException e) {
            e.printStackTrace();
        }
        return result;
    }

    /**
     * 目标登录主机信息
     */
    @Data
    @Builder
    @AllArgsConstructor
    @NoArgsConstructor
    public static class DestHost {

        private String host = "";
        private String username = "";
        private String password = "";
        private int port = 22;
        private int timeout = 60 * 60 * 1000;

    }


    public static void main(String[] args) {
        try {
            //SSHUtils.DestHost host = new SSHUtils.DestHost("192.168.1.110", "root", "root");
            SSHUtils.DestHost host = new DestHost.DestHostBuilder().host("192.168.1.110").username("root").password("root").build();
            String stdout = "";
//			stdout = SSHUtils.execCommandByJSch(host, "whoami");
            Session shellSession = SSHUtils.getJSchSession(host);
            stdout = SSHUtils.execCommandByJSch(shellSession, "cd ~");
            stdout = SSHUtils.execCommandByJSch(shellSession, "mkdir testtesttest");
            stdout = SSHUtils.execCommandByJSch(shellSession, "whoami");
            shellSession.disconnect();
            System.out.println(stdout);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
