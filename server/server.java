//Vũ Mạnh Hùng
//18020593
//Bài thực hành 11 

import java.io.*;
import java.net.*;
import java.util.HashMap; // import the HashMap class
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;

import javax.xml.namespace.QName;

import jdk.internal.org.objectweb.asm.tree.analysis.Value;

class User {
	private String ID;
	private Socket s;

	public User(Socket s, String ID) {
		this.s = s;
		this.ID = ID;
	}

	public void setID(String ID) {
		this.ID = ID;
	}

	public void setSocket (Socket s) {
		this.s = s;
	}

	public Socket getSocket(Socket s) {
		return this.s;
	}
	public String getID() {
		return this.ID;
	}

}
// server class 
public class server { 

	public static HashMap<Socket, String> list;
	public static HashMap<String, String> groupList;
	public static HashMap<String, User> group_user;
	public static void main(String[] args) throws IOException { 
	
		list = new HashMap<Socket, String>();
		groupList = new HashMap<String, String>();
		group_user = new HashMap<String, User>();

		ServerSocket ss = new ServerSocket(5000); 
		System.out.println("Waitting for client...");		

		while (true) { 
			Socket s = null; 
			
			try { 
				
				s = ss.accept(); 
				
				System.out.println("A new client is connected : " + s); 
			
				DataInputStream dis = new DataInputStream(s.getInputStream()); 
				DataOutputStream dos = new DataOutputStream(s.getOutputStream()); 
				
				System.out.println("Assigning new thread for this client"); 

				Thread t = new ClientHandler(s, dis, dos, list, groupList, group_user); 

				// Invoking the start() method 
				t.start(); 
				
			} 
			catch (Exception e){ 
				s.close(); 
				e.printStackTrace(); 
			} 
		} 
	} 
} 

class ClientHandler extends Thread { 
	final DataInputStream dis; 
	final DataOutputStream dos; 
	final Socket s;

	String userID = "";
	boolean joinned = false;
	HashMap<Socket, String> list;
	HashMap<String, String> groupList;
	HashMap<String, String> myGroup;
	HashMap<String, User> group_user;
	private boolean isLogin = false;

	public ClientHandler(Socket s, DataInputStream dis, DataOutputStream dos, 
						HashMap<Socket, String> list, HashMap<String, String> groupList, HashMap<String, User> group_user) { 
		this.s = s; 
		this.dis = dis; 
		this.dos = dos;

		this.list = list;
		this.groupList = groupList;
		this.group_user = group_user;
		this.myGroup = new HashMap<String, String>();		
	} 
	
	public void sentToCls(Socket cliSock, String mess) {
		try {
			DataOutputStream Dos = new DataOutputStream(cliSock.getOutputStream()); 
			Dos.writeUTF(mess);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public String recvFromClis() {
		try {
			return dis.readUTF();
		} catch (Exception e) {
			e.printStackTrace();
			return "Err read messeage!";
		}
	}

	public boolean avaiableUser(String user) {
		Iterator it = list.entrySet().iterator();
		while (it.hasNext()) {
			Map.Entry pair = (Map.Entry) it.next();
			if (user.equals(pair.getValue()))
				return false;
		}(
		return true;
	}

	public String listUser() {
		String users = "list online user: ";
		Iterator it = list.entrySet().iterator();
		while (it.hasNext()) {
			Map.Entry pair = (Map.Entry) it.next();
			users += pair.getValue() + " ";
		}
		return users;
	}

	public int handleLogin(String ID) {
		//String ID = recvFromClis();
		if (!avaiableUser(ID)) {
			sentToCls(s, "This username isn't available. please select other user name!");
			return 0;
		}
		sentToCls(s, "211 User ID " + ID + " OK");
		userID = ID;
		list.put(s, ID);
		sentToCls(s, listUser());
		isLogin = true;
		return 0;
	}

	public Socket cliSocket(String user) {
		Iterator it = list.entrySet().iterator();
		while (it.hasNext()) {
			Map.Entry pair = (Map.Entry) it.next();
			if (pair.getValue().equals(user))
				return (Socket) pair.getKey();
		}
		return null;
	}
	public String getUserId(String mess) {
		return "from " + userID + " " + mess;
	}
	public String createNotice(String mess, String user) {
		return "from " + user + " " + mess;
	}

	public int handleExit() {
		try {
			System.out.println("Client " + this.s + " sends exit...");
			System.out.println("Closing this connection.");
			list.remove(this.s);

			this.s.close();
			System.out.println("Connection closed");
		} catch (Exception e) {
			e.printStackTrace();
		}
		return -1;
	}

	private String myGroupList() {
		Iterator it = list.entrySet().iterator();
		String group = "Joinned group: ";
		while (it.hasNext()) {
			Map.Entry pair = (Map.Entry) it.next();
			group += pair.getKey() + " ";
		}
		return group;
	}
	
	private boolean validGroup(String id, String pass) {
		Iterator it = list.entrySet().iterator();
		while (it.hasNext()) {
			Map.Entry pair = (Map.Entry) it.next();
			if (pair.getKey().equals(id) && pair.getValue().equals(pass))
				return true;
		}
		return false;
	}

	private int groupJoin() {
		sentToCls(this.s, "Group id:\nGroup password:");
		String id = recvFromClis();
		String pass = recvFromClis();
		if (validGroup(id, pass)) {
			myGroup.put(id, pass);
			sentToCls(this.s, myGroupList());
			User user = new User(this.s, userID);
			group_user.put(id, user);
			joinned = true;
			return 1;
		}
		sentToCls(this.s, "Invalid group id or password!");
		return 0;
	}
	
	private boolean avaiableGroup(String name) {
		Iterator it = list.entrySet().iterator();
		while (it.hasNext()) {
			Map.Entry pair = (Map.Entry) it.next();
			if (pair.getKey().equals(name))
				return false;
		}
		return true;
	} 

	private int createGroup() {
		sentToCls(this.s, "Group name: \n0: Quit");
		String name = recvFromClis();
		if (name.equals("0"))
			return 0;
		if (avaiableGroup(name)) {
			sentToCls(this.s, "Group password: ");
			String pass =  recvFromClis();
			groupList.put(name, pass);
			return 0;
		}
		sentToCls(this.s, "This group name is taken, please slelect other name\n 0: Quit");
		return createGroup();
	}

	private int groupHandler() {
		String cliSelect = recvFromClis();
		if (cliSelect.equals("1")) {
			sentToCls(this.s, myGroupList());
			return 1;
		}
		if (cliSelect.equals("2"))
			return groupJoin();
		if (cliSelect.equals("3"))
			return createGroup();
		sentToCls(this.s, "Invalid command");
		return 0;
	}

	public int handle(String mess) {
		if (mess.equals("Exit")) {
			return handleExit();
		}

		if (!isLogin) {
			return handleLogin(mess);
		}
		
		if (mess.equals("LIST")) {
			sentToCls(s, listUser());
			return 1;
		}
		if (mess.equals("GROUP")) {
			sentToCls(s, "1: Joined Group \n 2: Join group \n 3:Create group");
			return groupHandler();
		}
		//sent messeage
		if (mess.substring(0, 2).equals("to")) {
			String user = mess.substring(3, mess.indexOf(" ", 3));
			if (cliSocket(user) != null)
				sentToCls(cliSocket(user), createNotice(mess, userID));
			else
				sentToCls(this.s, "Do not contains user id " + user);
			return 1;
		}
		sentToCls(s, "Invalid commad");
		return 0;		
	}

	@Override
	public void run() { 
		String received; 
		System.out.println("started..");
		sentToCls(s, "Enter user id");
		while (true) { 
			try { 

				received = dis.readUTF();
				System.out.println("received: " + received);
				int res = handle(received);
				if (res == -1)
					break;
			} catch (IOException e) { 
				e.printStackTrace();
				break;
			} 
		} 
		
		try { 
			this.dis.close(); 
			this.dos.close(); 
			
		} catch(IOException e){ 
			e.printStackTrace();
		
		} 
	} 
} 
