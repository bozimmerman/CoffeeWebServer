package com.planet_ink.coffee_web.tests;
import static org.junit.Assert.*;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.Socket;
import java.util.Hashtable;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.zip.GZIPInputStream;

import javax.ws.rs.core.Cookie;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.core.NewCookie;
import javax.ws.rs.core.Response.Status;

import org.junit.Test;

import com.planet_ink.coffee_web.http.HTTPHeader;
import com.planet_ink.coffee_web.http.HTTPStatus;
import com.planet_ink.coffee_web.servlets.HelloWorldServlet;
import com.planet_ink.coffee_common.logging.Log;
import com.planet_ink.coffee_web.util.CWThreadExecutor;
import com.planet_ink.coffee_web.util.CWConfig;
import com.sun.jersey.api.client.Client;
import com.sun.jersey.api.client.ClientResponse;
import com.sun.jersey.api.client.WebResource;
import com.sun.jersey.core.util.MultivaluedMapImpl;

/**
 * 
 * @author Bo Zimmerman
 *
 */
public class HttpTests
{
	
	@Test
	public void loadAndPersistentConnectionTest()
	{
		CWConfig config=new CWConfig();
		config.setLogger(Log.instance());
		CWThreadExecutor executor = new CWThreadExecutor("test", config, 25, 25, 25, TimeUnit.SECONDS, 10, 100000);
		final AtomicInteger failures=new AtomicInteger(0);
		int numRequests=100;
		final Hashtable <Thread,Client> clients=new Hashtable<Thread,Client>();
		for(int i=0;i<numRequests;i++)
			executor.execute(new Runnable(){
				@Override
				public void run()
				{
					Client httpClient=null;
					try
					{
						if(!clients.containsKey(Thread.currentThread()))
						{
							httpClient=Client.create();
							httpClient.setConnectTimeout(Integer.valueOf(1000));
							httpClient.setReadTimeout(Integer.valueOf(1000));
						}
						else
							httpClient = clients.get(Thread.currentThread());
						final WebResource webResource = httpClient.resource("http://localhost:8080/");
						final ClientResponse webResponse = webResource.accept(MediaType.TEXT_HTML_TYPE).get(ClientResponse.class);
						if(webResponse.getStatus() != Status.OK.getStatusCode())
						{
							failures.incrementAndGet();
						}
					}
					catch (Exception e)
					{
						failures.incrementAndGet();
						e.printStackTrace(System.err);
					}
				}
			});
		while(executor.getQueue().size()>0)
			try{Thread.sleep(2);}catch(Exception e){}
		executor.shutdown();
		try
		{
			executor.awaitTermination(10000, TimeUnit.MILLISECONDS);
		}
		catch (InterruptedException e)
		{
			e.printStackTrace();
		}
		for(Client c: clients.values())
			c.destroy();
		if(failures.get()>0)
			fail("FAIL, failures = "+failures+"/"+numRequests);
	}
	
	
	@Test
	public void simpleRequestTest()
	{
		Client httpClient=null;
		try
		{
			httpClient = Client.create();
			for(int i=0;i<10;i++)
			{
				WebResource webResource = httpClient.resource("http://localhost:8080/index.html");
				ClientResponse webResponse = webResource.get(ClientResponse.class);
				if(webResponse.getStatus() != 200)
				{
					fail("FAIL, web status = "+webResponse.getStatus());
					break;
				}
			}
		}
		catch (Exception e)
		{
			fail("FAIL: "+e.getMessage());
			e.printStackTrace(System.err);
		}
		finally
		{
			if(httpClient!=null)
				httpClient.destroy();
		}
	}
	
	@Test
	public void rangedRequestTest()
	{
		Client httpClient=null;
		try
		{
			httpClient = Client.create();
			WebResource webResource = httpClient.resource("http://localhost:8080/ranged.txt");
			ClientResponse webResponse;
			webResponse = webResource.header("Range","0-2,1-3").get(ClientResponse.class);
			if(webResponse.getStatus() != 206)
			{
				fail("FAIL, web status = "+webResponse.getStatus());
			}
			else
			{
				String body=webResponse.getEntity(String.class);
				if(!body.equals("0123"))
					fail("FAIL, body = "+body);
			}
			webResponse = webResource.header("Range","1-").get(ClientResponse.class);
			if(webResponse.getStatus() != 206)
			{
				fail("FAIL, web status = "+webResponse.getStatus());
			}
			else
			{
				String body=webResponse.getEntity(String.class);
				if(!body.equals("123456789ABCDEF0123456789ABCDEF0123456789ABCDEF0123456789ABCDEF0123456789ABCDEF0123456789ABCDEF0123456789ABCDEF0123456789ABCDEF0123456789ABCDEF0123456789ABCDEF0123456789ABCDEF0123456789ABCDEF0123456789ABCDEF0123456789ABCDEF0123456789ABCDEF0123456789ABCDEF"))
					fail("FAIL, body = "+body);
			}
		}
		catch (Exception e)
		{
			fail("FAIL: "+e.getMessage());
			e.printStackTrace(System.err);
		}
		finally
		{
			if(httpClient!=null)
				httpClient.destroy();
		}
	}
	
	@Test
	public void gzipTest()
	{
		Client httpClient=null;
		try
		{
			httpClient = Client.create();
			WebResource webResource = httpClient.resource("http://localhost:8080/ranged.txt");
			ClientResponse webResponse = webResource.header(HTTPHeader.Common.ACCEPT_ENCODING.toString(),"gzip").get(ClientResponse.class);
			if(webResponse.getStatus() != Status.OK.getStatusCode())
			{
				fail("FAIL, web status = "+webResponse.getStatus());
			}
			else
			if(!findHeaderVal(webResponse, HTTPHeader.Common.CONTENT_ENCODING.toString(), "gzip"))
			{
				fail("FAIL, encoding = "+getFirstHeaderVal(webResponse, HTTPHeader.Common.CONTENT_ENCODING.toString()));
			}
			else
			{
				byte[] body=webResponse.getEntity(byte[].class);
				GZIPInputStream decompressor=new GZIPInputStream(new ByteArrayInputStream(body));
				BufferedReader br=new BufferedReader(new InputStreamReader(decompressor));
				String s=br.readLine();
				if(!s.equals("0123456789ABCDEF0123456789ABCDEF0123456789ABCDEF0123456789ABCDEF0123456789ABCDEF0123456789ABCDEF0123456789ABCDEF0123456789ABCDEF0123456789ABCDEF0123456789ABCDEF0123456789ABCDEF0123456789ABCDEF0123456789ABCDEF0123456789ABCDEF0123456789ABCDEF0123456789ABCDEF"))
					fail("FAIL, body = "+s);
			}
		}
		catch (Exception e)
		{
			fail("FAIL: "+e.getMessage());
			e.printStackTrace(System.err);
		}
		finally
		{
			if(httpClient!=null)
				httpClient.destroy();
		}
	}

	@Test
	public void servletTest()
	{
		Client httpClient=null;
		try
		{
			httpClient = Client.create();
			WebResource webResource = httpClient.resource("http://localhost:8080/helloworld");
			final ClientResponse webResponse = webResource.get(ClientResponse.class);
			if(webResponse.getStatus() != Status.OK.getStatusCode())
			{
				fail("FAIL, web status = "+webResponse.getStatus());
			}
			else
			{
				String body=webResponse.getEntity(String.class);
				if(!body.equals(HelloWorldServlet.helloResponse))
					fail("FAIL, body = "+body);
			}
		}
		catch (Exception e)
		{
			fail("FAIL: "+e.getMessage());
			e.printStackTrace(System.err);
		}
		finally
		{
			if(httpClient!=null)
				httpClient.destroy();
		}
	}
	
	@Test
	public void notFoundTest()
	{
		Client httpClient=null;
		try
		{
			httpClient = Client.create();
			WebResource webResource = httpClient.resource("http://localhost:8080/helloworld.txt");
			final ClientResponse webResponse = webResource.get(ClientResponse.class);
			if(webResponse.getStatus() != Status.NOT_FOUND.getStatusCode())
			{
				fail("FAIL, web status = "+webResponse.getStatus());
			}
		}
		catch (Exception e)
		{
			fail("FAIL: "+e.getMessage());
			e.printStackTrace(System.err);
		}
		finally
		{
			if(httpClient!=null)
				httpClient.destroy();
		}
	}
	
	@Test
	public void mimeTest()
	{
		Client httpClient=null;
		try
		{
			httpClient = Client.create();
			WebResource webResource = httpClient.resource("http://localhost:8080/index.html");
			ClientResponse webResponse = webResource.accept(MediaType.TEXT_HTML).get(ClientResponse.class);
			if(webResponse.getStatus() != Status.OK.getStatusCode())
			{
				fail("FAIL, OK check -- web status = "+webResponse.getStatus());
			}
			webResource = httpClient.resource("http://localhost:8080/index.html");
			webResponse = webResource.accept(MediaType.APPLICATION_OCTET_STREAM).get(ClientResponse.class);
			if(webResponse.getStatus() != HTTPStatus.S406_NOT_ACCEPTABLE.getStatusCode())
			{
				fail("FAIL, NOT ACCEPTABLE, web status = "+webResponse.getStatus());
			}
		}
		catch (Exception e)
		{
			fail("FAIL: "+e.getMessage());
			e.printStackTrace(System.err);
		}
		finally
		{
			if(httpClient!=null)
				httpClient.destroy();
		}
	}
	
	@Test
	public void headTest()
	{
		Client httpClient=null;
		try
		{
			httpClient = Client.create();
			WebResource webResource = httpClient.resource("http://localhost:8080/index.html");
			ClientResponse standardWebResponse = webResource.accept(MediaType.TEXT_HTML).get(ClientResponse.class);
			
			ClientResponse webResponse = webResource.accept(MediaType.TEXT_HTML).head();
			if(webResponse.getStatus() != Status.OK.getStatusCode())
			{
				fail("FAIL, web status = "+webResponse.getStatus());
			}
			if(webResponse.hasEntity())
			{
				fail("FAIL, has entity");
			}
			for(String header : standardWebResponse.getHeaders().keySet())
			{
				if(!webResponse.getHeaders().containsKey(header))
				{
					fail("FAIL, no header "+header);
				}
				if(!webResponse.getHeaders().get(header).equals(standardWebResponse.getHeaders().get(header)))
				{
					fail("FAIL, mismatch header "+header);
				}
			}
		}
		catch (Exception e)
		{
			fail("FAIL: "+e.getMessage());
			e.printStackTrace(System.err);
		}
		finally
		{
			if(httpClient!=null)
				httpClient.destroy();
		}
	}
	
	@Test
	public void malformedTest()
	{
		try
		{
			Socket sock=new Socket("localhost",8080);
			BufferedReader br=new BufferedReader(new InputStreamReader(sock.getInputStream()));
			OutputStream o=sock.getOutputStream();
			o.write(("GET / HPTP/1.1\r\n").getBytes());
			o.flush();
			String str=br.readLine();
			boolean success=((str!=null)&&(str.indexOf(" 400 Bad Request")>=0));
			br.close();
			sock.close();
			if(!success)
			{
				fail("FAIL");
			}
		}
		catch (Exception e)
		{
			fail("FAIL: "+e.getMessage());
			e.printStackTrace(System.err);
		}
	}

	private String findCookieValue(ClientResponse webResponse, String cookieName)
	{
		for(NewCookie cookie : webResponse.getCookies())
			if(cookie.getName().equals(cookieName))
				return cookie.getValue();
		return null;
	}
	
	private boolean findCookie(ClientResponse webResponse, String cookieName, String value)
	{
		if(value.equals(findCookieValue(webResponse,cookieName)))
			return true;
		return false;
	}
	
	private String getFirstHeaderVal(ClientResponse webResponse, String headerName)
	{
		MultivaluedMap<String,String> hdrs=webResponse.getHeaders();
		if(!hdrs.containsKey(headerName))
			return null;
		List<String> vals=hdrs.get(headerName);
		if(vals.size()==0)
			return null;
		return vals.get(0);
	}
	
	private boolean findHeaderVal(ClientResponse webResponse, String headerName, String value)
	{
		String val = getFirstHeaderVal(webResponse,headerName);
		if(value.equals(val))
			return true;
		return false;
	}
	
	@Test
	public void urlEncodedAndCookieAndSessionAndPostTest()
	{
		Client httpClient=null;
		try
		{
			httpClient = Client.create();
			WebResource webResource = httpClient.resource("http://localhost:8080/helloworld");
			MultivaluedMap<String,String> formData = new MultivaluedMapImpl();
			formData.add("name1", "val1");
			formData.add("name2", "val2");
			ClientResponse webResponse = webResource.cookie(new Cookie("cisfor","cookie"))
												    .type(MediaType.APPLICATION_FORM_URLENCODED_TYPE)
													.post(ClientResponse.class, formData);
			if(webResponse.getStatus() != Status.NO_CONTENT.getStatusCode())
			{
				fail("FAIL, post web status = "+webResponse.getStatus());
			}
			else
			if(!findCookie(webResponse,"cisfor","cookie"))
			{
				fail("FAIL, no cookie.");
			}
			else
			if((!findHeaderVal(webResponse,"X-name1","val1"))||(!findHeaderVal(webResponse,"X-name2","val2")))
			{
				fail("FAIL, form data headers.");
			}
			else
			{
				String sessID=findCookieValue(webResponse,"cwsessid");
				if(sessID==null)
				{
					fail("FAIL, no session id");
				}
			}
		}
		catch (Exception e)
		{
			fail("FAIL: "+e.getMessage());
			e.printStackTrace(System.err);
		}
		finally
		{
			if(httpClient!=null)
				httpClient.destroy();
		}
	}
	
	
	@Test
	public void chunkedPostTest()
	{
		try
		{
			final String map="0123456789abcdefghijklmnopqrstuvwxyz~`!@#$%^&*()_-+=[{]}\\|;:'\",<.>/?";
			final byte[] body=new byte[62200];
			for(int b=0;b<body.length;b++)
				body[b]=(byte)map.charAt(b % map.length());
			ByteArrayOutputStream results=new ByteArrayOutputStream();
			results.write(HelloWorldServlet.helloResponseStart.getBytes());
			results.write(body);
			results.write(HelloWorldServlet.helloResponseEnd.getBytes());
			
			Socket socket=new Socket("localhost",8080);
			OutputStream out=new BufferedOutputStream(socket.getOutputStream());
			InputStream in=new BufferedInputStream(socket.getInputStream());
			out.write("POST /helloworld HTTP/1.1\r\n".getBytes());
			out.write("Connection: close\r\n".getBytes());
			out.write("Accept: text/html, image/gif, image/jpeg, *; q=.2, */*; q=.2\r\n".getBytes());
			out.write("Host: localhost:8080\r\n".getBytes());
			out.write("X-DynamicPost: true".getBytes());
			out.write("User-agent: Java/1.7.0_51\r\n".getBytes());
			out.write("Transfer-Encoding: chunked\r\n".getBytes());
			out.write("\r\n".getBytes());
			for(int i=0;i<body.length;i+=4098)
			{
				int rem=body.length-i;
				int len=(rem < 4098) ? rem : 4098; 
				out.write((Integer.toHexString(len)+"\r\n").getBytes());
				out.write(body,i,len);
				out.write("\r\n".getBytes());
				out.flush();
			}
			out.write("0\r\n\r\n".getBytes());
			out.flush(); // and that's all she wrote!
			int c;
			ByteArrayOutputStream bout=new ByteArrayOutputStream();
			while((c=in.read())>=0)
				bout.write(c);
			socket.close();
			String s1=new String(bout.toByteArray());
			int x=s1.indexOf("\r\n\r\n");
			if(x>0)
				s1=s1.substring(x+4);
			String s2=new String(results.toByteArray());
			if(!s1.equalsIgnoreCase(s2))
				fail("FAIL: Incorrect data returned.");
		}
		catch (Exception e)
		{
			fail("FAIL: "+e.getMessage());
			e.printStackTrace(System.err);
		}
	}
}
