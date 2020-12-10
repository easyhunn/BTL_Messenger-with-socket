# BTL_Messenger-with-socket
//Protocol

Server: User id

Client: {ID}	// 123 
		// user id is required

Server:211 user id {ID} ok

list online user: 123 //users separate by space

// chat to 1 client

Client: to {ID} [messeage] //sent [messeage] to {ID} 

				//to 345 hello

Case 345 unavailable: 

Server: Do not contains user id 345

// Group chat

Client: GROUP

Server: 1: List group

 2: Join group 

 3:Create group

2: Join group 

3:Create group

Client: 3

Server: Group name: 

0: Quit

Client: GROUP NAME //INT3304 

//case group name available

Server: Group password:

Cient: 123

// case group nam unavailable

Server: This group name is taken, please slelect other name

Group name: 

0: Quit


//Join group

Client: GROUP

Server: 1: List group

2: Join group 

3:Create group

Client: 2

Server: Group id:

Group password:

Client: INT3304

123

//case password wrong

Server: Invalid group id or password!

//conversation

Client: to 234 {messeage}

Server: from {client ID} to 234 {message} // send to 234

//other way

Client: CHAT 234  //every message after that will sent to 234

Client: hello

Server: From {client ID}: hello //send to 234

//send file to 1 client
Client: send {file} to {client receive ID}  //send abc.txt to 234 

// send file in group 
// only work when being in group

Client: send abc.txt to group

// void chat

Client: Voice chat {user ID} // Voice chat 123

//break conversation:

Client:@close

Client: Exit

Server: 500 bye;

