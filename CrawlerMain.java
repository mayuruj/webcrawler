// Project 2 : Webcrawler Team: mjnsh 
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.InetAddress;
import java.net.Socket;
import java.net.UnknownHostException;

import java.util.HashSet;
import java.util.Iterator;

import java.util.Set;


import java.util.Collections;
//import java.util.HashSet;
import java.util.LinkedHashSet;




import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;


public class CrawlerMain {

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		// TODO Auto-generated method stub
		
			try{
				new CrawlerBot(args[0],args[1]).init();
			}
			catch(ArrayIndexOutOfBoundsException e){
				System.out.println("Invalid number of parameters\n");
			}
			catch(Exception e){
				System.out.println("Invalid input to the crawler\n");
			}
		}

}

/**
 * @author joe
 *
 */
class CrawlerBot {

	/**
	 * Start of variable declaration block
	 */
	private 					String					userName;
	private 					String					password;
	private						Socket					socketConnection;
	private						OutputStream			crawlerOutputStream;
	private						InputStream				crawlerInputStream;
	private						InputStreamReader 		crawlerInputStreamReader;
	private						BufferedReader 			crawlerBufferedReader;
	private						HtmlParser				htmlParser;
	private 					CrawlerHttpRequest		crawlerHttpRequest;
	private 	static  final	String					TAG							=		"CrawlerBot";
	private		static			Cookies					cookie;
	private 	static			Cookies					updatedCookies;
	private		static			Iterator<String> 		iter;
	/**
	 * End of variable declaration block
	 * 
	 */
	/**@author joe
	 * @param 
	 */
	
	public void init(){

		try {
			openConnection();
			crawlerOutputStream.write(crawlerHttpRequest.getGETHeader("/accounts/login/")
					.getBytes());
			crawlerOutputStream.flush();
			HtmlParser htmlParser=new HtmlParser();
			String httpResponse=htmlParser.parseHtml(crawlerBufferedReader);
			cookie=htmlParser.setCookieFromHtml(httpResponse);
			closeConnection();
			openConnection();
			crawlerOutputStream.write(crawlerHttpRequest.getPOSTHeader("/accounts/login/", cookie,userName,password)
					.getBytes());		
			crawlerOutputStream.flush();
			if(htmlParser.getHttpStatusCode(crawlerBufferedReader)==302){
				
				httpResponse=htmlParser.parseHtml(crawlerBufferedReader);
				closeConnection();
				openConnection();
				updatedCookies=new Cookies();
				updatedCookies.setCsrfmiddelWareToken(cookie.getCsrfmiddelWareToken());
				updatedCookies.setSesssionId(htmlParser.setCookieFromHtml(httpResponse).getSesssionId());
				
				crawlerOutputStream.write(crawlerHttpRequest.
						getGETHeader("/fakebook/",
								updatedCookies).getBytes());
				crawlerOutputStream.flush();
				CrawlerConstants.synToDoSet.add("/fakebook/");
				httpResponse=htmlParser.parseHtml(crawlerBufferedReader);
				//httpResponse
				CrawlerConstants.synToDoSet.addAll(htmlParser.addPageAllUrls(httpResponse));
				closeConnection();
				startCrawl();
			}
			else{
				System.out.println("Message from crawler: Could not login..Please check your username and password\n");
				System.exit(0);
			}

			closeConnection();



		} catch (IOException e) {
			// TODO Auto-generated catch block
			System.out.println("Error : Error in the Outputstream\n");
			System.exit(0);
		}
		catch(Exception e){
			//e.printStackTrace();
			System.out.println("Message from the crawler: Error connecting\n");
			System.exit(0);
		}
	}
	
	public CrawlerBot(String userName, String password) {
		super();
		this.userName = userName;
		this.password = password;
		
	}
	
	public CrawlerBot() {
		super();
		// TODO Auto-generated constructor stub
	}

	/**
	 * @method: startCrawl
	 * @param
	 */
	public void startCrawl(){
		try{
			if(!CrawlerConstants.synToDoSet.isEmpty()){
			
				while(true){
				
					Set<String> hashsetUrl=doTraversal(CrawlerConstants.synToDoSet);
				
					if(hashsetUrl!=null){
				
						CrawlerConstants.synToDoSet.addAll(hashsetUrl);
					}	
				}
			}
		}catch(Exception e){
			System.out.println("Error : error start crawling\n");
		}

	}
	
	/**
	 * 
	 * @param hashSetUrl
	 * @return
	 */

	public  synchronized Set<String> doTraversal(Set<String> hashSetUrl){
		
		Set<String> set=new HashSet();
		Integer httpStatusCode=null;
		
		Iterator<String> iter=hashSetUrl.iterator();
		try {
			while(iter.hasNext()){
				String stringUrl=iter.next();
				if(!CrawlerConstants.synUrlSet.contains(stringUrl)){
					//System.out.println("crawling :"+stringUrl+"++++++++++++++++++++++++++\n");
					
					openConnection();
					HtmlParser htmlParser = new HtmlParser();
					crawlerOutputStream.write(crawlerHttpRequest.getGETHeader(stringUrl,updatedCookies)
					.getBytes());
					crawlerOutputStream.flush();
					//handle all http responses
					synchronized (crawlerBufferedReader) {
						if(crawlerHttpRequest.handleHttpStatusCode(crawlerBufferedReader,stringUrl)){
							String htmlResponse=htmlParser.parseHtml(crawlerBufferedReader);
							htmlParser.searchFlags(htmlResponse);
							set.addAll(htmlParser.addPageAllUrls(htmlResponse));
							CrawlerConstants.synUrlSet.add(stringUrl);
						}
					}
					
					
					// TODO Auto-generated method stub
					if(CrawlerConstants.allFlagsFound){
						System.exit(1);
					}

				}
			}
				
			
		} catch (IOException e) {
			// TODO Auto-generated catch block
			System.out.println("Error: Could not process traversal");
		}
		catch(Exception e){
			
			System.out.println("Error: Could not process traversal");
		}
		return set;
	}


	/**
	 *
	 * @method: openConnection
	 * 
	 */
	public void openConnection(){
				try {
					socketConnection=SocketConnection.open();
						if(socketConnection==null){
							System.out.println("Socket connection is null");
						}
					crawlerHttpRequest=new CrawlerHttpRequest();
					htmlParser=new HtmlParser();
					crawlerOutputStream=socketConnection.getOutputStream();
					crawlerInputStream=socketConnection.getInputStream();
					crawlerInputStreamReader=new InputStreamReader(crawlerInputStream);
					crawlerBufferedReader=new BufferedReader(crawlerInputStreamReader);
				} catch (IOException e) {
					// TODO Auto-generated catch block
					System.out.println("Error : Could not initialize connection\n");
				} catch(Exception e){
					System.out.println("Error : Could not connect\n");
				}

			}

     /**
      *
      * @method: closeConnection
      */
	public void closeConnection(){
		try {
				crawlerBufferedReader.close();
				crawlerInputStreamReader.close();
				crawlerInputStream.close();
				crawlerOutputStream.close();
				socketConnection.close();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				System.out.println("Error : Could not terminate connection\n");
			}
			catch(Exception e){
				System.out.println("Error : Could not connect\n");
			}

	}
	
	/**
	 * 
	 * @method retryRequestForFailure
	 */
	public BufferedReader retryRequestForFailure(String url, Integer statusCode){
		//boolean returnFlag=false;
		BufferedReader br=null;
		
		try{
			openConnection();
			while(statusCode==500 || statusCode==301){
				
				HtmlParser htmlParser = new HtmlParser();
				crawlerOutputStream.write(crawlerHttpRequest.getGETHeader(url,updatedCookies)
						.getBytes());
				crawlerOutputStream.flush();
			    statusCode=htmlParser.getHttpStatusCode(crawlerBufferedReader);
			}
			//crawlerHttpRequest.handleHttpStatusCode(crawlerBufferedReader, url);
			/*if(htmlParser.getHttpStatusCode(crawlerBufferedReader)==500){
				retryRequestForFailure(url);
			}*/
			br=crawlerBufferedReader;
			//System.out.println("Retry successfull");
			
			//closeConnection();
		}
		catch(Exception e){
			//e.printStackTrace();
			//System.out.println("Error : Could not retry");
			
		}
		return br;
	}
			
}

class CrawlerConstants {
	
	public 		static 			Set<String> 		urlSet			=	new HashSet<String>();
	public      static			Set<String>			synUrlSet		=   Collections.synchronizedSet(urlSet);
	public 		static			Set<String> 		searchFlagsSet	= 	new HashSet<String>();
	public 		static			Set<String>			synSearchFlags	= 	Collections.synchronizedSet(searchFlagsSet);
	public 		static			Set<String>			toDoSet	  		= 	new LinkedHashSet<String>();
	public 		static			Set<String>			synToDoSet		= 	Collections.synchronizedSet(toDoSet);
	public 	    static			boolean				allFlagsFound	=	false;
}

class HtmlParser {
	
	
	
	/**
	 * @method searchFlags 
	 * @param httpResponse
	 */

	public void searchFlags(String httpResponse){
		try{
			Document htmlDocument=null;
			if(httpResponse!=null){
				htmlDocument=Jsoup.parse(httpResponse);
				org.jsoup.select.Elements h2=htmlDocument.select("h2");
				for(int i=0;i<h2.size();i++){
				Element h2El=h2.get(i);
				if(h2El.attr("class").contains("secret_flag") || h2El.text().contains("FLAG:")){
					String secretFlagSplit[]=h2El.text().toString().split(": ");
					String secretFlag=secretFlagSplit[1];
					CrawlerConstants.synSearchFlags.add(secretFlag);
					
					}
				}
			}
			if(CrawlerConstants.searchFlagsSet.size()==5){
			CrawlerConstants.allFlagsFound=true;
			Iterator<String> iterator=CrawlerConstants.searchFlagsSet.iterator();
				while(iterator.hasNext()){
					System.out.println(iterator.next().toString());
				}
			}
		}
		catch(Exception e){
			System.out.println("Error : error searching flags\n");
		}
	}
	
	
	
	
	/**
	 * 
	 * @param bufReader
	 * @return
	 */
	public String parseHtml(BufferedReader bufReader){

		String htmlString="";
		String tempString="";
		StringBuffer stringBuffer=new StringBuffer();
		try {
			while((tempString=bufReader.readLine())!=null || bufReader.readLine()!=null){
				stringBuffer.append(tempString);
			}
			htmlString=stringBuffer.toString();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			System.out.println("Error : Could not read from buffer\n");
		}catch(Exception e){
			System.out.println("Error : error in parsing html\n");
		}
		
		return htmlString;
	}
	
	
	
	
	/**
	 * 
	 * @param htmlResponse
	 * @return
	 */
	public Cookies setCookieFromHtml(String htmlResponse){
		Cookies cookie=new Cookies();
		try{
			
			String[] splitArray=htmlResponse.split(" ");
			for(int i=0;i<splitArray.length;i++){
				//System.out.println("SplitArray"+splitArray[i]+" "+i);
				if(splitArray[i].contains("csrftoken")){
					String[] splitcsrftoken=splitArray[i].split("=");
					String[] splitSemiColon=splitcsrftoken[1].split(";");
					cookie.setCsrfmiddelWareToken(splitSemiColon[0]);
				

				}
				if(splitArray[i].contains("sessionid")){
					String[] splitSessionId=splitArray[i].split("=");
					String[] splitSemiColon=splitSessionId[1].split(";");
					cookie.setSesssionId(splitSemiColon[0]);
				
				}
			}
		}
		catch(Exception e){
			System.out.println("Error : could not set cookies");
		}
		return cookie;

	}
	
	
	/**
	 * 
	 * @param buf
	 * @return
	 */
	public Integer getHttpStatusCode(BufferedReader buf){
		
		Integer statusCode=0;
		try {
			String[] s=buf.readLine().split(" ");
			statusCode=Integer.parseInt(s[1]);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			//e.printStackTrace();
			System.out.println("Error : could not get http status code\n");
		}

		return statusCode;
	}
	
	
	
	/**
	 * 
	 * @param httpResponse
	 * @return
	 */
	public String getLocation(String httpResponse){
		
		StringBuffer Location=new StringBuffer();
		StringBuffer tempString=new StringBuffer();
		String splitString[]=httpResponse.split(":");
		int i=0;
		try{
			while(!splitString[i].contains("Location")){
				i++;
			}
			tempString=tempString.append(splitString[i+1]+":"+splitString[i+2]);
			int lasIndex=tempString.lastIndexOf("/");
			i=0;
			while(i!=lasIndex+1){
				Location.append(tempString.charAt(i));
				i++;
			}
		//httpResponse.indexOf("Location");
		//System.out.println("Got location : "+Location);
		}catch(Exception e){
			System.out.println("Error : error getting location");
		}
		return Location.toString();
	}
	
	
	/**
	 * 
	 * @param htmlResponse
	 * @return
	 */
	public synchronized Set<String> addPageAllUrls(String htmlResponse){


		Set<String> set=new HashSet<String>();
		try{
			Document htmlDocument=null;
			if(htmlResponse!=null){
				htmlDocument=Jsoup.parse(htmlResponse);
				org.jsoup.select.Elements href=htmlDocument.select("a[href]");	
				for(int i=0;i<href.size();i++){
					Element link=href.get(i);
					if(link.attr("href").startsWith("/") || link.attr("href").startsWith("http://cs5700.ccs.neu.edu")){
						set.add(link.attr("href"));
					}
				}
			}
		}
		catch(Exception e){
			System.out.println("Error : adding page url\n");
		}


		return set;

	}

}

class Cookies {
	/**
	 * Variable block
	 */
	private 		 				String		csrfMiddleWareToken;
	private							String		sesssionId;
	public String getCsrfmiddelWareToken() {
		return csrfMiddleWareToken;
	}
	public void setCsrfmiddelWareToken(String csrfmiddelWareToken) {
		this.csrfMiddleWareToken = csrfmiddelWareToken;
	}
	public String getSesssionId() {
		return sesssionId;
	}
	public void setSesssionId(String sesssionId) {
		this.sesssionId = sesssionId;
	}
	/**
	 * End of variable block
	 */
	
	
}

class CrawlerHttpRequest {
	
	public String getGETHeader(String url){
		String getRequest="";
		try{
				getRequest=
				"GET "+url+" HTTP/1.0\n"+
				"Host: cs5700.ccs.neu.edu\n"+
				""+
				"\n";
		}
		catch(Exception e){
			System.out.println("Error : error getting header\n");
		}
		
		return getRequest;		
	}
	
	/**
	 * 
	 */
	public String getGETHeader(String url, Cookies cookie){
		String getRequest="";
			try{
				getRequest=
				"GET "+url+" HTTP/1.0\n"+
				"Host: cs5700.ccs.neu.edu\n"+
				"Connection: close\r\n"+
				"Accept: */*\r\n"+
				"User-Agent: Java\r\n"+
				"DNT: 1\r\n"+
				"Content-Type: application/x-www-form-urlencoded\r\n"+
				"Cookie: csrftoken="+cookie.getCsrfmiddelWareToken()+";"+" "+"sessionid="+cookie.getSesssionId()+";"+"\r\n\r\n"+
				""+
				"\n";
			}
			catch(Exception e){
				System.out.println("Error : error getting header\n");
			}
		return getRequest;
	}
	public String getPOSTHeader(String url, Cookies cookie, String userName,String password){
		String params="";
		String postRequest="";
		params = "csrfmiddlewaretoken"+"="+cookie.getCsrfmiddelWareToken()+"&"+"username"+"="+userName+"&"+"password"+"="+password+"&"+"next="+"%2Ffakebook%2F";
		postRequest=
				"POST "+url+" HTTP/1.0\r\n"+
				"Host: cs5700.ccs.neu.edu\r\n"+
				"Connection: close\r\n"+
				"Accept: */*\r\n"+
				"Content-Type: application/x-www-form-urlencoded\r\n"+
				"Content-Length: " + params.length()+ "\r\n"+
				"Cookie: csrftoken="+cookie.getCsrfmiddelWareToken()+";"+" "+"sessionid="+cookie.getSesssionId()+";"+"\r\n\r\n"+
				params+"";
		
		return postRequest;
	}
	
	/**
	 * Method: getCookieHeader
	 */
	public String getCookieHeader(Cookies cookie){
		String cookieHeader="";
		try{
				cookieHeader=
				"csrftoken="+cookie.getCsrfmiddelWareToken()+
				";"+
				" "+
				"sessionid="+
				cookie.getSesssionId()+
				";"+
				"\r\n\r\n";
		}
		catch(Exception e){
			System.out.println("Error : Could not get cookie header\n");
		}
		return cookieHeader;
	}
	/**
	 * @method: handleHttpStatusCodeOk
	 * @param statusCode [Integer]
	 */
	public static boolean handleHttpStatusCode(BufferedReader buf,String url){
		boolean returnFlag=true;
		try{
			HtmlParser htmlParser=new HtmlParser();
			int statusCode=htmlParser.getHttpStatusCode(buf);
			CrawlerBot crawlerBot=new CrawlerBot();
			switch(statusCode){
			case 200: 
						
						returnFlag=true;
						break;
			case 301:
						
						
						crawlerBot.retryRequestForFailure(htmlParser.getLocation(htmlParser.parseHtml(buf)),statusCode);
						
						break;
			case 404:
						
						returnFlag=false;
						break;
			case 403:
						returnFlag=false;
						break;
			case 500:	
						
						
						
						
						crawlerBot.retryRequestForFailure(url,statusCode);
						
						
						break;
			default:
						returnFlag=true;
						break;
		}
		}catch(Exception e){
			System.out.println("Error : Exception in status code \n");
		}
		return returnFlag;
	}
	
	
	
	
}

class SocketConnection {
	/*
	 * Variable declaration block
	 */
	protected 	static 	final 	String 	host					= 		"cs5700.ccs.neu.edu";
	protected	static	final	Integer	port					=		80;
	protected					Socket	httpSocketConnection;  						
	/*
	 * End of variable decalaration
	 */
	
	public static Socket open(){
		
		Socket httpSocketConnection=null;
		try {
			
			httpSocketConnection=new Socket(InetAddress.getByName(host),port);
			
		} catch (UnknownHostException e) {
			// TODO Auto-generated catch block
			System.out.println("Message from crawler: Could not find host\n");
		} catch (IOException e) {
			// TODO Auto-generated catch block
			
			System.out.println("Message from crawler: Error connecting\n");
			System.exit(0);
		}
		catch (Exception e){
			
			System.out.println("Mesage from the crawler: Error connecting\n");
			System.exit(0);
		}
		return httpSocketConnection;
	}
}


