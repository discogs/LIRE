package net.semanticmetadata.lire.imageanalysis;

import net.semanticmetadata.lire.DocumentBuilder;

import net.semanticmetadata.lire.imageanalysis.opencvfeatures.CvSurfFeature;
import org.opencv.core.Core;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.core.MatOfKeyPoint;
import org.opencv.features2d.FeatureDetector;
import org.opencv.features2d.DescriptorExtractor;
import org.opencv.features2d.KeyPoint;
import org.opencv.imgproc.Imgproc;

import java.awt.image.BufferedImage;
import java.awt.image.DataBufferByte;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.LinkedList;
import java.util.List;

/**
 * Created by seanwolfe on 1/6/16.
 */
public class ORBFeature implements LireFeature {
    // ORBFeature Default values:
    private float scaleFactor = 1.2f; // Coefficient by which we divide the dimensions from one scale pyramid level to the next
    private int nLevels      = 8;   // The number of levels in the scale pyramid
    private int firstLevel   = 0;   // The level at which the image is given
    private int edgeThreshold = 31;  // How far from the boundary the points should be.
    private int patchSize     = 31;  // You can not change this, it is allways 31

    private int WTA_K = 2;           // How many random points are used to produce each cell of the descriptor (2, 3, 4 ...)
    private int scoreType = 0;           // 0 for HARRIS_SCORE / 1 for FAST_SCORE
    private int nFeatures = 500;        // not sure if 500 is default

    private FeatureDetector detector;
    private DescriptorExtractor extractor;

    private LinkedList<ORBFeature> _features;
    private MatOfKeyPoint _keyPoints;

    private float angle;
    private int class_id;
    private int octave;
    private double[] point;
    private float response;
    private float size;
    private double[] feature;


    public ORBFeature() {
        init();
    }
    public ORBFeature(float ang, int cl, int o, double[] pt, float res, float s, double[] feat) {
        angle = ang;
        class_id = cl;
        octave = o;
        point = pt;
        response = res;
        size = s;
        feature = feat;
    }

    private void init() {
        System.loadLibrary( Core.NATIVE_LIBRARY_NAME );

        detector = FeatureDetector.create(FeatureDetector.ORB);
        extractor = DescriptorExtractor.create(DescriptorExtractor.ORB);
        try {
            File temp = File.createTempFile("tempFile", ".tmp");
            String settings = "%YAML:1.0\nscaleFactor: " + getScaleFactor() + "\nnLevels: " + getnLevels() + "\nfirstLevel: " + getFirstLevel() + " \nedgeThreshold: " + getEdgeThreshold() + "\npatchSize: " + getPatchSize() + "\nWTA_K: " + getWTA_K() + "\nscoreType: " + getScoreType() + "\nnFeatures: " + getnFeatures();
            FileWriter writer = new FileWriter(temp, false);
            writer.write(settings);
            writer.close();
            extractor.read(temp.getPath());
            detector.read(temp.getPath());
            temp.deleteOnExit();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public String getStringRepresentation() {
        return null;
    }

    @Override
    public byte[] getByteArrayRepresentation() {
        return new byte[0];
    }

    @Override
    public String getFeatureName() {
        return "ORB";
    }

    @Override
    public double[] getDoubleHistogram() {
        return new double[0];
    }

    @Override
    public float getDistance(LireFeature feature) {
        return 0;
    }

    @Override
    public String getFieldName() {
        return DocumentBuilder.FIELD_NAME_CVORB;
    }

    @Override
    public void extract(BufferedImage image) {

        _keyPoints = new MatOfKeyPoint();
        Mat descriptors = new Mat();
        List<KeyPoint> myKeys;
        byte[] data = ((DataBufferByte) image.getRaster().getDataBuffer()).getData();
        Mat matRGB = new Mat(image.getHeight(), image.getWidth(), CvType.CV_8UC3);
        matRGB.put(0, 0, data);
        Mat matGray = new Mat(image.getHeight(),image.getWidth(),CvType.CV_8UC1);
        Imgproc.cvtColor(matRGB, matGray, Imgproc.COLOR_BGR2GRAY);              //TODO: RGB or BGR?
        byte[] dataGray = new byte[matGray.rows()*matGray.cols()*(int)(matGray.elemSize())];
        matGray.get(0, 0, dataGray);

        detector.detect(matGray, _keyPoints);
        extractor.compute(matGray, _keyPoints, descriptors);
        myKeys = _keyPoints.toList();

        _features = new LinkedList<ORBFeature>();
        KeyPoint key;
        ORBFeature feat;
        double[] desc;
        int cols, rows = myKeys.size();
        for (int i=0; i<rows; i++) {
            cols = (descriptors.row(i)).cols();
            desc = new double[cols];
            key = myKeys.get(i);
            for(int j=0; j < cols; j++)
            {
                desc[j]=descriptors.get(i, j)[0];
            }
            feat = new ORBFeature(key.angle, key.class_id, key.octave, new double[] {key.pt.x, key.pt.y}, key.response, key.size, desc);
            _features.add(feat);
        }
    }

    @Override
    public void setByteArrayRepresentation(byte[] featureData) {

    }

    @Override
    public void setByteArrayRepresentation(byte[] featureData, int offset, int length) {

    }

    @Override
    public void setStringRepresentation(String featureVector) {

    }

    /*
    ORBFeature parameter field accessors
     */
    public float getScaleFactor() {
        return scaleFactor;
    }

    public void setScaleFactor(float scaleFactor) {
        this.scaleFactor = scaleFactor;
    }

    public int getnLevels() {
        return nLevels;
    }

    public void setnLevels(int nLevels) {
        this.nLevels = nLevels;
    }

    public int getFirstLevel() {
        return firstLevel;
    }

    public void setFirstLevel(int firstLevel) {
        this.firstLevel = firstLevel;
    }

    public int getEdgeThreshold() {
        return edgeThreshold;
    }

    public void setEdgeThreshold(int edgeThreshold) {
        this.edgeThreshold = edgeThreshold;
    }

    public int getPatchSize() {
        return patchSize;
    }

    public void setPatchSize(int patchSize) {
        this.patchSize = patchSize;
    }

    public int getWTA_K() {
        return WTA_K;
    }

    public void setWTA_K(int WTA_K) {
        this.WTA_K = WTA_K;
    }

    public int getScoreType() {
        return scoreType;
    }

    public void setScoreType(int scoreType) {
        this.scoreType = scoreType;
    }

    public int getnFeatures() {
        return nFeatures;
    }

    public void setnFeatures(int nFeatures) {
        this.nFeatures = nFeatures;
    }
}
