package com.anz.rtl.transactions.request;

import java.util.HashSet;
import java.util.Set;

public enum ProductType {
	
	    DDA, 
	    CDA,
		ILS,
		RSV,
		FM;

	    private final static Set<String> values = new HashSet<String>(ProductType.values().length);

	    static{
	        for(ProductType f: ProductType.values())
	            values.add(f.name());
	    }

	    public static boolean contains( String value ){
	        return values.contains(value);
	    }

	}

