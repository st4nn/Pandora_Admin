package com.cehis.wms.module;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;

import com.wowza.wms.application.*;
import com.wowza.wms.amf.*;
import com.wowza.wms.client.*;
import com.wowza.wms.module.*;
import com.wowza.wms.request.*;
import com.wowza.wms.stream.*;
import com.wowza.wms.vhost.IVHost;
import com.wowza.wms.rtp.model.*;
import com.wowza.wms.httpstreamer.model.*;
import com.wowza.wms.httpstreamer.cupertinostreaming.httpstreamer.*;
import com.wowza.wms.httpstreamer.smoothstreaming.httpstreamer.*;

public class Pandora_Admin extends ModuleBase {

	public void doSomething(IClient client, RequestFunction function,
			AMFDataList params) {
		getLogger().info("doSomething");
		sendResult(client, params, "Hello Wowza");
	}

	public void onConnect(IClient client, RequestFunction function,
			AMFDataList params) {
		getLogger().info("onConnect: " + client.getClientId());
	}

	public void onConnectAccept(IClient client) throws IOException {
		getLogger().info("onConnectAccept: " + client.getClientId());
		
		CrearAplicacion(client.getAppInstance().getProperties().getPropertyStr("broncoURL"), client.getQueryStr(), client.getVHost());
	}

	public void onConnectReject(IClient client) {
		getLogger().info("onConnectReject: " + client.getClientId());
	}

	public void onDisconnect(IClient client) {
		getLogger().info("onDisconnect: " + client.getClientId());
	}
	
	public void CrearAplicacion(String Servidor, String Parametros, IVHost VHost) throws IOException
	{
		String[] arrayParametros = Parametros.split("&&");
		String NombreAplicacion = null, 
				NombreStream = null, 
				Usuario = null, 
				Clave = null, 
				Limite = null;
		int PuntosMontaje = 1;
		
		for (int i = 0; i < arrayParametros.length; i++) 
		{
			String[] arrayParametro = arrayParametros[i].split("=");
			arrayParametro[0] = arrayParametro[0].trim();
			arrayParametro[1] = arrayParametro[1].trim();
			
			if (arrayParametro[0].equals("NombreAplicacion"))
			{
				NombreAplicacion = arrayParametro[1];
			}
			
			if (arrayParametro[0].equals("NombreStream"))
			{
				NombreStream = arrayParametro[1];
			}
			if (arrayParametro[0].equals("PuntosMontaje"))
			{
				PuntosMontaje = Integer.parseInt(arrayParametro[1]);
			}
			if (arrayParametro[0].equals("Usuario"))
			{
				Usuario = arrayParametro[1];
			}
			if (arrayParametro[0].equals("Clave"))
			{
				Clave = arrayParametro[1];
			}
			if (arrayParametro[0].equals("Limite"))
			{
				Limite = arrayParametro[1];
			}
		}
		
		File folder1 = new File(VHost.getHomePath() + "/applications/" + NombreAplicacion);
		File folder2 = new File(VHost.getHomePath() + "/conf/" + NombreAplicacion);
		folder1.mkdir();
		folder2.mkdir();
		
		Parametros = "maxStreamViewers=:=" + Limite;
		
		
		
		CrearArchivo(VHost.getHomePath() + "/conf/" + NombreAplicacion + "/Application.xml", EnviaraServidor(Servidor, "generarXML", Parametros));
		String Contenido = "\n" + Usuario + " " + Clave;
		CrearArchivo(VHost.getHomePath() + "/conf/publish.password", Contenido);
		for (int i = 1; i <= PuntosMontaje; i++)
		{
			Contenido = "\n" + NombreStream + i+ "=${Stream.Name}";
			CrearArchivo(VHost.getHomePath() + "/conf/aliasmap.play.txt", Contenido);	
		}
	}
	
	public boolean CrearArchivo(String ruta, String Contenido)
	{
		boolean obj = true;
		try
		{
			File archivo=new File(ruta);
	
			FileWriter escribir=new FileWriter(archivo,true);
	
			escribir.write(Contenido);
			escribir.close();
		}
		catch(Exception e)
		{
			System.out.println("Error al escribir");
			obj = false;
		}
		return obj;
	}
	public String EnviaraServidor(String Servidor, String ArchivoPHP, String Parametros) throws IOException
	{
		PeticionPost post = new PeticionPost("http://localhost/whmcs/" + ArchivoPHP + ".php");
		String[] arrayParametros = Parametros.split(",!,");
		
		for (int i = 0; i < arrayParametros.length; i++) 
		{
			String[] arrayParametro = arrayParametros[i].split("=:=");
			if (arrayParametro.length > 1)
			{
				post.add(arrayParametro[0], arrayParametro[1]);
			}
			else
			{
				post.add(arrayParametro[0], "NULL");
			}
		}
		String respuesta = post.getRespueta();
		return respuesta;
	}
	public static String LeerArchivo(String Archivo) 
	{
	      File archivo = null;
	      FileReader fr = null;
	      BufferedReader br = null;

	      try {
	         // Apertura del fichero y creacion de BufferedReader para poder
	         // hacer una lectura comoda (disponer del metodo readLine()).
	         archivo = new File (Archivo);
	         fr = new FileReader (archivo);
	         br = new BufferedReader(fr);

	         // Lectura del fichero
	         String linea;
	         Archivo = "";
	         System.out.println("\n\n");
	         while((linea=br.readLine())!=null)
	         {
	            System.out.println(linea);
	            Archivo = Archivo + linea;
	         }
	         System.out.println("\n\n");
	      }
	      catch(Exception e){
	         e.printStackTrace();
	      }finally{
	         // En el finally cerramos el fichero, para asegurarnos
	         // que se cierra tanto si todo va bien como si salta 
	         // una excepcion.
	         try{                    
	            if( null != fr ){   
	               fr.close();     
	            }                  
	         }catch (Exception e2){ 
	            e2.printStackTrace();
	         }
	      }
		return Archivo;
	   }
}