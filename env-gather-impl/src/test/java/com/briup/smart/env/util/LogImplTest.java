package com.briup.smart.env.util;


import org.apache.log4j.Logger;
import org.junit.Test;

public class LogImplTest {
	
	private static final Logger logger = Logger.getRootLogger();

	@Test
	public void testLog() {
		logger.debug("debug test");
		
		logger.info("info test");
		
		logger.warn("warn test");
		
		logger.error("error test");
		
	}

}
