package met.semanticmetadata.lire.test;

import static org.junit.Assert.*;

import org.junit.Test;
import org.opencv.core.Core;
import org.opencv.core.CvType;
import org.opencv.core.Mat;

public class TestOpenCV {

	@Test
	public void test() {
		//System.setProperty("java.library.path", "/lire/src/test/resources/lib/opencv/x64/");
		System.out.println("lib: " + System.getProperty("java.library.path"));
		System.loadLibrary(Core.NATIVE_LIBRARY_NAME);
		Mat mat = Mat.eye(3, 3, CvType.CV_8UC1);
		System.out.println("mat = " + mat.dump());
	}

}
