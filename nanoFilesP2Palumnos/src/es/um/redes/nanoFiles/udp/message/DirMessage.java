package es.um.redes.nanoFiles.udp.message;

import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.UnknownHostException;
import java.nio.ByteBuffer;
import java.util.HashMap;
import java.util.LinkedList;

import es.um.redes.nanoFiles.util.FileInfo;

/**
 * Clase que modela los mensajes del protocolo de comunicación entre pares para
 * implementar el explorador de ficheros remoto (servidor de ficheros). Estos
 * mensajes son intercambiados entre las clases DirectoryServer y
 * DirectoryConnector, y se codifican como texto en formato "campo:valor".
 * 
 * @author rtitos
 *
 */
public class DirMessage {
	public static final int PACKET_MAX_SIZE = 65507; // 65535 - 8 (UDP header) - 20 (IP header)

	private static final char DELIMITER = ':'; // Define el delimitador
	private static final char END_LINE = '\n'; // Define el carácter de fin de línea

	/**
	 * Nombre del campo que define el tipo de mensaje (primera línea)
	 */
	private static final String FIELDNAME_OPERATION = "operation";
	/*
	 * TODO: Definir de manera simbólica los nombres de todos los campos que pueden
	 * aparecer en los mensajes de este protocolo (formato campo:valor)
	 */
	private static final String FIELDNAME_NICK = "nickname";
	private static final String FIELDNAME_SESSION = "sessionkey";
	private static final String FIELDNAME_USERS = "users";
	private static final String FIELDNAME_IP = "ip";
	private static final String FIELDNAME_PORT = "port";
	private static final String FIELDNAME_FILES = "files";
	private static final String FIELDNAME_HASH = "hash";
	private static final String FIELDNAME_SERVERS = "servers";


	/**
	 * Tipo del mensaje, de entre los tipos definidos en PeerMessageOps.
	 */
	private String operation = DirMessageOps.OPERATION_INVALID;
	/*
	 * TODO: Crear un atributo correspondiente a cada uno de los campos de los
	 * diferentes mensajes de este protocolo.
	 */
	private String nickname;
	private String sessionKey;
	private String users;
	private InetAddress ip;
	private String port;
	private String files;
	private String hash;
	private String servers;


	public DirMessage(String op) {
		operation = op;
	}


	/*
	 * TODO: Crear diferentes constructores adecuados para construir mensajes de
	 * diferentes tipos con sus correspondientes argumentos (campos del mensaje)
	 */

	public String getOperation() {
		return operation;
	}

	public void setNickname(String nick) {



		nickname = nick;
	}

	public String getNickname() {



		return nickname;
	}
	
	public String getSessionKey() {
		return this.sessionKey;
	}
	public void setSessionKey(String sessionKey) {
		this.sessionKey = sessionKey;
	}
	
	public String getUsers() {
		return this.users;
	}
	public void setUsers(String users) {
		this.users = users;
	}
	
	public InetAddress getIp() {
		return this.ip;
	}
	public void setIp(InetAddress ip) {
		this.ip = ip;
	}
	
	public String getPort() {
		return this.port;
	}
	public void setPort(String port) {
		this.port = port;
	}

	public String getFiles() {
		return files;
	}

	public void setFiles(String files) {
		this.files = files;
	}

	public String getServers() {
		return servers;
	}

	public void setServers(String servers) {
		this.servers = servers;
	}

	public String getHash() {
		return hash;
	}

	public void setHash(String hash) {
		this.hash = hash;
	}


	/**
	 * Método que convierte un mensaje codificado como una cadena de caracteres, a
	 * un objeto de la clase PeerMessage, en el cual los atributos correspondientes
	 * han sido establecidos con el valor de los campos del mensaje.
	 * 
	 * @param message El mensaje recibido por el socket, como cadena de caracteres
	 * @return Un objeto PeerMessage que modela el mensaje recibido (tipo, valores,
	 *         etc.)
	 * @throws UnknownHostException 
	 */
	public static DirMessage fromString(String message) throws UnknownHostException {
		/*
		 * TODO: Usar un bucle para parsear el mensaje línea a línea, extrayendo para
		 * cada línea el nombre del campo y el valor, usando el delimitador DELIMITER, y
		 * guardarlo en variables locales.
		 */

		// System.out.println("DirMessage read from socket:");
		// System.out.println(message);
		String[] lines = message.split(END_LINE + "");
		// Local variables to save data during parsing
		DirMessage m = null;



		for (String line : lines) {
			int idx = line.indexOf(DELIMITER); // Posición del delimitador
			String fieldName = line.substring(0, idx).toLowerCase(); // minúsculas
			String value = line.substring(idx + 1).trim();

			switch (fieldName) {
			case FIELDNAME_OPERATION: {
				assert (m == null);
				m = new DirMessage(value);
				break;
			}
			case FIELDNAME_NICK: {
				m.nickname = value;
				break;
			}
			case FIELDNAME_SESSION: {
				m.sessionKey = value;
				break;
			}
			case FIELDNAME_USERS: {
				m.users = value;
				break;
			}
			case FIELDNAME_IP: {
				m.ip = InetAddress.getByName(value.substring(1));
				break;
			}
			case FIELDNAME_PORT: {
				m.port = value;
				break;
			}
			case FIELDNAME_FILES: {
				m.files = value;
				break;
			}
			case FIELDNAME_HASH: {
				m.hash = value;
				break;
			}
			case FIELDNAME_SERVERS: {
				m.servers = value;
				break;
			}

			default:
				System.err.println("PANIC: DirMessage.fromString - message with unknown field name " + fieldName);
				System.err.println("Message was:\n" + message);
				System.exit(-1);
			}
		}




		return m;
	}

	/**
	 * Método que devuelve una cadena de caracteres con la codificación del mensaje
	 * según el formato campo:valor, a partir del tipo y los valores almacenados en
	 * los atributos.
	 * 
	 * @return La cadena de caracteres con el mensaje a enviar por el socket.
	 */
	public String toString() {

		StringBuffer sb = new StringBuffer();
		sb.append(FIELDNAME_OPERATION + DELIMITER + operation + END_LINE); // Construimos el campo
		/*
		 * TODO: En función del tipo de mensaje, crear una cadena con el tipo y
		 * concatenar el resto de campos necesarios usando los valores de los atributos
		 * del objeto.
		 */
		if(this.nickname!=null) {
			sb.append(FIELDNAME_NICK + DELIMITER + nickname + END_LINE);
		}
		
		if(this.sessionKey!=null) {
			sb.append(FIELDNAME_SESSION + DELIMITER + sessionKey + END_LINE);
		}
		if(this.users!=null) {
			sb.append(FIELDNAME_USERS + DELIMITER + users + END_LINE);
		}
		if(this.ip!=null) {
			sb.append(FIELDNAME_IP + DELIMITER + ip + END_LINE);
		}
		if(this.port!=null) {
			sb.append(FIELDNAME_PORT + DELIMITER + port + END_LINE);
		}
		if(this.files != null){
			sb.append(FIELDNAME_FILES + DELIMITER + files + END_LINE);
		}
		if(this.hash != null){
			sb.append(FIELDNAME_HASH + DELIMITER + hash + END_LINE);
		}
		if(this.servers != null){
			sb.append(FIELDNAME_SERVERS + DELIMITER + servers + END_LINE);
		}

		sb.append(END_LINE); // Marcamos el final del mensaje
		return sb.toString();
	}
}