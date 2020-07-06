
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Timer;
import java.util.TimerTask;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JTextArea;
import javax.swing.JTextField;

public class MainUi implements ActionListener {

	private TcpServer mTcpServer;
	private Thread mThread;
	private boolean mStarted = false;
	private final String[] characters = {"start", "stop"};
	private JButton start;
	private JButton stop;
	//private JTextField status;
	private JTextArea status;
	
	private Timer mTimer = new Timer();
    private boolean needreboot = false;
    private int mTimeoutAdb = 0;
    private boolean checkstatus = false;
    //play time period
    private final int[] REBOOT_TIME = {
    		Calendar.AM, 0, 0, 0,
    		Calendar.AM, 6, 0, 0,
    		Calendar.AM, 11, 55, 0,
    		Calendar.PM, 12, 0, 0,
    		Calendar.PM, 6, 0, 0
    };
    private final int[] LUNCHER_TIME = {
    		Calendar.AM, 0, 30, 0,
    		Calendar.AM, 3, 30, 0,
    		Calendar.AM, 5, 30, 0
    };
    private final int[] TV_TIME = {
    		Calendar.AM, 6, 10, 0,
    		Calendar.AM, 6, 20, 0,
    		Calendar.AM, 6, 30, 0,
    		Calendar.PM, 12, 30, 0,
    		Calendar.PM, 6, 30, 0
    };
    //luncher time period
    
    private TimerTask mTask = new TimerTask() {
        @Override
        public void run() {
        	Calendar now = Calendar.getInstance();
        	int year = now.get(Calendar.YEAR);
        	int month = now.get(Calendar.MONTH) + 1;
        	int day = now.get(Calendar.DATE);
        	int hour = now.get(Calendar.HOUR);
        	int minute = now.get(Calendar.MINUTE);
        	int second = now.get(Calendar.SECOND);
        	int night = now.get(Calendar.AM_PM);
        	int[] currentTime = {night, hour, minute, second};

        	if (isRebootTime(currentTime)) {
        		rebootAdbCmd();
        	}
        	if (isLuncherTime(currentTime)) {
        		switchLuncherAdbCmd();
        	}
        	if (isTvTime(currentTime)) {
        		switchTvAdbCmd();
        	}
        }
    };
	
    private boolean isRebootTime(int[] currentTime) {
    	boolean isRebootTime = false;
    	int equalCount = 0;
    	if (currentTime != null && currentTime.length == 4) {
    		for (int i = 0; i < REBOOT_TIME.length / 4; i++) {
        		equalCount = 0;
        		for (int j = 0; j < currentTime.length; j++) {
        			if (REBOOT_TIME[i * 4 + j] == currentTime[j]) {
        				equalCount++;
        			}
        		}
        		if (equalCount == 4) {
        			isRebootTime = true;
        			break;
        		}
        	}
    	}
    	return isRebootTime;
    }
    
    private boolean isLuncherTime(int[] currentTime) {
    	boolean isLuncherTime = false;
    	int equalCount = 0;
    	if (currentTime != null && currentTime.length == 4) {
    		for (int i = 0; i < LUNCHER_TIME.length / 4; i++) {
        		equalCount = 0;
        		for (int j = 0; j < currentTime.length; j++) {
        			if (LUNCHER_TIME[i * 4 + j] == currentTime[j]) {
        				equalCount++;
        			}
        		}
        		if (equalCount == 4) {
        			isLuncherTime = true;
        			break;
        		}
        	}
    	}
    	return isLuncherTime;
    }
    
    private boolean isTvTime(int[] currentTime) {
    	boolean isTvTime = false;
    	int equalCount = 0;
    	if (currentTime != null && currentTime.length == 4) {
    		for (int i = 0; i < TV_TIME.length / 4; i++) {
        		equalCount = 0;
        		for (int j = 0; j < currentTime.length; j++) {
        			if (TV_TIME[i * 4 + j] == currentTime[j]) {
        				equalCount++;
        			}
        		}
        		if (equalCount == 4) {
        			isTvTime = true;
        			break;
        		}
        	}
    	}
    	return isTvTime;
    }
    
    private boolean adbConnected = false;
    
    private void connectAdbCmd() {
    	new Thread(new Runnable(){
			@Override
			public void run() {
				// TODO Auto-generated method stub
				String result = mTcpServer.callShell("adb connect homedevice.iask.in:39634");
				if (result != null && result.contains("connected to homedevice.iask.in")) {
					adbConnected = true;
				}
				SaveLog.logToFile(SaveLog.getFilePath(), "connectAdbCmd result = " + result);
			}
    		
    	}).start();
    }
    
    private void rebootAdbCmd() {
    	new Thread(new Runnable(){
			@Override
			public void run() {
				// TODO Auto-generated method stub
				String result = mTcpServer.callShell("adb connect homedevice.iask.in:39634");
				System.out.println("rebootAdbCmd connect = " + result);
				SaveLog.logToFile(SaveLog.getFilePath(), "rebootAdbCmd connect result = " + result);
				if (result != null && result.contains("connected to homedevice.iask.in")) {
					result = mTcpServer.callShell("adb reboot");
					SaveLog.logToFile(SaveLog.getFilePath(), "rebootAdbCmd reboot result = " + result);
					System.out.println("rebootAdbCmd reboot result = " + result); 
				}
			}
    		
    	}).start();
    }
    
    private void switchLuncherAdbCmd() {
    	new Thread(new Runnable(){
			@Override
			public void run() {
				// TODO Auto-generated method stub
				String result = mTcpServer.callShell("adb connect homedevice.iask.in:39634");
				if (result != null && result.contains("connected to homedevice.iask.in")) {
					System.out.println("switchLuncherAdbCmd");
					SaveLog.logToFile(SaveLog.getFilePath(), "switchLuncherAdbCmd");
        			//back
        			mTcpServer.callShell("adb shell \"input keyevent 4\"");
        			delay(1000);
        			mTcpServer.callShell("adb shell \"input keyevent 4\"");
        			delay(1000);
        			mTcpServer.callShell("adb shell \"input keyevent 4\"");
        			delay(1000);
        			mTcpServer.callShell("adb shell \"input keyevent 4\"");
        			delay(1000);
        			
        			//home
        			mTcpServer.callShell("adb shell \"input keyevent 3\"");
        			delay(1000);
        			mTcpServer.callShell("adb shell \"input keyevent 3\"");
        			delay(1000);

        			SaveLog.logToFile(SaveLog.getFilePath(), "switchLuncherAdbCmd switch to Luncher");
				}	
			}
    	}).start();
    }
    
    private void switchTvAdbCmd() {
    	new Thread(new Runnable(){
			@Override
			public void run() {
				// TODO Auto-generated method stub
				String result = mTcpServer.callShell("adb connect homedevice.iask.in:39634");
				if (result != null && result.contains("connected to homedevice.iask.in")) {
					result = mTcpServer.callShell("adb shell \"dumpsys window | grep mCurrentFocus\"");
					System.out.println("currentAdbCmd current = " + result);
					SaveLog.logToFile(SaveLog.getFilePath(), "currentAdbCmd current result = " + result);
	        		if (result != null && !result.contains("IPTVPlayerActivity")) {
	        			checkstatus = true;
	        			//back
	        			mTcpServer.callShell("adb shell \"input keyevent 4\"");
	        			delay(1000);
	        			mTcpServer.callShell("adb shell \"input keyevent 4\"");
	        			delay(1000);
	        			mTcpServer.callShell("adb shell \"input keyevent 4\"");
	        			delay(1000);
	        			mTcpServer.callShell("adb shell \"input keyevent 4\"");
	        			delay(1000);
	        			
	        			//home
	        			mTcpServer.callShell("adb shell \"input keyevent 3\"");
	        			delay(1000);
	        			
	        			//num 1
	        			mTcpServer.callShell("adb shell \"input keyevent 8\"");
	        			delay(1000);
	        			mTcpServer.callShell("adb shell \"input keyevent 8\"");
	        			delay(1000);
	        			SaveLog.logToFile(SaveLog.getFilePath(), "currentAdbCmd switch to cctv 11");
	        		} else {
	        			checkstatus = false;
	        			SaveLog.logToFile(SaveLog.getFilePath(), "currentAdbCmd is cctv 11");
	        		}
				}	
			}
    	}).start();
    }
    
    private void delay(int ms) {
    	try {
			Thread.sleep(ms);
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
    }
    
    /*public static void main(String[] args) {
        MainUi mMain = new MainUi();
        mMain.mTcpServer = new TcpServer(19900);
    	mMain.mThread = new Thread(mMain.mTcpServer);
    	mMain.InitUI();
    	mMain.mThread.start();
    	mMain.mStarted = true;
    	mMain.status.setText("started");
    	mMain.setInterface(mMain.mServerCallback);
    	mMain.mTimer.schedule(mMain.mTask, 0, 1000 * 1);//run per 1 second
    }*/
    
    private void startServer() {
    	if (!mStarted) {
    		mTcpServer.setInterface(null);
    		mThread = null;
    		mTcpServer = null;
    		mTcpServer = new TcpServer(19900);
    		mTcpServer.setInterface(mServerCallback);
    		mThread = new Thread(mTcpServer);
    		mStarted = true;
    		mThread.start();
    	}
    }
    
    private void stopServer() {
    	if (mStarted) {
    		mStarted = false;
    		mTcpServer.closeSelf();
    	}
    }
    
    private void setInterface(TcpServer.ServerCallback callback) {
    	mTcpServer.setInterface(callback);
    }
    
    public TcpServer.ServerCallback mServerCallback = new TcpServer.ServerCallback() {
    	public void setResult(String value) {
    		status.setText(SaveLog.getTime("HH:mm:ss") + "-" + (mStarted ? "started: " : "stopped: ") + value);
    		SaveLog.logToFile(SaveLog.getFilePath(), status.getText());
    	}
    };
    
	public void InitUI() {
		JFrame frame=new JFrame();
		frame.setTitle("ADB cmd Server");
		frame.setSize(600,300);
		frame.setLocationRelativeTo(null);
		frame.setDefaultCloseOperation(3);
		
		FlowLayout f1=new FlowLayout(FlowLayout.LEFT);
		frame.setLayout(f1);

		start = new JButton("start");
		start.setPreferredSize(new Dimension(80,30));
		start.setActionCommand(characters[0]);
		start.addActionListener(this);
		frame.add(start);
		
		stop = new JButton("stop");
		stop.setPreferredSize(new Dimension(80,30));
		stop.setActionCommand(characters[1]);
		stop.addActionListener(this);
		frame.add(stop);

		//status = new JTextField("");
		status = new JTextArea("");
		status.setPreferredSize(new Dimension(400,200));
		status.setLineWrap(true);
		frame.add(status);
		
		frame.addWindowListener(new MyWin()); 
		
		frame.setVisible(true);
	}
	
	@Override  
    public void actionPerformed(ActionEvent e) {  
        String text = e.getActionCommand();  
   
        if (characters[0].equals(text)) {
            startServer();
            status.setText("started");
        } else if (characters[1].equals(text)) {
        	stopServer();
        	status.setText("stopped");
        }  
    }

	class MyWin extends WindowAdapter{  
	      
	    @Override  
	    public void windowClosing(WindowEvent e) {  
	        // TODO Auto-generated method stub  
	        //System.out.println("Window closing"+e.toString());  
	        System.out.println("Closing"); 
	        stopServer();
	        System.exit(0);  
	    }  
	    @Override  
	    public void windowActivated(WindowEvent e) {  
	        System.out.println("Activated");    
	        //super.windowActivated(e);  
	    }  
	    @Override  
	    public void windowOpened(WindowEvent e) {  
	        // TODO Auto-generated method stub  
	        System.out.println("Opened");  
	        //super.windowOpened(e);  
	    }
	    @Override
	    public void windowDeactivated(WindowEvent e) {
	    	System.out.println("Deactivated");
	    }
	}
}