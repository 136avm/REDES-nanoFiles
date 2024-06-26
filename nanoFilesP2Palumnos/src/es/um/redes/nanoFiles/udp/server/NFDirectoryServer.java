package es.um.redes.nanoFiles.udp.server;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.SocketException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Random;
import java.util.Set;

import es.um.redes.nanoFiles.application.NanoFiles;
import es.um.redes.nanoFiles.udp.message.DirMessage;
import es.um.redes.nanoFiles.udp.message.DirMessageOps;
import es.um.redes.nanoFiles.util.FileInfo;

public class NFDirectoryServer {
	/**
	 * Número de puerto UDP en el que escucha el directorio
	 */
	public static final int DIRECTORY_PORT = 6868;
	public static final String LOGIN = "login";
	public static final String LOGIN_OK = "loginok";
	public static final String LOGIN_ERR = "login_failed";
	public static final String LOGOUT_OK = "logoutok";
	public static final String LOGOUT_ERR = "logout_failed";
	public static final String LIST_OK = "userlistok";
	public static final String LIST_ERR = "userlist_failed";
	public static final String DOWNLOADFROM_OK = "downloadfrom_ok";
	public static final String DOWNLOADFROM_ERR = "downloadfrom_failed";
	public static final String REGISTER_OK = "register_ok";
	public static final String REGISTER_ERR = "register_failed";
	public static final String UNREGISTER_OK = "unregister_ok";
	public static final String UNREGISTER_ERR = "unregister_failed";
	public static final String PUBLISH_OK = "publish_ok";
	public static final String PUBLISH_ERR = "publish_failed";
	public static final String FILELIST_OK = "filelist_ok";
	public static final String FILELIST_ERR = "filelist_failed";
	public static final String SEARCH_OK = "search_ok";
	public static final String SEARCH_ERR = "search_failed";

	/**
	 * Socket de comunicación UDP con el cliente UDP (DirectoryConnector)
	 */
	private DatagramSocket socket = null;
	/**
	 * Estructura para guardar los nicks de usuarios registrados, y clave de sesión
	 * 
	 */
	private HashMap<String, Integer> nicks;
	/**
	 * Estructura para guardar las claves de sesión y sus nicks de usuario asociados
	 * 
	 */
	private HashMap<Integer, String> sessionKeys;
	
	private HashMap<String, String> nickPort;
	private HashMap<String, InetAddress> nickIP;
	private HashMap<Integer, HashMap<String,String>> publishedFiles;
	private HashMap<String, LinkedList<String>> fichServers;
	/*
	 * TODO: Añadir aquí como atributos las estructuras de datos que sean necesarias
	 * para mantener en el directorio cualquier información necesaria para la
	 * funcionalidad del sistema nanoFilesP2P: ficheros publicados, servidores
	 * registrados, etc.
	 */




	/**
	 * Generador de claves de sesión aleatorias (sessionKeys)
	 */
	Random random = new Random();
	/**
	 * Probabilidad de descartar un mensaje recibido en el directorio (para simular
	 * enlace no confiable y testear el código de retransmisión)
	 */
	private double messageDiscardProbability;

	public NFDirectoryServer(double corruptionProbability) throws SocketException {
		/*
		 * Guardar la probabilidad de pérdida de datagramas (simular enlace no
		 * confiable)
		 */
		messageDiscardProbability = corruptionProbability;
		/*
		 * TODO: (Boletín UDP) Inicializar el atributo socket: Crear un socket UDP
		 * ligado al puerto especificado por el argumento directoryPort en la máquina
		 * local,
		 */
		this.socket = new DatagramSocket(DIRECTORY_PORT);
		/*
		 * TODO: (Boletín UDP) Inicializar el resto de atributos de esta clase
		 * (estructuras de datos que mantiene el servidor: nicks, sessionKeys, etc.)
		 */
		this.nicks = new HashMap<String, Integer>();
		this.sessionKeys = new HashMap<Integer, String>();
		this.nickPort = new HashMap<String, String>();
		this.nickIP = new HashMap<String, InetAddress>();
		this.publishedFiles = new HashMap<Integer , HashMap<String,String>>();
		this.fichServers = new HashMap<String, LinkedList<String>>();

		if (NanoFiles.testMode) {
			if (socket == null || nicks == null || sessionKeys == null) {
				System.err.println("[testMode] NFDirectoryServer: code not yet fully functional.\n"
						+ "Check that all TODOs in its constructor and 'run' methods have been correctly addressed!");
				System.exit(-1);
			}
		}
	}

	public void run() throws IOException {
		byte[] receptionBuffer = null;
		InetSocketAddress clientAddr = null;
		int dataLength = -1;
		/*
		 * TODO: (Boletín UDP) Crear un búfer para recibir datagramas y un datagrama
		 * asociado al búfer
		 */
		receptionBuffer = new byte[DirMessage.PACKET_MAX_SIZE];
		DatagramPacket packetFromClient = new DatagramPacket(receptionBuffer, receptionBuffer.length);

		System.out.println("Directory starting...");

		while (true) { // Bucle principal del servidor de directorio

			// TODO: (Boletín UDP) Recibimos a través del socket un datagrama
			System.out.println("Waiting to receive datagram...");
			socket.receive(packetFromClient);
			// TODO: (Boletín UDP) Establecemos dataLength con longitud del datagrama
			// recibido
			dataLength = packetFromClient.getLength();
			// TODO: (Boletín UDP) Establecemos 'clientAddr' con la dirección del cliente,
			// obtenida del
			// datagrama recibido
			clientAddr = (InetSocketAddress)packetFromClient.getSocketAddress();

			if (NanoFiles.testMode) {
				if (receptionBuffer == null || clientAddr == null || dataLength < 0) {
					System.err.println("NFDirectoryServer.run: code not yet fully functional.\n"
							+ "Check that all TODOs have been correctly addressed!");
					System.exit(-1);
				}
			}
			System.out.println("Directory received datagram from " + clientAddr + " of size " + dataLength + " bytes");

			// Analizamos la solicitud y la procesamos
			if (dataLength > 0) {
				/*
				 * TODO: (Boletín UDP) Construir una cadena a partir de los datos recibidos en
				 * el buffer de recepción
				 */
				String messageFromClient = new String(receptionBuffer, 0, dataLength);

				if (NanoFiles.testMode) { // En modo de prueba (mensajes en "crudo", boletín UDP)
					System.out.println("[testMode] Contents interpreted as " + dataLength + "-byte String: \""
							+ messageFromClient + "\"");
					/*
					 * TODO: (Boletín UDP) Comprobar que se ha recibido un datagrama con la cadena
					 * "login" y en ese caso enviar como respuesta un mensaje al cliente con la
					 * cadena "loginok". Si el mensaje recibido no es "login", se informa del error
					 * y no se envía ninguna respuesta.
					 */
					
					double rand = Math.random();
					if (rand < messageDiscardProbability) {
						System.err.println("Directory DISCARDED datagram from " + clientAddr);
						continue;
					}
					
					String login = "login";
					if(messageFromClient.equals(login)) {
						String messageToClient = new String("loginok");
						byte[] dataToClient = messageToClient.getBytes();
						System.out.println("Sending datagram with message \"" + messageToClient + "\"");
						System.out.println("Destination is client at addr: " + clientAddr);
						DatagramPacket packetToClient = new DatagramPacket(dataToClient, dataToClient.length, clientAddr);
						socket.send(packetToClient);
					} else {
						System.err.println("Login fallido.");		
					}

				} else { // Servidor funcionando en modo producción (mensajes bien formados)

					// Vemos si el mensaje debe ser ignorado por la probabilidad de descarte
					double rand = Math.random();
					if (rand < messageDiscardProbability) {
						System.err.println("Directory DISCARDED datagram from " + clientAddr);
						continue;
					}

					/*
					 * TODO: Construir String partir de los datos recibidos en el datagrama. A
					 * continuación, imprimir por pantalla dicha cadena a modo de depuración.
					 * Después, usar la cadena para construir un objeto DirMessage que contenga en
					 * sus atributos los valores del mensaje (fromString).
					 */
					System.out.println(messageFromClient);
					DirMessage request = DirMessage.fromString(messageFromClient);
					/*
					 * TODO: Llamar a buildResponseFromRequest para construir, a partir del objeto
					 * DirMessage con los valores del mensaje de petición recibido, un nuevo objeto
					 * DirMessage con el mensaje de respuesta a enviar. Los atributos del objeto
					 * DirMessage de respuesta deben haber sido establecidos con los valores
					 * adecuados para los diferentes campos del mensaje (operation, etc.)
					 */
					DirMessage mensajeProcesado = buildResponseFromRequest(request, clientAddr);
					/*
					 * TODO: Convertir en string el objeto DirMessage con el mensaje de respuesta a
					 * enviar, extraer los bytes en que se codifica el string (getBytes), y
					 * finalmente enviarlos en un datagrama
					 */
					String paraEnviar = mensajeProcesado.toString();
					byte[] dataToClient = paraEnviar.getBytes();
					System.out.println("Sending datagram with message \"" + paraEnviar + "\"");
					System.out.println("Destination is client at addr: " + clientAddr);
					DatagramPacket packetToClient = new DatagramPacket(dataToClient, dataToClient.length, clientAddr);
					socket.send(packetToClient);
				}
			} else {
				System.err.println("Directory ignores EMPTY datagram from " + clientAddr);
			}

		}
	}

	private DirMessage buildResponseFromRequest(DirMessage msg, InetSocketAddress clientAddr) {
		/*
		 * TODO: Construir un DirMessage con la respuesta en función del tipo de mensaje
		 * recibido, leyendo/modificando según sea necesario los atributos de esta clase
		 * (el "estado" guardado en el directorio: nicks, sessionKeys, servers,
		 * files...)
		 */
		String operation = msg.getOperation();

		DirMessage response = null;


		switch (operation) {
		case DirMessageOps.OPERATION_LOGIN: {
			String username = msg.getNickname();

			/*
			 * TODO: Comprobamos si tenemos dicho usuario registrado (atributo "nicks"). Si
			 * no está, generamos su sessionKey (número aleatorio entre 0 y 1000) y añadimos
			 * el nick y su sessionKey asociada. NOTA: Puedes usar random.nextInt(10000)
			 * para generar la session key
			 */
			if(!nicks.containsKey(username)) {
				Integer sessionKey = random.nextInt(1000);
				nicks.put(username, sessionKey);
				sessionKeys.put(sessionKey, username);
				response = new DirMessage(LOGIN_OK);
				response.setNickname(username);
				response.setSessionKey(sessionKey.toString());
				System.out.println("Login successful.");
			} else {
				response = new DirMessage(LOGIN_ERR);
				System.out.println("ERROR: Login failed.");
			}
			/*
			 * TODO: Construimos un mensaje de respuesta que indique el éxito/fracaso del
			 * login y contenga la sessionKey en caso de éxito, y lo devolvemos como
			 * resultado del método.
			 */
			/*
			 * TODO: Imprimimos por pantalla el resultado de procesar la petición recibida
			 * (éxito o fracaso) con los datos relevantes, a modo de depuración en el
			 * servidor
			 */
			return response;
		}
		case DirMessageOps.OPERATION_LOGOUT: {
			int sessionKey = Integer.parseInt(msg.getSessionKey());
			if(sessionKeys.containsKey(sessionKey)) {
				String username = sessionKeys.get(sessionKey);
				nicks.remove(username);
				sessionKeys.remove(sessionKey);
				nickPort.remove(username);
				nickIP.remove(username);
				response = new DirMessage(LOGOUT_OK);
				response.setSessionKey(msg.getSessionKey());
				response.setNickname(username);
				System.out.println("Logout successful.");
				break;
			} else {
				response = new DirMessage(LOGOUT_ERR);
				System.out.println("ERROR: Logout error. Invalid sessionKey");
				break;
			}
		}
		case DirMessageOps.OPERATION_LIST: {
			int sessionKey = Integer.parseInt(msg.getSessionKey());
			if(sessionKeys.containsKey(sessionKey)) {
				String username = "";
				for(String usr : nicks.keySet()) {
					String usrInfo = usr;
					if(nickPort.containsKey(usr)) {
						usrInfo+=":Server";
						usrInfo+="\t Port: " + nickPort.get(usr);
					}else {
						usrInfo+=":User";
					}
					username += usrInfo+",";
				}
				response = new DirMessage(LIST_OK);
				response.setUsers(username);
				response.setSessionKey(msg.getSessionKey());
				response.setNickname(sessionKeys.get(Integer.parseInt(msg.getSessionKey())));
				System.out.println("List successful.");
				break;
			} else {
				response = new DirMessage(LIST_ERR);
				System.out.println("ERROR: List failed. Invalid sessionKey");
				break;
			}
		}
		case DirMessageOps.OPERATION_DOWNLOADFROM: {
			String username = msg.getNickname();
			int sessionKey = Integer.parseInt(msg.getSessionKey());
			if(sessionKeys.containsKey(sessionKey)) {
				if(nickPort.containsKey(username) && nickIP.containsKey(username)) {
					response = new DirMessage(DOWNLOADFROM_OK);
					response.setPort(nickPort.get(username));
					response.setIp(clientAddr.getAddress());
					System.out.println("Downloadfrom successful.");
					break;
				} else {
					response = new DirMessage(DOWNLOADFROM_ERR);
					System.out.println("ERROR: downloadfrom failed. Invalid nickname");
					break;
				}
			} else {
				response = new DirMessage(DOWNLOADFROM_ERR);
				System.out.println("ERROR: downloadfrom failed. Invalid sessionKey");
				break;
			}
		}
		case DirMessageOps.OPERATION_REGISTER: {
			int sessionKey = Integer.parseInt(msg.getSessionKey());
			if(sessionKeys.containsKey(sessionKey)) {
				String username = sessionKeys.get(sessionKey);
				nickPort.put(username, msg.getPort());
				nickIP.put(username, clientAddr.getAddress());
				response = new DirMessage(REGISTER_OK);
				System.out.println("Register successfu.");
				System.out.println(nickPort);
				break;
			} else {
				response = new DirMessage(REGISTER_ERR);
				System.out.println("ERROR: register error. Invalid sessionKey");
				break;
			}
		}
		case DirMessageOps.OPERATION_UNREGISTER: {
			int sessionKey = Integer.parseInt(msg.getSessionKey());
			if(sessionKeys.containsKey(sessionKey)) {
				String username = sessionKeys.get(sessionKey);
				nickPort.remove(username);
				nickIP.remove(username);
				publishedFiles.remove(sessionKey);
				for(LinkedList<String> servers : fichServers.values()) {
					if(servers.contains(username)) {
						servers.remove(username);
					}
				}
				LinkedList<String> vacias = new LinkedList<String>();
				for(Map.Entry<String, LinkedList<String>> entry : fichServers.entrySet()) {
					if(entry.getValue().isEmpty()) {
						vacias.add(entry.getKey());
					}
				}
				for(String entry : vacias) {
					fichServers.remove(entry);
				}
				response = new DirMessage(UNREGISTER_OK);
				System.out.println("Unregister successful.");
				break;
			} else {
				response = new DirMessage(REGISTER_ERR);
				System.out.println("ERROR: unregister error. Invalid sessionKey");
				break;
			}
		}
		case DirMessageOps.OPERATION_PUBLISH: {
			int sessionKey = Integer.parseInt(msg.getSessionKey());
			if(sessionKeys.containsKey(sessionKey)) {
				String[] files = msg.getFiles().split(":");
				HashMap<String,String> hashesSet = new HashMap<String,String>();
				for(int i = 0; i < files.length; i++) {
					hashesSet.put(files[i].split(",")[0], files[i].split(",")[1]);
				}
				publishedFiles.put(sessionKey, hashesSet);
				for(String hash : hashesSet.keySet()) {
					if(!fichServers.containsKey(hash)) {
						fichServers.put(hash, new LinkedList<String>());
					}
					fichServers.get(hash).add(sessionKeys.get(sessionKey));
				}
				response = new DirMessage(PUBLISH_OK);
				System.out.println("Files published successfully");
				break;
			} else {
				response = new DirMessage(PUBLISH_ERR);
				System.out.println("ERROR: publish error. Invalid sessionKey");
				break;
			}
		}
		case DirMessageOps.OPERATION_FILELIST: {
			int sessionKey = Integer.parseInt(msg.getSessionKey());
			if(sessionKeys.containsKey(sessionKey)) {
				response = new DirMessage(FILELIST_OK);
				String files = "";
				Set<String> hashesInserted = new HashSet<String>();
				for(Map.Entry<Integer,HashMap<String,String>> server : publishedFiles.entrySet()) {
					for(Map.Entry<String,String> fichero : server.getValue().entrySet()){
						if(!hashesInserted.contains(fichero.getKey())){
							files += fichero.getKey() + "," + fichero.getValue() + ":";
							hashesInserted.add(fichero.getKey());
						}
					}

				}
				response.setFiles(files);
				break;
			} else {
				response = new DirMessage(FILELIST_ERR);
				System.out.println("ERROR: filelist error. Invalid sessionKey");
				break;
			}
		}
		case DirMessageOps.OPERATION_SEARCH: {
			int sessionKey = Integer.parseInt(msg.getSessionKey());
			if(sessionKeys.containsKey(sessionKey)) {
				String hash = msg.getHash();
				String servers = "";
				if(fichServers.containsKey(hash)) {
					for(String username : fichServers.get(hash)) {
						servers += username + ",";
					}
				}
				if(servers.length()>0) {
					response = new DirMessage(SEARCH_OK);
					response.setServers(servers);
					System.out.println("Search successful");
					break;
				} else {
					response = new DirMessage(SEARCH_ERR);
					System.out.println("ERROR: search error. File does not exist");
					break;
				}
			} else {
				response = new DirMessage(SEARCH_ERR);
				System.out.println("ERROR: search error. Invalid sessionKey");
				break;
			}
		}



		default:
			System.out.println("Unexpected message operation: \"" + operation + "\"");
		}
		return response;

	}
}