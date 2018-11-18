
package common;

import java.text.DecimalFormat;
import java.io.*;
import java.util.*;

import org.w3c.dom.Document;

public class Utils {

	public static String dateConvert(String dateStr, boolean isEC) {
		if (isEmpty(dateStr))
			return "";
		if (dateStr.length() <= 4)
			return dateStr; // invalid
		if (!isEC && dateStr.charAt(3) == '年')
			return dateStr; // no need to convert
		try {
			boolean isDate = dateStr.charAt(4) == '-' || dateStr.charAt(4) == '/';
			String yStr = dateStr.substring(0, 4);
			String mStr = isDate ? dateStr.substring(5, 7) : dateStr.substring(4, 6);
			String dStr = isDate ? dateStr.substring(8, 10) : dateStr.substring(6, 8);
			if (!isEC) {
				yStr = Integer.toString(Integer.parseInt(yStr) - 1911);
				// if (yStr.length()<3) yStr += " ";
			}
			return yStr + " 年 " + mStr + " 月 " + dStr + " 日";
		} catch (Exception e) {
			e.printStackTrace();
		}
		return dateStr;
	}

	public static String dateConvert2(String dateStr, boolean isEC) {
		if (isEmpty(dateStr))
			return "";
		if (dateStr.length() <= 4)
			return dateStr; // invalid
		if (!isEC && dateStr.charAt(3) == '/')
			return dateStr; // no need to convert
		try {
			boolean isDate = dateStr.charAt(4) == '-' || dateStr.charAt(4) == '/';
			String yStr = dateStr.substring(0, 4);
			String mStr = isDate ? dateStr.substring(5, 7) : dateStr.substring(4, 6);
			String dStr = isDate ? dateStr.substring(8, 10) : dateStr.substring(6, 8);
			if (!isEC) {
				yStr = Integer.toString(Integer.parseInt(yStr) - 1911);
			}
			return yStr + "/" + mStr + "/" + dStr;
		} catch (Exception e) {
			e.printStackTrace();
		}
		return dateStr;
	}

	public static boolean isEmpty(String s) {
		return s == null || s.isEmpty();
	}

	static final String _largeAn = "０１２３４５６７８９ａｂｃｄｅｆｇｈｉｊｋｌｍｎｏｐｑｒｓｔｕｖｗｘｙｚＡＢＣＤＥＦＧＨＩＪＫＬＭＮＯＰＱＲＳＴＵＶＷＸＹＺ";

	static boolean is_alphanum(char c) {
		if (c >= '0' && c <= '9')
			return true;
		if (c >= 'a' && c <= 'z')
			return true;
		if (c >= 'A' && c <= 'Z')
			return true;
		if (_largeAn.indexOf(c) >= 0)
			return true;
		return false;
	}

	public static String[] addrSplit(String addr, int lenMax) {
		String[] aAddr = new String[2];
		int len = 0;
		int split_idx = -1;
		int an_idx = -1;
		int an_len = 0;
		for (int i = 0; i < addr.length(); ++i) {
			char c = addr.charAt(i);
			int width = c > 255 ? 2 : 1;
			if (is_alphanum(c)) {
				// System.out.println(c);
				if (an_idx == -1) {
					an_idx = i;
				}
				an_len += width;
			} else {
				// System.out.println(an_len);
				if (an_len > 0) {
					len += an_len;
					if (len > lenMax) {
						split_idx = an_idx;
						break;
					}
					an_idx = -1;
					an_len = 0;
				}
				len += width;
				if (len > lenMax) {
					split_idx = i;
					break;
				}
			}
		}
		if (split_idx == -1 && an_len > 0) {
			len += an_len;
			if (len > lenMax)
				split_idx = an_idx;
		}
		// System.out.println(split_idx);
		// System.out.println(len);
		if (split_idx == -1) {
			aAddr[0] = addr;
			aAddr[1] = "";
		} else {
			aAddr[0] = addr.substring(0, split_idx);
			aAddr[1] = addr.substring(split_idx);
		}
		return aAddr;
	}

	public static String fmt3(String str) {
		try {
			if (isEmpty(str))
				return "";
			if (str.indexOf(',') != -1)
				return str;
			String str2 = null;
			int idx = str.indexOf('.');
			if (idx != -1) {
				str2 = str.substring(idx);
				str = str.substring(0, idx);
			}
			DecimalFormat fmt = new DecimalFormat("#,###,###,###,###");
			return str2 == null ? fmt.format(Long.parseLong(str)) : fmt.format(Long.parseLong(str)) + str2;
		} catch (Exception e) {
		}
		return str;
	}

	public static boolean deleteDirectory(File directory) {
		if (directory.exists()) {
			File[] files = directory.listFiles();
			if (null != files) {
				for (int i = 0; i < files.length; i++) {
					if (files[i].isDirectory()) {
						deleteDirectory(files[i]);
					} else {
						files[i].delete();
					}
				}
			}
		}
		return (directory.delete());
	}

	public static boolean deleteDir(String dir) {
		return deleteDirectory(new File(dir));
	}

	static final String[] chineseNumber = { "零", "壹", "貳", "參", "肆", "伍", "陸", "柒", "捌", "玖" };
	static final String[] chineseUnit = { "", "拾", "佰", "仟", "萬", "拾萬", "佰萬", "仟萬", "億", "拾億", "佰億", "仟億" };

	public static String numToChinese(String input) {
		StringBuffer str = new StringBuffer();
		int index = input.length();
		boolean naZero = false;//

		for (char a : input.toCharArray()) {
			index--;

			if (a > '0') {
				if (naZero) {
					str.append(chineseNumber[0]);
					naZero = false;
				}
				str.append(chineseNumber[(int) (a - '0')] + chineseUnit[index]);
			} else {
				naZero = true;
			}

			String b = chineseUnit[index <= 7 ? 4 : 8];
			int c = str.indexOf(b);
			int d = str.lastIndexOf(b);
			if (c != d) {
				str.deleteCharAt(c);
			}
		}

		if (str.length() == 0) {
			str.append(chineseNumber[0]);
		}

		return str.toString();
	}

	/*private static final Hashtable<String, String> APPROVENAME_MAP = createApproveMap();

	private static Hashtable<String, String> createApproveMap() {
		Hashtable<String, String> result = new Hashtable<String, String>();
		result.put("UNB", "UNB_LTR_APPROVE_FLW");
		result.put("POS", "POS_LTR_APPROVE_FLW");
		result.put("PA", "PA_LTR_APPROVE_FLW");
		result.put("CLM", "CLM_LTR_APPROVE_FLW");
		result.put("SC", "SC_LTR_APPROVE_FLW");
		result.put("SPA", "SPA_LTR_APPROVE_FLW");
		return result;
	}*/

	public static String shortNameToApproveName(String shortName, Document dom) {
		
		String approvalFlow = getApprovalFlowName(shortName);
		/*String dept = get_dept(shortName);
		if (APPROVENAME_MAP.containsKey(dept))
			return APPROVENAME_MAP.get(dept);*/
		return approvalFlow;
	}

	/**
	 * This method used for check letter whether need to go through approval process
	 * when we send letter to customer
	 * 
	 * @param key  letter short name code
	 * @return name of process flow
	 */
	private static String getApprovalFlowName(String key) {
		Configuration c = Configuration.getInstance();
		String value = c.getValue(key);
		if (value == null || "".equals(value)) {
			return null;
		}
		return value;
	}

	private static final Hashtable<String, String> DEPTNAME_MAP = createDeptMap();

	private static Hashtable<String, String> createDeptMap() {
		Hashtable<String, String> result = new Hashtable<String, String>();
		result.put("UNB", "契約處");
		result.put("POS", "客戶服務處");
		result.put("PA", "客戶服務處");
		result.put("CLM", "理賠處");
		result.put("SC", "業務規劃行政處");
		return result;
	}

	public static String get_dept(String shortName) {
		String dept = "";
		int idx = shortName.indexOf('_');
		if (idx >= 0) {
			shortName = shortName.substring(idx + 1);
			idx = shortName.indexOf('_');
			if (idx >= 0) {
				dept = shortName.substring(0, idx);
			}
		}
		return dept;
	}

	public static String shortNameToDeptName(String shortName) {
		String dept = get_dept(shortName);
		if (DEPTNAME_MAP.containsKey(dept))
			return DEPTNAME_MAP.get(dept);
		return "";
	}

	private static final Hashtable<String, String> SENDMODE_MAP = createSendModeMap();

	private static Hashtable<String, String> createSendModeMap() {
		Hashtable<String, String> result = new Hashtable<String, String>();
		result.put("FMT_POS_0060", "04");
		result.put("FMT_POS_0080", "09");
		result.put("FMT_POS_0090", "04");
		result.put("FMT_POS_7220", "04");

		result.put("FMT_PA_0860", "01");
		result.put("FMT_PA_0940", "04");
		result.put("FMT_PA_0950", "04");
		result.put("FMT_PA_0951", "04");
		result.put("FMT_PA_0960", "04");
		result.put("FMT_PA_0961", "04");		
		result.put("FMT_PA_0970", "04");
		result.put("FMT_PA_0980", "04");

		result.put("FMT_UNB_0140", "04"); // IR: 241213 2018/08/27
		//result.put("FMT_UNB_0170", "04"); IR: 241213 2018/09/19

		return result;
	}

	public static String shortNameToSendMode(String shortName) {
		if (SENDMODE_MAP.containsKey(shortName))
			return SENDMODE_MAP.get(shortName);
		return "03";
	}

	public static void main(String[] args) {
		System.out.println(args[0] + "->" + numToChinese(args[0]));
	}
}
