import java.io.*;
import java.net.*;
import java.util.ArrayList;
import java.util.HashMap; // import the HashMap class
import java.util.Iterator;
import java.util.List;
import java.util.Map;

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

	public void setSocket(Socket s) {
		this.s = s;
	}

	public Socket getSocket() {
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
	public static HashMap<String, List<User>> group_users;
	public static void main(String[] args) throws IOException { 
	
		list = new HashMap<Socket, String>();
		groupList = new HashMap<String, String>();
		group_users = new HashMap<String, List<User>>();

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

				Thread t = new ClientHandler(s, dis, dos, list, groupList, group_users); 

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
	HashMap<Socket, String> list; //user online
	HashMap<String, String> groupList;
	HashMap<String, String> myGroups;
	String myGroup;
	HashMap<String, List<User>> group_users;
	private boolean isLogin = false;

	public ClientHandler(Socket s, DataInputStream dis, DataOutputStream dos, 
						HashMap<Socket, String> list, 
						HashMap<String, String> groupList, 
						HashMap<String, List<User>> group_users) { 
		this.s = s; 
		this.dis = dis; 
		this.dos = dos;

		this.list = list;
		this.groupList = groupList;
		this.group_users = group_users;
		this.myGroups = new HashMap<String, String>();
		this.myGroup = "";		
	} 
	
	//get client socket by user id
	public Socket cliSocket(String user) {
		Iterator it = list.entrySet().iterator();
		while (it.hasNext()) {
			Map.Entry pair = (Map.Entry) it.next();
			if (pair.getValue().equals(user))
				return (Socket) pair.getKey();
		}
		return null;
	}

	//notice sent messeage
	public String createNotice(String mess) {
		return "from " + userID + " " + mess;
	}
	public String createNotice(String mess, String user) {
		return "from " + user + ": " + mess;
	}

	//sent messeage to client
	public void sentToCls(Socket cliSock, String mess) {
		try {
			DataOutputStream Dos = new DataOutputStream(cliSock.getOutputStream());
			Dos.writeUTF(mess);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	public void sentToCls(Socket cliSock, long mess) {
		try {
			DataOutputStream Dos = new DataOutputStream(cliSock.getOutputStream()); 
			Dos.writeLong(mess);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	//sent messeage to whole clients in group
	public void sentToGroupCls(String mess) {
		for (User i : group_users.get(myGroup)) {
			sentToCls(i.getSocket(), "To Group " + createNotice(mess));
		}
	}

	//remove member from group chat
	public void removeGroupMember(String user) {
		group_users.get(myGroup).remove(user);
	}
	//receive messeage from client
	public String recvFromClis() {
		try {
			return dis.readUTF();
		} catch (Exception e) {
			e.printStackTrace();
			return "Err read messeage!";
		}
	}

	public String recvFromClis(String user) {
		try {
			DataInputStream mDis = new DataInputStream(cliSocket(user).getInputStream());
			return mDis.readUTF();
		} catch (Exception e) {
			e.printStackTrace();
			return "Err read messeage!";
		}
	}

	//check id user is taken or not
	public boolean avaiableUser(String user) {
		Iterator it = list.entrySet().iterator();
		while (it.hasNext()) {
			Map.Entry pair = (Map.Entry) it.next();
			if (user.equals(pair.getValue()))
				return false;
		}
		return true;
	}

	// list online users
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
		if (!avaiableUser(ID)) {
			sentToCls(s, "This username isn't available. please select other user name!");
			return 0;
		}

		sentToCls(s, "211 User ID " + ID + " OK");
		userID = ID;
		list.put(s, ID); // add to list online user
		sentToCls(s, listUser());
		isLogin = true;
		return 0;
	}

	//client disconnect
	public int handleExit() {
		try {
			System.out.println("Client " + this.s + " sends exit...");
			System.out.println("Closing this connection.");
			
			list.remove(this.s); //remove from online list
			this.s.close();
			
			System.out.println("Connection closed");
		} catch (Exception e) {
			e.printStackTrace();
		}
		return -1;
	}

	//get all socket of member in group (to sent messeage to whole group member)
	private String groupList() {
		Iterator it = groupList.entrySet().iterator();
		String group = "group: ";
		while (it.hasNext()) {
			Map.Entry pair = (Map.Entry) it.next();
			group += pair.getKey() + " ";
		}
		return group;
	}
	
	//check if group id pass word is correct to allow join
	private boolean validGroup(String id, String pass) {
		Iterator it = groupList.entrySet().iterator();
		while (it.hasNext()) {
			Map.Entry pair = (Map.Entry) it.next();
			System.out.println(pair.getKey() + " " + pair.getValue());
			if (pair.getKey().equals(id) && pair.getValue().equals(pass))
				return true;
		}
		return false;
	}

	//get all user joining in group
	public String get_group_users(String grName) {
		String s = "";
		for (User u : group_users.get(grName)) {
			s += u.getID() + ", ";
		}
		return s;
	}
	//handle join group
	private int groupJoin() {
		sentToCls(this.s, "Group id:\nGroup password:");
		String id = recvFromClis();
		String pass = recvFromClis();
		if (validGroup(id, pass)) {
			myGroup = id;
			User user = new User(this.s, userID);
			group_users.get(id).add(user);
			sentToCls(this.s, "Available member: " + get_group_users(id));
			joinned = true;
			return 1;
		}
		sentToCls(this.s, "Invalid group id or password!");
		return 0;
	}
	
	// is group name has taken
	private boolean avaiableGroup(String name) {
		Iterator it = groupList.entrySet().iterator();
		while (it.hasNext()) {
			Map.Entry pair = (Map.Entry) it.next();
			if (pair.getKey().equals(name))
				return false;
		}
		return true;
	} 

	//handle create group
	private int createGroup() {
		sentToCls(this.s, "Group name: \n0: Quit");
		String name = recvFromClis();
		if (name.equals("0"))
			return 0;
		if (avaiableGroup(name)) {
			sentToCls(this.s, "Group password: ");
			String pass = recvFromClis();

			//new group			
			groupList.put(name, pass);
			myGroup = name;
			//initialize group_users
			List<User> users = new ArrayList<User>();
			users.add(new User(this.s, userID));
			group_users.put(name, users);
			joinned = true;
			sentToCls(this.s, "create group successfuly");
			return 0;
		}	
		sentToCls(this.s, "This group name is taken, please slelect other name");
		return createGroup();
	}

	//handle select command from client
	private int groupHandler() {
		String cliSelect = recvFromClis();
		if (cliSelect.equals("1")) {
			sentToCls(this.s, groupList());
			return 1;
		}
		if (cliSelect.equals("2"))
			return groupJoin();
		if (cliSelect.equals("3"))
			return createGroup();
		sentToCls(this.s, "Invalid command");
		return 0;
	}	

	private int handleConversation(String user) {
		while (true) {
			String mess = recvFromClis();
			if (mess.equals("@close"))
				return 0;
			sentToCls(cliSocket(user), "From " + userID + ": " + mess);
		}
	}

	private int downloadFile(String fileName, Socket s) {
		File file = new File(fileName);
		System.out.println("file name:" + fileName);
		try {
			if (file.exists()) {
				sentToCls(s, "!Download");
				System.out.println("File size: " + file.length());
				sentToCls(s, file.length());
				sentToCls(s, fileName);
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
				bis.close();
				//sentToCls(s, "Download successfully");
			} else {
				sentToCls(s, "!File not found");
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return 0;
	}
	
	private void getFileFromClis(String fileName, long fileLength) {
		try {
        	byte[] contents = new byte[10000];
        	//Initialize the FileOutputStream to the output file's full path.
    		FileOutputStream fos = new FileOutputStream(fileName);
        	BufferedOutputStream bos = new BufferedOutputStream(fos);
    		InputStream is = this.s.getInputStream();
    		//No of bytes read in one read() call
    		int bytesRead = 0;
        	long current = 0;
        	while ((bytesRead = is.read(contents)) != -1) {
            	current += bytesRead;
				bos.write(contents, 0, bytesRead);
        		if (current == fileLength) break;
        	}
			bos.flush();
			bos.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	// Main handle when client isn't joinning any group
	private int handle(String mess) {
		try {
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
				sentToCls(s, "1: List group\n 2: Join group \n 3:Create group");
				return groupHandler();
			}

			//sent messeage to 1 client
			if (mess.substring(0, 2).equals("to")) {
				String user = mess.substring(3, mess.indexOf(" ", 3));
				if (cliSocket(user) != null)
					sentToCls(cliSocket(user), createNotice(mess, userID));
				else
					sentToCls(this.s, "Do not contains user id " + user);
				return 1;
			}

			//make conversation to 1 client (eg: CHAT 123)
			if (mess.substring(0, 4).equals("CHAT")) {
				String user = mess.substring(5, mess.length());

				if (!avaiableUser(user)) {
					return handleConversation(user);
				}
				sentToCls(this.s, "user " + user + " isn't connecting!");
				return 0;
			}

			//sent file (message type:  Download abc.txt)
			if (mess.substring(0, 8).equals("Download")) {
				String fileName = mess.substring(9, mess.length());
				return downloadFile(fileName, this.s);
			}

			//recvFileFromClient
			if (mess.equals("!Up file")) {
				String messContent = dis.readUTF(); //send abc.txt to 123
				long fileLength = dis.readLong();
				String fileName = messContent.substring(5, messContent.indexOf(" ", 6));
				String userRecev = messContent.substring(messContent.indexOf(" ", messContent.indexOf("to")) + 1, messContent.length());
				System.out.println("user: " + userRecev);
				getFileFromClis(fileName, fileLength);
				
				downloadFile(fileName, cliSocket(userRecev));
				return 0;
			}
		} catch (Exception e) {
			sentToCls(s, "Invalid commad");
			return -1;
		}
		sentToCls(s, "Invalid commad");
		
		return 0;
	}
	
	//handle when client joinning group
	private int handleGroupChat(String mess) {
		//closing group chat
		if (mess.equals("@END")) {
			removeGroupMember(userID);
			if (group_users.get(myGroup).size() > 0)
				sentToGroupCls(userID + " has left this conversation");
			else {
				//delete group when it have no member
				groupList.remove(myGroup);
				group_users.remove(myGroup);
			}
			myGroup = "";
			joinned = false;
			return 0;
		}

		//list member
		if (mess.equals("@MEM")) {
			sentToCls(this.s, get_group_users(myGroup));
			return 1;
		}
		if (mess.equals("!Up file")) {
			try {
				String messContent = dis.readUTF(); //send abc.txt to group
				long fileLength = dis.readLong();
				String fileName = messContent.substring(5, messContent.indexOf(" ", 6));

				getFileFromClis(fileName, fileLength);
				for (User u : group_users.get(myGroup)) {
					downloadFile(fileName, cliSocket(u.getID()));
				}	
			} catch (Exception e) {
				e.printStackTrace();
			}
			return 1;
		}
		createNotice(mess);
		sentToGroupCls(mess);
		return 0;
	}

	@Override
	public void run() { 
		String received; 
		System.out.println("started..");
		sentToCls(s, "User id:");
		while (true) { 
			try { 
				
				received = dis.readUTF();
				System.out.println("received: " + received);
				int res;
				if (!joinned)
					res = handle(received);
				else
					res = handleGroupChat(received);

				if (res == -1) //Exit
					break;
			} catch (IOException e) { 
				e.printStackTrace();
				list.remove(this.s);
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
