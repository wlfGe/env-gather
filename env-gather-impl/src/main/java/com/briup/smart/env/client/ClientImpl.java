package com.briup.smart.env.client;

import java.io.ObjectOutputStream;
import java.net.Socket;
import java.util.Collection;
import java.util.Properties;

import com.briup.smart.env.Configuration;
import com.briup.smart.env.constant.EnvGatherConstants;
import com.briup.smart.env.entity.Environment;
import com.briup.smart.env.support.ConfigurationAware;
import com.briup.smart.env.support.PropertiesAware;
import com.briup.smart.env.util.Log;

public class ClientImpl implements Client,PropertiesAware,ConfigurationAware{
	
	private String host;
	private int port;
	private Log logger;
	
	@Override
	public void send(Collection<Environment> c) throws Exception {
		
		if(c==null || c.size()==0) {
			logger.warn("客户端网络模块接收到的集合长度为0");
			return;
		}
		
		Socket socket = null;
		
		ObjectOutputStream out = null;
		
		try {
			socket = new Socket(host, port);
			
			logger.debug("客户端向服务器端连接成功");
			
			out = new ObjectOutputStream(socket.getOutputStream());
			
			logger.debug("客户端向服务器端开始发送数据");
			
			out.writeObject(c);
			out.flush();
			
			logger.debug("客户端向服务器端发送数据完成");
			
		} finally {
			if(out!=null) {
				out.close();
			}
			if(socket!=null) {
				socket.close();
			}
		}
		
		
		
	}

	@Override
	public void init(Properties properties) throws Exception {
		host = properties.getProperty(EnvGatherConstants.MODULE_CLIENT_HOST);
		port = Integer.parseInt(properties.getProperty(EnvGatherConstants.MODULE_CLIENT_PORT));
		
	}

	@Override
	public void setConfiguration(Configuration configuration) throws Exception {
		logger = configuration.getLogger();
	}
	
}
