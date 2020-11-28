# BTL_Messenger-with-socket
//Protocol
Server: User id

Client: 123 // user id is required

Server:211 user id 123 ok

list online user: 123 //users separate by space

// chat to 1 client

Client: to 345 [messeage] //sent messeage to 345 

vd to 345 hello

Case 345 unavailable: 

Server: Do not contains user id 345

// Group chat

Client: GROUP

Server: 1: List group

 2: Join group 

 3:Create group

Client: 3

Server: Group name: 

0: Quit

Client: INT3304

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

//break conversation:

Client:@close

Client: Exit
Server: 500 bye;
