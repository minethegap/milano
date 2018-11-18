
package common;

public class PageRange {
	
	public int sta = 0, end = 0;

	public PageRange (int s, int e) { sta = s; end = e; }

	public boolean isEmpty () {
		return sta==0 || end==0 || end<sta;
	}


	public static boolean isEmpty (PageRange pr) {
		if (pr != null) return pr.isEmpty();
		return true;
	}

}
