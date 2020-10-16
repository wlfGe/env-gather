package com.briup.smart.env.server;

import java.sql.Timestamp;
import java.util.Calendar;

import org.junit.Test;


public class DBStoreImplTest {

	@Test
	public void test_date() {
		Timestamp date = new Timestamp(System.currentTimeMillis());
//		System.out.println(date.getDate());//21
//		System.out.println(date.getDay());//1
		
		Calendar calendar = Calendar.getInstance();
		calendar.setTime(date);
		int dayOfMonth = calendar.get(Calendar.DAY_OF_MONTH);
		System.out.println(dayOfMonth);
	}

}
