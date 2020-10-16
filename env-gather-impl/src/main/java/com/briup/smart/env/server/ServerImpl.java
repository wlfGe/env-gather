package com.briup.smart.env.server;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.List;
import java.util.Properties;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import com.briup.smart.env.Configuration;
import com.briup.smart.env.constant.EnvGatherConstants;
import com.briup.smart.env.entity.Environment;
import com.briup.smart.env.support.ConfigurationAware;
import com.briup.smart.env.support.PropertiesAware;
import com.briup.smart.env.util.Log;

public class ServerImpl implements Server,PropertiesAware,ConfigurationAware{
	
	private int reciverServerPort;
	
	private int shutdownServerPort;
	
	private DBStore dbStore = new DBStoreImpl();

	private volatile boolean flag = true;
	
	private ServerSocket reciverServer;
	private ServerSocket shutdownServer;
	
	private ExecutorService threadPool = Executors.newFixedThreadPool(5);
	
	private Socket socket;
	private ObjectInputStream in;
	
	private Log logger;
	
	@Override
	public void reciver() throws Exception {
		
		logger.info("shutdown服务器已经启动："+shutdownServerPort);
		startShutdownServer();
		
		logger.info("reciver服务器已经启动："+reciverServerPort);
		startReciverServer();
		
	}
	
	/**
	 * 启动reciver服务器（接收服务器）
	 * 作用：该服务器的端口9999，负责接收客户端发送的数据
	 */
	private void startReciverServer()throws Exception {
		
		try {
			
			reciverServer = new ServerSocket(reciverServerPort);
			
			while(flag) {
				logger.debug("reciver服务器正在等待客户端连接");
				
				socket = reciverServer.accept();
				
				logger.debug("reciver服务器接收到客户端连接");
				logger.debug("reciver服务器使用线程池处理客户端");
				threadPool.execute(()->{
					try {
						
						in = new ObjectInputStream(socket.getInputStream());
						
						@SuppressWarnings("unchecked")
						List<Environment> list = (List<Environment>)in.readObject();
						
						dbStore.saveDB(list);
						
						socket.close();
						
					} catch (Exception e) {
						e.printStackTrace();
					}finally {
						if(socket!=null) {
							try {
								socket.close();
							} catch (IOException e) {
								e.printStackTrace();
							}
						}
					}
					
					
				});
				
			}
			
			
		} finally {
			if(socket!=null) {
				socket.close();
			}
			if(reciverServer!=null) {
				reciverServer.close();
			}
		}
		
	}

	/**
	 * 启动shutdown服务器（关闭服务器）
	 * 作用：该服务器的端口8888，负责接收用户发送的消息，可以关闭所有服务器（reciver和shutdown）
	 */
	private void startShutdownServer()throws Exception {
		
		
//		Runnable run = ()->{};
		Runnable run = ()->{
			
			try {
				shutdownServer = new ServerSocket(shutdownServerPort);
				
				shutdownServer.accept();
				
				ServerImpl.this.shutdown();
				
			}catch (Exception e) {
				e.printStackTrace();
			}finally {
				if(shutdownServer!=null) {
					try {
						shutdownServer.close();
					} catch (IOException e) {
						e.printStackTrace();
					}
				}
			}
			
		};
		
		new Thread(run).start();
		
	}

	@Override
	public void shutdown() throws Exception {
		this.flag = false;
		reciverServer.close();
		threadPool.shutdown();//当serverSocket关闭时，线程池也应关闭
	}

	@Override
	public void init(Properties properties) throws Exception {
		reciverServerPort = Integer.parseInt(properties.getProperty(EnvGatherConstants.MODULE_SERVER_RECIVER_SERVER_PORT));
		shutdownServerPort = Integer.parseInt(properties.getProperty(EnvGatherConstants.MODULE_SERVER_SHUTDOWN_SERVER_PORT));
		
	}

	@Override
	public void setConfiguration(Configuration configuration) throws Exception {
		logger = configuration.getLogger();
		
	}

}
