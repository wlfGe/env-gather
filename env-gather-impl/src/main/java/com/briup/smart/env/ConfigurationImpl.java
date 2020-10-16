package com.briup.smart.env;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Properties;

import org.dom4j.Document;
import org.dom4j.DocumentException;
import org.dom4j.Element;
import org.dom4j.io.SAXReader;

import com.briup.smart.env.client.Client;
import com.briup.smart.env.client.Gather;
import com.briup.smart.env.server.DBStore;
import com.briup.smart.env.server.Server;
import com.briup.smart.env.support.ConfigurationAware;
import com.briup.smart.env.support.PropertiesAware;
import com.briup.smart.env.util.Backup;
import com.briup.smart.env.util.Log;

public class ConfigurationImpl implements Configuration {

	private static final ConfigurationImpl CONFIG = new ConfigurationImpl();

	// Map负责存储 配置模块中创建 出来的其他模块对象 K：模块名字，V：模块对象
	private static Map<String, Object> map = new HashMap<>();

	private static Properties properties = new Properties();

	static {
		
		try {
			parseXML("conf-deploy.xml");
//			parseXML("src/main/resources/conf.xml");
			initModule();
		} catch (Exception e) {
			e.printStackTrace();
		}

	}

	
	private static void initModule() throws Exception{
		for(Object obj:map.values()) {
			if(obj instanceof PropertiesAware) {
				((PropertiesAware)obj).init(properties);
			}
			
			if(obj instanceof ConfigurationAware) {
				((ConfigurationAware) obj).setConfiguration(CONFIG);
			}
			
		}
	}
	
	private static void parseXML(String filePath) {
		InputStream in = ConfigurationImpl.class.getClassLoader().getResourceAsStream(filePath);
		try {
			Document document = parse(in);
			handler(document);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	private static void handler(Document document) throws Exception {
		// 获取根节点
		Element root = document.getRootElement();

		for (Iterator<Element> it = root.elementIterator(); it.hasNext();) {
			// 拿到遍历到的元素
			Element element = it.next();
			String moduleName = element.getName();
			String className = element.attribute("class").getValue();
//			System.out.println(moduleName+" : "+className);
			Object value = Class.forName(className).newInstance();
			map.put(moduleName.toUpperCase(), value);

			for (Iterator<Element> childIt = element.elementIterator(); childIt.hasNext();) {
				Element childElement = childIt.next();
				String elementName = childElement.getName();
				String elementText = childElement.getText();

//				System.out.println(elementName+" : "+elementText);
				properties.put(elementName, elementText);
			}

		}

	}

	// 构造器声明为private
	private ConfigurationImpl() {
	}

	public static ConfigurationImpl getInstance() {

		return CONFIG;
	}


	@Override
	public Log getLogger() throws Exception {
		return (Log) map.get(ModuleName.LOGGER.name());
	}

	@Override
	public Server getServer() throws Exception {
		return (Server) map.get(ModuleName.SERVER.name());
	}

	@Override
	public Client getClient() throws Exception {
		return (Client) map.get(ModuleName.CLIENT.name());
	}

	@Override
	public DBStore getDbStore() throws Exception {
		return (DBStore) map.get(ModuleName.DBSTORE.name());
	}

	@Override
	public Gather getGather() throws Exception {
		return (Gather) map.get(ModuleName.GATHER.name());
	}

	@Override
	public Backup getBackup() throws Exception {
		return (Backup) map.get(ModuleName.BACKUP.name());
	}

	@SuppressWarnings("unused")
	private static Document parse(File file) throws DocumentException, FileNotFoundException {
        return parse(new FileInputStream(file));
    }
	
	private static Document parse(InputStream in) throws DocumentException {
        SAXReader reader = new SAXReader();
        Document document = reader.read(in);
        return document;
    }

	/**
	 * 模块名
	 * 
	 * @author 王龙飞
	 * @description
	 * @date 2020年9月23日上午10:04:26
	 */
	private enum ModuleName {
		GATHER, CLIENT, SERVER, DBSTORE, BACKUP, LOGGER, CONFIG;
	}

}
