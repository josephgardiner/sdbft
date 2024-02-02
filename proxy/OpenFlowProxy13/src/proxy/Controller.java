package proxy;

import java.io.IOException;
import java.net.Socket;

public class Controller {
	ServerConnection connection;
	String IP;
	int port;

	public Controller(ServerConnection connection, String iP, int port) {
		super();
		this.connection = connection;
		IP = iP;
		this.port = port;
	}

	public Controller(String iP, int port) {
		super();
		IP = iP;
		this.port = port;
	}

	public void setUpConnection(Control control) {
		Socket sock = null;
		try {
			sock = new Socket(IP, port);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		this.connection = new ServerConnection(IP, sock, control);
		Thread th = new Thread(this.connection);
		System.out.println("Starting server thread");
		th.start();

	}

	public void send(byte[] data, int size) {
		this.connection.send(data, size);
	}

	public ServerConnection getConnection() {
		return connection;
	}

	public void setConnection(ServerConnection connection) {
		this.connection = connection;
	}

	public String getIP() {
		return IP;
	}

	public void setIP(String iP) {
		IP = iP;
	}

	public int getPort() {
		return port;
	}

	public void setPort(int port) {
		this.port = port;
	}

	public void stop(){
		connection.close();
	}

}
