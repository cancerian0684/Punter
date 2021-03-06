package org.shunya.punter.utils;

import java.io.DataInputStream;
import java.io.FileInputStream;
import java.io.InputStream;



class RhymingWords {
    public static void main(String args[]) {

	try {
	    DataInputStream words = new DataInputStream(new FileInputStream("words.txt"));
	    InputStream rhymedWords = StringUtils.reverse(StringUtils.sort(StringUtils.reverse(words)));

	    DataInputStream dis = new DataInputStream(rhymedWords);
	    String input;

            while ((input = dis.readLine()) != null) {
                System.out.println(input);
            }
	    dis.close();

	} catch (Exception e) {
	    System.out.println("RhymingWords: " + e);
	}
    }
}