package es.um.redes.nanoFiles.udp.client;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.SocketTimeoutException;

import es.um.redes.nanoFiles.tcp.server.NFServer;
import es.um.redes.nanoFiles.udp.message.DirMessage;
import es.um.redes.nanoFiles.udp.message.DirMessageOps;
import es.um.redes.nanoFiles.udp.server.NFDirectoryServer;
import es.um.redes.nanoFiles.util.FileInfo;

/**
 * Cliente con métodos de consulta y actualización específicos del directorio
 */
public class DirectoryConnector {
	/**
	 * Puerto en el que atienden los servidores de directorio
	 */
	private static final int DIRECTORY_PORT = 6868;
	/**
	 * Tiempo máximo en milisegundos que se esperará a recibir una respuesta por el
	 * socket antes de que se deba lanzar una excepción SocketTimeoutException para
	 * recuperar el control
	 */
	private static final int TIMEOUT = 1000;
	/**
	 * Número de intentos máximos para obtener del directorio una respuesta a una
	 * solicitud enviada. Cada vez que expira el timeout sin recibir respuesta se
	 * cuenta como un intento.
	 */
	private static final int MAX_NUMBER_OF_ATTEMPTS = 5;

	/**
	 * Valor inválido de la clave de sesión, antes de ser obtenida del directorio al
	 * loguearse
	 */
	public static final int INVALID_SESSION_KEY = -1;

	/**
	 * Socket UDP usado para la comunicación con el directorio
	 */
	private DatagramSocket socket;
	/**
	 * Dirección de socket del directorio (IP:puertoUDP)
	 */
	private InetSocketAddress directoryAddress;

	private int sessionKey = INVALID_SESSION_KEY;
	private boolean successfulResponseStatus;
	private String errorDescription;

	public DirectoryConnector(String address) throws IOException {
		/*
		 * TODO: Convertir el nombre de host 'address' a InetAddress y guardar la
		 * dirección de socket (address:DIRECTORY_PORT) del directorio en el atributo
		 * directoryAddress, para poder enviar datagramas a dicho destino.
		 */
		InetAddress serverIp = InetAddress.getByName(address);
		this.directoryAddress = new InetSocketAddress(serverIp, DIRECTORY_PORT);
		/*
		 * TODO: Crea el socket UDP en cualquier puerto para enviar datagramas al
		 * directorio
		 */
		this.socket = new DatagramSocket();
	}

	/**
	 * Método para enviar y recibir datagramas al/del directorio
	 * 
	 * @param requestData los datos a enviar al directorio (mensaje de solicitud)
	 * @return los datos recibidos del directorio (mensaje de respuesta)
	 * @throws IOException
	 */
	private byte[] sendAndReceiveDatagrams(byte[] requestData) throws IOException {
		byte responseData[] = new byte[DirMessage.PACKET_MAX_SIZE];
		byte response[] = null;
		if (directoryAddress == null) {
			System.err.println("DirectoryConnector.sendAndReceiveDatagrams: UDP server destination address is null!");
			System.err.println(
					"DirectoryConnector.sendAndReceiveDatagrams: make sure constructor initializes field \"directoryAddress\"");
			System.exit(-1);

		}
		if (socket == null) {
			System.err.println("DirectoryConnector.sendAndReceiveDatagrams: UDP socket is null!");
			System.err.println(
					"DirectoryConnector.sendAndReceiveDatagrams: make sure constructor initializes field \"socket\"");
			System.exit(-1);
		}
		/*
		 * TODO: Enviar datos en un datagrama al directorio y recibir una respuesta. El
		 * array devuelto debe contener únicamente los datos recibidos, *NO* el búfer de
		 * recepción al completo.
		 */
		DatagramPacket packetToServer = new DatagramPacket(requestData, requestData.length, directoryAddress);
		int att = 0;
		while (att < MAX_NUMBER_OF_ATTEMPTS) {
			try {
				socket.send(packetToServer);
				DatagramPacket packetFromServer = new DatagramPacket(responseData, responseData.length);
				socket.setSoTimeout(TIMEOUT);
				socket.receive(packetFromServer);
				String messageFromServer = new String(responseData, 0, packetFromServer.getLength());
				response = messageFromServer.getBytes();
				break;
			} catch (SocketTimeoutException e) {
				att++;
				if (att < MAX_NUMBER_OF_ATTEMPTS) {
					System.out.println("Timeout excedeed, retying...");
				} else {
					System.err.println("Attempts limit reached. Aborting.");
					System.exit(-1);
				}
			}
		}
		/*
		 * TODO: Una vez el envío y recepción asumiendo un canal confiable (sin
		 * pérdidas) esté terminado y probado, debe implementarse un mecanismo de
		 * retransmisión usando temporizador, en caso de que no se reciba respuesta en
		 * el plazo de TIMEOUT. En caso de salte el timeout, se debe reintentar como
		 * máximo en MAX_NUMBER_OF_ATTEMPTS ocasiones.
		 */
		/*
		 * TODO: Las excepciones que puedan lanzarse al leer/escribir en el socket deben
		 * ser capturadas y tratadas en este método. Si se produce una excepción de
		 * entrada/salida (error del que no es posible recuperarse), se debe informar y
		 * terminar el programa.
		 */
		/*
		 * NOTA: Las excepciones deben tratarse de la más concreta a la más genérica.
		 * SocketTimeoutException es más concreta que IOException.
		 */

		if (response != null && response.length == responseData.length) {
			System.err.println("Your response is as large as the datagram reception buffer!!\n"
					+ "You must extract from the buffer only the bytes that belong to the datagram!");
		}
		return response;
	}

	/**
	 * Método para probar la comunicación con el directorio mediante el envío y
	 * recepción de mensajes sin formatear ("en crudo")
	 * 
	 * @return verdadero si se ha enviado un datagrama y recibido una respuesta
	 * @throws IOException
	 */
	public boolean testSendAndReceive() throws IOException {
		/*
		 * TODO: Probar el correcto funcionamiento de sendAndReceiveDatagrams. Se debe
		 * enviar un datagrama con la cadena "login" y comprobar que la respuesta
		 * recibida es "loginok". En tal caso, devuelve verdadero, falso si la respuesta
		 * no contiene los datos esperados.
		 */
		boolean success = false;

		String message = "login";
		byte[] login = message.getBytes();
		byte[] response = this.sendAndReceiveDatagrams(login);
		String response2 = new String(response);
		String loginok = "loginok";

		if (response2.equals(loginok)) {
			success = true;
		}

		return success;
	}

	public InetSocketAddress getDirectoryAddress() {
		return directoryAddress;
	}

	public int getSessionKey() {
		return sessionKey;
	}

	/**
	 * Método para "iniciar sesión" en el directorio, comprobar que está operativo y
	 * obtener la clave de sesión asociada a este usuario.
	 * 
	 * @param nickname El nickname del usuario a registrar
	 * @return La clave de sesión asignada al usuario que acaba de loguearse, o -1
	 *         en caso de error
	 * @throws IOException
	 */
	public boolean logIntoDirectory(String nickname) throws IOException {
		assert (sessionKey == INVALID_SESSION_KEY);
		boolean success = false;
		// TODO: 1.Crear el mensaje a enviar (objeto DirMessage) con atributos adecuados
		// (operation, etc.) NOTA: Usar como operaciones las constantes definidas en la
		// clase DirMessageOps
		DirMessage mensaje = new DirMessage(DirMessageOps.OPERATION_LOGIN);
		mensaje.setNickname(nickname);
		// TODO: 2.Convertir el objeto DirMessage a enviar a un string (método toString)
		// TODO: 3.Crear un datagrama con los bytes en que se codifica la cadena
		String login = mensaje.toString();
		byte[] loginBytes = login.getBytes();
		// TODO: 4.Enviar datagrama y recibir una respuesta (sendAndReceiveDatagrams).
		byte[] respuesta = sendAndReceiveDatagrams(loginBytes);
		// TODO: 5.Convertir respuesta recibida en un objeto DirMessage (método
		// DirMessage.fromString)
		String respuestaString = new String(respuesta);
		DirMessage respuestaMensaje = DirMessage.fromString(respuestaString);
		// TODO: 6.Extraer datos del objeto DirMessage y procesarlos (p.ej., sessionKey)
		if (respuestaMensaje.getOperation().equals(NFDirectoryServer.LOGIN_OK)) {
			sessionKey = Integer.parseInt(respuestaMensaje.getSessionKey());
			System.out.println("Login successful. SessionKey: " + sessionKey);
			success = true;
		} else {
			System.err.println("ERROR: Login failed. You are already logged with this username.");
		}
		// TODO: 7.Devolver éxito/fracaso de la operación
		
		return success;
	}

	/**
	 * Método para obtener la lista de "nicknames" registrados en el directorio.
	 * Opcionalmente, la respuesta puede indicar para cada nickname si dicho peer
	 * está sirviendo ficheros en este instante.
	 * 
	 * @return La lista de nombres de usuario registrados, o null si el directorio
	 *         no pudo satisfacer nuestra solicitud
	 * @throws IOException 
	 */
	public String[] getUserList() throws IOException {
		String[] userlist = null;
		// TODO: Ver TODOs en logIntoDirectory y seguir esquema similar
		DirMessage mensaje = new DirMessage(DirMessageOps.OPERATION_LIST);
		mensaje.setSessionKey(Integer.toString(sessionKey));
		String list = mensaje.toString();
		byte[] listBytes = list.getBytes();
		byte[] respuesta = sendAndReceiveDatagrams(listBytes);
		String respuestaString = new String(respuesta);
		DirMessage respuestaMensaje = DirMessage.fromString(respuestaString);
		if (respuestaMensaje.getOperation().equals(NFDirectoryServer.LIST_OK)) {
			userlist = respuestaMensaje.getUsers().split(",");
		} else {
			System.err.println("ERROR: Userlist failed. You must be logged in to do userlist");
		}
		return userlist;
	}

	/**
	 * Método para "cerrar sesión" en el directorio
	 * 
	 * @return Verdadero si el directorio eliminó a este usuario exitosamente
	 * @throws IOException 
	 */
	public boolean logoutFromDirectory() throws IOException {
		// TODO: Ver TODOs en logIntoDirectory y seguir esquema similar
		assert (sessionKey != INVALID_SESSION_KEY);
		boolean success = false;
		DirMessage mensaje = new DirMessage(DirMessageOps.OPERATION_LOGOUT);
		mensaje.setSessionKey(Integer.toString(sessionKey));
		String logout = mensaje.toString();
		byte[] logoutBytes = logout.getBytes();
		byte[] respuesta = sendAndReceiveDatagrams(logoutBytes);
		String respuestaString = new String(respuesta);
		DirMessage respuestaMensaje = DirMessage.fromString(respuestaString);
		if (respuestaMensaje.getOperation().equals(NFDirectoryServer.LOGOUT_OK)) {
			sessionKey = INVALID_SESSION_KEY;
			System.out.println("Logout successful. Bye!");
			success = true;
		} else {
			System.err.println("ERROR: Logout failed. You must be logged in to do logout.");
		}
		return success;
	}

	/**
	 * Método para dar de alta como servidor de ficheros en el puerto indicado a
	 * este peer.
	 * 
	 * @param serverPort El puerto TCP en el que este peer sirve ficheros a otros
	 * @return Verdadero si el directorio acepta que este peer se convierta en
	 *         servidor.
	 * @throws IOException 
	 */
	public boolean registerServerPort(int serverPort) throws IOException {
		// TODO: Ver TODOs en logIntoDirectory y seguir esquema similar
		boolean success = false;
		
		DirMessage mensaje = new DirMessage(DirMessageOps.OPERATION_REGISTER);
		mensaje.setPort(Integer.toString(serverPort));
		mensaje.setSessionKey(Integer.toString(this.sessionKey));
		String register = mensaje.toString();
		byte[] registerBytes = register.getBytes();
		byte[] respuesta = sendAndReceiveDatagrams(registerBytes);
		String respuestaString = new String(respuesta);
		DirMessage respuestaMensaje = DirMessage.fromString(respuestaString);
		if (respuestaMensaje.getOperation().equals(NFDirectoryServer.REGISTER_OK)) {
			System.out.println("Server registered successfuly.");
			success = true;
		} else {
			System.err.println("ERROR: Server registration failed.");
		}
		
		return success;
	}
	
	public boolean unregisterServer() throws IOException {
		// TODO: Ver TODOs en logIntoDirectory y seguir esquema similar
		boolean success = false;
		
		DirMessage mensaje = new DirMessage(DirMessageOps.OPERATION_UNREGISTER);
		mensaje.setSessionKey(Integer.toString(this.sessionKey));
		String unregister = mensaje.toString();
		byte[] unregisterBytes = unregister.getBytes();
		byte[] respuesta = sendAndReceiveDatagrams(unregisterBytes);
		String respuestaString = new String(respuesta);
		DirMessage respuestaMensaje = DirMessage.fromString(respuestaString);
		if (respuestaMensaje.getOperation().equals(NFDirectoryServer.UNREGISTER_OK)) {
			System.out.println("Server unregistered successfuly.");
			success = true;
		} else {
			System.err.println("ERROR: Server unregistration failed.");
		}
		
		return success;
	}

	/**
	 * Método para obtener del directorio la dirección de socket (IP:puerto)
	 * asociada a un determinado nickname.
	 * 
	 * @param nick El nickname del servidor de ficheros por el que se pregunta
	 * @return La dirección de socket del servidor en caso de que haya algún
	 *         servidor dado de alta en el directorio con ese nick, o null en caso
	 *         contrario.
	 * @throws IOException 
	 */
	public InetSocketAddress lookupServerAddrByUsername(String nick) throws IOException {
		InetSocketAddress serverAddr = null;
		// TODO: Ver TODOs en logIntoDirectory y seguir esquema similar
		
		DirMessage mensaje = new DirMessage(DirMessageOps.OPERATION_DOWNLOADFROM);
		mensaje.setNickname(nick);
		mensaje.setSessionKey(Integer.toString(sessionKey));
		String lookUp = mensaje.toString();
		byte[] lookUpBytes = lookUp.getBytes();
		byte[] respuesta = sendAndReceiveDatagrams(lookUpBytes);
		String respuestaString = new String(respuesta);
		DirMessage respuestaMensaje = DirMessage.fromString(respuestaString);
		if (respuestaMensaje.getOperation().equals(NFDirectoryServer.DOWNLOADFROM_OK)) {
			InetAddress ip = respuestaMensaje.getIp();
			int port = Integer.parseInt(respuestaMensaje.getPort());
			serverAddr = new InetSocketAddress(ip, port);
		} else {
			System.err.println("ERROR: downloadfrom failed. There is no user logged with this nickname.");
		}

		return serverAddr;
	}

	/**
	 * Método para publicar ficheros que este peer servidor de ficheros están
	 * compartiendo.
	 * 
	 * @param files La lista de ficheros que este peer está sirviendo.
	 * @return Verdadero si el directorio tiene registrado a este peer como servidor
	 *         y acepta la lista de ficheros, falso en caso contrario.
	 * @throws IOException 
	 */
	public boolean publishLocalFiles(FileInfo[] files) throws IOException {
		boolean success = false;

		DirMessage mensaje = new DirMessage(DirMessageOps.OPERATION_PUBLISH);
		mensaje.setSessionKey(Integer.toString(sessionKey));
		String fileStr = "";
		for (FileInfo file : files) {
			fileStr += file.fileHash + "," + file.fileName + ":";
		}
		mensaje.setFiles(fileStr);
		String publish = mensaje.toString();
		byte[] publishBytes = publish.getBytes();
		byte[] respuesta = sendAndReceiveDatagrams(publishBytes);
		String respuestaString = new String(respuesta);
		DirMessage respuestaMensaje = DirMessage.fromString(respuestaString);
		if(respuestaMensaje.getOperation().equals(NFDirectoryServer.PUBLISH_OK)) {
			System.out.println("Files published successfully");
			success = true;
		} else {
			System.err.println("ERROR: publish failed");
		}

		return success;
	}

	/**
	 * Método para obtener la lista de ficheros que los peers servidores han
	 * publicado al directorio. Para cada fichero se debe obtener un objeto FileInfo
	 * con nombre, tamaño y hash. Opcionalmente, puede incluirse para cada fichero,
	 * su lista de peers servidores que lo están compartiendo.
	 * 
	 * @return Los ficheros publicados al directorio, o null si el directorio no
	 *         pudo satisfacer nuestra solicitud
	 * @throws IOException 
	 */
	public FileInfo[] getFileList() throws IOException {
		FileInfo[] filelist = null;
		// TODO: Ver TODOs en logIntoDirectory y seguir esquema similar

		DirMessage mensaje = new DirMessage(DirMessageOps.OPERATION_FILELIST);
		mensaje.setSessionKey(Integer.toString(sessionKey));
		String fileList = mensaje.toString();
		byte[] fileListBytes = fileList.getBytes();
		byte[] respuesta = sendAndReceiveDatagrams(fileListBytes);
		String respuestaString = new String(respuesta);
		DirMessage respuestaMensaje = DirMessage.fromString(respuestaString);
		if(respuestaMensaje.getOperation().equals(NFDirectoryServer.FILELIST_OK)) {
			String[] filelistArray = respuestaMensaje.getFiles().split(":");
			filelist = new FileInfo[filelistArray.length];
			try{
				for (int i = 0; i < filelist.length; i++) {

					filelist[i] = new FileInfo(filelistArray[i].split(",")[0], filelistArray[i].split(",")[1], 0, null);
				}
			} catch (ArrayIndexOutOfBoundsException e) {

			}
		} else {
			System.err.println("ERROR: filelist failed");
		}
		
		return filelist;
	}

	/**
	 * Método para obtener la lista de nicknames de los peers servidores que tienen
	 * un fichero identificado por su hash. Opcionalmente, puede aceptar también
	 * buscar por una subcadena del hash, en vez de por el hash completo.
	 * 
	 * @return La lista de nicknames de los servidores que han publicado al
	 *         directorio el fichero indicado. Si no hay ningún servidor, devuelve
	 *         una lista vacía.
	 * @throws IOException 
	 */
	public String[] getServerNicknamesSharingThisFile(String fileHash) throws IOException {
		String[] nicklist = null;
		// TODO: Ver TODOs en logIntoDirectory y seguir esquema similar
		DirMessage mensaje = new DirMessage(DirMessageOps.OPERATION_SEARCH);
		mensaje.setSessionKey(Integer.toString(sessionKey));
		mensaje.setHash(fileHash);
		String search = mensaje.toString();
		byte[] searchBytes = search.getBytes();
		byte[] respuesta = sendAndReceiveDatagrams(searchBytes);
		String respuestaString = new String(respuesta);
		DirMessage respuestaMensaje = DirMessage.fromString(respuestaString);
		if(respuestaMensaje.getOperation().equals(NFDirectoryServer.SEARCH_OK)) {
			nicklist = respuestaMensaje.getServers().split(",");
		} else {
			System.err.println("ERROR: search failed");
		}
		
		return nicklist;
	}

}