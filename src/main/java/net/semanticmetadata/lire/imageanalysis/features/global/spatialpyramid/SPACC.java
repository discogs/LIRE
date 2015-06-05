/*
 * This file is part of the LIRE project: http://www.semanticmetadata.net/lire
 * LIRE is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or
 * (at your option) any later version.
 *
 * LIRE is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with LIRE; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 *
 * We kindly ask you to refer the any or one of the following publications in
 * any publication mentioning or employing Lire:
 *
 * Lux Mathias, Savvas A. Chatzichristofis. Lire: Lucene Image Retrieval –
 * An Extensible Java CBIR Library. In proceedings of the 16th ACM International
 * Conference on Multimedia, pp. 1085-1088, Vancouver, Canada, 2008
 * URL: http://doi.acm.org/10.1145/1459359.1459577
 *
 * Lux Mathias. Content Based Image Retrieval with LIRE. In proceedings of the
 * 19th ACM International Conference on Multimedia, pp. 735-738, Scottsdale,
 * Arizona, USA, 2011
 * URL: http://dl.acm.org/citation.cfm?id=2072432
 *
 * Mathias Lux, Oge Marques. Visual Information Retrieval using Java and LIRE
 * Morgan & Claypool, 2013
 * URL: http://www.morganclaypool.com/doi/abs/10.2200/S00468ED1V01Y201301ICR025
 *
 * Copyright statement:
 * ====================
 * (c) 2002-2013 by Mathias Lux (mathias@juggle.at)
 *  http://www.semanticmetadata.net/lire, http://www.lire-project.net
 *
 * Updated: 11.07.13 10:41
 */

package net.semanticmetadata.lire.imageanalysis.features.global.spatialpyramid;

import net.semanticmetadata.lire.imageanalysis.features.global.AutoColorCorrelogram;
import net.semanticmetadata.lire.imageanalysis.features.GlobalFeature;
import net.semanticmetadata.lire.imageanalysis.features.LireFeature;
import net.semanticmetadata.lire.utils.MetricsUtils;

import java.awt.image.BufferedImage;

/**
 * Created with IntelliJ IDEA.
 * User: mlux
 * Date: 28.05.13
 * Time: 16:11
 * To change this template use File | Settings | File Templates.
 */
public class SPACC implements GlobalFeature {
    //    private int histLength = 1024;
    private int histLength = 256;
    //    int histogramSize = histLength * 4 * 4;
    int histogramSize = histLength * 5 + histLength * 4 * 4;
    double[] histogram = new double[histogramSize];

    // Temp:
    int tmp;

    @Override
    public void extract(BufferedImage bimg) {
        // level 0:
        AutoColorCorrelogram acc = new AutoColorCorrelogram();
        acc.extract(bimg);
        System.arraycopy(acc.getFeatureVector(), 0, histogram, 0, histLength);
        // level 1:
        int w = bimg.getWidth() / 2;
        int h = bimg.getHeight() / 2;
        acc.extract(bimg.getSubimage(0, 0, w, h));
        System.arraycopy(acc.getFeatureVector(), 0, histogram, histLength * 1, histLength);
        acc.extract(bimg.getSubimage(w, 0, w, h));
        System.arraycopy(acc.getFeatureVector(), 0, histogram, histLength * 2, histLength);
        acc.extract(bimg.getSubimage(0, h, w, h));
        System.arraycopy(acc.getFeatureVector(), 0, histogram, histLength * 3, histLength);
        acc.extract(bimg.getSubimage(w, h, w, h));
        System.arraycopy(acc.getFeatureVector(), 0, histogram, histLength * 4, histLength);
        // level 2:
        int wstep = bimg.getWidth() / 4;
        int hstep = bimg.getHeight() / 4;
        int binPos = 0; // the next free section in the histogram
        for (int i = 0; i < 4; i++) {
            for (int j = 0; j < 4; j++) {
                acc.extract(bimg.getSubimage(i * wstep, j * hstep, wstep, hstep));
                System.arraycopy(acc.getFeatureVector(), 0, histogram, histLength * binPos, histLength);
                binPos++;
            }
        }

    }

    /**
     * Provides a faster way of serialization.
     *
     * @return a byte array that can be read with the corresponding method.
     * @see SPJCD#setByteArrayRepresentation(byte[])
     */
    public byte[] getByteArrayRepresentation() {
        byte[] result = new byte[histogramSize/2];
        for (int i = 0; i < result.length; i++) {
            tmp = ((int) (histogram[(i << 1)])) << 4;
            tmp = (tmp | ((int) (histogram[(i << 1) + 1])));
            result[i] = (byte) (tmp - 128);
        }
        return result;
    }

    /**
     * Reads descriptor from a byte array. Much faster than the String based method.
     *
     * @param in byte array from corresponding method
     * @see SPJCD#getByteArrayRepresentation
     */
    public void setByteArrayRepresentation(byte[] in) {
        setByteArrayRepresentation(in, 0, in.length);
    }

    public void setByteArrayRepresentation(byte[] in, int offset, int length) {
        for (int i = offset; i < offset + length; i++) {
            tmp = in[i] + 128;
            histogram[((i - offset) << 1) + 1] = ((double) (tmp & 0x000F));
            histogram[(i - offset) << 1] = ((double) (tmp >> 4));
        }
    }

    @Override
    public double[] getFeatureVector() {
        return histogram;
    }

    @Override
    public double getDistance(LireFeature feature) {
        if (!(feature instanceof SPACC)) return -1;
        return MetricsUtils.tanimoto(histogram, feature.getFeatureVector());
    }

//    @Override
//    public String getStringRepresentation() {
//        throw new UnsupportedOperationException("Not implemented!");
//    }
//
//    @Override
//    public void setStringRepresentation(String s) {
//        throw new UnsupportedOperationException("Not implemented!");
//    }

    @Override
    public String getFeatureName() {
        return "Auto Color Correlogram Spatial Pyramid";
    }

    @Override
    public String getFieldName() {
        return "f_spacc";
    }
}
