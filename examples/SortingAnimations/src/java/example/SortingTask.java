// -*- mode:java; encoding:utf-8 -*-
// vim:set fileencoding=utf-8:
// @homepage@

package example;

import java.awt.*;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.ExecutionException;
import javax.swing.*;

// SortAnim.java -- Animate sorting algorithms
// Copyright (C) 1999 Lucent Technologies
// From 'Programming Pearls' by Jon Bentley
// Sorting Algorithm Animations from Programming Pearls
// http://www.cs.bell-labs.com/cm/cs/pearls/sortanim.html
// modified by aterai aterai@outlook.com
@SuppressWarnings("PMD.TooManyMethods")
public class SortingTask extends SwingWorker<String, Rectangle> {
  public static final int DOT_SIZE = 4;
  private static final long DELAY = 5L;
  private final SortAlgorithm algorithm;
  private final List<Double> array;
  private final Rectangle area;

  public SortingTask(SortAlgorithm algorithm, List<Double> array, Rectangle area) {
    super();
    this.algorithm = algorithm;
    this.array = array;
    this.area = new Rectangle(area);
  }

  public static Rectangle getDotBounds(Rectangle area, int size, int index, double value) {
    int x = area.x + (int) (area.width * index / (double) size);
    int y = area.y + area.height - (int) (area.height * value);
    return new Rectangle(x, y, DOT_SIZE, DOT_SIZE);
  }

  @Override protected final String doInBackground() throws InterruptedException {
    int n = array.size();
    switch (algorithm) {
      case INSERTION:
        insertionSort(n);
        break;
      case SELECTION:
        selectionSort(n);
        break;
      case SHELL:
        shellSort(n);
        break;
      case HEAP:
        heapSort(n);
        break;
      case QUICK:
        quickSort(0, n - 1);
        break;
      case TWO_WAY_QUICK:
        twoWayQuickSort(0, n - 1);
        break;
      default:
        throw new AssertionError("Unknown SortAlgorithm: " + algorithm);
    }
    return "Done";
  }

  protected final String getDoneMessage() {
    String msg;
    try {
      msg = isCancelled() ? "Cancelled" : get();
    } catch (InterruptedException ex) {
      msg = "Interrupted";
      Thread.currentThread().interrupt();
    } catch (ExecutionException ex) {
      msg = "Error: " + ex.getMessage();
    }
    return msg;
  }

  private void swap(int i, int j) throws InterruptedException {
    if (isCancelled()) {
      throw new InterruptedException();
    }
    // erase the dots at their old positions...
    publishDirtyRegion(i);
    publishDirtyRegion(j);
    Collections.swap(array, i, j);
    // ...and draw them at their new positions
    publishDirtyRegion(i);
    publishDirtyRegion(j);
    Thread.sleep(DELAY);
  }

  private void publishDirtyRegion(int index) {
    Rectangle r = getDotBounds(area, array.size(), index, array.get(index));
    // Graphics#drawOval(x, y, w, h) covers (w + 1) x (h + 1) pixels
    r.setSize(r.width + 1, r.height + 1);
    publish(r);
  }

  // Sorting Algorithms
  private void insertionSort(int n) throws InterruptedException {
    for (int i = 1; i < n; i++) {
      for (int j = i; j > 0 && array.get(j - 1) > array.get(j); j--) {
        swap(j - 1, j);
      }
    }
  }

  private void selectionSort(int n) throws InterruptedException {
    for (int i = 0; i < n - 1; i++) {
      int min = i;
      for (int j = i + 1; j < n; j++) {
        if (array.get(j) < array.get(min)) {
          min = j;
        }
      }
      if (min != i) {
        swap(i, min);
      }
    }
  }

  private void shellSort(int n) throws InterruptedException {
    // Knuth's gap sequence: 1, 4, 13, 40, 121, ...
    int h = 1;
    while (h < n / 3) {
      h = 3 * h + 1;
    }
    for (; h > 0; h /= 3) {
      for (int i = h; i < n; i++) {
        for (int j = i; j >= h && array.get(j - h) > array.get(j); j -= h) {
          swap(j - h, j);
        }
      }
    }
  }

  private void siftDown(int root, int last) throws InterruptedException {
    int parent = root;
    int child = 2 * parent + 1;
    while (child <= last) {
      if (child < last && array.get(child + 1) > array.get(child)) {
        child++;
      }
      if (array.get(parent) >= array.get(child)) {
        break;
      }
      swap(parent, child);
      parent = child;
      child = 2 * parent + 1;
    }
  }

  private void heapSort(int n) throws InterruptedException {
    for (int i = n / 2 - 1; i >= 0; i--) {
      siftDown(i, n - 1);
    }
    for (int i = n - 1; i > 0; i--) {
      swap(0, i);
      siftDown(0, i - 1);
    }
  }

  private void quickSort(int lower, int upper) throws InterruptedException {
    if (lower < upper) {
      int m = lower;
      for (int i = lower + 1; i <= upper; i++) {
        if (array.get(i) < array.get(lower)) {
          m++;
          swap(m, i);
        }
      }
      swap(lower, m);
      quickSort(lower, m - 1);
      quickSort(m + 1, upper);
    }
  }

  private void twoWayQuickSort(int lower, int upper) throws InterruptedException {
    if (lower < upper) {
      int m = partition(lower, upper);
      twoWayQuickSort(lower, m - 1);
      twoWayQuickSort(m + 1, upper);
    }
  }

  private int partition(int lower, int upper) throws InterruptedException {
    double pivot = array.get(lower);
    int i = lower;
    int j = upper + 1;
    while (true) {
      do {
        i++;
      } while (i <= upper && array.get(i) < pivot);
      do {
        j--;
      } while (array.get(j) > pivot);
      if (i > j) {
        break;
      }
      swap(i, j);
    }
    swap(lower, j);
    return j;
  }
}
