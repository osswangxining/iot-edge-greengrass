package jsonpath;

import java.util.concurrent.TimeUnit;

import org.apache.edgent.function.Predicate;
import org.apache.edgent.providers.direct.DirectProvider;
import org.apache.edgent.topology.TStream;
import org.apache.edgent.topology.Topology;

import jsonpath.SamplePredicate;

public class TempSensorSampleApplication {
	public static void main(String[] args) throws Exception {
		GenericSensor sensor = new GenericSensor();
		DirectProvider dp = new DirectProvider();
		Topology topology = dp.newTopology();
		TStream<String> tempReadings = topology.poll(sensor, 1, TimeUnit.MILLISECONDS);
		
		String condition = "@.d.temp>=60 || @.d.temp<=20";
		Predicate<String> p = new SamplePredicate(condition);
		// TStream<Double> filteredReadings = tempReadings.filter(reading -> reading <
		// 50 || reading > 80);
		TStream<String> filteredReadings = tempReadings.filter(p);
		filteredReadings.print();
		dp.submit(topology);
	}
}