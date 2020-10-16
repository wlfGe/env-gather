package com.briup.smart.env.server;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.Timestamp;
import java.util.Calendar;
import java.util.Collection;
import java.util.HashSet;
import java.util.Properties;
import java.util.Set;

import com.briup.smart.env.Configuration;
import com.briup.smart.env.constant.EnvGatherConstants;
import com.briup.smart.env.entity.Environment;
import com.briup.smart.env.support.ConfigurationAware;
import com.briup.smart.env.support.PropertiesAware;
import com.briup.smart.env.util.JdbcUtils;
import com.briup.smart.env.util.Log;

public class DBStoreImpl implements DBStore,PropertiesAware,ConfigurationAware {
	
	private int batchSize;
	
	private Log logger;

	@Override
	public void saveDB(Collection<Environment> c) throws Exception {
		
		Set<Integer> set = new HashSet<>();
		
		Connection conn = null;
		PreparedStatement ps = null;
		
		try {
			conn = JdbcUtils.getConnection();
			
			conn.setAutoCommit(false);
			
			int last_dayOfMonth = -1;
			
			String sql = null;
			
			//当前批处理中有多少条数据
			int i = 0;
			
			for(Environment env : c) {
				
				Timestamp date = env.getGather_date();
				
				Calendar calendar = Calendar.getInstance();
				
				calendar.setTime(date);
				
				int dayOfMonth = calendar.get(Calendar.DAY_OF_MONTH);
				set.add(dayOfMonth);
				//e_detail_x
				
				if(last_dayOfMonth == -1) {
					sql = "insert into e_detail_"+dayOfMonth+"(name,srcId,desId,devId,sersorAddress,count,cmd,status,data,gather_date) values(?,?,?,?,?,?,?,?,?,?)";
					ps = conn.prepareStatement(sql);
				}else {
					
					if(dayOfMonth != last_dayOfMonth) {
						ps.executeBatch();
						ps.close();
						i = 0;
						sql = "insert into e_detail_"+dayOfMonth+"(name,srcId,desId,devId,sersorAddress,count,cmd,status,data,gather_date) values(?,?,?,?,?,?,?,?,?,?)";
						ps = conn.prepareStatement(sql);
					}
					
				}
				
				ps.setString(1,env.getName());
				ps.setString(2,env.getSrcId());
				ps.setString(3,env.getDesId());
				ps.setString(4,env.getDevId());
				ps.setString(5,env.getSersorAddress());
				ps.setInt(6,env.getCount());
				ps.setString(7,env.getCmd());
				ps.setInt(8,env.getStatus());
				ps.setFloat(9,env.getData());
				ps.setTimestamp(10,env.getGather_date());
				
				ps.addBatch();
				i++;
				
				if(i==batchSize) {
					ps.executeBatch();
					i = 0;
				}
				
				last_dayOfMonth = dayOfMonth;
			}
			
			//最后在提交一次，保证没有遗漏数据
			ps.executeBatch();
			
			conn.commit();
			
			logger.debug("入库模块执行完成，数据分布："+set);
			
		} finally {
			JdbcUtils.close(ps, conn);
		}
		
	}

	@Override
	public void init(Properties properties) throws Exception {
		batchSize = Integer.parseInt(properties.getProperty(EnvGatherConstants.MODULE_DBSTORE_BATCH_SIZE));
		
	}

	@Override
	public void setConfiguration(Configuration configuration) throws Exception {
		logger = configuration.getLogger();
		
	}

}
