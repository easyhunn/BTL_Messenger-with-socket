
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
			Global_Variable gVar = new Global_Variable();
			Thread ts = new sendHandler(s, dos, gVar);
			Thread tr = new receiveHandler(s, dis, dos, gVar);
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
	final DataOutputStream dos;
	final Socket s;
	static int bytesRead = 0;
	static boolean Sound = false;
	Global_Variable gVar;

	receiveHandler(Socket s, DataInputStream dis, DataOutputStream dos, Global_Variable gVar) {
		this.s = s;
		this.dis = dis;
		this.gVar = gVar;
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

		while (true) {
			try {
				String received = dis.readUTF();
				System.out.println("received from server:" + received);
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
				if (received.length() > 11)
				if ((received.substring(0, 11)).equals("!Voice chat")) {
					
					dos.writeUTF(received);
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
					microphone.start();
					int bytesRead = 0;
					byte[] soundData = new byte[1];
					
					Thread inSound = new Thread(new SoundReceiver(this.s, gVar));
					inSound.start();
					gVar.setMicStatus(true);
					while(bytesRead != -1 && gVar.getMicStatus()) {
						bytesRead = microphone.read(soundData, 0, soundData.length);
						if(bytesRead > 0){
							dos.write(soundData, 0, bytesRead);
						}
					}
					receiveHandler.sleep(20);
					microphone.close();
					
				}

				if (received.equals("500 bye")) {
					Thread.sleep(100);
					dis.close();
					break;
				}

				
			} catch (Exception e) {
				e.printStackTrace();
			}
		}

	}
}

class sendHandler extends Thread {

	final DataOutputStream dos;
	final Socket s;
	Global_Variable gVar;

	sendHandler(Socket s, DataOutputStream dos, Global_Variable gVar) {
		this.s = s;
		this.dos = dos;
		this.gVar = gVar;
	}
	
	@Override
	public void run() {
		Scanner in = new Scanner(System.in);
		String toSend = "";

		while (true) {
			toSend = in.nextLine();
			if (toSend.equals("q")) gVar.setMicStatus(false);
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