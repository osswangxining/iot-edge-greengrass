package jsonpath;

import java.util.List;

import org.apache.edgent.function.Predicate;

import com.jayway.jsonpath.JsonPath;

public class SamplePredicate implements Predicate<String> {

	/**
	 * 
	 */
	private static final long serialVersionUID = -914039055022206052L;
	private String condition;

	public SamplePredicate(String condition) {
		this.condition = condition;
	}

	@Override
	public boolean test(String obj) {
		//String json = "{\"d\":{\"name\":\"device001\",\"temp\":63.74451305929553}}";
		//String condition = "@.d.temp>=60 || @.d.temp<=20";
		//System.out.println(obj);
		//System.out.println("$[?(" + this.condition + ")]");
		List<String> result = JsonPath.parse(obj).read("$[?(" + this.condition + ")]");
		return !result.isEmpty();
	}

}
