package com.sapient.punter.test;

import java.io.File;

import neoe.ne.EditPanel;
import neoe.ne.U;

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