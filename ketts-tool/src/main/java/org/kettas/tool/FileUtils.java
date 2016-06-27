package org.kettas.tool;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Map;
import java.util.Map.Entry;

import org.apache.commons.io.FilenameUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.io.filefilter.AndFileFilter;
import org.apache.commons.io.filefilter.DirectoryFileFilter;
import org.apache.commons.io.filefilter.IOFileFilter;
import org.apache.commons.io.filefilter.NotFileFilter;
import org.apache.commons.io.filefilter.SuffixFileFilter;


public class FileUtils extends org.apache.commons.io.FileUtils {
	/**
	 * 创建目录
	 * 假设D盘是空的：createDirectory("D:\resin3.1.s081219\webapps\WebSite\lists\download")是允许的
	 * @param dir 目录
	 * @return File 文件对象
	 * @throws java.io.IOException
	 */
	public static File createDirectory(String dir) throws java.io.IOException {
		String file="";
		dir=dir.replace("\\", "//");
		String[] paths=dir.split("/");
		File _File=null;
		file=paths[0];
		for(int i=1;i<paths.length;i++){
			if(paths[i]==null||"".equals(paths[i])){
				continue;
			}
			file+="/".concat(paths[i]);
			_File=createDirectory(new File(file));
		}
		return _File;
	}
	/**
	 * 创建目录
	 * @param dir 目录
	 * @return File 文件对象
	 * @throws java.io.IOException
	 */
	public static File createDirectory(File dir) throws java.io.IOException {
		try {
			if (!dir.exists()) {
				dir.mkdir();
			}
			return dir;
		} catch (Exception e) {
			throw new IOException("创建目录[" + dir + "]失败！");
		}
	}
	/**
	 * 打开<code>url</code>并读取其中的内容返回<code>String</code>对象,访问失败则抛出异常默认是UTF-8编码。
	 * <code> String content =
	 * FileUtils.readFileToString(FileUtils.toFile(new
	 * URL("http://www.baidu.com")));
	 * </code>
	 * @param url 
	 * @return String
	 * @throws IOException
	 */
	public static String readURLToString(URL url) throws IOException {
		return readURLToString(url,null);
	}
	public static String[] downFile(String[] urls, String savePath)throws IOException{
		if (urls == null) {
			return null;
		}
		// 保存文件路径
		String[] reStatus = new String[urls.length];
		String saveName="";
		for (int i = 0; i < urls.length; i++) {
			if(urls[i]==null||urls[i].trim().length()<1||urls[i].lastIndexOf(".")<0){
				continue;
			}
			saveName=FilenameUtils.getName(urls[i]);
			// 格式验证
			//saveName = "rmt" + new Date().getTime() + urls[i].substring(urls[i].lastIndexOf("."));
			// 大小验证
			URL fileUrl = null;
			fileUrl = new URL(urls[i]);
			File dir = new File(savePath);
			if (!dir.exists()) {
				dir.mkdirs();
			}
			File savetoFile = new File(savePath + "/" + saveName);
			org.apache.commons.io.FileUtils.copyURLToFile(fileUrl, savetoFile);
			reStatus[i]=saveName;
		}
		return reStatus;
	}
	/**
	 * 打开<code>URL</code>并读取其中的内容返回<code>String</code>对象,访问失败则抛出异常，可自定义<code>URL</code>的编码格式
	 * <code> String content =
	 * FileUtils.readFileToString(FileUtils.toFile(new
	 * URL("http://www.baidu.com")));
	 * </code>
	 * @param url 
	 * @return String
	 * @throws IOException
	 */
	public static String readURLToString(URL source, String encoding)
			throws IOException {
		InputStream input = source.openStream();
		try {
			return IOUtils.toString(input, encoding);
		} finally {
			IOUtils.closeQuietly(input);
		}
	}
	public static String readURLToString(URL url, String encoding,int timeout)throws IOException{
		return readURLToString(url, encoding, "GET", timeout);
	}
	public static String readURLToString(URL url, String encoding,String method)
	throws IOException {
		return readURLToString(url, encoding, method, 3000);
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
        	return inputStreamToString(httpConn.getInputStream(),encoding);
        }
		return "";
	}
	public static String readURLToStringByPOST(URL url, String encoding,Map<String, String> params)
	throws IOException {
		return readURLToStringByPOST(url,encoding,params,true);
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
	/**
	 * 读取URL的内容(method为post，可指定多个参数)
	 * @param url 网络地址(http://www.****.com)
	 * @param encoding 编码格式(gbk,iso8859-1,gb18030,gb2312等)
	 * @param params map的参数(key为参数名,value为参数值)
	 * @return String
	 * @throws IOException
	 */
	public static String readURLToStringByPOST(URL url, String encoding,Map<String, String> params,boolean post)
	throws IOException {
		HttpURLConnection con = null;
		// 构建请求参数
		StringBuffer sb = new StringBuffer();
		if (params != null) {
			for (Entry<String, String> e : params.entrySet()) {
				sb.append(e.getKey());
				sb.append("=");
				sb.append(String.valueOf(e.getValue()));
				sb.append("&");
			}
			if(sb.length()>0){
				sb.substring(0, sb.length() - 1);
			}
		}
		// 尝试发送请求
		try {
			con = (HttpURLConnection) url.openConnection();
			con.setRequestMethod(post?"POST":"GET");con.setDoOutput(true);
			con.setDoInput(true);
			con.setUseCaches(false);
			con.setRequestProperty("accept", "*/*");
			con.setRequestProperty("user-agent", "ozilla/5.0 (Windows NT 6.1; WOW64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/51.0.2704.19 Safari/537.36");
			con.setRequestProperty("Content-Type","application/x-www-form-urlencoded");
			OutputStreamWriter osw = new OutputStreamWriter(con.getOutputStream(),encoding);
			if (params != null) {
				osw.write(sb.toString());
			}
			osw.flush();
			osw.close();
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			if (con != null) {
				con.disconnect();
			}
		}
		// 读取返回内容
		StringBuffer buffer = new StringBuffer();
		try {
			BufferedReader br = new BufferedReader(new InputStreamReader(con
					.getInputStream(),encoding));
			String temp;
			while ((temp = br.readLine()) != null) {
				buffer.append(temp);
				buffer.append("\n");
			}
		}catch (FileNotFoundException e) {
			throw e;	
		} catch (IOException e) {
			throw e;	
		}catch (Exception e) {
			e.printStackTrace();
		}

		return buffer.toString();
	}
	/**
	 * 读取文件或者目录的大小
	 *
	 * @param distFilePath
	 *            目标文件或者文件夹
	 * @return 文件或者目录的大小，如果获取失败，则返回-1
	 */
	public static long genFileSize(String distFilePath) {
		File distFile = new File(distFilePath);
		if (distFile.isFile()) {
			return distFile.length();
		} else if (distFile.isDirectory()) {
			return FileUtils.sizeOfDirectory(distFile);
		}
		return -1L;
	}
	/**
	 * 本地某个目录下的文件列表（不递归）
	 * <pre>
	 * String[] filelist=listFilebySuffix()
	 * </pre>
	 * @param folder
	 *            ftp上的某个目录
	 * @param suffix
	 *            文件的后缀名（比如.mov.xml)
	 * @return 文件名称列表
	 */
	public static String[] listFilebySuffix(String folder, String suffix) {
		IOFileFilter fileFilter1 = new SuffixFileFilter(suffix);
		IOFileFilter fileFilter2 = new NotFileFilter(
				DirectoryFileFilter.INSTANCE);
		FilenameFilter filenameFilter = new AndFileFilter(fileFilter1,
				fileFilter2);
		return new File(folder).list(filenameFilter);
	}

	/**
	 * 获得文件目录
	 *
	 * <pre>
	 * File file = new File(&quot;d:\\a&quot;);
	 * File[] files=FileUtils.getDirectorys(file);
	 * for (File file2 : files) {
	 *   System.out.println(&quot;目录名:&quot;+file2.toString());
	 * }
	 * </pre>
	 *
	 * @param file
	 * @return File[]
	 */
	public static File[] getDirectorys(File file) {
		File[] files = file.listFiles(new FilenameFilter() {
			/**
			 * 测试指定文件是否应该包含在某一文件列表中。
			 *
			 * @param dir
			 *            被找到的文件所在的目录。
			 * @param name
			 *            文件的名称。
			 * @return 当且仅当该名称应该包含在文件列表中时返回 true；否则返回
			 *         false。返回true时，该文件将被允许添加到文件列表中，否则不能添加到文件列表中。
			 */
			public boolean accept(File dir, String name) {
				File f = new File(dir, name);
				if (f.isDirectory())
					return true;// 如果是文件夹，返回true。
				else
					return false;// 否则返回false。
			}
		});
		return files;
	}
	/**
	 * 获得目录下的文件
	 *
	 * <pre>
	 * File file = new File(&quot;d:\\a&quot;);
	 * File[] files=FileUtils.getDirectorys(file);
	 * for (File file2 : files) {
	 *   System.out.println(&quot;目录名:&quot;+file2.toString());
	 * }
	 * </pre>
	 *
	 * @param file
	 * @return File[]
	 */
	public static File[] getFiles(File file) {
		File[] files = file.listFiles(new FilenameFilter() {
			public boolean accept(File dir, String name) {
				File f = new File(dir, name);
				if (f.isFile())
					return true;// 如果是文件夹，返回true。
				else
					return true;// 否则返回false。
			}
		});
		return files;
	}
}
