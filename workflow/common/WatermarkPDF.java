
package common;

import java.util.*;
import java.io.*;
import com.itextpdf.text.*;
import com.itextpdf.text.pdf.*;


public class WatermarkPDF
{
	java.util.Hashtable<String,com.itextpdf.text.Image> _htImg = new java.util.Hashtable<String,com.itextpdf.text.Image>();

	public com.itextpdf.text.Image loadImage (String fn) throws Exception {
		com.itextpdf.text.Image img = _htImg.get(fn);
		if (null == img) {
			img = com.itextpdf.text.Image.getInstance(fn);
			if (null != img) {
				img.scalePercent(100 * 72f / img.getDpiX(), 100 * 72f / img.getDpiY());
				img.setAbsolutePosition(0, 0);
				if (img.isMaskCandidate()) 
                    img.makeMask();
				_htImg.put(fn,img);
			}
		}
		return img;
	}

	public boolean start (String src, String dst, java.util.List<String> wmFrontList, java.util.List<String> wmBackList) {
		boolean rc = false;

		try {
			PdfReader reader = new PdfReader(src);
        	int nPages = reader.getNumberOfPages();
        	PdfStamper stamper = new PdfStamper(reader, new FileOutputStream(dst));
      
        	PdfContentByte cb;
        	for (int i = 1; i <= nPages; i++) {
	            cb = stamper.getUnderContent(i);
	            java.util.List<String> wmList = (i&1)==1? wmFrontList : wmBackList;
	            for (String fn : wmList) {
                	cb.addImage(loadImage(fn));
            	}
        	}
        	stamper.close();
        	reader.close();
			
			rc = true;
		}
		catch (Exception e) {
			e.printStackTrace();
		}

		return rc;
	}

	public static void main (String[] args) {
		if (args.length >= 3) {
			java.util.List<String> wmFrontList = new java.util.ArrayList<String> ();
			java.util.List<String> wmBackList = new java.util.ArrayList<String> ();
			for (int i=2; i<args.length; ++i) {
				if ((i&1)==0)
					wmFrontList.add(args[i]);
				else
					wmBackList.add(args[i]);
			}
			boolean ok = (new WatermarkPDF()).start(args[0], args[1], wmFrontList, wmBackList);
		}
	}

}
