package eu.bde.sc7pilot.imageaggregator.utils;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

public class Sentinel1ImagesHandler {
	
	private static final int BUFFER_SIZE = 65536;
	private String zipLocalFilepath;
	private String unzippedLocalFilePath;
	private String imgName;
	
	public Sentinel1ImagesHandler(String imgLocalFilepath) {
		// verification code that the imgLocalFilepath is a .zip file.
		zipLocalFilepath = imgLocalFilepath;
		unzippedLocalFilePath = unzip();
		File unzippedFile = new File(unzippedLocalFilePath);
		imgName = unzippedFile.getName().substring(0, unzippedFile.getName().length() - 5);
	}
	
/*	public String findTiff() {
		return "not implemented yet";
	}
	*/
	
	public File getUnzipFile() {
		File unzippedFile = new File(unzippedLocalFilePath);
		return unzippedFile;
	}
	
	public String findQuickLook() {
		String qlookPath = unzippedLocalFilePath + File.separator + "preview" + File.separator + "quick-look.png";
		System.out.println("SentinelImages1Handler msg:\tThe quick-look.png of img " + imgName + " is " + qlookPath);
		return qlookPath;
	}
	
	private String unzip() {
		double start = System.currentTimeMillis();
		File zipFile = new File(zipLocalFilepath);
		String destDirectory = zipFile.getParent();
		ZipInputStream zipIn = null;
		try {
			zipIn = new ZipInputStream(new FileInputStream(zipLocalFilepath));
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		try {
			ZipEntry entry;
			entry = zipIn.getNextEntry();
	        while (entry != null) {
	            String filePath = destDirectory + File.separator + entry.getName();
	            if (!entry.isDirectory()) {
	                extractFile(zipIn, filePath);
	            } else {
	                File dir = new File(filePath);
	                dir.mkdir();
	            }
	            zipIn.closeEntry();
	            entry = zipIn.getNextEntry();
	        }
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		double end = System.currentTimeMillis();
		double duration = end - start;
		System.out.println("SentinelImages1Handler msg:\tFile " + zipLocalFilepath + " unzipped in " + duration + " ms.");
		
		String unzippedFilePath = zipLocalFilepath.replace("zip", "SAFE");
		return unzippedFilePath;
	}

    private void extractFile(ZipInputStream zipIn, String filePath) throws IOException {
        BufferedOutputStream bos = new BufferedOutputStream(new FileOutputStream(filePath));
        byte[] bytesIn = new byte[BUFFER_SIZE];
        int read = 0;
        while ((read = zipIn.read(bytesIn)) != -1) {
            bos.write(bytesIn, 0, read);
        }
        bos.close();
    }

}
