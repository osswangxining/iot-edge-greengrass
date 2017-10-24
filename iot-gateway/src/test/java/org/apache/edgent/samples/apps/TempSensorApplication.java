package org.apache.edgent.samples.apps;

import java.util.concurrent.TimeUnit;

import org.apache.edgent.function.Predicate;
import org.apache.edgent.providers.direct.DirectProvider;
import org.apache.edgent.topology.TStream;
import org.apache.edgent.topology.Topology;

import jsonpath.SamplePredicate;

public class TempSensorApplication {
  public static void main(String[] args) throws Exception {
    TempSensor sensor = new TempSensor();
    DirectProvider dp = new DirectProvider();
    Topology topology = dp.newTopology();
    TStream<Double> tempReadings = topology.poll(sensor, 1, TimeUnit.MILLISECONDS);
    //Predicate<Double> p = new SamplePredicate<Double>();
    TStream<Double> filteredReadings = tempReadings.filter(reading -> reading < 50 || reading > 80);
    //TStream<Double> filteredReadings = tempReadings.filter(p);
    filteredReadings.print();
    dp.submit(topology);
  }
}