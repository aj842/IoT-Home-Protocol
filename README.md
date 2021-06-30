# IoT-Home-Protocol
Requirements:
1. To run and compile the protocol the system should have java version 8 or higher installed.
2. All the packages should be present in the same folder.
3. The DES_STORE file needs to be present in the same folder as that of the other packages.
4. jdk path should be added to environment variable PATH in order to run and compile the code from the command prompt.

Instructions to run and compile the protocol:
1. To run the program on a Windows operating system open command prompt.
2. Change the directory in the command prompt using the cd command.

To run the protocol first the server needs to be compiled and executed:
Command for compilation: javac server\Server.java
Command for execution: java server\Server or java server.Server

Now the client can be executed as follows:
Command for compilation: javac client\Client.java
Command for execution: Can be executed using either of the following commands-
1. java client\Client -host:127.0.0.1 -port:9070 -login username:password 
or 
java client.Client -host:127.0.0.1 -port:9070 -login username:password
2. java client\Client -default
or
java client.Client -default

The username is 'abhi' and the password is 'abhi1234'

Special Note: If the client throws a NullPointerException at sometime re-run the execution command, the error is generated sometimes due to some issue with encryption.


Video: https://drive.google.com/file/d/18U0zkz_BMxC2X7DBbJlOTLxmY26yICPC/view?usp=sharing
