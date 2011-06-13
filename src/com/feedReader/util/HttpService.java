package com.feedReader.util;

import org.apache.http.client.HttpClient;
import org.apache.http.conn.ClientConnectionManager;
import org.apache.http.conn.params.ConnManagerParams;
import org.apache.http.conn.scheme.PlainSocketFactory;
import org.apache.http.conn.scheme.Scheme;
import org.apache.http.conn.scheme.SchemeRegistry;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.impl.conn.tsccm.ThreadSafeClientConnManager;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.params.HttpConnectionParams;
import org.apache.http.params.HttpParams;
/**
 * Singleton class to manage HttpClient
 * HttpClient will be shared across the application
 * @author Gopal Biyani
 *
 */
public class HttpService {
	private final HttpClient mHttpClient;
	
	private HttpService() {
		mHttpClient = initClient();
	}
	
	private static class HttpServiceHolder{
		public static final HttpService mHttpService = new HttpService();
	}
	
	private HttpClient initClient() {
		//define http params
		HttpParams httpParams = new BasicHttpParams(); 
		// Set the timeout in milliseconds until a connection is established.
		int timeoutConnection = 3000;
		HttpConnectionParams.setConnectionTimeout(httpParams, timeoutConnection);
		// Set the default socket timeout (SO_TIMEOUT) 
		// in milliseconds which is the timeout for waiting for data.
		int timeoutSocket = 5000;
		HttpConnectionParams.setSoTimeout(httpParams, timeoutSocket);
		//set Max total connections
		ConnManagerParams.setMaxTotalConnections(httpParams, 5);
		//set version and scheme registry
//		HttpProtocolParams.setVersion(httpParams, HttpVersion.HTTP_1_1); 
		//define scheme registry
		SchemeRegistry schemeRegistry = new SchemeRegistry(); 
		schemeRegistry.register(new Scheme("http", PlainSocketFactory.getSocketFactory(), 80));
		//create thread safe client connection manager
		ClientConnectionManager clientConnectionManager = new ThreadSafeClientConnManager(httpParams, schemeRegistry); 
		return new DefaultHttpClient(clientConnectionManager, httpParams);//create new client 
	}
	/**
	 * Get HttpService 
	 * @return a {@link HttpService}
	 */
	public static HttpService getInstance(){
		return HttpServiceHolder.mHttpService;
	}
	/**
	 * Get HttpClient
	 * @return a {@link HttpClient}
	 */
	public HttpClient getHttpClient(){
		return mHttpClient;
	}
	
}
