
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.text.SimpleDateFormat;
import java.util.Date;

class SaveLog {
	private static final String TAG = SaveLog.class.getSimpleName();
	private static boolean mToFile = true;
	private static String mLogFilePath = null;
	private static SaveLog mInstance = null;
	
	public static SaveLog getInstance() {
		if (mInstance == null) {
			mInstance = new SaveLog();
			mLogFilePath = mInstance.getClass().getResource("/").getPath() + "Log/";
		}
		return mInstance;
	}

	public static void setFilePath(String path) {
		getInstance().mLogFilePath = path;
	}
	
	public static String getFilePath() {
		return getInstance().mLogFilePath;
	}
	
	/*
	 * "yyyy-MM-dd HH:mm:ss"
	 */
	public static String getTime(String format) {
		SimpleDateFormat df = new SimpleDateFormat(format);
		String date = df.format(new Date());
		return date;
	}
	
	public static synchronized void logToFile(String path, String info) {
		File pathFile = null;
		String currrentDate = getTime("yyyy-MM-dd") + ".txt";
		try {
			if (path != null) {
				pathFile = new File(path + getTime("yyyy-MM-dd") + ".txt");
			}
			if(pathFile != null && !pathFile.exists()) {
				pathFile.createNewFile();
			}
		} catch (Exception e) {
			pathFile = null;
		}
		
		OutputStream out = null;
		if (pathFile != null && pathFile.exists()) {
			String newContent = getTime("yyyy-MM-dd HH:mm:ss") + "\r\n" + info + "\r\n";
			try {
				out = new FileOutputStream(pathFile, true);
				out.write(newContent.getBytes("utf-8"));
				out.flush();
				out.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
			try {
				if (out != null) {
					out.close();
				}
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		
		
	}
}