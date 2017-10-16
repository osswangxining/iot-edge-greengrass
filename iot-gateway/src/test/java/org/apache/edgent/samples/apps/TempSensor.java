package org.apache.edgent.samples.apps;

import java.util.Random;

import org.apache.edgent.function.Supplier;

public class TempSensor implements Supplier<Double> {
  double currentTemp = 65.0;
  Random rand;

  TempSensor() {
    rand = new Random();
  }

  @Override
  public Double get() {
    // Change the current temperature some random amount
    double newTemp = rand.nextGaussian() + currentTemp;
    currentTemp = newTemp;
    return currentTemp;
  }
}
