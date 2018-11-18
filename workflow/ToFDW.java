
//ToFDW.java - INT-1160
//
//ToFDW <sql_name> <batch_id> 

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.StringReader;
import java.io.StringWriter;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.sql.Clob;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import common.Sql;
import common.Utils;
import common.WatermarkPDF;
import common.Xml;
import sip.util.SysVar;

public class ToFDW {

	private static final Logger logger = Logger.getLogger(ToFDW.class);

	static final int _dataFileMax = 20000;
	
	private final static String STD_CHANNEL_CODE = "2";
	private final static String STD_BRBDBRACNCODE_INSTEAD = "STD";

	private static final String wmPath = "D:\\Case\\TGL\\WM\\";

	public static void main(String args[]) {

		int rc = 0x00010000;

		try {
			String connName = "@jdbc_TGL";
			String connName2 = "@jdbc_ODS";
			String fmtTblName = "t_fmt_document";

			SysVar sv = new SysVar();

			String sqlName = args.length >= 1 && !Utils.isEmpty(args[0]) ? args[0] : "ToFDW.sql";
			String batchId = args.length >= 2 && !Utils.isEmpty(args[1]) ? args[1] : null;
			String fdwFolder = sv.get("FDW_FOLDER", "D:\\SIPTEMP\\FDW");
			String pdfFolder = sv.get("HA_FOLDER", "\\\\tglcifs\\sip_ha");

			boolean isAdhoc = sqlName.contains("_A");

			String dateStr = null;
			if (!Utils.isEmpty(batchId))
				dateStr = "20" + batchId.substring(1, 7);
			if (Utils.isEmpty(dateStr))
				dateStr = (new java.text.SimpleDateFormat("yyyyMMdd")).format(new java.util.Date());

			if (isAdhoc) {
				fdwFolder += "\\ADHOC\\" + dateStr;
			} else {
				fdwFolder += "\\NB\\" + dateStr;
			}

			String sqlSelect = sv.getSql(sqlName);
			if (!Utils.isEmpty(batchId))
				sqlSelect = sqlSelect.replace("?", "'" + batchId + "'");

			System.out.println(sqlName);
			System.out.println(sqlSelect);
			System.out.println(pdfFolder);
			System.out.println(fdwFolder);

			ToFDW toFDW = new ToFDW(connName, connName2, sqlSelect, pdfFolder, fdwFolder,
					(isAdhoc ? "A" : "B") + dateStr, fmtTblName);

			while (true) {
				toFDW.start();
				break;
			}

			if (toFDW._err_update_cnt > 0)
				rc += 33;
			else if (toFDW._err_job_cnt > 0)
				rc += 10;

		} catch (Exception e) {
			e.printStackTrace();
			rc += 98;
		}

		System.exit(rc);
	}

	public int _err_update_cnt = 0;
	public int _err_job_cnt = 0;

	String _connName;
	String _connName2;
	String _sqlSelect;
	String _pdfFolder;
	String _fdwFolder;
	String _batchId;
	String _fmtTblName;

	PreparedStatement _stmtUpdateEMAILED = null;
	PreparedStatement _stmtUpdateERRMSG = null;

	DocumentBuilder _db;

	String _lastErr;

	int _seq = 0;
	int _seqBatch = 1;
	StringBuilder _sbDataFile;

	public ToFDW(String connName, String connName2, String sqlSelect, String pdfFolder, String fdwFolder,
			String batchId, String fmtTblName) {
		_connName = connName;
		_connName2 = connName2;
		_sqlSelect = sqlSelect;
		_pdfFolder = pdfFolder;
		_fdwFolder = fdwFolder;
		_batchId = batchId;
		_fmtTblName = fmtTblName;
	}

	public void start() throws Exception {

		java.text.SimpleDateFormat sdf = new java.text.SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

		DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
		_db = dbf.newDocumentBuilder();

		Connection conn = null;
		PreparedStatement stmtSelect = null;
		ResultSet result = null;

		Connection conn2 = null;

		String prev_shortName = null;

		java.util.List<String> wmFrontList = new java.util.ArrayList<String>();
		java.util.List<String> wmBackList = new java.util.ArrayList<String>();
		WatermarkPDF watermarkPDF = new WatermarkPDF();

		int jobs = 0;
		int done = 0;

		try {

			conn = (new Sql(_connName)).getConn();

			conn2 = (new Sql(_connName2)).getConn();
			conn2.setAutoCommit(false);

			_stmtUpdateEMAILED = conn2.prepareStatement("update " + _fmtTblName
					+ " set EMAILED=? where HOSTCODE=? and SPOOLNAME=? and JOBNO=? and SUBJOBNO=?");
			_stmtUpdateERRMSG = conn2.prepareStatement("update " + _fmtTblName
					+ " set ERRMSG=? where HOSTCODE=? and SPOOLNAME=? and JOBNO=? and SUBJOBNO=?");

			stmtSelect = conn.prepareStatement(_sqlSelect);
			result = stmtSelect.executeQuery();
			while (result.next()) {
				String hostCode = result.getString("HOSTCODE");
				String spoolName = result.getString("SPOOLNAME");
				long jobNo = result.getLong("JOBNO");
				int subjobNo = result.getInt("SUBJOBNO");

				String shortName = result.getString("SHORT_NAME");
				long listId = result.getLong("LIST_ID");

				String wmFront = result.getString("WM_EMAIL_FRONT");
				String wmBack = result.getString("WM_EMAIL_BACK");

				if (!shortName.equals(prev_shortName)) {

					prev_shortName = shortName;

					wmFrontList.clear();
					wmBackList.clear();
					if (!Utils.isEmpty(wmFront))
						wmFrontList.add(wmPath + wmFront);
					if (!Utils.isEmpty(wmBack))
						wmBackList.add(wmPath + wmBack);
				}

				System.out.format("%s hostCode=%s, spoolName=%s, jobNo=%d, subjobNo=%d, shortName=%s, listId=%d%n",
						sdf.format(new java.util.Date()), hostCode, spoolName, jobNo, subjobNo, shortName, listId);

				Clob rptData = result.getClob("CONTENT");

				++jobs;

				if (_seq == 0)
					new File(_fdwFolder).mkdirs();

				boolean ok = false;

				ok = do_one(hostCode, spoolName, jobNo, subjobNo, rptData, watermarkPDF, wmFrontList, wmBackList);
				// System.out.println("ret from do_one ="+ok);
				if (ok)
					++done;

				String emailed = ok ? "Y" : "N";

				updateEmailed(_stmtUpdateEMAILED, hostCode, spoolName, jobNo, subjobNo, emailed);
				if (!ok)
					updateErrMsg(_stmtUpdateERRMSG, hostCode, spoolName, jobNo, subjobNo, _lastErr);

			} // while (result.next()) {

			if (_seq > 0) {
				do_ctl();
				conn2.commit();
			}

			_err_job_cnt = jobs - done;

		} catch (Exception e) {
			handleException(e);
			_err_update_cnt++;
			if (conn2 != null)
				conn2.rollback();
		} finally {
			if (result != null) {
				result.close();
				result = null;
			}
			if (_stmtUpdateEMAILED != null) {
				_stmtUpdateEMAILED.close();
				_stmtUpdateEMAILED = null;
			}
			if (_stmtUpdateERRMSG != null) {
				_stmtUpdateERRMSG.close();
				_stmtUpdateERRMSG = null;
			}
			if (stmtSelect != null) {
				stmtSelect.close();
				stmtSelect = null;
			}
			if (conn2 != null) {
				conn2.close();
				conn2 = null;
			}
			if (conn != null) {
				conn.close();
				conn = null;
			}
		}

		System.out.format("jobs=%d, done=%d%n", jobs, done);

	}

	static final String _xmlFmtStr = "<WSLetterUnit>" + "<shortName>%s</shortName>" + "<documentId>%s</documentId>"
			+ "<documentNo>%s</documentNo>" + "%s" + "<ReportInfo>" + "<insu3DaysIndi>%s</insu3DaysIndi>" + // 是否為三日保全照會單Y/N
			"<noteType0800>%s</noteType0800>" + // 0800照會單類型
			"<fileName>%s</fileName>" + // 照會單檔名及路徑
			"</ReportInfo>" + "%s" + "</WSLetterUnit>";

	boolean do_one(String hostCode, String spoolName, long jobNo, int subjobNo, Clob rptData, WatermarkPDF watermarkPDF,
			java.util.List<String> wmFrontList, java.util.List<String> wmBackList) {
		boolean rc = false;

		_lastErr = "";

		try {
			byte[] baData = rptData.getSubString(1, (int) rptData.length()).getBytes("UTF-8");
			Document dom = _db.parse(new ByteArrayInputStream(baData));

			Node idxNode = Xml.getNode(dom, "/WSLetterUnit/index");
			String policyCode = getTagValue(idxNode, dom, "policyCode");
			
			final String salesChannel = Xml.getNodeText(dom,"/WSLetterUnit/basicInfo/salesChannel");
			String brbdBranchCode = Xml.getNodeText(dom,"/WSLetterUnit/basicInfo/brbdBranchCode");
			String policyHolder = Xml.getNodeText(dom,"/WSLetterUnit/basicInfo/policyHolderName");
			final String documentNo = Xml.getNodeText(dom,"/WSLetterUnit/documentNo");
			
			if (logger.isDebugEnabled())
			{
				logger.debug(String.format("Document No = %s !", documentNo));
				logger.debug(String.format("Sales Channel = %s !", salesChannel));
				logger.debug(String.format("BR BD Branch Code = %s !", brbdBranchCode));
				logger.debug(String.format("Policy Holder = %s !", policyHolder));
			}
			
//			SinoDetect sinoDetect = new SinoDetect();
//			final int encodingOfPHStr = sinoDetect.detectEncoding(policyHolder.getBytes("UTF-8"));
//			ZHCode zhcode = new ZHCode();
//			zhcode.setUnsupportedStrategy(ZHCode.CYCLEMARK_FULLWIDTH);
//			policyHolder = zhcode.convertString(policyHolder, encodingOfPHStr, ZHCode.UTF8);
			
//			if (logger.isDebugEnabled())
//			{
//				logger.debug(String.format("Sales Channel = %s !", salesChannel));
//				logger.debug(String.format("BR BD Branch Code = %s !", brbdBranchCode));
//				logger.debug(String.format("Policy Holder = %s !", policyHolder));
//				logger.debug(String.format("Encoding of Policy Holder Name = %s !", encodingOfPHStr));
//			}

			String srcPdf = String.format("%s\\%s\\%s\\%08x\\%06x.pdf", _pdfFolder, hostCode, spoolName, jobNo,
					subjobNo);

//			String fileName = String.format("%s_%s-%s-%08x-%06x.pdf", policyCode, hostCode, spoolName, jobNo, subjobNo);
			if (StringUtils.equalsAnyIgnoreCase(STD_CHANNEL_CODE, salesChannel))
			{
				brbdBranchCode = STD_BRBDBRACNCODE_INSTEAD;
			}
			String fileName = String.format("%s_%s_%s_%s.pdf", documentNo, policyCode, brbdBranchCode, policyHolder);
			if (logger.isDebugEnabled())
			{
				logger.debug(String.format("Taret File Name = %s !", fileName));
			}
			
			
			String dstPdf = String.format("%s\\%s", _fdwFolder, fileName);

			if (!wmFrontList.isEmpty() || !wmBackList.isEmpty()) {
				if (watermarkPDF.start(srcPdf, dstPdf, wmFrontList, wmBackList)) {

				} else {
					throw new Exception("add watermark error " + srcPdf);
				}
			} else {

				Files.copy((new File(srcPdf)).toPath(), (new File(dstPdf)).toPath(),
						StandardCopyOption.REPLACE_EXISTING);
				System.out.format("%s->%s%n", srcPdf, dstPdf);
			}

			Node root = Xml.getNode(dom, "/WSLetterUnit");
			Node basicInfo = Xml.getNode(dom, "/WSLetterUnit/basicInfo");
			Node agentNoteMergedInfo = Xml.getNode(dom, "/WSLetterUnit/agentNoteMergedInfo");

			String insu3DaysIndi = getTagValue(root, dom, "insu3DaysIndi");
			String noteType0800 = "N";

			if (Utils.isEmpty(insu3DaysIndi))
				insu3DaysIndi = "N";

			String xmlStr = String.format(_xmlFmtStr, getTagValue(root, dom, "shortName"),
					getTagValue(root, dom, "documentId"), getTagValue(root, dom, "documentNo"), node2Str(basicInfo),
					insu3DaysIndi, noteType0800, fileName, node2Str(agentNoteMergedInfo));

			++_seq;

			int cnt = _seq % _dataFileMax;
			if (cnt == 1)
				_sbDataFile = new StringBuilder();
			_sbDataFile.append(trimNewLine(xmlStr));
			_sbDataFile.append('\n');

			if (cnt == _dataFileMax) {
				String dataFileName = String.format("%s\\%s-%d-%d.dat", _fdwFolder, _batchId, _seqBatch,
						_seq / _dataFileMax);
				str2File(_sbDataFile.toString(), dataFileName);
			}

			rc = true;
		} catch (Exception e) {
			e.printStackTrace();
			_lastErr = e.toString();
		}

		System.out.println("do_one ret=" + rc);

		return rc;
	}

	String trimNewLine(String input) {
		BufferedReader reader = new BufferedReader(new StringReader(input));
		StringBuffer result = new StringBuffer();
		try {
			String line;
			while ((line = reader.readLine()) != null)
				result.append(line.trim());
			return result.toString();
		} catch (IOException e) {
			e.printStackTrace();
		}
		return input;
	}

	/*
	 * static final String _xmlFmtStr = "<noteRoot>" + "<reportInfo>" +
	 * "<dataDate>%s</dataDate>" + "<reportId>%s</reportId>" +
	 * "<documentId>%s</documentId>" + "<documentNo>%s</documentNo>" +
	 * "<NoteType0800>%s</NoteType0800>" + "<fileName>%s</fileName>" +
	 * "</reportInfo>" + "<basicInfo>" + "<policyCode>%s</policyCode>" +
	 * "<policyHolderID>%s</policyHolderID>" +
	 * "<policyHolderName>%s</policyHolderName>" + "</basicInfo>" + "<agentInfo>" +
	 * "<salesChannel>%s</salesChannel>" + "<brbdCompany1>%s</brbdCompany1>" +
	 * "<brbdBranch1>%s</brbdBranch1>" + "<agentID1>%s</agentID1>" +
	 * "<agentName1>%s</agentName1>" + "<brbdCompany2>%s</brbdCompany2>" +
	 * "<brbdBranch2>%s</brbdBranch2>" + "<agentID2>%s</agentID2>" +
	 * "<agentName2>%s</agentName2>" + "</agentInfo>" + "<requesterInfo>" +
	 * "<salesChannel>%s</salesChannel>" + "<channelCode1>%s</channelCode1>" +
	 * "<channelCode2>%s</channelCode2>" + "<agentID>%s</agentID>" +
	 * "<agentName>%s</agentName>" + "</requesterInfo>" + "</noteRoot>%n";
	 * 
	 * boolean do_one_OLD (String hostCode, String spoolName, long jobNo, int
	 * subjobNo, Clob rptData) { boolean rc = false;
	 * 
	 * _lastErr = "";
	 * 
	 * try { byte[] baData = rptData.getSubString(1,
	 * (int)rptData.length()).getBytes("UTF-8"); Document dom = _db.parse(new
	 * ByteArrayInputStream(baData));
	 * 
	 * String srcPdf = String.format("%s\\%s\\%s\\%08x\\%06x.pdf", _pdfFolder,
	 * hostCode, spoolName, jobNo, subjobNo);
	 * 
	 * String fileName = String.format("%s-%s-%08x-%06x.pdf", hostCode, spoolName,
	 * jobNo, subjobNo); String dstPdf = String.format("%s\\%s", _fdwFolder,
	 * fileName);
	 * 
	 * Files.copy((new File(srcPdf)).toPath(), (new File(dstPdf)).toPath(),
	 * StandardCopyOption.REPLACE_EXISTING); System.out.format("%s->%s%n", srcPdf,
	 * dstPdf);
	 * 
	 * Node root = Xml.getNode(dom, "/WSLetterUnit"); Node basicInfo =
	 * Xml.getNode(dom, "/WSLetterUnit/basicInfo"); Node idxNode = Xml.getNode(dom,
	 * "/WSLetterUnit/index");
	 * 
	 * ++_seq; String xmlStr = String.format(_xmlFmtStr,
	 * toDateStr(getTagValue(idxNode, dom, "dataDate")), getTagValue(root, dom,
	 * "shortName"), getTagValue(root, dom, "documentId"), getTagValue(root, dom,
	 * "documentNo"), "0", fileName,
	 * 
	 * getTagValue(idxNode, dom, "policyCode"), getTagValue(basicInfo, dom,
	 * "policyHolderId"), getTagValue(basicInfo, dom, "policyHolderName"),
	 * 
	 * getTagValue(basicInfo, dom, "salesChannel"), getTagValue(basicInfo, dom,
	 * "brbdCompanyCode"), getTagValue(basicInfo, dom, "brbdBranchCode"),
	 * getTagValue(basicInfo, dom, "agentId"), getTagValue(basicInfo, dom,
	 * "agentName"), "", "", "", "",
	 * 
	 * getTagValue(basicInfo, dom, "salesChannel"), getTagValue(basicInfo, dom,
	 * "brbdCompanyCode"), getTagValue(basicInfo, dom, "brbdBranchCode"),
	 * getTagValue(basicInfo, dom, "agentId"), getTagValue(basicInfo, dom,
	 * "agentName") );
	 * 
	 * int cnt = _seq % _dataFileMax; if (cnt==1) _sbDataFile = new StringBuilder();
	 * _sbDataFile.append(xmlStr);
	 * 
	 * if (cnt==_dataFileMax) { String dataFileName =
	 * String.format("%s\\%s-%d-%d.dat", _fdwFolder, _batchId, _seqBatch, _seq /
	 * _dataFileMax); str2File(_sbDataFile.toString(), dataFileName); }
	 * 
	 * rc = true; } catch (Exception e) { e.printStackTrace(); _lastErr =
	 * e.toString(); }
	 * 
	 * System.out.println("do_one ret="+rc);
	 * 
	 * return rc; }
	 */
	static final String _ctlFmtStr = "batchID:%s%n" + "batchDate:%s%n" + "totalCount:%d%n" + "dataFileList%n" + "%s";

	boolean do_ctl() throws Exception {
		boolean rc = false;

		int cnt = _seq % _dataFileMax;
		if (cnt != _dataFileMax) {
			String dataFileName = String.format("%s\\%s-%d-%d.dat", _fdwFolder, _batchId, _seqBatch,
					1 + _seq / _dataFileMax);
			str2File(_sbDataFile.toString(), dataFileName);
		}

		int numDataFile = (_seq + _dataFileMax - 1) / _dataFileMax;

		StringBuilder sb = new StringBuilder();
		for (int i = 0; i < numDataFile; ++i) {
			String dataFileName = String.format("%s-%d-%d.dat%n", _batchId, _seqBatch, 1 + i);
			sb.append(dataFileName);
		}

		String str = String.format(_ctlFmtStr, String.format("%s-%d", _batchId, _seqBatch),
				(new java.text.SimpleDateFormat("yyyyMMdd-HHmmss")).format(new java.util.Date()), _seq, sb.toString());

		str2File(str, String.format("%s\\%s-%d.ctl", _fdwFolder, _batchId, _seqBatch));

		rc = true;

		return rc;
	}

	void handleException(Exception e) {
		if (e instanceof SQLException) {
			System.err.println("SQLState=" + ((SQLException) e).getSQLState());
			System.err.println("ErrorCode=" + ((SQLException) e).getErrorCode());
		}
		e.printStackTrace();
	}

	void updateEmailed(PreparedStatement stmt, String hostCode, String spoolName, long jobNo, int subjobNo,
			String emailed) throws Exception {
		try {
			stmt.setString(1, emailed);
			stmt.setString(2, hostCode);
			stmt.setString(3, spoolName);
			stmt.setLong(4, jobNo);
			stmt.setInt(5, subjobNo);
			stmt.execute();
		} catch (Exception e) {
			e.printStackTrace();
			_err_update_cnt++;
		}
	}

	void updateErrMsg(PreparedStatement stmt, String hostCode, String spoolName, long jobNo, int subjobNo,
			String errMsg) throws Exception {
		try {
			stmt.setString(1, errMsg.length() > 2000 ? errMsg.substring(0, 2000) : errMsg);
			stmt.setString(2, hostCode);
			stmt.setString(3, spoolName);
			stmt.setLong(4, jobNo);
			stmt.setInt(5, subjobNo);
			stmt.execute();
		} catch (Exception e) {
			e.printStackTrace();
			_err_update_cnt++;
		}
	}

	void str2File(String str, String fileName) throws Exception {
		File file = new File(fileName);
		FileOutputStream fos = new FileOutputStream(file);

		byte[] ba = str.getBytes("UTF-8");
		fos.write(ba);

		fos.close();
	}

	String getTagValue(Node parent, Document dom, String tag) {
		String v = null == parent ? null : Xml.getNodeText(parent, tag);
		if (Utils.isEmpty(v) && dom != null) {
			NodeList nl = dom.getElementsByTagName(tag);
			if (nl != null && nl.getLength() > 0)
				v = nl.item(0).getTextContent();
		}
		return v;
	}

	String toDateStr(String str) {
		if (str.length() >= 10) {
			return str.substring(0, 4) + str.substring(5, 7) + str.substring(8, 10);
		}
		return str;
	}

	String node2Str(Node node) throws Exception {
		if (node != null) {
			StringWriter sw = new StringWriter();

			Transformer t = TransformerFactory.newInstance().newTransformer();
			t.setOutputProperty(OutputKeys.OMIT_XML_DECLARATION, "yes");
			t.setOutputProperty(OutputKeys.INDENT, "no");
			t.transform(new DOMSource(node), new StreamResult(sw));
			return sw.toString();
		}
		return "";
	}

}
