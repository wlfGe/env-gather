package com.briup.smart.env.client;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.util.Collection;

import org.junit.Test;

import com.briup.smart.env.entity.Environment;
import com.briup.smart.env.util.Backup;
import com.briup.smart.env.util.BackupImpl;

public class GatherImplTest {

	@Test
	public void test() {
		try {
			Collection<Environment> collection = new GatherImpl().gather();
			System.out.println(collection);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	@Test
	public void test_append() {
		BufferedReader in = null;
		Backup backup = new BackupImpl();
		
		try {
			String filepath = "src/main/resources/data-file-simple-backup";
			File file = new File(filepath);
			in = new BufferedReader(new FileReader(file));
			
			long currentLen = file.length();//文件长度
//			System.out.println(len);
			
			String fileName = "last_file_length";
			Object obj = backup.load(fileName, Backup.LOAD_UNREMOVE);
			if(obj!=null){
				in.skip((long)obj);
			}
			
			
			
			String line = null;
			while((line=in.readLine())!=null) {
				if("".equals(line.trim())) {
					continue;
				}
				System.out.println(line);
			}
			
			backup.store(fileName, currentLen, Backup.STORE_OVERRIDE);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

}
