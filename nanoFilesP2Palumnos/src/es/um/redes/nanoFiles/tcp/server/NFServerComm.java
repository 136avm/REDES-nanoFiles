package es.um.redes.nanoFiles.tcp.server;

import java.io.BufferedInputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.EOFException;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.net.Socket;

import es.um.redes.nanoFiles.application.NanoFiles;
import es.um.redes.nanoFiles.tcp.message.PeerMessage;
import es.um.redes.nanoFiles.tcp.message.PeerMessageOps;
import es.um.redes.nanoFiles.util.FileInfo;

public class NFServerComm {

	public static void serveFilesToClient(Socket socket) {
		/*
		 * TODO: Crear dis/dos a partir del socket
		 */
		try {
			DataInputStream dis = new DataInputStream(socket.getInputStream());
			DataOutputStream dos = new DataOutputStream(socket.getOutputStream());
			
			/*
			 * TODO: Mientras el cliente esté conectado, leer mensajes de socket,
			 * convertirlo a un objeto PeerMessage y luego actuar en función del tipo de
			 * mensaje recibido, enviando los correspondientes mensajes de respuesta.
			 */
			
			while(socket.isConnected()) {
				PeerMessage message = PeerMessage.readMessageFromInputStream(dis);
				
				switch(message.getOpcode()) {
				
				case(PeerMessageOps.OPCODE_DOWNLOAD_FROM):
					int hash = message.getHash();
				
					/*
					 * TODO: Para servir un fichero, hay que localizarlo a partir de su hash (o
					 * subcadena) en nuestra base de datos de ficheros compartidos. Los ficheros
					 * compartidos se pueden obtener con NanoFiles.db.getFiles(). El método
					 * FileInfo.lookupHashSubstring es útil para buscar coincidencias de una
					 * subcadena del hash. El método NanoFiles.db.lookupFilePath(targethash)
					 * devuelve la ruta al fichero a partir de su hash completo.
					 */
				
					FileInfo[] archivos = FileInfo.lookupHashSubstring(NanoFiles.db.getFiles(), Integer.toString(hash));
					if(archivos == null || archivos.length>1) {
						PeerMessage response = new PeerMessage(PeerMessageOps.OPCODE_INVALID_CODE);
						response.writeMessageToOutputStream(dos);
					} else {
						PeerMessage response = new PeerMessage(PeerMessageOps.OPCODE_DOWNLOAD_FROM_OK);
						String path = NanoFiles.db.lookupFilePath(Integer.toString(hash));
						File file = new File(path);
						long fileSize = file.length();
						byte[] fileData = new byte[(int) fileSize];
						response.setFileSize(fileSize);
						response.setFile(fileData);
						response.writeMessageToOutputStream(dos);
					}
					
				}
			}
			
		} catch (EOFException e) {
			System.err.println("ERROR: Client has aborted.");		
		} catch (IOException e) {
			System.err.println("ERROR: IOException has occurred.");
		}
	}
}
