
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.text.SimpleDateFormat;
import java.util.Date;

class SaveLog{
	private boolean fileLog = true;
	private String logFileName = "C:/ServerLog/adbserverlog/";//指定程序执行结果保存的文件路径
	private long lasttime = -1;
	private String lasttile = null;
	
	public SaveLog() {
		
	}
	
	public OutputStream getOutputStream(){
		lasttile = getTime("yyyy-MM-dd");
		try {
			if(fileLog) {
				File file = new File(logFileName + lasttile + ".txt");
				if(!file.exists())
					file.createNewFile();
				return new FileOutputStream(file, true);	
			} else {
				return System.out;
			}
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return System.out;
	}

	public String getTime(String format) {
		SimpleDateFormat df = new SimpleDateFormat(format/*"yyyy-MM-dd HH:mm:ss"*/);//设置日期格式
		String date = df.format(new Date());// new Date()为获取当前系统时间，也可使用当前时间戳
		return date;
	}
	
	public synchronized void log(String info) {
		OutputStream out = getOutputStream();
		
		String temp = getTime("yyyy-MM-dd HH:mm:ss") + "\r\n" + info;
		try {
			out.write(temp.getBytes("utf-8"));
			out.flush();
			out.close();
		} catch (UnsupportedEncodingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
}