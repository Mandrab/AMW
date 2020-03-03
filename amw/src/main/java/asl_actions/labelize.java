package asl_actions;

import jason.asSemantics.DefaultInternalAction;
import jason.asSemantics.TransitionSystem;
import jason.asSemantics.Unifier;
import jason.asSyntax.*;
import jason.stdlib.term2string;
import model.utils.LiteralBuilder;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.yaml.snakeyaml.Yaml;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import static java.util.stream.Collectors.groupingBy;
import static java.util.stream.Collectors.joining;
import static model.utils.LiteralParser.split;

public class labelize extends DefaultInternalAction {

	@Override
	public Object execute( TransitionSystem ts, Unifier un, Term[] args) throws IOException {

		String script = ( ( StringTerm ) args[ 0 ] ).getString( );

		List<String> plans = split( script.substring( 1, script.length( ) -1 ) );

		AtomicInteger ai = new AtomicInteger( );

		StringTerm labeledPlans = new StringTermImpl( "[" + plans.stream( )
				.map( s -> labelizePlan( s, ( ) -> ai.getAndIncrement( ) + "" ) ).collect( joining( "," ) ) + "]" );

		un.unifies( labeledPlans, args[ 1 ] );

		return true;
	}

	public static String labelizePlan( String planString, Supplier<String> code ) {
		Function<String, String> newLabel = s -> "@l" + code.get( ) + s + " ";

		planString = planString.substring( 1 );                   // remove "{"

		if ( planString.startsWith( "@" ) ) {
			String oldLabel = planString.substring( 1, planString.indexOf( " " ) );
			planString = planString.substring( planString.indexOf( " " ) );

			if ( oldLabel.contains( "[" ) )
				return "{" + newLabel.apply( oldLabel.substring( oldLabel.indexOf( "[" ) ) ) + planString;
			return "{" + newLabel.apply( "" ) + planString;
		} return "{" + newLabel.apply( "" ) + planString;
	}
}
