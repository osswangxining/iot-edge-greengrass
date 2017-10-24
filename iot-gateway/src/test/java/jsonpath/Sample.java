package jsonpath;

import java.util.List;

import com.jayway.jsonpath.JsonPath;

public class Sample {

	public static void main(String[] args) {
		//String json = "{\"d\":{\"a\":2,\"FamilyData\":{\"Age\":50,\"Name\":\"test\"}}}";
		String json = "{\"d\":{\"name\":\"device001\",\"temp\":63.74451305929553}}";
		//String condition = "@.d.FamilyData.Age>=60 || @.d.FamilyData.Age<=20";
		String condition = "@.d.temp>=60 || @.d.temp<=20";
		List<String> result = JsonPath.parse(json).read("$[?("+condition+")]");
		System.out.println(result);
	}

}
