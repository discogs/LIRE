package net.semanticmetadata.lire.imageanalysis;

import net.semanticmetadata.lire.DocumentBuilder;

import net.semanticmetadata.lire.utils.SerializationUtils;
import net.semanticmetadata.lire.utils.MetricsUtils;
import org.apache.commons.math3.util.MathArrays;
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
import java.util.Iterator;

/**
 * Created by seanwolfe on 1/6/16.
 */
public class ORBFeature implements LireFeature {
    // ORBFeature Default values:
    private static float scaleFactor = 1.2f; // Coefficient by which we divide the dimensions from one scale pyramid level to the next
    private static int nLevels      = 8;   // The number of levels in the scale pyramid
    private static int firstLevel   = 0;   // The level at which the image is given
    private static int edgeThreshold = 31;  // How far from the boundary the points should be.
    private static int patchSize     = 31;  // You can not change this, it is allways 31
    private static int WTA_K = 2;           // How many random points are used to produce each cell of the descriptor (2, 3, 4 ...)
    private static int scoreType = 0;           // 0 for HARRIS_SCORE / 1 for FAST_SCORE
    private static int nFeatures = 500;        // not sure if 500 is default

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

    private static String yamlPath;

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

    static {
        System.loadLibrary( Core.NATIVE_LIBRARY_NAME );

        try {
            File temp = File.createTempFile("tempFile", ".tmp");
            String settings = "%YAML:1.0\nscaleFactor: " + getScaleFactor() + "\nnLevels: " + getnLevels() + "\nfirstLevel: " + getFirstLevel() + " \nedgeThreshold: " + getEdgeThreshold() + "\npatchSize: " + getPatchSize() + "\nWTA_K: " + getWTA_K() + "\nscoreType: " + getScoreType() + "\nnFeatures: " + getnFeatures();
            FileWriter writer = new FileWriter(temp, false);
            writer.write(settings);
            writer.close();
            yamlPath = temp.getPath();
            temp.deleteOnExit();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void init() {

        detector = FeatureDetector.create(FeatureDetector.ORB);
        extractor = DescriptorExtractor.create(DescriptorExtractor.ORB);
        extractor.read(yamlPath);
        detector.read(yamlPath);
    }

    @Override
    public String getStringRepresentation() {
        return null;
    }

    @Override
    public byte[] getByteArrayRepresentation() {
        if(_features == null) {
            return SerializationUtils.toByteArray(feature);
        } else {
            byte[] tmp, featureBytes = new byte[_features.size() * 256];
            for (int index = 0; index < _features.size(); index++) {
                ORBFeature feature = _features.get(index);
                tmp = feature.getByteArrayRepresentation();
                System.arraycopy(tmp, 0, featureBytes, index * 256, 256);
            }
            return featureBytes;
        }
    }

    @Override
    public String getFeatureName() {
        return "ORB";
    }

    @Override
    public double[] getDoubleHistogram() {
        if( _features == null) {
            return feature;
        } else {
            double[] allFeatures = new double[_features.size() * 32];
            for(int index=0; index < _features.size(); index++) {
               System.arraycopy(_features.get(index).getDoubleHistogram(), 0, allFeatures, index * 32, 32);
            }
            return allFeatures;
        }
    }

    public LinkedList<ORBFeature> getFeatures() {
        return _features;
    }

    @Override
    public float getDistance(LireFeature otherFeature) {
        if (!(otherFeature instanceof ORBFeature)) return -1;
        if(_features == null) {
            return (float) MetricsUtils.distL2(this.feature, ((ORBFeature) otherFeature).feature);
        } else {
            float sum = 0.0f;

            LinkedList<ORBFeature> otherFeatures = ((ORBFeature) otherFeature).getFeatures();
            for(int index=0; index < _features.size(); index++) {
                sum += _features.get(index).getDistance((otherFeatures.get(index)));
            }
            return (float)Math.sqrt(sum);
        }
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
        if(featureData.length > 256) {
            byte[] tmp;
            for(int index=0; index < featureData.length; index += 256) {
                tmp = new byte[256];
                System.arraycopy(featureData, index, tmp, 0, 256);
                ORBFeature orbf = new ORBFeature();
                orbf.setByteArrayRepresentation(tmp);
                _features.add(orbf);
            }
        } else {
            feature = SerializationUtils.toDoubleArray(featureData);
        }
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
    public static float getScaleFactor() {
        return scaleFactor;
    }

    public static void setScaleFactor(float value) {
        scaleFactor = value;
    }

    public static int getnLevels() {
        return nLevels;
    }

    public static void setnLevels(int value) {
        nLevels = value;
    }

    public static int getFirstLevel() {
        return firstLevel;
    }

    public static void setFirstLevel(int value) {
        firstLevel = value;
    }

    public static int getEdgeThreshold() {
        return edgeThreshold;
    }

    public static void setEdgeThreshold(int value) {
        edgeThreshold = value;
    }

    public static int getPatchSize() {
        return patchSize;
    }

    public static void setPatchSize(int value) {
        patchSize = value;
    }

    public static int getWTA_K() {
        return WTA_K;
    }

    public static void setWTA_K(int value) {
        WTA_K = value;
    }

    public static int getScoreType() {
        return scoreType;
    }

    public static void setScoreType(int value) {
        scoreType = value;
    }

    public static int getnFeatures() {
        return nFeatures;
    }

    public static void setnFeatures(int value) {
        nFeatures = value;
    }
}
