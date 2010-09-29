package com.sapient.punter.utils;
/*
 * Copyright (c) 1994 Sun Microsystems, Inc. All Rights Reserved.
 *
 * Permission to use, copy, modify, and distribute this software
 * and its documentation for NON-COMMERCIAL purposes and without
 * fee is hereby granted provided that this copyright notice
 * appears in all copies. Please refer to the file "copyright.html"
 * for further important copyright and licensing information.
 *
 * SUN MAKES NO REPRESENTATIONS OR WARRANTIES ABOUT THE SUITABILITY OF
 * THE SOFTWARE, EITHER EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED
 * TO THE IMPLIED WARRANTIES OF MERCHANTABILITY, FITNESS FOR A
 * PARTICULAR PURPOSE, OR NON-INFRINGEMENT. SUN SHALL NOT BE LIABLE FOR
 * ANY DAMAGES SUFFERED BY LICENSEE AS A RESULT OF USING, MODIFYING OR
 * DISTRIBUTING THIS SOFTWARE OR ITS DERIVATIVES.
 */
import java.io.*;

class StringUtils {
    public static InputStream reverse(InputStream source) {
        PipedOutputStream ps = null;
        PipedInputStream is = null;

	try {
	    DataInputStream dis = new DataInputStream(source);
	    String input;

            ps = new PipedOutputStream();
            is = new PipedInputStream(ps);
            PrintStream os = new PrintStream(ps);

	    while ((input = dis.readLine()) != null) {
	        os.println(reverseString(input));
	    }
	    os.close();
	} catch (Exception e) {
	    System.out.println("StringUtils reverse: " + e);
	}
	return is;
    }

    private static String reverseString(String source) {
	int i, len = source.length();
	StringBuffer dest = new StringBuffer(len);

	for (i = (len - 1); i >= 0; i--) {
	    dest.append(source.charAt(i));
	}
	return dest.toString();
    }

    public static InputStream sort(InputStream source) {

	int MAXWORDS = 50;

        PipedOutputStream ps = null;
        PipedInputStream is = null;

	try {
	    DataInputStream dis = new DataInputStream(source);
	    String input;

            ps = new PipedOutputStream();
            is = new PipedInputStream(ps);
            PrintStream os = new PrintStream(ps);

	    String listOfWords[] = new String[MAXWORDS];
	    int numwords = 0, i = 0;

	    while ((listOfWords[numwords] = dis.readLine()) != null) {
		numwords++;
	    }
	    quicksort(listOfWords, 0, numwords-1);
	    for (i = 0; i < numwords; i++) {
		os.println(listOfWords[i]);
	    }
	    os.close();
	} catch (Exception e) {
	    System.out.println("StringUtils sort: " + e);
	}
	return is;
    }

    private static void quicksort(String a[], int lo0, int hi0) {
	int lo = lo0;
	int hi = hi0;
	if (lo >= hi) {
	    return;
	}
	String mid = a[(lo + hi) / 2];
	while (lo < hi) {
	    while (lo<hi && a[lo].compareTo(mid) < 0) {
		lo++;
	    }
	    while (lo<hi && a[hi].compareTo(mid) > 0) {
		hi--;
	    }
	    if (lo < hi) {
		String T = a[lo];
		a[lo] = a[hi];
		a[hi] = T;
	    }
	}
	if (hi < lo) {
	    int T = hi;
	    hi = lo;
	    lo = T;
	}
	quicksort(a, lo0, lo);
	quicksort(a, lo == lo0 ? lo+1 : lo, hi0);
    }
}