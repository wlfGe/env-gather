package com.briup.smart.env;

import org.junit.Test;

import com.briup.smart.env.client.Gather;

public class ConfigurationImplTest {

	@Test
	public void test1() {
		Configuration config1 = ConfigurationImpl.getInstance();
		Configuration config2 = ConfigurationImpl.getInstance();
		
		System.out.println(config1 == config2);
	}
	
	@Test
	public void test2() {
		Configuration config = ConfigurationImpl.getInstance();
		try {
			Gather gather = config.getGather();
			System.out.println(gather);
		} catch (Exception e) {
			e.printStackTrace();
		}
		
	}
	
	@Test
	public void test3() {
		ConfigurationImpl.getInstance();
	}

}
