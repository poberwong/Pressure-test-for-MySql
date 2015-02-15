package util;

import java.sql.*;
import java.util.ResourceBundle;

public class JDBCUtil {
	private JDBCUtil(){}//防止类外构造
	private static ResourceBundle rb= ResourceBundle.getBundle("util/jdbc");
	private static String URL= null;
	private static String USER= null;
	private static String PASSWORD= null;
	private static String DRIVER= null;
	static {//不能再静态块里构造静态变量，最好放在块外
		URL= rb.getString("jdbc.url");
		USER= rb.getString("jdbc.user");
		PASSWORD= rb.getString("jdbc.password");
		DRIVER= rb.getString("jdbc.driver");
		try {
			Class.forName(DRIVER);
		} catch (ClassNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	public static Connection getConnection()throws SQLException//创建连接方法,因为每一个操纵都需要得到一个连接，所以需要返回值
	{
		Connection conn= null;//此处因为可能和出现连接失败的情况，容易使得conn变为空，因此为了防止出现异常，赋给默认值null
		conn= DriverManager.getConnection(URL, USER, PASSWORD);
	/*	try {
			conn= DriverManager.getConnection(URL, USER, PASSWORD);
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.toString();
		}*/
		return conn;
	}
	
	public static void close(ResultSet rs, Statement st,Connection conn)
	{
			try {
			if(rs!= null)	rs.close();
			if(st!= null)	st.close(); 
			if(conn!= null)   conn.close();
			} catch (SQLException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
	}
}
