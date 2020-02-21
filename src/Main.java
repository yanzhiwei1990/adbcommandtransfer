
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

public class Main implements ActionListener {

	private TcpServer mTcpServer;
	private Thread mThread;
	private boolean mStarted = false;
	private final String[] characters = {"start", "stop"};
	private JButton start;
	private JButton stop;
	//private JTextField status;
	private JTextArea status;
	private SaveLog mSaveLog;
	
	private Timer mTimer = new Timer();
    private boolean needreboot = false;
    private int mTimeoutAdb = 0;
    private boolean checkstatus = false;
    private TimerTask mTask = new TimerTask() {
        @Override
        public void run() {
        	Calendar now = Calendar.getInstance();
        	SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        	Date date = now.getTime();
        	String dateStringParse = sdf.format(date);
        	//System.out.println("current time = " + dateStringParse);
        	int year = now.get(Calendar.YEAR); //2015����ǰ���
        	int month = now.get(Calendar.MONTH) + 1; //12����ǰ�£�ע��� 1
        	int day = now.get(Calendar.DATE); //23����ǰ��
        	int hour = now.get(Calendar.HOUR);
        	int minute = now.get(Calendar.MINUTE);
        	int second = now.get(Calendar.SECOND);
        	int night = now.get(Calendar.AM_PM);
        	/*if (!needreboot && hour == 0 && minute == 0) {
        		//need reboot
        		needreboot = true;
        		mTimeoutAdb = 0;
        		connectAdbCmd();
        	} else {
        		mTimeoutAdb++;
        		if (mTimeoutAdb > 10 * 60 * 1000) {//10 min
        			needreboot = false;
        		}
        	}
        	if (adbConnected) {
        		rebootAdbCmd();
        		System.out.println("adb reboot result"); 
        		adbConnected = false;
        	}*/
        	
        	if ((night == Calendar.AM && //reboot at am 0:00 6:00 12:00 pm 5:00
        			((hour == 0 && minute == 0 && second == 0) || (hour == 6 && minute == 0 && second == 0) || (hour == 11 && minute == 59 && second == 0))) ||
        			(night == Calendar.PM && (hour == 5 && minute == 0 && second == 0))) {
        		rebootAdbCmd();
        	}
        	
        	if ((night == Calendar.AM && ((hour == 6 && minute == 0 && second == 0) || (hour == 6 && minute == 30 && second == 0) || (hour == 7 && minute == 0 && second == 0)))||
        			(night == Calendar.PM && ((hour == 0 && minute == 5 && second == 0) || (hour == 0 && minute == 10 && second == 0) || (hour == 0 && minute == 30 && second == 0)))) {//check at am 6:00 6:30 7:00 pm 
        		currentAdbCmd();
        	}
        }
    };
	
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
				mSaveLog.log("connectAdbCmd result = " + result);
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
				mSaveLog.log("rebootAdbCmd connect result = " + result);
				if (result != null && result.contains("connected to homedevice.iask.in")) {
					result = mTcpServer.callShell("adb reboot");
					mSaveLog.log("rebootAdbCmd reboot result = " + result);
					System.out.println("rebootAdbCmd reboot = " + result); 
				}
			}
    		
    	}).start();
    }
    
    private void currentAdbCmd() {
    	new Thread(new Runnable(){
			@Override
			public void run() {
				// TODO Auto-generated method stub
				String result = mTcpServer.callShell("adb connect homedevice.iask.in:39634");
				if (result != null && result.contains("connected to homedevice.iask.in")) {
					result = mTcpServer.callShell("adb shell \"dumpsys window | grep mCurrentFocus\"");
					System.out.println("currentAdbCmd current = " + result);
					mSaveLog.log("currentAdbCmd current result = " + result);
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
	        			mSaveLog.log("currentAdbCmd switch to cctv 11");
	        		} else {
	        			checkstatus = false;
	        			mSaveLog.log("currentAdbCmd is cctv 11");
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
    
    public static void main(String[] args) {
        Main mMain = new Main();
        mMain.mTcpServer = new TcpServer(19900);
    	mMain.mThread = new Thread(mMain.mTcpServer);
    	mMain.InitUI();
    	mMain.mThread.start();
    	mMain.mStarted = true;
    	mMain.status.setText("started");
    	mMain.setInterface(mMain.mServerCallback);
    	mMain.mSaveLog = new SaveLog();
    	mMain.mTimer.schedule(mMain.mTask, 0, 1000 * 1);//run per 10 second
    }
    
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
    		//status.setText((mStarted ? "started: " : "stopped: ") + value);
    		/*if (status.getLineCount() > 10) {
    			mSaveLog.log(status.getText());
    			status.setText("line is over 10, save and clear!\r\n");
    		}*/
    		status.setText(mSaveLog.getTime("HH:mm:ss") + "-" + (mStarted ? "started: " : "stopped: ") + value);
    		mSaveLog.log(status.getText());
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
		
		/*start.addActionListener(new ActionListener() {  
            @Override  
            public void actionPerformed(ActionEvent e) {  
                // TODO Auto-generated method stub
                System.out.println("actionPerformed:" + e.getActionCommand());  
            }  
        });  
          
		start.addMouseListener(new MouseAdapter() {    
            public void mouseClicked(MouseEvent e){  
                if(e.getClickCount()==2){  
                    System.out.println("˫������");  
                }else  
                    System.out.println("�������");  
            }  
              
        }); */

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