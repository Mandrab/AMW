package asl_actions;

import jason.asSemantics.DefaultInternalAction;
import jason.asSemantics.TransitionSystem;
import jason.asSemantics.Unifier;
import jason.asSyntax.*;
import model.utils.LiteralBuilder;
import org.yaml.snakeyaml.Yaml;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import static java.util.stream.Collectors.groupingBy;
import static model.utils.LiteralParser.split;

public class labelize extends DefaultInternalAction {

	@Override
	public Object execute( TransitionSystem ts, Unifier un, Term[] args) throws IOException {
		ListTerm plans = ( ( ListTerm ) args[ 0 ] );

		AtomicInteger ai = new AtomicInteger( );

		plans.stream( ).map( p -> ( Plan ) p ).forEach( p -> p.setLabel( new Pred( "l" + ai.getAndIncrement( ) ) ) );

		un.unifies( plans, args[ 1 ] );

		return true;
	}
}
