
package common;

public class StringIntPair {
	    private String _s;
	    private int _i;

	    public StringIntPair(String s, int i) {
	        _s = s;
	        _i = i;
	    }

	    public String getString() {
	        return _s;
	    }

	    public int getInteger() {
	        return _i;
	    }

	    public String getIntStr () {
	    	return Integer.toString(_i);
	    }
	}