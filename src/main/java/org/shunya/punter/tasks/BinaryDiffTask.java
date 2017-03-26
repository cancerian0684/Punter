package org.shunya.punter.tasks;

import org.shunya.punter.annotations.InputParam;
import org.shunya.punter.annotations.PunterTask;
import org.shunya.punter.jbdiff.JBDiff;

import java.io.File;
import java.io.IOException;

@PunterTask(author="munishc",name="BinaryDiffTask",description="Create Patch for Binary diff of two files.",documentation= "src/main/resources/docs/TextSamplerDemoHelp.html")
public class BinaryDiffTask extends Tasks {
	@InputParam(required = true,description="Old file")
	private String oldFilePath;

	@InputParam(required = true,description="New file")
	private String newFilePath;

	@InputParam(required = true,description="Patch file")
	private String patchFilePath;

	@Override
	public boolean run() {
        File oldFile = new File(oldFilePath);
        File newFile = new File(newFilePath);
        File diffFile = new File(patchFilePath);
		LOGGER.get().info("oldFile {}, newFile {}, PatchFile {}", oldFilePath, newFilePath, patchFilePath);
        try {
            getTaskHistory().setActivity("Creating JbDiff Patch");
            JBDiff.bsdiff(oldFile, newFile, diffFile);
        } catch (IOException e) {
            LOGGER.get().error("IOException occurred", e);
            return false;
        }
        return true;
	}
}