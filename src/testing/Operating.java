package testing;

public class Operating implements Runnable {
	private String operation;//给该线程做一个具体的动作
	private int tableCount= 1;//定义表的数量
	private int fieldCount= 1;//定义字段数量
	private int dataCount= 0;
	private TestMain tm= new TestMain();
	public Operating(String operation, int tableCount, int fieldCount, int dataCount) {
		// TODO Auto-generated constructor stub
		this.operation= operation;
		this.tableCount= tableCount;
		this.fieldCount= fieldCount;
		this.dataCount= dataCount;
	}
	@Override
	public  void run() {
		// TODO Auto-generated method stub
		long startTime= System.currentTimeMillis();//获取执行前的时间
		switch (operation) {
		case "insert testing":
			tm.add(tableCount, fieldCount, dataCount);
			break;
		case "query testing": 
			tm.query(tableCount, dataCount);
			break;
		case "clean": 
			tm.clean();
			break;
		case "add table":
			tm.addTable(Integer.parseInt(Gui.jtf5.getText()));
			break;
		case "add column":
			tm.addField(Integer.parseInt(Gui.jtf6.getText()));
		}
		String ThreadName= Thread.currentThread().getName();
		Gui.jta.append(ThreadName+"-->"+operation+": "+(System.currentTimeMillis()-startTime)+"ms\n");	
	}
}
