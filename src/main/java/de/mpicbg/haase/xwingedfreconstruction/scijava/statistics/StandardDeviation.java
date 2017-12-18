package de.mpicbg.haase.xwingedfreconstruction.scijava.statistics;

import net.imglib2.Cursor;
import net.imglib2.IterableInterval;
import net.imglib2.RandomAccessibleInterval;
import net.imglib2.type.numeric.RealType;
import net.imglib2.view.Views;

/**
 * Author: Robert Haase (http://haesleinhuepf.net) at MPI CBG (http://mpi-cbg.de)
 * November 2017
 */
public class StandardDeviation<T extends RealType<T>>
{
  private IterableInterval<T> image;
  private Average<T> average;

  boolean resultValid = false;
  double standardDeviation;

  public StandardDeviation(IterableInterval<T> image) {
    this.image = image;
    this.average = new Average<T>(image);
  }
  public StandardDeviation(IterableInterval<T> image, Average<T> average)
  {
    this.image = image;
    this.average = average;
  }
  private void process() {
    if (resultValid) {
      return;
    }
    Cursor<T> cursor = (image).localizingCursor();
    double mean = average.getAverage();
    long count = average.getCount();

    double sum = 0;
    while (cursor.hasNext()) {
      sum += Math.pow(cursor.next().getRealDouble() - mean, 2);
    }
    standardDeviation = sum / (count - 1);
  }

  public double getStandardDevation() {
    process();
    return standardDeviation;
  }
}
