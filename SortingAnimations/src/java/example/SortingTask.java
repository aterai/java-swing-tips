package example;
// -*- mode:java; encoding:utf-8 -*-
// vim:set fileencoding=utf-8:
// @homepage@

import java.awt.*;
import java.util.Collections;
import java.util.List;
import javax.swing.*;

// SortAnim.java -- Animate sorting algorithms
// Copyright (C) 1999 Lucent Technologies
// From 'Programming Pearls' by Jon Bentley
// Sorting Algorithm Animations from Programming Pearls
// http://www.cs.bell-labs.com/cm/cs/pearls/sortanim.html
// modified by aterai aterai@outlook.com

public class SortingTask extends SwingWorker<String, Rectangle> {
  private final List<Double> array;
  private final int number;
  private final double factorx;
  private final double factory;
  private final Rectangle rect;
  private final Rectangle repaintArea;
  private final SortAlgorithms sortAlgorithm;

  public SortingTask(SortAlgorithms sortAlgorithm, int number, List<Double> array, Rectangle rect, double factorx, double factory) {
    super();
    this.sortAlgorithm = sortAlgorithm;
    this.number = number;
    this.array = array;
    this.rect = rect;
    this.factorx = factorx;
    this.factory = factory;
    this.repaintArea = new Rectangle(rect);
    this.repaintArea.grow(5, 5);
  }

  @Override public String doInBackground() {
    try {
      switch (sortAlgorithm) {
        case ISORT:
          isort(number);
          break;
        case SELSORT:
          ssort(number);
          break;
        case SHELLSORT:
          shellsort(number);
          break;
        case HSORT:
          heapsort(number);
          break;
        case QSORT:
          qsort(0, number - 1);
          break;
        case QSORT2:
          qsort2(0, number - 1);
          break;
        default:
          throw new AssertionError("Unknown SortAlgorithms");
      }
    } catch (InterruptedException ex) {
      return "Interrupted";
    }
    return "Done";
  }

  private void swap(int i, int j) throws InterruptedException {
    if (isCancelled()) {
      throw new InterruptedException();
    }
    int px = (int) (rect.x + factorx * i);
    int py = rect.y + rect.height - (int) (factory * array.get(i));
    publish(new Rectangle(px, py, 4, 4));

    // double t = array.get(i);
    // array.set(i, array.get(j));
    // array.set(j, t);
    Collections.swap(array, i, j);

    px = (int) (rect.x + factorx * i);
    py = rect.y + rect.height - (int) (factory * array.get(i));
    publish(new Rectangle(px, py, 4, 4));

    publish(repaintArea);
    Thread.sleep(5);
  }

  // Sorting Algs
  private void isort(int n) throws InterruptedException {
    for (int i = 1; i < n; i++) {
      for (int j = i; j > 0 && array.get(j - 1) > array.get(j); j--) {
        swap(j - 1, j);
      }
    }
  }

  private void ssort(int n) throws InterruptedException {
    for (int i = 0; i < n - 1; i++) {
      for (int j = i; j < n; j++) {
        if (array.get(j) < array.get(i)) {
          swap(i, j);
        }
      }
    }
  }

  private void shellsort(int n) throws InterruptedException {
    int i;
    int j;
    int h = 1;
    while (h < n) {
      h = 3 * h + 1;
    }
    for (;;) {
      h /= 3;
      if (h - 1 < 0) {
        break;
      }
      for (i = h; i < n; i++) {
        for (j = i; j >= h; j -= h) {
          if (array.get(j - h) < array.get(j)) {
            break;
          }
          swap(j - h, j);
        }
      }
    }
  }

  private void siftdown(int l, int u) throws InterruptedException {
    int i = l;
    int c;
    for (;;) {
      c = 2 * i;
      if (c > u) {
        break;
      }
      if (c + 1 <= u && array.get(c + 1) > array.get(c)) {
        c++;
      }
      if (array.get(i) >= array.get(c)) {
        break;
      }
      swap(i, c);
      i = c;
    }
  }

  private void heapsort(int n) throws InterruptedException { // BEWARE!!! Sorts x[1..n-1]
    int i;
    for (i = n / 2; i > 0; i--) {
      siftdown(i, n - 1);
    }
    for (i = n - 1; i >= 2; i--) {
      swap(1, i);
      siftdown(1, i - 1);
    }
  }

  private void qsort(int l, int u) throws InterruptedException {
    if (l >= u) {
      return;
    }
    int m = l;
    for (int i = l + 1; i <= u; i++) {
      if (array.get(i) < array.get(l)) {
        swap(++m, i);
      }
    }
    swap(l, m);
    qsort(l, m - 1);
    qsort(m + 1, u);
  }

  private void qsort2(int l, int u) throws InterruptedException {
    if (l >= u) {
      return;
    }
    int i = l;
    int j = u + 1;
    for (;;) {
      do {
        i++;
      } while (i <= u && array.get(i) < array.get(l));
      do {
        j--;
      } while (array.get(j) > array.get(l));
      if (i > j) {
        break;
      }
      swap(i, j);
    }
    swap(l, j);
    qsort2(l, j - 1);
    qsort2(j + 1, u);
  }
}
