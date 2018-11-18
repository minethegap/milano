
package common;

import java.util.*;
import java.io.*;
import com.itextpdf.text.*;
import com.itextpdf.text.pdf.*;


public class MergePDF
{
	public boolean merge (java.util.List<String> srcList, String dst) {
		boolean rc = false;

		try {
			System.out.format("merge to %s%nfrom ", dst);
			Document document = new Document();
			PdfCopy copy = new PdfSmartCopy(document, new FileOutputStream(dst));
			document.open();

			for (String src:srcList) {
				System.out.format("%s%n", src);
				PdfReader reader = new PdfReader(src);
				copy.addDocument(reader);
	    		reader.close();
			}
			document.close();
			rc = true;
		}
		catch (Exception e) {
			e.printStackTrace();
		}

		return rc;
	}

	public static void main (String[] args) {
		if (args.length >=2) {
			java.util.List<String> srcList = new java.util.ArrayList<String> ();
			for (int i=1; i<args.length; ++i)
				srcList.add(args[i]);
			boolean ok = (new MergePDF()).merge(srcList, args[0]);
		}
	}

}
