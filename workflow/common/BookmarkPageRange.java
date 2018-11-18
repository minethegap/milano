
package common;

import com.itextpdf.text.*;
import com.itextpdf.text.pdf.*;
import java.util.HashMap;
import java.util.List;

public class BookmarkPageRange {

	PdfReader reader;
	HashMap<String,Object> bookmarksRoot = null;

	public BookmarkPageRange (PdfReader reader) {
		this.reader = reader;
		List<HashMap<String,Object>> bookmarks = SimpleBookmark.getBookmark(reader);
		if (bookmarks != null) {
			System.out.format("bookmarkList size=%d%n", bookmarks.size());
			if (bookmarks.size() > 0) bookmarksRoot = (HashMap<String, Object>)bookmarks.get(0);
		}
		else {
			System.out.format("bookmarkList null%n");
		}
	}

	public PageRange find (String bmStr) {
		PageRange pr = null;
		if (bookmarksRoot != null && !Utils.isEmpty(bmStr)) {
			String [] arrBmStr = bmStr.split(",");
			pr = find(arrBmStr, 0, (List<HashMap<String,Object>>)bookmarksRoot.get("Kids"));
			if (pr != null && pr.sta > 0 && pr.end==0)
				pr.end = reader.getNumberOfPages();
			if (pr != null) System.out.format("%s=>%d,%d%n", bmStr, pr.sta, pr.end);
		}
		return pr;	
	}

	PageRange find (String[] arrBmStr, int depth, List<HashMap<String,Object>> bms) {
		PageRange pr = null;
		for (int i=0; i<bms.size() && pr==null; ++i) {
			HashMap<String, Object> bm = bms.get(i);
			if (arrBmStr[depth].equalsIgnoreCase((String)bm.get("Title"))) {
				if (arrBmStr.length == 1+depth) { //found
					pr = new PageRange(getPage(bm), 0);
				}
				else {
					pr = find(arrBmStr, 1+depth, (List<HashMap<String,Object>>)bm.get("Kids"));
				}
				if (pr != null && pr.sta > 0 && pr.end==0 && (i+1)<bms.size()) {
					bm = bms.get(1+i);
					pr.end = getPage(bm);
					if (pr.end > 0) --pr.end;
				}
			}
		}
		return pr;
	}

	int getPage (HashMap<String, Object> bm) {
		int pg = 0;
		String pgStr = (String)bm.get("Page");
		int idx = pgStr.indexOf(' ');
		if (idx >= 0) pgStr = pgStr.substring(0,idx);
		try {
			pg = Integer.parseInt(pgStr);
		} catch (Exception e) {
		}
		return pg;
	}


	public static void main (String[] args) throws Exception {
		String srcPdf = args[0];
		String bmStr = args[1];
		(new BookmarkPageRange(new PdfReader(srcPdf))).find(bmStr);
	}

}
