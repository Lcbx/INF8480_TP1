package ca.polymtl.inf8480.tp1.client;

import java.rmi.AccessException;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;

import java.util.*;
import java.io.*;

import ca.polymtl.inf8480.tp1.shared.ServerInterface;

public class Client {
	
	
	public static void main(String[] args) {
		Client client = new Client();
		client.run(args);
	}
	
	// the ID used for gonnection
	private int clientID;
	// stub we're using for remote calls
	private ServerInterface serverStub = null;
	// retains data between sessions, mainly the association <string filename, int as string checksum>
	private java.util.Properties properties;
	// directory used to store data
	private String folder = "./clientFiles/";
	// directory that stores the managed files
	private String storage = folder + "cloud_storage/";
	
	
	public Client() {
		super();
	}

	private ServerInterface loadServerStub(String hostname) {
		ServerInterface stub = null;

		try {
			
			Registry registry = LocateRegistry.getRegistry(hostname);
			stub = (ServerInterface) registry.lookup("server");
			
		} catch (NotBoundException e) {
			System.out.println("Erreur: Le nom '" + e.getMessage() 	+ "' n'est pas défini dans le registre.");
		} catch (AccessException e) {
			System.err.println("Erreur: " + e.getMessage());
		} catch (RemoteException e) {
			System.err.println("Erreur: " + e.getMessage());
		}

		return stub;
	}
	
	
	public void run(String[] args){
		
		
		if (System.getSecurityManager() == null) {
			System.setSecurityManager(new SecurityManager());
		}
		
		serverStub = loadServerStub("127.0.0.1");
		
		try{
			
			properties = new Properties();
			properties.loadFromXML(new FileInputStream(folder + "properties.xml"));
			
		} catch ( IOException e) {
			System.err.println("Erreur loading properties : " + e.getMessage());
		}
		
		
		if (args.length == 0) {
			System.out.println("usage : \" ./client <command> <arguments> \"");
		}
		else {
			
			// check client has a valid ID
			try {
				switch(args[0]){
					
					case "ID":
					case "createClientID":
					case "CreateClientID":{
						clientID = serverStub.CreateClientID();
						properties.setProperty("clientID", Integer.toString(clientID));
						break;
					}
					
					default:{
						try{
							clientID =  Integer.parseInt( properties.getProperty("clientID"));
						}
						catch(NullPointerException e){
							System.out.println("you need to call createClientId() first.");
							return;
						}
						break;
					}
					
				}
				// if so we can do what he wants
				switch(args[0]){
					
					case "create":{
						
						String nom = args[1];
						boolean reussi = serverStub.create(nom);
						
						if(reussi) System.out.println("a file named \"" + nom + "\" has been added.");
						else  System.out.println("a file named \"" + nom + "\" already exists.");
						
						break;
					}
					
					case "list":
					{
						
						String[][] files = serverStub.list();
						
						for(String[] file : files){
							System.out.print("* " + file[0] + " \t");
							if(file.length > 1) System.out.println("locked by " + file[1]);
							else System.out.println("unlocked");
						}
						
						break;
					}
					
					case "sync":
					case "syncLocalDirectory":
					{
						String[][] files = serverStub.syncLocalDirectory();
						for(String[] file: files){
							try {
								File f = new File(storage + file[0]);
								FileWriter fw = new FileWriter(f);
								fw.write(file[1]);
								fw.flush();
								fw.close();
							} catch (IOException e) {
								System.err.println("Error writing file content in syncLocalDirectory : " + e.getMessage());
							}
						}
						break;
					}
					
					case "get":
					{
						String nom = args[1];
						int checksum = Integer.parseInt(args[2]);

						File f = new File(storage+nom);
						if(!f.exists()){
							checksum = null;
						}

						String file = serverStub.get(nom, checksum);
						break;
					}
					
					case "lock":
					{
						String nom = args[1];
						int clientID = Integer.parseInt(args[2]);
						int checksum = Integer.parseInt(args[3]);
						break;
					}
					
					case "push":
					{
						String nom = args[1];
						int clientID = Integer.parseInt(args[2]);
						int checksum = Integer.parseInt(args[3]);
						break;
					}	
					
				}
			
			} catch (RemoteException e) {
				System.err.println("Erreur: " + e.getMessage());
			}
			
			
			try{
		
				properties.storeToXML(new FileOutputStream(folder + "/properties.xml"), "");
		
			} catch ( IOException e) {
				System.err.println("Erreur exporting properties : " + e.getMessage());
			}
			
			
		}		
		
	}
	
}