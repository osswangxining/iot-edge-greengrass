package org.apache.edgent.samples.apps;

import org.apache.edgent.providers.direct.DirectProvider;
import org.apache.edgent.providers.direct.DirectTopology;
import org.apache.edgent.topology.TStream;

public class HelloEdgent {
	public static void main(String[] args) {
		DirectProvider dp = new DirectProvider();
		DirectTopology newTopology = dp.newTopology();
		TStream<String> helloStream = newTopology.strings("hello", "edgent", "!");
		helloStream.print();
		dp.submit(newTopology);
	}
}
