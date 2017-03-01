// Copyright (c) 2015 D1SM.net
package net.fs.client;

import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import net.fs.rudp.Route;

public class PortMapManager {
	
	MapClient mapClient;

	Gson gson = new Gson();
	
	ArrayList<MapRule> mapList=new ArrayList<MapRule>();
	
	HashMap<Integer, MapRule> mapRuleTable=new HashMap<Integer, MapRule>();
	
	String configFilePath="port_map.json";
	
	PortMapManager(MapClient mapClient){
		this.mapClient=mapClient;
		//listenPort();
		loadMapRule();
	}


	
	void loadMapRule(){
		String content;
		Map<String,List<Map<String,String>>> json=null;
		try {
			content = readFileUtf8(configFilePath);
			json =gson.fromJson(content,new TypeToken<Map<String,List<Map<String,String>>>>(){}.getType());
		} catch (Exception e) {
			//e.printStackTrace();
		}
		if(json!=null&&json.containsKey("map_list")){
			List<Map<String,String>> json_map_list=json.get("map_list");
			for(int i=0;i<json_map_list.size();i++){
				Map<String,String> json_rule=json_map_list.get(i);
				MapRule mapRule=new MapRule();
				mapRule.name=json_rule.get("name");
				mapRule.listen_port=Integer.parseInt(json_rule.get("listen_port"));
				mapRule.dst_port=Integer.parseInt(json_rule.get("dst_port"));
				mapList.add(mapRule);
				ServerSocket serverSocket;
				try {
					serverSocket = new ServerSocket(mapRule.getListen_port());
					listen(serverSocket);
					mapRule.serverSocket=serverSocket;
				} catch (IOException e) {
					mapRule.using=true;
					e.printStackTrace();
				}
				mapRuleTable.put(mapRule.listen_port, mapRule);
			}
		}

	}
	
	MapRule getMapRule(String name){
		MapRule rule=null;
		for(MapRule r:mapList){
			if(r.getName().equals(name)){
				rule=r;
				break;
			}
		}
		return rule;
	}
	
	public ArrayList<MapRule> getMapList() {
		return mapList;
	}

	public void setMapList(ArrayList<MapRule> mapList) {
		this.mapList = mapList;
	}

	void listen(final ServerSocket serverSocket){
		Route.es.execute(new Runnable() {

			@Override
			public void run() {
				while(true){
					try {
						final Socket socket=serverSocket.accept();
						Route.es.execute(new Runnable() {
							
							@Override
							public void run() {
								int listenPort=serverSocket.getLocalPort();
								MapRule mapRule=mapRuleTable.get(listenPort);
								if(mapRule!=null){
									Route route=null;
									if(mapClient.isUseTcp()){
										route=mapClient.route_tcp;
									}else {
										route=mapClient.route_udp;
									}
									PortMapProcess process=new PortMapProcess(mapClient,route, socket,mapClient.serverAddress,mapClient.serverPort,null, 
											null,mapRule.dst_port);
								}
							}
							
						});

					} catch (IOException e) {
						e.printStackTrace();
						break;
					}
				}
			}
		});
	}
	
	void saveFile(byte[] data,String path) throws Exception{
		FileOutputStream fos=null;
		try {
			fos=new FileOutputStream(path);
			fos.write(data);
		} catch (Exception e) {
			throw e;
		} finally {
			if(fos!=null){
				fos.close();
			}
		}
	}
	
	public static String readFileUtf8(String path) throws Exception{
		String str=null;
		FileInputStream fis=null;
		DataInputStream dis=null;
		try {
			File file=new File(path);

			int length=(int) file.length();
			byte[] data=new byte[length];

			fis=new FileInputStream(file);
			dis=new DataInputStream(fis);
			dis.readFully(data);
			str=new String(data,"utf-8");

		} catch (Exception e) {
			//e.printStackTrace();
			throw e;
		}finally{
			if(fis!=null){
				try {
					fis.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
			if(dis!=null){
				try {
					dis.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}

		return str;
	}
}
