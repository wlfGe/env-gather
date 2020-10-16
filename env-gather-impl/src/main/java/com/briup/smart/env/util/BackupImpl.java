package com.briup.smart.env.util;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.Properties;

import com.briup.smart.env.constant.EnvGatherConstants;
import com.briup.smart.env.support.PropertiesAware;

public class BackupImpl implements Backup,PropertiesAware{

	private String baseLocation;
	
	@Override
	public Object load(String fileName, boolean del) throws Exception {
		ObjectInputStream in = null;
		FileInputStream fis = null;
		Object obj = null;
		
		try {
			File file = new File(baseLocation, fileName);
			
			if(!file.exists()) {
				return null;
			}
			
			fis = new FileInputStream(file);
			in = new ObjectInputStream(fis);
			
			obj = in.readObject();
			
			if(del) {
				file.delete();
			}
		} finally {
			if(fis!=null) {
				fis.close();
			}
			if(in!=null) {
				in.close();
			}
		}
		return obj;
	}

	@Override
	public void store(String fileName, Object obj, boolean append) throws Exception {
		ObjectOutputStream out = null;//序列化操作 将Java对象转换为字节序列
		FileOutputStream fos = null;
		
		try {
			File file = new File(baseLocation, fileName);
			fos = new FileOutputStream(file,append);
			
			out = new ObjectOutputStream(fos);
			
			out.writeObject(obj);
			out.flush();
		} finally {
			if(fos!=null) {
				fos.close();
			}
			if(out!=null) {
				out.close();
			}
		}
	}

	@Override
	public void init(Properties properties) throws Exception {
		baseLocation = properties.getProperty(EnvGatherConstants.MODULE_BACKUP_BASE_LOCATION);
		
	}

}
