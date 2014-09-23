package org.js.galleryreview.model.imgaccess;

import static org.junit.Assert.*;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.junit.Test;

public class ImageLocatorTest {

	private static final String TEST_DIR = "tstFiles";
	
	@Test
	public void testReadFiles() throws IOException {
		List<String> fileNames = createTestFiles(TEST_DIR, randVal(10, 30));
		ImageLocator locator = new ImageLocator(new File(TEST_DIR).getAbsolutePath());
		List<PhysicalFile> tree = locator.readFiles();
		assertFalse(fileNames.isEmpty());
		System.out.println("Created files: " + fileNames.size());
		List<PhysicalFile> pFiles = getFlatFiles(tree, false);
		assertEquals(fileNames.size(), pFiles.size());
		Iterator<String> iter = fileNames.iterator();
		while (iter.hasNext()){
			String aPath = iter.next();
			System.out.println("Search: " + aPath);
			for (PhysicalFile aPF: pFiles){
				String pfPath = aPF.getFile().getAbsolutePath();
//				System.out.println(pfPath);
				if (!aPF.isDirectory() && pfPath.equals(aPath)){
					iter.remove();
					break;
				}
			}
		}
		System.out.println("Not found files: " + fileNames.size());
		assertTrue(fileNames.isEmpty());
	}

	private int notificationCount=0;
	
	@Test
	public void testNotification() throws IOException{
		notificationCount=0;
		int delta = 37;
		int fileCount = randVal(100, 1000);
		List<String> fileNames = createTestFiles(TEST_DIR, fileCount);
		ImageLocator locator = new ImageLocator(new File(TEST_DIR).getAbsolutePath());
		final List<PhysicalFile> allNotified=new ArrayList<PhysicalFile>();
		locator.addFilesParsedListener(delta, (List<PhysicalFile> newFiles) -> {
			for(PhysicalFile pf: newFiles){
				System.out.println(pf.getFile().getPath());
			}
			allNotified.addAll(newFiles);
			notificationCount++;
		});
		
		List<PhysicalFile> completeList = getFlatFiles(locator.readFiles(), true);
		
		// the physical file list contains files and directories, the fileNames list contains only files, therefore it will be shorter
		double minNotif = Math.ceil(((double)fileNames.size()) / ((double)delta));
		assertTrue(notificationCount >= ((int)minNotif));
		
		assertEquals(completeList.size(), allNotified.size());
		assertEquals(completeList.size(), locator.getFilesRead());
		for (PhysicalFile aPF: completeList){
			assertTrue(allNotified.contains(aPF));
		}
		
		
	}

	private List<PhysicalFile> getFlatFiles(List<PhysicalFile> tree, boolean includeDirs) {
		List<PhysicalFile> files = new ArrayList<PhysicalFile>();
		if (includeDirs){
			for (PhysicalFile pf : files) {
				files.add(pf);
			}
		}
		readFilesRec(files, tree, includeDirs);
		return files;
	}

	private void readFilesRec(List<PhysicalFile> allFiles,
			List<PhysicalFile> subTree, boolean includeDirs) {
		for (PhysicalFile aPF: subTree){
			if (!aPF.isDirectory()){
				allFiles.add(aPF);
			}else{
				if (includeDirs){
					allFiles.add(aPF);
				}
				readFilesRec(allFiles, aPF.getChildren(), includeDirs);
			}
		}
	}

	private List<String> createTestFiles(String baseDir, int fileCount) throws IOException {
		clearOldDir(baseDir);
		List<String> list = new ArrayList<String>();
		for (int i=0; i < fileCount ; i++){
			int depth = randVal(1,5);
			String dirName = baseDir;
			for (int j=0; j < depth; j ++){
				String subDirName="d" + randVal(0,100);
				dirName += File.separator + subDirName;
			}
			File dirFile = new File(dirName);
			dirFile.mkdirs();
			String tmpName = "f" + randVal(0,100)+".png";
			File tmpFile = new File(dirFile, tmpName);
			try(PrintWriter pw = new PrintWriter(tmpFile)){
				pw.println("Hi");
			}
			list.add(tmpFile.getAbsolutePath());
		}
		return list;
	}

	private int randVal(int min, int max) {
		int fileCount = min+(int)(((double)max)*Math.random());
		return fileCount;
	}

	private void clearOldDir(String baseDir) throws IOException {
		Path directory = Paths.get(baseDir);
		if (directory.toFile().exists()) {
			Files.walkFileTree(directory, new SimpleFileVisitor<Path>() {
				@Override
				public FileVisitResult visitFile(Path file,
						BasicFileAttributes attrs) throws IOException {
					Files.delete(file);
					return FileVisitResult.CONTINUE;
				}

				@Override
				public FileVisitResult postVisitDirectory(Path dir,
						IOException exc) throws IOException {
					Files.delete(dir);
					return FileVisitResult.CONTINUE;
				}

			});
		}

	}

}
