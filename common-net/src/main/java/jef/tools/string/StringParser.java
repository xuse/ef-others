package jef.tools.string;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang.ArrayUtils;

/**
 * 将一些不常用的方法从StringUtils中移动到这里，目的是
 * 1、降低StringUtils使用难度
 * 2、大部分应用减少perm开销
 * @author Administrator
 *
 */
public class StringParser {
	/**
	 * 从字符串冲提取关键字
	 * 
	 * @param p
	 *            源字符串 ctrlChars 各种分隔符 holdKeys 即便包含分隔符也要保留的关键字 ignoreCase
	 *            在分析保留关键字时忽略大小写
	 */
	public static List<String> extractKeywords(String p, String ctrlChars, String[] holdKeys, boolean holdKeyIgnoreCase) {
		KeywordExtractor e = new KeywordExtractor(ctrlChars);
		e.setHoldKeys(holdKeys);
		e.setHoldKeyIgnoreCase(holdKeyIgnoreCase);
		return e.extract(p);
	}

	/**
	 * 从字符串冲提取关键字
	 * 
	 * @param p
	 *            源字符串 ctrlChars 各种分隔符 uppercaseWithoutQuot 将不被引号括起的关键字转为大写输入
	 */
	public static List<String> extractKeywords(String p, String ctrlChars, boolean uppercaseWithoutQuot) {
		KeywordExtractor e = new KeywordExtractor(ctrlChars);
		e.setUppercaseNonQuot(uppercaseWithoutQuot);
		return e.extract(p);
	}

	/**
	 * 从字符串冲提取关键字
	 * 
	 * @param p
	 *            源字符串
	 * @param ctrlChars
	 *            各种分隔符
	 * @param uppercaseWithoutQuot
	 *            将不被引号括起的关键字转为大写输入
	 * @param holdKeys
	 *            即便包含分隔符也要保留的关键字
	 * @param holdKeyIgnoreCase
	 *            在分析保留关键字时忽略大小写
	 */
	public static List<String> extractKeywords(String p, String ctrlChars, boolean uppercaseWithoutQuot, String[] holdKeys, boolean holdKeyIgnoreCase) {
		KeywordExtractor e = new KeywordExtractor(ctrlChars);
		e.setHoldKeys(holdKeys);
		e.setHoldKeyIgnoreCase(holdKeyIgnoreCase);
		e.setUppercaseNonQuot(uppercaseWithoutQuot);
		return e.extract(p);
	}

	/**
	 * 从字符串冲提取关键字
	 * 
	 * @param p
	 *            源字符串
	 * @param ctrlChars
	 *            各种分隔符
	 * @param uppercaseWithoutQuot
	 *            将不被引号括起的关键字转为大写输入
	 * @param holdKeys
	 *            即便包含分隔符也要保留的关键字
	 * @param holdIgnoreCase
	 *            在分析保留关键字时忽略大小写
	 * @param quot
	 *            指定引号类型
	 * @param keepQuot
	 *            保留引号
	 */
	public static List<String> extractKeywords(String p, String ctrlChars, boolean uppercaseWithoutQuot, String[] holdKeys, boolean holdKeyIgnoreCase, char quot, boolean keepQuot) {
		KeywordExtractor e = new KeywordExtractor(ctrlChars);
		e.setHoldKeys(holdKeys);
		e.setHoldKeyIgnoreCase(holdKeyIgnoreCase);
		e.setKeepQuot(keepQuot);
		e.setQuot1(quot);
		e.setQuot2(quot);
		e.setUppercaseNonQuot(uppercaseWithoutQuot);
		return e.extract(p);
	}

	/**
	 * 从字符串冲提取关键字
	 * 
	 * @param p
	 *            源字符串
	 * @param ctrlChars
	 *            各种分隔符
	 * @param uppercaseWithoutQuot
	 *            将不被引号括起的关键字转为大写输入
	 * @param holdKeys
	 *            即便包含分隔符也要保留的关键字
	 * @param holdIgnoreCase
	 *            在分析保留关键字时忽略大小写
	 * @param quot1
	 *            指定引号1类型
	 * @param quot2
	 *            指定引号2类型
	 * @param keepQuot
	 *            保留引号
	 */
	public static List<String> extractKeywords(String p, String ctrlChars, boolean uppercaseWithoutQuot, String[] holdKeys, boolean holdKeyIgnoreCase, char quot1, char quot2, boolean keepQuot) {
		KeywordExtractor e = new KeywordExtractor(ctrlChars);
		e.setHoldKeys(holdKeys);
		e.setHoldKeyIgnoreCase(holdKeyIgnoreCase);
		e.setKeepQuot(keepQuot);
		e.setQuot1(quot1);
		e.setQuot2(quot2);
		e.setUppercaseNonQuot(uppercaseWithoutQuot);
		return e.extract(p);
	}

	/**
	 * 从字符串冲提取关键字
	 * 
	 * @param p
	 *            源字符串
	 * @param ctrlChars
	 *            各种分隔符
	 * @param quot1
	 *            指定引号1类型
	 * @param quot2
	 *            指定引号2类型
	 * @param keepQuot
	 *            保留引号
	 */
	public static List<String> extractKeywords(String p, String ctrlChars, char quot1, char quot2, boolean keepQuot) {
		KeywordExtractor e = new KeywordExtractor(ctrlChars);
		e.setKeepQuot(keepQuot);
		e.setQuot1(quot1);
		e.setQuot2(quot2);
		return e.extract(p);
	}

	/**
	 * 解析
	 * @param words
	 * @param keys
	 * @return
	 */
	public static Map<String, String> tokeyMaps(List<String> words, String... keys) {
		Map<String, String> map = new HashMap<String, String>();
		for (int i = 0; i < words.size(); i++) {
			String key = words.get(i);
			if (ArrayUtils.contains(keys, key)) {
				i++;
				if (i < words.size()) {
					map.put(key, words.get(i));
				} else {
					map.put(key, null);
				}
			}
		}
		return map;
	}
	
	/**
	 * 文本截断（从中间开始省略，省去掉地方用...代替）
	 */
	public static String omitCentret(String url, int length) {
		if (url.length() <= length)
			return url;
		int center = url.length() / 2;
		int n = url.length() - length + 3;
		StringSpliter sp = new StringSpliter(url);
		sp.setKey(center - n / 2, center + (n - n / 2));
		String result = sp.getLeft().toString() + "..." + sp.getRight().toString();
		return result;
	}
	
	/**
	 * 类似indexof,区别是查找字符串可以是多个。
	 * @param source
	 * @param keys
	 * @return
	 */
	public static int indexOf(String source, String[] keys) {
		int result = Integer.MAX_VALUE;
		for (String key : keys) {
			int n = source.indexOf(key);
			if (n < result) {
				result = n;
			}
		}
		if (result == Integer.MAX_VALUE)
			return -1;
		return result;
	}

	/**
	 * 类似indexof,区别是查找字符串可以是多个。
	 * 
	 * @param source
	 * @param keys
	 * @return
	 */
	public static int lastIndexOf(String source, String[] keys) {
		int result = -1;
		for (String key : keys) {
			int n = source.lastIndexOf(key);
			if (n > result) {
				result = n;
			}
		}
		return result;
	}
	

	public static boolean containAllKeywords(String[] ss, String[] keywords) {
		for (String s : keywords) {
			if (!ArrayUtils.contains(ss, s))
				return false;
		}
		return true;
	}

}
