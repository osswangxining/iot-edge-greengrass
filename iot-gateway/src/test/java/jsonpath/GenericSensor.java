package jsonpath;

import java.util.Random;

import org.apache.edgent.function.Supplier;

import com.google.gson.JsonObject;

public class GenericSensor implements Supplier<String> {
  double currentTemp = 65.0;
  Random rand;

  GenericSensor() {
    rand = new Random();
  }

  @Override
  public String get() {
    // Change the current temperature some random amount
    double newTemp = rand.nextGaussian() + currentTemp;
    currentTemp = newTemp;
    
    JsonObject obj = new JsonObject();
    JsonObject d = new JsonObject();
    obj.add("d", d);
    d.addProperty("name", "device001");
    d.addProperty("temp", currentTemp);
    
    return obj.toString();
  }
}
