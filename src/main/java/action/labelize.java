package action;

import jason.asSemantics.DefaultInternalAction;
import jason.asSemantics.TransitionSystem;
import jason.asSemantics.Unifier;
import jason.asSyntax.*;

import java.io.*;
import java.util.Arrays;
import java.util.List;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.IntStream;

import static java.util.stream.Collectors.joining;

public class labelize extends DefaultInternalAction {

	@Override public Object execute(TransitionSystem ts, Unifier un, Term[] args) throws IOException {
		String script = ((StringTerm) args[0]).getString();
		script = script.substring(0, script.length() -1);

		List<String> plans = Arrays.asList(script.split("\\.(\n)+"));

		String labeledPlans = IntStream.range(0, plans.size())
				.mapToObj(i -> labelizePlan(plans.get(i), () -> String.valueOf(i))).collect(joining(","));

		return un.unifies(new StringTermImpl("[" + labeledPlans + "]"), args[1]);
	}

	public static String labelizePlan(String planString, Supplier<String> code) {
		Function<String, String> newLabel = s -> "@l" + code.get() + s + " ";

		if (planString.startsWith("@")) {
			String oldLabel = planString.substring(1, planString.indexOf(" "));
			planString = planString.substring(planString.indexOf(" "));

			if (oldLabel.contains("["))
				return "{" + newLabel.apply(oldLabel.substring(oldLabel.indexOf("["))) + planString + "}";
			return "{" + newLabel.apply("") + planString + "}";
		} return "{" + newLabel.apply("") + planString + "}";
	}
}
