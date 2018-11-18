package sip.util;

public class DbConnStr {

	public native String getConnStr (String name);
	public native String getJdbcDriver (String name);


	static boolean loadDll (String dll)
	{
		boolean rc = false;
		try {
			System.out.println("load "+dll);
			//System.load(dll);
			System.loadLibrary(dll);
			rc = true;
		} catch (Throwable e) {
			System.out.println(e);
			//e.printStackTrace();
		}
		return rc;
	}

	static {
		try {
			//System.out.println("java.library.path="+System.getProperty("java.library.path"));
			if (loadDll("connstr64")) {
				//System.out.println("use 32-bit dll");
			}
			else if (loadDll("connstr")) {
				//System.out.println("use 64-bit dll");
			}
			else {

			}
		} catch (Throwable e) {
			//e.printStackTrace();
		}
	}
}
