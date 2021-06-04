package jp.co.seiko_sol.common;

import java.sql.Connection;
import java.sql.SQLException;

import org.apache.commons.dbcp2.BasicDataSource;

/*
 * JDBCAdapter用コネクションファクトリ
 * @author　ひでぽんた
 * @version 1.0
 */
public class ConnectionFactory {

	private static BasicDataSource dataSource;
    
	private ConnectionFactory() {
	}

	/*
	 * DBコネクションの取得
	 * @param 
	 * @return 
	 */
	public static Connection getConnection() throws SQLException {
	  
		if (dataSource == null) {
			dataSource = new BasicDataSource();
			//dataSource.setUrl("jdbc:postgresql://10.208.77.60:5432/toms_#1049?useSSL=false");
			//dataSource.setDriverClassName("org.postgresql.Driver");
			//dataSource.setUsername("postgres");
			//dataSource.setPassword("postgres");
			dataSource.setUrl("jdbc:postgresql://127.0.0.1:15432/tomssdx01?useSSL=false");
			dataSource.setDriverClassName("org.postgresql.Driver");
			dataSource.setUsername("jadadco");
			dataSource.setPassword("jdac2019#doa");
			//dataSource.setUrl("jdbc:postgresql://doxsdx01.caagwsqxswsb.ap-northeast-1.rds.amazonaws.com:15432/tomssdx01?useSSL=false");
			//dataSource.setDriverClassName("org.postgresql.Driver");
			//dataSource.setUsername("jadadco");
			//dataSource.setPassword("jdac2019#doa");
		}
		return dataSource.getConnection();
	}
}