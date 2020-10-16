package com.briup.smart.env.shutdown;

import java.io.IOException;
import java.net.Socket;

public class ReceiverServerShutdown {

	private String host = "127.0.0.1";
	private int port = 8888;
	
	public void shutdown() {
		Socket socket = null;
		try {
			socket = new Socket(host,port);
			if(socket!=null) {
				socket.close();
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	public static void main(String[] args) {
		ReceiverServerShutdown s = new ReceiverServerShutdown();
		s.shutdown();
	}
}
