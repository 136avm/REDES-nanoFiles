package es.um.redes.nanoFiles.tcp.server;


import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.net.SocketAddress;

public class NFServerThread extends Thread {
	private Socket socket;
	
	public NFServerThread(Socket socket) {
		this.socket = socket;
	}
	
	public void run() {
		NFServerComm.serveFilesToClient(socket);				
	}
}