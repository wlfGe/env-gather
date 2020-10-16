package com.briup.smart.env.client;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Properties;

import com.briup.smart.env.Configuration;
import com.briup.smart.env.constant.EnvGatherConstants;
import com.briup.smart.env.entity.Environment;
import com.briup.smart.env.support.ConfigurationAware;
import com.briup.smart.env.support.PropertiesAware;
import com.briup.smart.env.util.Backup;
import com.briup.smart.env.util.Log;

public class GatherImpl implements Gather,PropertiesAware,ConfigurationAware{
	
	private String filePath;
	
	private Backup backup;

	private String bakFileName;
	
	private Log logger;
	
	@Override
	public Collection<Environment> gather() throws Exception {
		
		List<Environment> list = new ArrayList<Environment>();
		
		BufferedReader in = null;
		
		try {
			
			File file = new File(filePath);
			
			long currentLen = 0;
			
			if(file.exists()) {
				in = new BufferedReader(new FileReader(file));
				//文件中共有多少个字节
				currentLen = file.length();
			}
			else {
				InputStream is = GatherImpl.class.getClassLoader().getResourceAsStream(filePath);
				in = new BufferedReader(new InputStreamReader(is));
				//可以一次不阻塞的读取多少个字节
				currentLen = is.available();
			}
			
			
			Object obj = backup.load(bakFileName , Backup.LOAD_UNREMOVE);
			if(obj!=null) {
				in.skip((long)obj);
			}
			
			String line = null;
			
			Environment env1 = null;
			//如果当前是温度和湿度的数据，env2就会被用到
			Environment env2 = null;
			
			while((line=in.readLine())!=null) {

				if("".equals(line.trim())) {
					logger.warn("采集模块读到空行");
					continue;
				}
				
				//100|101|2|16|1|3|5d606f7802|1|1516323596029
				// 0   1  2  3 4 5      6     7       8
				
				String[] arr = line.split("[|]");
				
				if(arr.length==9) {
					env1 = new Environment();
					
					//发送端id
					env1.setSrcId(arr[0]);
					//树莓派系统id
					env1.setDesId(arr[1]);
					//实验箱区域模块id(1-8)
					env1.setDevId(arr[2]);
					//模块上传感器地址 16 256 1280
					env1.setSersorAddress(arr[3]);
					//传感器个数
					env1.setCount(Integer.parseInt(arr[4]));
					//发送指令标号 3表示接收数据 16表示发送命令
					env1.setCmd(arr[5]);
					//状态 默认1表示成功
					env1.setStatus(Integer.parseInt(arr[7]));
					//采集时间
					env1.setGather_date(new Timestamp(Long.parseLong(arr[8])));
					
					
					switch (env1.getSersorAddress()) {
						case "16":
							
							env2 = copyObj(env1);
							
							env1.setName("温度");
							String dataStr1 = arr[6].substring(0,4);
							int data1 = Integer.parseInt(dataStr1, 16);
							//(data*(0.00268127F))-46.85F
							env1.setData((data1*(0.00268127F))-46.85F);
							
							env2.setName("湿度");
							String dataStr2 = arr[6].substring(4,8);
							int data2 = Integer.parseInt(dataStr2, 16);
							//(data*0.00190735F)-6
							env2.setData((data2*0.00190735F)-6);
							
							list.add(env1);
							list.add(env2);
							
							break;
							
						case "256":
							
							env1.setName("光照强度");
							String dataStr3 = arr[6].substring(0,4);
							env1.setData(Integer.parseInt(dataStr3, 16));
							
							break;
							
						case "1280":
							env1.setName("二氧化碳");
							String dataStr4 = arr[6].substring(0,4);
							env1.setData(Integer.parseInt(dataStr4, 16));
							
							break;
	
						default:
							
							logger.error("数据类型有误："+env1.getSersorAddress()+"（16、256、1280）");
							
							break;
					}
					
					
				}else {
					logger.error("当前读到数据不符合要求："+line);
				}
				
			}
			
			backup.store(bakFileName, currentLen, Backup.STORE_OVERRIDE);
			
		} finally {
			if(in!=null) {
				in.close();
			}
		}
		
		
		return list;
	}
	
	
	@SuppressWarnings("unchecked")
	public <T> T copyObj(T t) throws InstantiationException, IllegalAccessException {
		
		Class<T> c = (Class<T>) t.getClass();
		
//		T copy2 = new T();
		T copy = c.newInstance();
		
//		copy.setName(t.getName());
		Field[] fields = c.getDeclaredFields();
		
		for(Field f:fields) {
			//如果这个属性是final修饰的，那么就跳过
			if(Modifier.isFinal(f.getModifiers())) {
				continue;
			}
			
			//设置私有属性可访问
			f.setAccessible(true);
			//获取原对象中f属性的值
			//String value = s1.name;
			Object value = f.get(t);
			//给copy对象设置f属性的值
			//copy.name = value;
			f.set(copy, value);
		}
		return copy;
	}


	@Override
	public void init(Properties properties) throws Exception {
		filePath = (String) properties.getProperty(EnvGatherConstants.MODULE_GATHER_DATA_FILE_PATH);
		bakFileName = (String) properties.getProperty(EnvGatherConstants.MODULE_GATHER_BAK_FILE_PATH);
	}


	@Override
	public void setConfiguration(Configuration configuration) throws Exception {
		backup = configuration.getBackup();
		logger = configuration.getLogger();
	}
	
	

}
