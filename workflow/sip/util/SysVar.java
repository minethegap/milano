package sip.util;

import java.io.*;

public class SysVar {

	String fnSysVar;

	public SysVar () {
		fnSysVar = "c:\\sip\\cfg\\sysvar.txt";
	}

	public SysVar (String fn) {
		fnSysVar = fn;
	}    

	public String get (String sn, String def) { 
		try {
        	au.com.bytecode.opencsv.CSVReader reader = new au.com.bytecode.opencsv.CSVReader(new FileReader(fnSysVar),',', '"', '|');
    
	        String[] nextLine;
	      	while ((nextLine = reader.readNext()) != null) {
	      		if (sn.equalsIgnoreCase(nextLine[0]))
	      			return nextLine[1];
	        }
	    } catch (Exception e) {
            e.printStackTrace();
        }
           
       	return def;
    }

    public String get (String sn) { 
    	return get(sn,null);
    }

    public String getSql (String sqlName) throws Exception { 
    	String sqlStr = new String(java.nio.file.Files.readAllBytes(java.nio.file.Paths.get("c:\\sip\\cfg\\"+sqlName)));
    	return repSql(sqlStr);
    }

    public String repSql (String sqlStr) {
        sqlStr = sqlStr.replace("[BATCH_TBL]", get("BATCH_TBL"));
        sqlStr = sqlStr.replace("[DOC_TBL]", get("DOC_TBL"));
        sqlStr = sqlStr.replace("[POLICY_TBL]", get("POLICY_TBL"));
        sqlStr = sqlStr.replace("[CFG_TBL]", get("CFG_TBL"));
        return sqlStr;
    }

}

