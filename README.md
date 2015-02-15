# Pressure-test-for-MySql
由java实现的一个多线程对Mysql数据库测试
本次设计采用java语言连接mysql数据库，利用jdbc来实现具体的操作
##程序模块 ：
   
(1)util包：	
JDBCUtil 类和 jdbc.properties ，后者为mysql数据库root用户账户信息、数据库驱动以及数据库地址的配置信息。前者是数据库的连接获取以及关闭流方法。


(2)testing包：
Info负责信息的描述
Gui类负责用户界面以及按键响应的实现
Operating负责实现多线程操作的实现
TestMain 负责具体数据库具体操作的实现

##详细设计 
###JDBCUtil工具类的实现：
```Java
public class JDBCUtil {
	private JDBCUtil(){}//防止类外构造
	Private static ResourceBundle rb= ResourceBundle.getBundle("util/jdbc");
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
```
在这里，我们把配置文件jdbc.properties里的内容的加载，以及jdbc驱动的加载都写到了静态块里，这样是为了在编译以及运行的时候，直接随着类的加载而加载。做到多次运行，只加载一次。

```Java
public static Connection getConnection()throws SQLException//创建连接方法,因为每一个操纵都需要得到一个连接，所以需要返回值
	{
		Connection conn= null;//此处因为可能和出现连接失败的情况，容易使得conn变为空，因此为了防止出现异常，赋给默认值null
		conn= DriverManager.getConnection(URL, USER, PASSWORD);
		return conn;
	}
```
我们把异常抛出了，是为了后期在处理的时候对异常的更好的捕捉

```Java
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
```
单独把数据库各种流的关闭提取出来，因为有些操作用不到resultSet，所以在每一个流关闭之前，都需要判断一下是否为空。

###package testing;
```Java
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
			conn = JDBCUtil.getConnection();//获取数据库连接,，异常被外部捕获后，程序自动停止。而异常被抛进来之后，程序会觉                                      //得外部没有异常，这样依旧会持续那个永真while
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
```
以上分别是插入方法、查询方法、加表、加字段（也可以减）、查询当前表个数、当前每个表所具备的字段数量、清空所有表数据。
具体的说明在方法注释上已经备注。作为内嵌的几个功能，我没有将它单独连接数据库，而是直接嵌入到了某一次测试当中。这样可以减少对数据库额外连接消耗。
对于多线程并发的处理，这里的两个测试方法里一旦出现异常，会及时捕获，然后在运行结果框里声明当前线程挂掉，同时也杀死当前线程。防止后续的计时机构受到影响。
在多线程运行的时候还发现就是在打印结果日志的时候，会出现由于线程并发而引起的打印混乱的情况，因此我在打印以及杀死线程的地方用到了互斥锁synchronized来对该代码块进行线程同步。


###package testing;// Gui界面的实现以及各个控件之间与对应功能的协调。
```Java
public class Gui extends JFrame implements ActionListener{
	private JTabbedPane jtp;
	private JLabel jl0,jl1,jl2,jl3,jl4,jl5,jl6,jl7;//显示线程数,表数目,字段数,数据量,添加表,添加字段,运行结果。
	
	public static JTextField jtf1,jtf2,jtf3,jtf4,jtf5,jtf6;//与标签相对应的文本框
	private GridLayout gl= null;
	
	private JPanel jp1,jp2,jp3,jp4,jp5;
	
	private JButton jb1,jb2,jb3,jb4,jb5;

	private JScrollPane jsp= null;
	public static JTextArea jta= null;
	
	public Gui() {
		// TODO Auto-generated constructor stub
		this.setTitle("数据库压力测试");
		
		jtp= new JTabbedPane();
		jl0= new JLabel(new ImageIcon("images/picture.png"));
		jl1= new JLabel("线程数",JLabel.CENTER);
		jl2= new JLabel("表数目",JLabel.CENTER);
		jl3= new JLabel("字段数",JLabel.CENTER);
		jl4= new JLabel("数据量",JLabel.CENTER);
		jl5= new JLabel("添加表",JLabel.CENTER);
		jl6= new JLabel("添加字段",JLabel.CENTER);
		jl7= new JLabel("运行结果",JLabel.CENTER);
		jtf1= new JTextField("1",10);
		jtf2= new JTextField("1",10);
		jtf3= new JTextField("1",10);
		jtf4= new JTextField("0",10);
		jtf5= new JTextField("0",10);
		jtf6= new JTextField("0",10);
		
		jtf1.setHorizontalAlignment(JTextField.CENTER);
		jtf2.setHorizontalAlignment(JTextField.CENTER);
		jtf3.setHorizontalAlignment(JTextField.CENTER);
		jtf4.setHorizontalAlignment(JTextField.CENTER);
		jtf5.setHorizontalAlignment(JTextField.CENTER);
		jtf6.setHorizontalAlignment(JTextField.CENTER);
		
		jb1= new JButton("插入测试");
		jb2= new JButton("查询测试");
		jb3= new JButton("清空表");
		jb4= new JButton("添加表");
		jb5= new JButton("添加列");
		
		jb1.addActionListener(this);//注册5个按钮监听
		jb2.addActionListener(this);
		jb3.addActionListener(this);
		jb4.addActionListener(this);
		jb5.addActionListener(this);
		
		jta= new JTextArea(5,20);
		jsp= new JScrollPane(jta);
		
		jp1= new JPanel();
		jp2= new JPanel();
		jp3= new JPanel();
		jp4= new JPanel();
		jp5= new JPanel();
		
		gl= new GridLayout(5,2);
		gl.setHgap(5);	gl.setVgap(5);
		jp1.setLayout(gl);//5行2列的网格布局
		jp1.add(jl1);	jp1.add(jtf1); 
		jp1.add(jl2);	jp1.add(jtf2);
		jp1.add(jl3);	jp1.add(jtf3);
		jp1.add(jl4);	jp1.add(jtf4);
		jp1.add(jb1);	jp1.add(jb2); 
		
		gl= new GridLayout(3,1);
		gl.setVgap(5);
		jp4.setLayout(gl);
		jp4.add(jb3); jp4.add(jb4); jp4.add(jb5);
		
		gl= new GridLayout(2,2);
		gl.setHgap(5);	gl.setVgap(5);
		jp5.setLayout(gl);
		jp5.add(jl5);	jp5.add(jtf5);
		jp5.add(jl6);	jp5.add(jtf6);
		jp2.setLayout(new BorderLayout());
		jp2.add(jp5,BorderLayout.NORTH);	jp2.add(jp4);
		
		jp3.setLayout(new BorderLayout());//设置画板上的布局
		jp3.add(jl7,BorderLayout.NORTH);
		jp3.add(jsp);
		
		jtp.add(jp1);	jtp.add(jp2);
		jp1.setName("压力测试");	jp2.setName("修改表结构");
	
		jtp.add(jp1);
		jtp.add(jp2);
		this.add(jl0,BorderLayout.NORTH);//添加图片
		this.add(jtp);//添加窗格
		this.add(BorderLayout.SOUTH,jp3);
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		setVisible(true);
		setBounds(200, 200, 270, 400);
		setResizable(false);
	}
		
	@Override
	public void actionPerformed(ActionEvent e) {
		// TODO Auto-generated method stub	
		int threadCount= Integer.parseInt(jtf1.getText());
		int tableCount= Integer.parseInt(jtf2.getText());
		int fieldCount= Integer.parseInt(jtf3.getText());
		int dataCount= Integer.parseInt(jtf4.getText());
		Operating operating;
		String operation;
		Object op= e.getSource();
		jta.setText("..............\n");
		
		if(op== jb1)//这里也可以在注册监听的地方用setActionCommand,然后再这里使用getActionCommand获取发送的信息
		{//插入被点击
			operation= "insert testing";
		}
		else if(op== jb2)//查询被点击
		{
			operation= "query testing";
		}
		else if(op== jb3)//清空被点击
		{
			operation= "clean";
			threadCount= 1;
		}
		else if(op== jb4)//添加表
		{
			threadCount= 1;
			operation= "add table";
		}
		else{//添加列
			threadCount= 1;
			operation= "add column";
		}
		
		operating= new Operating(operation, tableCount, fieldCount, dataCount);
		for (int i = 0; i < threadCount; i++) {//创建指定数量的线程并启动
			new Thread(operating,"线程"+i).start();
		}
	}
	
	public static void main(String[] args) {
		new Gui();
	}
}
```

##测试与分析 
	            
测试页面展示

![](https://github.com/Bob1993/ImageCache/blob/master/Images/数据库压力测试1.png)
![](https://github.com/Bob1993/ImageCache/blob/master/Images/数据库压力测试2.png)
在输入数据的时候，我们如果不知道当前的表的数量以及字段数，这个时候如果不处理好，就会在删除表以及删除字段的时候出现难以想象的bug，同时在数据测试的时候也会出现查询范围或者插入范围错乱的情况，因此在每次操作之前，都会将操作数和当前已存在的数据量进行对比，都是在取合理值在进行操作。





插入测试：
 
![](https://github.com/Bob1993/ImageCache/blob/master/Images/数据库压力测试3.png)

在这里，虽然显示是20张表 20个字段，其实真正的库只有10张表，9个字段。

查询测试：

![](https://github.com/Bob1993/ImageCache/blob/master/Images/数据库压力测试4.png)

这个是双线程的查询测试。
