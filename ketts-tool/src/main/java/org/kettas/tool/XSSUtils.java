package org.kettas.tool;

import javax.servlet.http.HttpServletRequest;

/**
 * XSS 帮助类
 * <ul>
 * <li> (空格)</li>
 * <li>&</li>
 * <li>'</li>
 * <li>"</li>
 * <li>&gt;</li>
 * <li>&lt;</li>
 * </ul>
 * 
 * @author Kettas
 */
public class XSSUtils {
	private static final String[] replaceString = new String[] { "'", "<", ">",
			" " };
	private static final String[] replateText = new String[] { "&#039;",
			"&lt;", "&gt;", ">", "&nbsp;" };

	/**
	 * 判断参数中是否存在XSS攻击关键字
	 * 
	 * @param request
	 * @return boolean
	 */
	public static boolean checkString(String src) {
		if (src == null) {
			return false;
		}
		for (int i = 0; replaceString != null && i < replaceString.length; i++) {
			if (src.indexOf(replaceString[i]) > -1) {
				return true;
			}
		}
		return false;
	}

	/**
	 * 判断提交的参数中是否存在XSS攻击关键字
	 * 
	 * @param request
	 * @param name
	 * @return boolean
	 */
	public static boolean checkRequestParameter(HttpServletRequest request,
			String name) {
		if (request == null || name == null || name.trim().length() < 1) {
			return false;
		}
		if (request.getParameter(name) == null) {
			return false;
		}
		String value = request.getParameter(name).toString();
		return checkString(value);
	}

	/**
	 * 判断参数中是否存在攻击代码并自动替换后返回安全的内容
	 * 
	 * @param request
	 * @param name
	 * @param defaultNum
	 *            默认值
	 * @return int
	 */
	public static int checkRequestParameterToInt(HttpServletRequest request,
			String name, int defaultNum) {
		String value = checkRequestParameterToString(request, name);
		try {
			return Integer.valueOf(value);
		} catch (Exception e) {
			return defaultNum;
		}
	}

	/**
	 * 判断参数中是否存在攻击代码并自动替换后返回安全的字符
	 * 
	 * @param request
	 * @param name
	 * @return String
	 */
	public static String checkStringToString(String src, String name) {
		if (src == null || name == null || name.trim().length() < 1) {
			return "";
		}
		src = src.replace("&", "&amp;");
		return checkStringToString(src, replaceString, replateText);
	}
	
	/**
	 * 判断提交参数中是否存在攻击代码并自动替换后返回安全的字符
	 * 
	 * @param request
	 * @param name
	 * @return String
	 */
	public static String checkRequestParameterToString(
			HttpServletRequest request, String name) {
		if (request == null || name == null || name.trim().length() < 1) {
			return "";
		}
		if (request.getParameter(name) == null) {
			return "";
		}
		String value = request.getParameter(name).toString();
		value = value.replace("&", "&amp;");
		return checkStringToString(value, replaceString, replateText);
	}
	/**
	 * 判断提交参数中是否存在攻击代码并自动替换后返回安全的字符
	 * @param request
	 * @param name key
	 * @param defaultValue request 中 key 对应值不存在时返回自定义的默认值 
	 * @return
	 */
	public static String checkRequestParameterToString(
			HttpServletRequest request, String name, String defaultValue) {
		if (request == null || name == null || name.trim().length() < 1) {
			return defaultValue;
		}
		if (request.getParameter(name) == null) {
			return defaultValue;
		}
		String value = request.getParameter(name).toString();
		value = value.replace("&", "&amp;");
		value = checkStringToString(value, replaceString, replateText);
		if (value.trim().length() < 1) {
			return defaultValue;
		}
		return value;
	}

	/**
	 * 过滤参数并返回替换后的文本
	 * 
	 * @param value
	 *            内容
	 * @param replaceString
	 *            需要过滤的数组
	 * @param replaceText
	 *            需要过滤的数组替换文本
	 * @return String
	 */
	public static String checkStringToString(String value,
			String[] replaceString, String[] replaceText) {
		if (value == null) {
			return "";
		}
		for (int i = 0; replaceString != null && i < replaceString.length; i++) {
			while (!replaceString[i].equals("&")
					&& value.indexOf(replaceString[i]) > -1) {
				value = value.replace(replaceString[i], replaceText[i]);
			}
		}
		return value;
	}
	/**
     * 半角转全角
     * @param input String.
     * @return 全角字符串.
     */
    public static String toQuanJaoByBanJiaoString(String input) {
             char c[] = input.toCharArray();
             for (int i = 0; i < c.length; i++) {
               if (c[i] == ' ') {
                 c[i] = '\u3000';
               } else if (c[i] < '\177') {
                 c[i] = (char) (c[i] + 65248);

               }
             }
             return new String(c);
    }

    /**
     * 全角转半角
     * @param input String.
     * @return 半角字符串
     */
    public static String toBanJaoByQuanJiaoString(String input) {
             char c[] = input.toCharArray();
             for (int i = 0; i < c.length; i++) {
               if (c[i] == '\u3000') {
                 c[i] = ' ';
               } else if (c[i] > '\uFF00' && c[i] < '\uFF5F') {
                 c[i] = (char) (c[i] - 65248);

               }
             }
        String returnString = new String(c);
             return returnString;
    }
}
