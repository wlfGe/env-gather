package com.briup.smart.env.main;

import com.briup.smart.env.Configuration;
import com.briup.smart.env.ConfigurationImpl;
import com.briup.smart.env.server.Server;

//服务器入口类
public class ServerMain {
	
	public static void main(String[] args) {
		Configuration config = ConfigurationImpl.getInstance();
		
		try {
			Server server = config.getServer();
			server.reciver();//接收数据
		} catch (Exception e) {
			e.printStackTrace();
		}
		
	}
	
}
