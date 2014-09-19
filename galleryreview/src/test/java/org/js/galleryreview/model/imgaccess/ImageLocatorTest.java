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
		List<String> fileNames = getTestFileNames(TEST_DIR);
		ImageLocator locator = new ImageLocator(new File(TEST_DIR).getAbsolutePath());
		List<PhysicalFile> tree = locator.readFiles();
		assertFalse(fileNames.isEmpty());
		System.out.println("Created files: " + fileNames.size());
		List<PhysicalFile> pFiles = getFlatFiles(tree);
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

	private List<PhysicalFile> getFlatFiles(List<PhysicalFile> tree) {
		List<PhysicalFile> files = new ArrayList<PhysicalFile>();
		readFilesRec(files, tree);
		return files;
	}

	private void readFilesRec(List<PhysicalFile> allFiles,
			List<PhysicalFile> subTree) {
		for (PhysicalFile aPF: subTree){
			if (!aPF.isDirectory()){
				allFiles.add(aPF);
			}else{
				readFilesRec(allFiles, aPF.getChildren());
			}
		}
	}

	private List<String> getTestFileNames(String baseDir) throws IOException {
		clearOldDir(baseDir);
		int fileCount = 10+(int)(30d*Math.random());
		List<String> list = new ArrayList<String>();
		for (int i=0; i < fileCount ; i++){
			int depth = 1 + (int)(5d * Math.random());
			String dirName = baseDir;
			for (int j=0; j < depth; j ++){
				String subDirName="d" + (int)(100d * Math.random());
				dirName += File.separator + subDirName;
			}
			File dirFile = new File(dirName);
			dirFile.mkdirs();
			String tmpName = "f" + (int)(100d * Math.random())+".png";
			File tmpFile = new File(dirFile, tmpName);
			try(PrintWriter pw = new PrintWriter(tmpFile)){
				pw.println("Hi");
			}
			list.add(tmpFile.getAbsolutePath());
		}
		return list;
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
