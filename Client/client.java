//Vũ Mạnh Hùng
//18020593
//Bài thực hành 11 

import java.io.*;
import java.net.*;
import java.util.Scanner;
import java.security.MessageDigest;
import java.io.DataOutputStream;
import javax.sound.sampled.*;

//client class
public class client {
	static String sentToServ, recvFromServ;

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
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}

class receiveHandler extends Thread {
	final DataInputStream dis;
	final Socket s;

	receiveHandler(Socket s, DataInputStream dis) {
		this.s = s;
		this.dis = dis;

	}

	@Override
	public void run() {

		while (true) {
			try {
				String received = dis.readUTF();
				if (received.equals("!Download")) {
					long fileLength = dis.readLong();
					String fileName = dis.readUTF();
					byte[] contents = new byte[10000];
					// Initialize the FileOutputStream to the output file's full path.
					FileOutputStream fos = new FileOutputStream(fileName);
					BufferedOutputStream bos = new BufferedOutputStream(fos);
					InputStream is = this.s.getInputStream();
					// No of bytes read in one read() call
					int bytesRead = 0;
					long current = 0;
					while ((bytesRead = is.read(contents)) != -1) {
						current += bytesRead;
						bos.write(contents, 0, bytesRead);
						if (current == fileLength)
							break;
					}
					bos.flush();
					bos.close();

				}

				if (received.equals("500 bye")) {
					Thread.sleep(100);
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
	static int bytesRead = 0;
    static boolean Sound = false;

	sendHandler(Socket s, DataOutputStream dos) {
		this.s = s;
		this.dos = dos;
	}
	public int getBytesRead() {
        return bytesRead;
    }

    public void setBytesRead(int bytesRead) {
    	this.bytesRead = bytesRead;
    }

    public void setSound(boolean sound) {
        Sound = sound;
    }

    public boolean getSound(){
        return Sound;
    }
	@Override
	public void run() {
		Scanner in = new Scanner(System.in);
		String toSend = "";
		TargetDataLine microphone = null;
		//audio
		try {
			AudioFormat af = new AudioFormat(8000.0f,8,1,true,false);
        	DataLine.Info info = new DataLine.Info(TargetDataLine.class, af);
        	microphone = (TargetDataLine)AudioSystem.getLine(info);
        	microphone.open(af);
		} catch (Exception e) {
			e.printStackTrace();
		}

		while (true) {
			toSend = in.nextLine();
			try {
				if (toSend.length() > 5)
					if (toSend.substring(0, 4).equals("send")) {
						String fileName = toSend.substring(5, toSend.indexOf(" ", 5));
						File file = new File(fileName);
						if (file.exists()) {
							dos.writeUTF("!Up file");
							dos.writeUTF(toSend);
							dos.writeLong(file.length());
							// dos.writeUTF(fileName);
							FileInputStream fis;
							fis = new FileInputStream(file);
							BufferedInputStream bis = new BufferedInputStream(fis);

							OutputStream os = (s).getOutputStream();

							byte[] contents;
							long fileLength = file.length();
							long current = 0;

							while (current != fileLength) {
								int size = 10000;
								if (fileLength - current >= size)
									current += size;
								else {
									size = (int) (fileLength - current);

									current = fileLength;
								}
								contents = new byte[size];
								bis.read(contents, 0, size);
								os.write(contents);
							}
							os.flush();

						} else {
							dos.writeUTF("File not found!");
						}
						continue;
					}
				
				dos.writeUTF(toSend);
				if (toSend.length() > 10) {
					if (toSend.substring(0, 10).equals("Voice chat")) {
						microphone.start();
						int bytesRead = 0;
						byte[] soundData = new byte[1];
						Thread senMess = new Thread(new SendMess(this.s, dos));
        				senMess.start();
        				Thread inThread = new Thread(new SoundReceiver(this.s));
						inThread.start();
						while(bytesRead != -1) {
							bytesRead = microphone.read(soundData, 0, soundData.length);
							if(bytesRead >= 0){
								dos.write(soundData, 0, bytesRead);
							}
						}
					}
				}

				if (toSend.equals("Exit")) {
					Thread.sleep(100);
					dos.close();
					break;
				}
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		in.close();
	}
}