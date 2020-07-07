

import java.io.BufferedInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketTimeoutException;
import java.util.ArrayList;
import java.util.List;

public class TcpServer implements Runnable{
    private String TAG = "TcpServer";
    private int port = 19900;
    private boolean isListen = true;
    public List<ServerSocketThread> SST = new ArrayList<ServerSocketThread>();
    private boolean hasLogClient = false;
    private ServerSocket mServerSocket;
    private boolean allrun = true;
    private ServerCallback mServerCallback;
    
    public TcpServer(int port){
        this.port = port;
    }

    public TcpServer(){
    	
    }

    public void setIsListen(boolean b){
        isListen = b;
    }

    public void setInterface(ServerCallback callback) {
    	mServerCallback = callback;
    }
    
    public interface ServerCallback {
    	public void setResult(String value);
    }
    
    public boolean hasLogClient(){
        for (ServerSocketThread s : SST) {
        	if (s.isLogClient()) {
        		hasLogClient = true;
        		return true;
        	}
        }
        hasLogClient = false;
        return false;
    }
    
    public void LogtoClient(String value){
        for (ServerSocketThread s : SST) {
        	if (s.isLogClient()) {
        		s.send(value);
        		return;
        	}
        }
        System.out.println("run: log client is disconnected");
    }
    
    public void SendtoHttpClient(char[] value){
    	int i = -1;
        for (ServerSocketThread s : SST) {
        	if (s.isHttpClient()) {
        		s.sendchar(value);
        		//i = SST.indexOf(s);
        		//System.out.println("http client i = " + i + ", ip = " + s.socket.getRemoteSocketAddress());
        		return;
        	}
        }
        System.out.println("run: send char to all http client");
    }
    
    public void SendtoHttpClient(byte[] value){
    	int i = -1;
        for (ServerSocketThread s : SST) {
        	if (s.isHttpClient()) {
        		s.sendbyte(value);
        		//i = SST.indexOf(s);
        		//System.out.println("http client i = " + i + ", ip = " + s.socket.getRemoteSocketAddress());
        		return;
        	}
        }
        System.out.println("run: send byte to all http client");
    }
    
    public void SendtoHttpClient(String value){
    	int i = -1;
        for (ServerSocketThread s : SST) {
        	if (s.isHttpClient()) {
        		s.send(value);
        		//i = SST.indexOf(s);
        		//System.out.println("http client i = " + i + ", ip = " + s.socket.getRemoteSocketAddress());
        		return;
        	}
        }
        System.out.println("run: send string to all http client");
    }
    
    public void SendtoRequesterClient(String value){
        for (ServerSocketThread s : SST) {
        	if (s.isRequesterClient()) {
        		s.send(value);
        		//s.stopSocket();
        		//return;
        	}
        }
        System.out.println("run: send to all requester client is disconnected");
    }
    
    public void closeSelf(){
        isListen = false;
        allrun = false;
        try {
			mServerSocket.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
        if (SST == null) {
        	return;
        }
        /*for (ServerSocketThread s : SST){
        	SST.remove(s);
            s.isRun = false;
            s.stopSocket();
            System.out.println("close all current = " + s.ClientName);
        }*/
    }

    private Socket getSocket(ServerSocket serverSocket){
        try {
            return serverSocket.accept();
        } catch (SocketTimeoutException e) {
            //e.printStackTrace();
        	//System.out.println("run: listen timeout SocketTimeoutException");
            return null;
        }catch (IOException e) {
            //e.printStackTrace();
            //System.out.println("run: listen timeout IOException");
            return null;
        } 
    }

    @Override
    public void run() {
        try {
        	mServerSocket = new ServerSocket(port);
        	mServerSocket.setSoTimeout(10000);
        	allrun = true;
            //long count = 0;
            while (isListen){
                //System.out.println("run: start listen...SST = " + SST.size());
                if (SST.size() > 10) {
                	System.out.println("run: start listen much than 10");
                	continue;
                }
                Socket socket = getSocket(mServerSocket);
                if (socket != null){
                	//count++;
                    new ServerSocketThread(socket);
                    //System.out.println("run: Current client = " + SST.size());
                }
            }
            mServerSocket.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    
    public class ServerSocketThread extends Thread{
        Socket socket = null;
        private PrintWriter pw;
        private InputStream is = null;
        private OutputStream os = null;
        private String ip = null;
        private boolean isRun = true;
        private boolean isLogClient = false;
        private String LogClientName = "LogClient";
        private String ClientName = null;
        private boolean isHttpClient = false;
        private String HttpClientName = "HttpClient";
        private boolean isHttpRequester = false;
        private String RequesterClientName = "RequesterClient";

        ServerSocketThread(Socket socket){
            this.socket = socket;
            ip = socket.getRemoteSocketAddress().toString();
            //System.out.println("ServerSocketThread:new cilent in,ip:" + ip);

            try {
                socket.setSoTimeout(1000);
                os = socket.getOutputStream();
                is = socket.getInputStream();
                pw = new PrintWriter(os,true);
                start();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        public void send(String msg){
            pw.println(msg);
            pw.flush();
        }
        
        public void sendchar(char[] msg){
            pw.println(msg);
            pw.flush();
        }
        
        public void sendbyte(byte[] msg){
            try {
				os.write(msg);
				os.flush();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
        }
        
        public boolean isLogClient(){
            return isLogClient;
        }
        
        public String getClientName(){
            return ClientName;
        }

        public boolean isHttpClient(){
            return isHttpClient;
        }
        
        public String getHttpClientName(){
            return HttpClientName;
        }
        
        public boolean isRequesterClient(){
            return isHttpRequester;
        }
        
        public String getRequesterClientName(){
            return RequesterClientName;
        }
        
        public void stopSocket() {
        	try {
				socket.close();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
        	isRun = false;
        }
        
        @Override
        public void run() {
            byte buff[]  = new byte[4096];
            String rcvMsg;
            byte[] rcvByte;
            int rcvLen;
            System.out.println("SST size: " + SST.size());
            SST.add(this);
            while (allrun && isRun && !socket.isClosed()){
            	//System.out.println("socket = " + socket.toString() + ", out = " + socket.isOutputShutdown() + ", input = " + socket.isInputShutdown() +
            	//		", connected = " + socket.isConnected());
                try {
                    if ((rcvLen = is.read(buff)) != -1 ){
                    	//System.out.println("rcvLen = " + rcvLen);
                        rcvMsg = new String(buff,0,rcvLen, "UTF-8");
                        rcvByte = new byte[rcvLen];
                        System.arraycopy(buff, 0, rcvByte, 0, rcvLen);
                        //System.out.println("rcvByte = " + (rcvByte != null ? DatatypeConverter.printHexBinary(rcvByte) : "empty"));
                        System.out.println("rcvMessage: " + rcvMsg);
                        /*
                        Intent intent =new Intent();
                        intent.setAction("tcpServerReceiver");
                        intent.putExtra("tcpServerReceiver",rcvMsg);
                        */
                        if (rcvMsg.startsWith("name")){
                        	ClientName = rcvMsg.substring(rcvMsg.indexOf(":") + 1);
                        	//System.out.println("run:ClientName: " + ClientName);
                        	if (LogClientName.equals(ClientName)) {
                        		isLogClient = true;
                        	} else if (HttpClientName.equals(ClientName)) {
                        		isHttpClient = true;
                        		int httpclientcount = 0;
                        		int httpindex = -1;
                        		for (int i = 0; i < SST.size(); i++) {
                        			if (SST.get(i).isHttpClient()) {
                        				httpclientcount++;
                        				httpindex = i;
                        				System.out.println("http client index = " + httpindex);
                        			}
                        		}
                        		if (httpclientcount > 1) {
                        			for (int i = 0; i < SST.size(); i++) {
                        				System.out.println("remove http client size = " + SST.size());
                        				ServerSocketThread ss = SST.get(i);
                            			if (ss.isHttpClient() && i < httpindex) {
                            				System.out.println("remove http client index = " + i);
                            				ss.stopSocket();
                            				SST.remove(ss);
                            			}
                            		}
                        		}
                        		
                        	}
                        }
                        if (rcvMsg.startsWith("log:")){
                        	LogtoClient(rcvMsg.substring(rcvMsg.indexOf(":") + 1));
                        }
                        if ((rcvMsg.contains("GET") && rcvMsg.contains("HTTP"))){
                        	isHttpRequester = true;
                        	SendtoHttpClient(rcvMsg);
                        }
                        if ((rcvMsg.startsWith("mcu:") || rcvMsg.startsWith("remote:")) && isLogClient()) {
                        	System.out.println("mcu command = " + rcvMsg);
                        	SendtoHttpClient(rcvMsg);
                        }
                        if (rcvMsg.contains("NodeMcu Power Control")){
                        	System.out.println(rcvMsg);
                        	SendtoRequesterClient(rcvMsg);
                        }
                        if (hasLogClient()) {
                        	LogtoClient(rcvMsg);
                        }
                        if (rcvMsg.equals("QuitServer")){
                            isRun = false;
                        }
                        if (rcvMsg.equals("colseall")){
                        	isRun = false;
                        	closeSelf();
                        }
                        if (rcvMsg.indexOf("cmd:") != -1 && rcvMsg.length() > 4) {
                        	if (!rcvMsg.startsWith("cmd:")) {
                        		if (rcvMsg.length() - rcvMsg.indexOf("cmd:") > 4) {
                        			rcvMsg = rcvMsg.substring(rcvMsg.indexOf("cmd:") + 4);
                        		} else {
                        			rcvMsg = null;
                        		}
                        	}
                        	if (rcvMsg != null) {
                        		mServerCallback.setResult(rcvMsg.substring(4) + " wait for result...");//clear
                            	send(rcvMsg.substring(4) + " wait for result...");
                            	String result = callShell(rcvMsg.substring(4));
                            	if (result == null || result.length() == 0) {
                            		result = rcvMsg.substring(4) + "\r\n";
                            	}
                            	System.out.println("cmd result = " + result);
                            	send("result:" + result);
                            	mServerCallback.setResult(result);
                        	} else {
                        		System.out.println("cmd rcvMsg error");
                        	}
                        }
                    } else {
                    	System.out.println("run:buffer rcvLen = " + rcvLen);
                    	//if (!(isHttpClient() || isRequesterClient())) {
                    		isRun = false;
                    		break;
                    	//}
                    }
                }  catch (SocketTimeoutException e) {
                	//System.out.println("SocketTimeoutException");
                	//e.getStackTrace();
                	//e.printStackTrace();
                	continue;
                	//e.printStackTrace();
                } catch (IOException e) {
                	//System.out.println("IOException");
                	e.printStackTrace();
                	//e.getStackTrace();
                	break;
                    //
                }
            }
            try {
                socket.close();
                //SST.clear();
                SST.remove(this);
                System.out.println("run: disconnect");
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public static String callShell(String shellString) {
    	String result = null;
    	System.out.println("cmd:" + shellString);
    	
    	ByteArrayOutputStream resultOutStream = null;
    	BufferedInputStream errorInStream = null;
    	BufferedInputStream processInStream = null;
    	
        try {  
        	Process process = null;
        	String osVersion = System.getProperty("os.name");
        	System.out.println("callShell os.name: " + osVersion);
        	if (osVersion.toLowerCase().contains("linux".toLowerCase())) {
        		process = Runtime.getRuntime().exec(new String[]{ "sh", "-c", shellString});//linux
        	} else if (osVersion.toLowerCase().contains("window".toLowerCase())) {
        		process = Runtime.getRuntime().exec(new String[]{ "cmd", "/c", shellString});//Windows 10
        	} else {
        		System.out.println("callShell unkown os.name " + osVersion);
        		return result;
        	}

        	resultOutStream = new ByteArrayOutputStream();
        	errorInStream = new BufferedInputStream(process.getErrorStream());
        	processInStream = new BufferedInputStream(process.getInputStream());
        	
	        int length = 0;  
	        byte[] buffer = new byte[1024 * 1024];
	        int trytime = 5;
	        int trytimePeriod = 100;
	        
	        while (true) {
	        	if (errorInStream.available() <= 0) {
	        		trytime--;
	        		if (trytime > 0) {
	        			//System.out.println("errorInStream try rest time = " + trytime * trytimePeriod + " ms");
	        			Thread.sleep(trytimePeriod);
	        			continue;
	        		} else {
	        			break;
	        		}
	        	} else if ((length = errorInStream.read(buffer, 0, buffer.length))!= -1) {
	        		resultOutStream.write(buffer, 0, length);
	        		break;
	        	}
	        }

	        trytime = 5;
	        length = 0;
	        while (true) {
	        	if (processInStream.available() <= 0) {
	        		trytime--;
	        		if (trytime > 0) {
	        			//System.out.println("processInStream try rest time = " + trytime * trytimePeriod + " ms");
	        			Thread.sleep(trytimePeriod);
	        			continue;
	        		} else {
	        			break;
	        		}
	        	} else if ((length = processInStream.read(buffer, 0, buffer.length)) != -1) {
	        		resultOutStream.write(buffer, 0, length);
	        		break;
	        	}
	        }
	        result = new String(resultOutStream.toByteArray(), "UTF-8");
        } catch (Exception e) {  
        	System.out.println("call shell failed. " + e);  
        } finally {
        	if (errorInStream != null) {
				try {
					errorInStream.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
				errorInStream = null; 
			}
	         
	        if (processInStream != null) {
				try {
					processInStream.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
	        	processInStream = null; 
	        }
	         
	        if (resultOutStream != null) {
				try {
					resultOutStream.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
				resultOutStream = null;
	        }
        }
        if (result != null && result.length() == 0) {
        	result = shellString + " no ack but sucess";
        }

        System.out.println("result:\n" + result);
        return result;
    }
}
