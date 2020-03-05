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
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import static java.util.stream.Collectors.groupingBy;
import static java.util.stream.Collectors.joining;
import static model.utils.LiteralParser.split;

public class load_commands extends DefaultInternalAction {

	@Override
	public Object execute( TransitionSystem ts, Unifier un, Term[] args) throws IOException {
		final File folder = new File( new java.io.File( "." ).getCanonicalPath( ) + File.separator
				+ "src" + File.separator
				+ "asl_agents" + File.separator
				+ "commands" + File.separator );

		ListTerm result = new ListTermImpl( );
		result.addAll( Arrays.stream( Objects.requireNonNull( folder.listFiles( ) ) )
				.filter( file -> file.getName( ).startsWith( "cid" ) )
				.collect( groupingBy( file -> file.getName( ).substring( 3, file.getName( ).indexOf( "-" ) ) ) )
				.values( ).stream( ).map( load_commands::getCommand ).collect( Collectors.toList( ) ) );

		un.unifies( result, args[ 0 ] );

		return true;
	}

	public static Literal getCommand( List<File> versions ) {
		File infoFile = versions.stream( ).filter( f -> f.getName( ).contains( "info" ) ).findAny( )
				.orElseThrow( ( ) -> new IllegalArgumentException( "No info file!" ) );
		versions.remove( infoFile );

		return new LiteralBuilder( ).setName( "command" ).addValues(
				new LiteralBuilder( "id" ).addValues( new StringTermImpl( infoFile.getName( ).substring( 0, infoFile.getName( ).indexOf( "-" ) ) ) ).build( ),
				new LiteralBuilder( "name" ).addValues( new StringTermImpl( getName( infoFile ) ) ).build( ),
				new LiteralBuilder( "description" ).addValues( new StringTermImpl( getDescription( infoFile ) ) ).build( ) )
				.addQueue( versions.stream( ).filter( f -> ! f.getName( ).contains( "info" ) )
						.map( load_commands::getVariant ).collect( Collectors.toList( ) )
						.toArray( new Literal[] { } ) ).build( );
	}

	public static String getName( File file ) {
		return ( String ) Objects.requireNonNull( openYaml( file ) ).get( "name" );
	}

	public static String getDescription( File file ) {
		return ( String ) Objects.requireNonNull( openYaml( file ) ).get( "description" );
	}

	public static Map<String, Object> openYaml( File file ) {
		try {
			Yaml yaml = new Yaml( );
			InputStream inputStream = new FileInputStream( file );
			return yaml.load( inputStream );
		} catch ( FileNotFoundException e ) {
			e.printStackTrace( );
		}
		return null;
	}

	public static Literal getVariant( File file ) {
		try {
			String fileName = file.getName( );

			List<String> terms = getRequirements( file );

			return new LiteralBuilder( ).setName( "variant" ).addValues( new LiteralBuilder( "v_id" ).addValues( new StringTermImpl(
							fileName.substring( fileName.indexOf( "-" ) +1, fileName.indexOf( ".asl" ) ) ) ).build( ),
					new LiteralBuilder( ).setName( "requirements" ).addQueue( terms.toArray( new String[] { } ) )
							.build( ), new LiteralBuilder( "script" ).addValues( new StringTermImpl( getScript( file ) ) ).build( ) ).build( );
		} catch ( IOException e ) {
			e.printStackTrace( );
		}
		return null;
	}

	public static List<String> getRequirements( File file ) throws IOException {
		List<String> lines = Files.readAllLines( file.toPath( ), StandardCharsets.UTF_8 );

		return getRequirements( String.join( "\n", lines ) );
	}

	public static List<String> getRequirements( String s ) {
		List<String> lines = new LinkedList<>( Arrays.asList( s.split( "\n" ) ) );

		StringBuilder requirements = new StringBuilder( lines.remove( 0 ) );

		while( ! requirements.toString( ).contains( "[" ) && ! requirements.toString( ).contains( "]" ) ) {
			String line = lines.remove( 0 );
			if ( ! line.contains( "]" ) )
				requirements.append( line );
			else
				requirements.append( line, 0, line.indexOf( "]" ) + 1 );
		}

		requirements = new StringBuilder( requirements.toString( ).replaceAll( " ", "" ) );

		return split( requirements.substring( requirements.indexOf( "[" ), requirements.indexOf( "]" ) + 1 ) );
	}

	public static String getScript( File path ) throws IOException {
		List<String> lines = Files.readAllLines( path.toPath( ), StandardCharsets.UTF_8 ).stream( )
				.filter( load_commands::valid ).map( load_commands::removeComments ).collect( Collectors.toList( ) );

		return getScript( String.join( "\n", lines ) );
	}

	public static String getScript( String string ) {
		List<String> lines = new LinkedList<>( Arrays.asList( string.split( "\n" ) ) );

		boolean requirementsEnded = false;
		while ( ! requirementsEnded ) {
			String line = lines.get( 0 );

			if ( line.contains( "]" ) ) {
				requirementsEnded = true;
				if ( line.indexOf( "]" ) < line.length( ) )
					lines.add( 0, line.substring( lines.remove( 0 ).indexOf( "]" ) +1 ) );
			} else {
				lines.remove( 0 );
			}
		}

		Pattern r = Pattern.compile( "\\.[\t| ]" );
		for ( int i = 0; i < lines.size( ); i++ ) {
			Matcher m = r.matcher( lines.get( i ) );
			if ( m.find( ) ) {
				String l = lines.remove( i );
				lines.add( i++, l.substring( 0, l.indexOf( ". " ) + 1 ) );
				String[] plans = m.replaceAll( "\\.\n" ).split( "\n" );
				for ( int j = plans.length -1; j > 0; j-- ) {
					lines.add( i, plans[ j ] );
				}
			}
		}

		AtomicInteger ai = new AtomicInteger(  );
		AtomicInteger labelIdx = new AtomicInteger(  );

		return "[" + lines.stream( ).collect( groupingBy( s -> s.endsWith( "." ) ? ai.getAndIncrement( ) : ai.get( ) ) )    // group string in sublist till find a plan end
				.values( ).stream( ).map( plan -> String.join( "", plan ) )                // join plans
				.map( s -> "{" + s.substring( 0, s.length( ) -1 ) + "}" )
				.map( s -> labelize.labelizePlan( s, ( ) -> labelIdx.getAndIncrement( ) + "" ) )
				.map( s -> s.replaceAll( "\"", "'" ) )
				.collect( Collectors.joining( "," ) ) + "]";
	}

	private static boolean valid( String s ) {
		s = s.replace( " ", "" ).replace( "\t", "" );

		return ! ( s.isEmpty( ) || s.startsWith( "//" ) );
	}

	private static String removeComments( String s ) {
		if ( ! valid( s ) ) return "";
		if ( ! s.contains( "//" ) ) return s;

		int idx = s.indexOf( "//" );
		return s.substring( 0, idx );
	}
}
