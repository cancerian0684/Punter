package org.shunya.punter.test;

import neoe.ne.EditPanel;

import java.io.File;

public class Main {
	public static void main(String[] args) throws Exception {
		if (args.length > 0) {
			File f = new File(args[0]);
//			U.openFile(f);
		} else {
			EditPanel editor = new EditPanel("neoeedit");
//			editor.
//			editor.page.ptSelection.selectAll();
			editor.openWindow();
			editor.setLocation(100, 100);
		}
	}
}