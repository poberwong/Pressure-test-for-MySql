package testing;

import java.sql.*;
import util.JDBCUtil;

public class TestMain {
	private Connection conn = null;
	private PreparedStatement ps = null;
	private ResultSet rs = null;
	private String sql;
	private String sqlTemp;

	public void add(int tableCount, int fieldCount, int dataCount)// 添加测试,三个参数分别是：
	// 所操作的表的个数，字段数，数据量
	{
		Thread currentThread = Thread.currentThread();
		int count = 0;
		try {
			conn = JDBCUtil.getConnection();// 获取数据库连接,，异常被外部捕获后，程序自动停止。而异常被抛进来之后，程序会觉得外部没有异常，这样依旧会持续那个永真while
			tableCount = tableCount > tableCount() ? tableCount() : tableCount;// 在目标表的数量和实际表数量选小的作为实际值
			fieldCount = fieldCount > fieldCount() ? fieldCount() : fieldCount;
			/*
			 * 开始拼接sql语句
			 */
			sqlTemp = " (";
			String val = ")values(";
			for (int i = 0; i < fieldCount - 1; i++) {// 不用给主键赋值
				sqlTemp += "id" + i + ",";// 拼接前n-1个字段
				val += "0,";// 给所有条目的内容添加0
			}
			sqlTemp += "id" + (fieldCount - 1) + val + "0);";// sql语句的后半部分拼接完毕

			while (dataCount > 0) {
				for (int i = 0; i < tableCount; i++) {
					sql = "insert into " + "target" + i + sqlTemp;// 表示向targeti这个表中插入数据
					ps = conn.prepareStatement(sql);// 因此，我们只需要在异常抛出的地方，设置一下对应窗体需要显示的内容即可
					ps.executeUpdate();// 执行更新
					dataCount--;
					count++;
				}
			}
		} catch (SQLException e) {// 捕获处理
			// TODO: handle exception
			synchronized (this) {
			Gui.jta.append(currentThread.getName() + "挂掉，所插入的数据为：" + count+ '\n');
			currentThread.stop();	
			}
		} finally {
			JDBCUtil.close(rs, ps, conn);
		}
	}

	public void query(int tableCount, int dataCount)// 查询测试
	{
		Thread currenThread = Thread.currentThread();
		int id = 0, count = 0;
		try {
			conn = JDBCUtil.getConnection();// 连接数据库
			tableCount = tableCount > tableCount() ? tableCount() : tableCount;// 在目标表的数量和实际表数量选小的作为实际值
			int counting = counter(tableCount);// 记录所有表中的记录总数
			counting = (dataCount < counting ? dataCount : counting);// 获取需要查询数据量和总数据量之间的较小值
			Gui.jta.append("数据查询量为：" + counting + '\n');// 表示不论线程是否挂掉，都要输出查询总量，因为如果没挂掉，则这个数就表示查询到的结果
			sqlTemp = " where id0= ?;";// 表名不可以用占位符，只有值可以用，因为在数据库中，任何书
			while (counting > 0)// 查询的最终结束条件。
			{
				for (int i = 0; i < tableCount; i++) {
					sql = "select * from target" + i + sqlTemp;
					ps = conn.prepareStatement(sql);// 创建预编译器
					ps.setInt(1, id);
					ps.executeQuery();// 执行查询
					counting--;
					count++;
				}
				id++;
			}
		} catch (Exception e) {
			// TODO: handle exception
			synchronized (this) {
				Gui.jta.append(currenThread.getName() + "挂掉，查询量：" + count+'\n');// 如果挂掉，就输出所查询成功的次数
				currenThread.stop();
			}
		} finally {
			JDBCUtil.close(rs, ps, conn);
		}
	}

	public void addTable(int tableCount)// 加表技术，当让也可以是负数，这样表示删除表，不论是添加还是删除，都是倒序。
	{
		try {
			conn = JDBCUtil.getConnection();// 连接数据库,将异常抛进来了
			int count = tableCount();// 获取当前表的数量

			if (count == 0)// 如果test库里没有表，那么就直接创建表
			{
				for (int i = 0; i < tableCount; i++) {
					ps = conn.prepareStatement("create table target" + i
							+ "(id0 int(10) primary key auto_increment)");// 倒着开始删除表
					ps.execute();// 执行
				}
				System.out.println(123456);
			} else if (tableCount <= 0)// 删除表的操作
			{
				tableCount = -tableCount;//
				for (int i = count - 1; (i > count - 1 - tableCount) && i >= 0; i--) {
					ps = conn.prepareStatement("drop table target" + i + ";");// 倒着开始删除表
					ps.execute();// 执行删除
				}
			} else {// 添表操作
				for (int i = count; i < (count + tableCount); i++) {
					sql = "create table target" + i + " like target0;";// 以表0为模板加表
					ps = conn.prepareStatement(sql);
					ps.execute();
				}
			}
		} catch (Exception e) {
			// TODO: handle exception
		} finally {
			JDBCUtil.close(rs, ps, conn);
		}
		Gui.jta.append("add Table successfully!!!\n");// 通知添表成功
	}

	public void addField(int fieldCount)// 为所有表添加字段，同样包括删除操作
	{
		sqlTemp = "alter table ";
		try {
			conn = JDBCUtil.getConnection();// 连接数据库,将异常抛进来了
			int fieldscount = fieldCount();
			int tablecount = tableCount();// 获取当前表的总数

			for (int i = 0; i < tablecount; i++) {// 遍历每一个表
				if (fieldCount < 0)// 删除字段
				{
					for (int j = fieldscount - 1; j > fieldscount + fieldCount
							- 1; j--) {
						sql = sqlTemp + "target" + i + " drop column id" + j
								+ ";";
						ps = conn.prepareStatement(sql);
						ps.execute();
					}
				} else {
					for (int j = fieldscount; j < fieldscount + fieldCount; j++) {
						sql = sqlTemp + "target" + i + " add column id" + j
								+ " int(10);";
						ps = conn.prepareStatement(sql);
						ps.execute();
					}
				}
			}
		} catch (Exception e) {
			// TODO: handle exception
		} finally {
			JDBCUtil.close(rs, ps, conn);
		}
		Gui.jta.append("add field successfully!!!\n");// 通知添加成功
	}

	public void clean()// 清空所有表，因为做测试用，所以每次用的时候最好全部清空一次好点。
	{
		sqlTemp = "truncate table ";
		try {
			conn = JDBCUtil.getConnection();// 连接数据库,将异常抛进来了
			int count = tableCount();
			for (int i = 0; i < count; i++) {
				sql = sqlTemp + "target" + i + ";";
				ps = conn.prepareStatement(sql);
				ps.execute();
			}
		} catch (Exception e) {
			// TODO: handle exception
		} finally {
			JDBCUtil.close(rs, ps, conn);
		}
		Gui.jta.append("clean successfully!!!\n");
	}

	public int counter(int tableCount) throws SQLException// 统计目标表中的所有记录条数，主要是为查询提供数据量的上限。
	{// 为什么不查询所有表的记录数呢？ 因为在我们做查询测试的时候，数据量的上限主要由对应表的总记录数决定，而非test库中记录总数
		int count = 0;
		sqlTemp = "select count(id0) as c from ";
		for (int i = 0; i < tableCount; i++) {
			sql = sqlTemp + "target" + i + ";";
			ps = conn.prepareStatement(sql);
			rs = ps.executeQuery();
			rs.next();// 默认游标指的是rs数据集中数据的前一个游标，故应该是0；
			count += rs.getInt("c");// 临时存储所获取的结果
		}
		return count;
	}

	public int tableCount() throws SQLException// 获取当前表的总数
	{
		ps = conn.prepareStatement("show tables;");
		rs = ps.executeQuery();
		rs.last();
		return rs.getRow();// 获取当前表的数量
	}

	public int fieldCount() throws SQLException {
		ps = conn.prepareStatement("desc target0;");// 呈现表结构
		rs = ps.executeQuery();
		rs.last();
		return rs.getRow();// 获取字段总数
	}
}
