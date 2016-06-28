package org.kettas.tool;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.Calendar;

import javax.sql.DataSource;

import org.apache.commons.dbutils.DbUtils;
import org.apache.commons.io.Charsets;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;

public class CacheUtil {
	private static boolean JsoupGetContent=true;
	private static DataSource dataSource=null;
	static{
		Connection connection=null;
		PreparedStatement pStatement=null;
		try{
			dataSource=DbUtils.getDataSource();
			connection=dataSource.getConnection();
			pStatement=connection.prepareStatement("delete from paBody where createDate is null or createDate<?");
			SimpleDateFormat simpleDateFormat=new SimpleDateFormat("yyyy-MM-dd");
			Calendar c=Calendar.getInstance();
			c.roll(Calendar.DAY_OF_MONTH, -1);
			java.util.Date last=c.getTime();
			pStatement.setObject(1, new java.sql.Date(simpleDateFormat.parse(simpleDateFormat.format(last)).getTime()));
			pStatement.executeUpdate();
		}catch (Exception e) {
			e.printStackTrace();
		}finally{
			DbUtils.closeQuietly(connection);
			DbUtils.closeQuietly(pStatement);
		}
		
	}
	public static void remove(String url){
		Connection connection=null;
		PreparedStatement pStatement=null;
		try{
			dataSource=DbUtils.getDataSource();
			connection=dataSource.getConnection();
			pStatement=connection.prepareStatement("delete from paBody where url=?");
			pStatement.setObject(1,url);
			pStatement.executeUpdate();
		}catch (Exception e) {
			e.printStackTrace();
		}finally{
			DbUtils.closeQuietly(connection);
			DbUtils.closeQuietly(pStatement);
		}
	}
	public static boolean exist(String url){
		String sqlString="select count(*) as count_num  from paBody where url=?";
		ResultSet rSet=new DbUtils(dataSource).executeQuery(sqlString,new Object[]{url});
		try {
			if(rSet.next()){
				return rSet.getInt("count_num")>0;
			}
		} catch (SQLException e) {
			e.printStackTrace();
		} 
		return false;
	}
	public static void putCache(String url,String body){
		String sqlString="insert into paBody(url,body,createDate)values(?,?,?)";
		Object[]obj=new Object[]{url,body,new java.sql.Date(new java.util.Date().getTime())};
		try {
			new DbUtils(dataSource).executeUpdate(sqlString,obj);
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}
	public static Document getCacheDocument(String url,String encode){
		return Jsoup.parse(getCache(url, encode),url);
	}
	private static String inputStreamToString(InputStream inputStream,String encoding){
		try{
			BufferedReader in = new BufferedReader(new InputStreamReader(inputStream,encoding));  
	        StringBuffer buffer = new StringBuffer();  
	        String line = "";  
	        while ((line = in.readLine()) != null){  
	          buffer.append(line);  
	        }  
	       return buffer.toString(); 
		}catch(Exception x){
			return x.getMessage();
		}
    }
	public static String readURLToString(URL url, String encoding,String method,int timeout)
			throws IOException {
		HttpURLConnection httpConn = (HttpURLConnection) url.openConnection();
        // 设置连接属性
        httpConn.setConnectTimeout(timeout);
        httpConn.setDoInput(true);
        httpConn.setDoOutput(true);
        httpConn.setRequestMethod(method);
        httpConn.setRequestProperty("accept", "*/*");
        httpConn.setRequestProperty("user-agent", "ozilla/5.0 (Windows NT 6.1; WOW64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/51.0.2704.19 Safari/537.36");
        httpConn.setRequestProperty("Content-Type","application/x-www-form-urlencoded");
        int respCode = httpConn.getResponseCode();
        if (respCode == 200){
        	return inputStreamToString(httpConn.getInputStream(), encoding);
        }
		return "";
	}
	private static String getUrlContent(URL url,String encode)throws Exception{
		return getUrlContent(url,encode,"GET");
	}
	private static String getUrlContent(URL url,String encode,String method)throws Exception{
		if(JsoupGetContent){
			return readURLToString(url,encode,method,5000);
		}else{
			return Jsoup.parse(url.openStream(),encode,url.toString()).toString();
		}
	}
	public static String getCache(String url,Charsets charset)throws RuntimeException{
		return getCache(url, charset.toString());
	}
	public static String getCache(String url,String encode,String method)throws RuntimeException{
		String sqlString="select body  from paBody where url=?";
		ResultSet rSet=new DbUtils(dataSource).executeQuery(sqlString,new Object[]{url});
		try {
			if(rSet!=null&&rSet.next()){
				return rSet.getString("body");
			}else{
				String body=getUrlContent(new URL(url),encode,method);
				putCache(url, body);
				return body;
			}
		} catch (Exception e) {
			e.printStackTrace();
			throw new RuntimeException("获得内容:"+url+"失败!",e);
		}
	}
	public static String getCache(String url,String encode)throws RuntimeException{
		return getCache(url,encode,"GET");
	}
}
