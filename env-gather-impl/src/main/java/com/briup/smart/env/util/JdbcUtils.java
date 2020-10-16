package com.briup.smart.env.util;

import java.io.InputStream;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;
import java.util.function.Function;

import javax.sql.DataSource;

import com.alibaba.druid.pool.DruidDataSourceFactory;

public class JdbcUtils {

	private static DataSource dataSource;
	static {
		Properties properties = new Properties();
		InputStream inputStream = JdbcUtils.class.getClassLoader().getResourceAsStream("druid.properties");
		try {
			properties.load(inputStream);
			dataSource = DruidDataSourceFactory.createDataSource(properties);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public static Connection getConnection() throws SQLException {
		return dataSource.getConnection();

	}

	// DDL DML
	public static int executeUpdate(String sql) {
		int rows = 0;
		Connection conn = null;
		Statement stmt = null;

		try {
			conn = getConnection();
			stmt = conn.createStatement();
			rows = stmt.executeUpdate(sql);
		} catch (SQLException e) {
			e.printStackTrace();
		} finally {
			close(stmt, conn);
		}

		return rows;

	}

	// 查询结果具体封装成什么类型的对象，让将来用户来决定（泛型）
	public static <T> T queryForObject(String sql, Function<ResultSet, T> f) {
		T t = null;

		Connection conn = null;
		Statement stmt = null;
		ResultSet rs = null;

		try {
			conn = getConnection();
			stmt = conn.createStatement();
			rs = stmt.executeQuery(sql);
			
			//rs -->obj 到时候用户传入function就行了，执行该方法就行
			t = f.apply(rs);
		} catch (SQLException e) {
			e.printStackTrace();
		} finally {
			close(rs, stmt, conn);
		}
		// rs -->obj(T)
		return t;
	}

	// 利于实现的queryForList方法，顺序实现queryForOne
	// 该方法表示查询结果只需要封装为一个指定类型对象即可
	public static <T> Object queryForOne(String sql, Class<T> clazz) {
		return queryForList(sql, clazz).get(0);
	}

	// 该方法表示将查询结果封装为指定类型的对象，并存放集合中，再返回
	public static <T> List<T> queryForList(String sql, Class<T> clazz) {
		List<T> result = new ArrayList<>();

		Connection conn = null;
		Statement stmt = null;
		ResultSet rs = null;

		try {
			conn = getConnection();
			stmt = conn.createStatement();
			rs = stmt.executeQuery(sql);
			
			// 解析这个传入的类型（clazz），获取到它的每一个 属性类型和属性名
			List<TypeAndName> list = parse(clazz);

			T obj = null;

			while (rs.next()) {

				// 反射创建对象
				obj = clazz.newInstance();
				// 循环拿到指定类型中的每一个 属性类型和属性名字
				for (TypeAndName typeAndName : list) {
					// 根据属性类型，决定调用getXxx方法获取查询结果中指定字段上的值
					// 并将值封装到对象的属性中
					if ("long".equals(typeAndName.type)) {
						long value = rs.getLong(typeAndName.name);
						typeAndName.invokeSet(obj, long.class, value);
					} else if ("String".equals(typeAndName.type)) {
						String value = rs.getString(typeAndName.name);
						typeAndName.invokeSet(obj, String.class, value);
					} else if ("int".equals(typeAndName.type)) {
						int value = rs.getInt(typeAndName.name);
						typeAndName.invokeSet(obj, int.class, value);
					}
					// ...如果需要可以继续编写其他类型和getXxx方法的对应关系

				}
				// 将封装好的对象存入集合
				result.add(obj);
			}

		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			close(rs, stmt, conn);
		}

		return null;
	}

	// 解析后，返回封装好的List集合，里面有属性名和属性类型
	private static <T> List<TypeAndName> parse(Class<T> clazz) {
		Field[] declaredFields = clazz.getDeclaredFields();
		List<TypeAndName> list = new ArrayList<>();

		for (int i = 0; i < declaredFields.length; i++) {
			Field field = declaredFields[i];
			String name = field.getName();//获取属性
			String type = field.getType().getSimpleName();//获取属性名
			list.add(new TypeAndName(type, name));//存入到list中
		}
		return list;
	}

	public static void close(ResultSet rs, Statement stmt, Connection conn) {
		if (rs != null) {
			try {
				rs.close();
			} catch (SQLException e) {
				e.printStackTrace();
			}
		}
		if (stmt != null) {
			try {
				stmt.close();
			} catch (SQLException e) {
				e.printStackTrace();
			}
		}
		if (conn != null) {
			try {
				conn.close();
			} catch (SQLException e) {
				e.printStackTrace();
			}
		}

	}

	public static void close(Statement stmt, Connection conn) {
		close(null, stmt, conn);
	}

	// 私有的静态内部类，辅助解析类型（clazz）
	// 封装指定类型中的 属性类型和属性名
	private static class TypeAndName {

		String type;
		String name;

		public TypeAndName(String type, String name) {
			this.type = type;
			this.name = name;
		}

		// 反射调用指定对象中的setXxx方法，将值存放到属性中
		public <T> void invokeSet(Object obj, Class<T> c, Object value) {
			try {
				Method m = obj.getClass().getDeclaredMethod("set" + initCap(name), c);
				m.invoke(obj, value);
			} catch (Exception e) {
				e.printStackTrace();
			}
		}

		// 字符串首字母大写
		private String initCap(String name) {
			return name.substring(0, 1).toUpperCase() + name.substring(1);
		}
	}

}
