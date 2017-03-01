// Copyright (c) 2015 D1SM.net

package net.fs.client;

import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import net.fs.rudp.Route;
import net.fs.utils.LogOutputStream;
import net.fs.utils.MLog;
import org.pcap4j.core.Pcaps;

public class Client {

    MapClient mapClient;

    ClientConfig config = null;

    String configFilePath = "client_config.json";

    String domain = "";

    String homeUrl;

    public static Client ui;

    Exception capException = null;
    boolean b1 = false;

    String systemName = null;

    String updateUrl;
    
    boolean min=false;
    
    LogOutputStream los;
    
    boolean tcpEnable=true;

    {
        domain = "ip4a.com";
        homeUrl = "http://www.ip4a.com/?client_fs";
        updateUrl = "http://fs.d1sm.net/finalspeed/update.properties";
    }

    Client() {
        los=new LogOutputStream(System.out);
        System.setOut(los);
        System.setErr(los);

        systemName = System.getProperty("os.name").toLowerCase();
        MLog.info("System: " + systemName + " " + System.getProperty("os.version"));
        ui = this;
        loadConfig();
        Route.localDownloadSpeed=config.downloadSpeed;
        Route.localUploadSpeed=config.uploadSpeed;

        boolean tcpEnvSuccess=true;

        Thread thread = new Thread() {
            public void run() {
                try {
                    Pcaps.findAllDevs();
                    b1 = true;
                } catch (Exception e3) {
                    e3.printStackTrace();

                }
            }
        };
        thread.start();
        try {
            thread.join();
        } catch (InterruptedException e1) {
            e1.printStackTrace();
        }
        if (!b1) {
        	tcpEnvSuccess=false;
            String msg = "启动失败,请先安装libpcap,否则无法使用tcp协议";
            if (systemName.contains("windows")) {
                msg = "启动失败,请先安装winpcap,否则无法使用tcp协议";
            }
            MLog.println(msg);
            if (systemName.contains("windows")) {
                try {
                    Process p = Runtime.getRuntime().exec("winpcap_install.exe", null);
                } catch (IOException e) {
                    e.printStackTrace();
                }
                tcpEnable=false;
                //System.exit(0);
            }
        }


        try {
            mapClient = new MapClient(this,tcpEnvSuccess);
        } catch (final Exception e1) {
            e1.printStackTrace();
            capException = e1;
        }

        mapClient.setMapServer(config.getServerAddress(), config.getServerPort(), config.getRemotePort(), null, null, config.isDirect_cn(), config.getProtocal().equals("tcp"),
                null);

    }

    ClientConfig loadConfig() {
        Gson gson = new Gson();
        ClientConfig cfg = new ClientConfig();
        if (!new File(configFilePath).exists()) {
            Map map = new HashMap<String,String>();
            try {
                saveFile(gson.toJson(map).getBytes(), configFilePath);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        try {
            String content = readFileUtf8(configFilePath);

            Map<String,String> json = gson.fromJson(content,   new TypeToken<Map<String,String>>() {
            }.getType());
            cfg.setServerAddress(json.get("server_address").toString());
            cfg.setServerPort(Integer.parseInt(json.get("server_port")));
            cfg.setRemotePort(parseInt("remote_port",json));
            cfg.setRemoteAddress(json.get("remote_address"));
            if (json.containsKey("direct_cn")) {
                cfg.setDirect_cn(Boolean.parseBoolean(json.get("direct_cn")));
            }
            cfg.setDownloadSpeed(Integer.parseInt(json.get("download_speed")));
            cfg.setUploadSpeed(Integer.parseInt(json.get("upload_speed")));
            if (json.containsKey("socks5_port")) {
                cfg.setSocks5Port(Integer.parseInt(json.get("socks5_port")));
            }
            if (json.containsKey("protocal")) {
                cfg.setProtocal(json.get("protocal"));
            }
            if (json.containsKey("auto_start")) {
                cfg.setAutoStart(Boolean.parseBoolean(json.get("auto_start")));
            }
            config = cfg;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return cfg;
    }

    public static int parseInt(String key,Map<String,String> map){
        try {
            return Integer.parseInt(map.get(key));
        }catch (Exception e){
            return 0;
        }
    }


    public static String readFileUtf8(String path) throws Exception {
        String str = null;
        FileInputStream fis = null;
        DataInputStream dis = null;
        try {
            File file = new File(path);

            int length = (int) file.length();
            byte[] data = new byte[length];

            fis = new FileInputStream(file);
            dis = new DataInputStream(fis);
            dis.readFully(data);
            str = new String(data, "utf-8");

        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        } finally {
            if (fis != null) {
                try {
                    fis.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            if (dis != null) {
                try {
                    dis.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }

        return str;
    }

    void saveFile(byte[] data, String path) throws Exception {
        FileOutputStream fos = null;
        try {
            fos = new FileOutputStream(path);
            fos.write(data);
        } catch (Exception e) {
            if (systemName.contains("windows")) {
                MLog.info("保存配置文件失败,请尝试以管理员身份运行! " + path);
                System.exit(0);
            }
            throw e;
        } finally {
            if (fos != null) {
                fos.close();
            }
        }
    }

}
