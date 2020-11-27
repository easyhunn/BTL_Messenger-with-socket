//Vũ Mạnh Hùng
//18020593
//Bài thực hành 11 

import java.io.*; 
import java.net.*; 
import java.util.Scanner;
import java.security.MessageDigest;

//client class
public class client { 


    public static byte[] createChecksum(String filename) throws Exception {
        InputStream fis = new FileInputStream(filename);

        byte[] buffer = new byte[1024];
        MessageDigest complete = MessageDigest.getInstance("MD5");
        int numRead;

        do {
            numRead = fis.read(buffer);
            if (numRead > 0) {
                complete.update(buffer, 0, numRead);
            }
        } while (numRead != -1);

        fis.close();
        return complete.digest();
    }

    public static String getMD5Checksum(String filename) throws Exception {
        byte[] b = createChecksum(filename);
        String result = "";

        for (int i = 0; i < b.length; i++) {
            result += Integer.toString((b[i] & 0xff) + 0x100, 16).substring(1);
        }
        return result;
    }

	public static void main(String[] args) throws IOException { 
		try { 
			Socket s = new Socket("127.0.0.1", 5000); 
			System.out.println("connected!");
			// obtaining input and out streams 
			DataInputStream dis = new DataInputStream(s.getInputStream()); 
			DataOutputStream dos = new DataOutputStream(s.getOutputStream()); 
			Thread ts = new sendHandler(s, dos); 
			Thread tr = new receiveHandler(s, dis);
			// Invoking the start() method 
			ts.start(); 
			tr.start();
		} catch(Exception e){ 
			e.printStackTrace(); 
		} 
	} 
} 

class receiveHandler extends Thread{
	final DataInputStream dis;  
	final Socket s;

	receiveHandler(Socket s, DataInputStream dis) {
		this.s = s;
		this.dis = dis;
	}
	@Override
	public void run () {
		while (true) {
			try {
				String received = dis.readUTF();
				if (received.equals("500 bye")) {
					dis.close();
					break;
				}
				System.out.println("received from server:" + received);
			} catch (Exception e) {
				break;
			}
		}
		
	}
}

class sendHandler extends Thread {
	
	final DataOutputStream dos;  
	final Socket s;

	sendHandler(Socket s, DataOutputStream dos) {
		this.s = s;
		this.dos = dos;
	}
	@Override
	public void run() {
		while (true) {
			
			try {
				Scanner in = new Scanner(System.in);
				String tosend = in.nextLine();
				dos.writeUTF(tosend);
				if (tosend.equals("Exit")) {
					dos.close();
					break;
				}
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}
}