package action;

import jason.asSemantics.DefaultInternalAction;
import jason.asSemantics.TransitionSystem;
import jason.asSemantics.Unifier;
import jason.asSyntax.ListTermImpl;
import jason.asSyntax.Term;

import java.util.List;
import java.util.stream.Collectors;

public class implement extends DefaultInternalAction {

	@Override
	public Object execute( TransitionSystem ts, Unifier un, Term[] args) {
		List<String> implementations = ((ListTermImpl)args[0]).stream().map(Term::toString)
				.collect(Collectors.toList());
		List<String> requirements = ((ListTermImpl)args[1]).stream().map(Term::toString).collect(Collectors.toList());

		return (requirements.size() == 1 && requirements.get(0).isBlank())
				|| requirements.stream().parallel().allMatch(implementations::contains);
	}
}
