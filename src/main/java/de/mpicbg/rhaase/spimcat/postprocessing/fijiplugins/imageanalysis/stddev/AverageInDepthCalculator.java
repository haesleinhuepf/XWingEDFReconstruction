package de.mpicbg.rhaase.spimcat.postprocessing.fijiplugins.imageanalysis.stddev;

import de.mpicbg.rhaase.spimcat.postprocessing.fijiplugins.imageanalysis.statistics.Average;
import de.mpicbg.rhaase.spimcat.postprocessing.regions.RingRegionGenerator;
import net.imagej.ops.OpService;
import net.imglib2.*;
import net.imglib2.img.Img;
import net.imglib2.roi.Regions;
import net.imglib2.type.logic.BitType;
import net.imglib2.type.logic.BoolType;
import net.imglib2.type.numeric.RealType;

import java.util.ArrayList;

/**
 * Author: Robert Haase (http://haesleinhuepf.net) at MPI CBG (http://mpi-cbg.de)
 * December 2017
 */
public class AverageInDepthCalculator<T extends RealType<T>> {

    private Img<T> image;
    private Img<T> qualityImage;

    private OpService ops;

    private int maxDepthInPixels = 50;


    private int depthStep = 10;

    private Double[] measurements = null;
    private Long[] pixelCounts = null;

    private RandomAccessibleInterval<BoolType>[] analysedRegions;
    private int minDepthInPixels;

    public AverageInDepthCalculator(Img<T> image, Img<T> qualityImage, OpService ops) {
        this.image = image;
        this.qualityImage = qualityImage;
        this.ops = ops;
    }

    private void process() {
        if (measurements != null) {
            return;
        }

        IterableInterval<BitType> binaryIi = ops.threshold().huang(image);

        Img<BitType> binaryImg = ops.convert().bit(binaryIi);

        RingRegionGenerator ringRegionGenerator = new RingRegionGenerator(binaryImg, ops);

        ArrayList<Double> averageList = new ArrayList<>();
        ArrayList<Long> pixelCountList = new ArrayList<>();
        ArrayList<RandomAccessibleInterval<BoolType>> analysedRegionsList = new ArrayList<>();

        for (int position = minDepthInPixels + depthStep; position <= maxDepthInPixels; position += depthStep) {
            RandomAccessibleInterval<BoolType> ring = ringRegionGenerator.getRing(position - depthStep, position);

            IterableInterval<Void> ringIi = Regions.iterable(ring);

            RandomAccessible<T> imageRA = qualityImage;

            IterableInterval<T> region = Regions.sample(ringIi, imageRA);

            Average<T> average = new Average<>(region);

            analysedRegionsList.add(ring);
            averageList.add(average.getAverage());
            pixelCountList.add(average.getCount());
        }

        measurements = new Double[averageList.size()];
        averageList.toArray(measurements);


        pixelCounts = new Long[pixelCountList.size()];
        pixelCountList.toArray(pixelCounts);

        analysedRegions = new RandomAccessibleInterval[analysedRegionsList.size()];
        analysedRegionsList.toArray(analysedRegions);

        ringRegionGenerator.reset();
    }

    public Double[] getMeasurements() {
        process();
        return measurements;
    }
    public Long[] getPixelCounts() {
        process();
        return pixelCounts;
    }


    public RandomAccessibleInterval[] getAnalysedRegions() {
        process();
        return analysedRegions;
    }

    public void setMaxDepthInPixels(int maxDepthInPixels) {
        this.maxDepthInPixels = maxDepthInPixels;
    }

    public void setDepthStep(int depthStep) {
        this.depthStep = depthStep;
    }

    public void setMinDepthInPixels(int minDepthInPixels) {
        this.minDepthInPixels = minDepthInPixels;
    }
}
