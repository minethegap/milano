
package common;

import com.itextpdf.text.*;
import com.itextpdf.text.pdf.*;
import java.util.HashMap;
import java.util.List;
import java.util.ArrayList;
import java.io.*;

public class ExtractPDF {

	PdfReader reader;
	
	public ExtractPDF (PdfReader reader) {
		this.reader = reader;
	}

	public boolean extract (List<PageRange> prs, String dstPdf) {
		boolean b = false;
		try {
			Document document = new Document();
	        PdfCopy copy = new PdfCopy(document, new FileOutputStream(dstPdf));
	        document.open();
			for (PageRange pr: prs) {
				if (!PageRange.isEmpty(pr)) {
					for (int i=pr.sta; i<=pr.end; ++i)
						copy.addPage(copy.getImportedPage(reader, i));
				}
			}
			document.close();
			b = true;
		} catch (Exception e) {
			e.printStackTrace();
		}
		return b;	
	}

	public static void main (String[] args) throws Exception {
		String srcPdf = args[0];
		String dstPdf = args[1];
		PdfReader pdfReader = new PdfReader(srcPdf);
		BookmarkPageRange bpr = new BookmarkPageRange(pdfReader);
		ExtractPDF ep = new ExtractPDF(pdfReader);
		List<PageRange> prs = new ArrayList<PageRange>();
		int idx = 2; 
		while (idx < args.length) {
			String bmStr = args[idx++];
			PageRange pr = bpr.find(bmStr);
			if (!PageRange.isEmpty(pr)) {
				prs.add(pr);
			}
			else {
				System.out.format("%s=>empty%n", bmStr);
			}
		}
		if (prs.size() > 0)
			ep.extract(prs, dstPdf);
	}

}
