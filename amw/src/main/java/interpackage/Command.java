package interpackage;

import model.utils.LiteralParser;
import org.apache.commons.lang3.tuple.Pair;
import org.apache.commons.lang3.tuple.Triple;

import java.util.List;
import java.util.stream.Collectors;

import static model.utils.LiteralParser.*;

public class Command {

	private String id;
	private String name;
	private String description;
	private List<Version> versions;

	public Command ( String id, String name, String description, List<Version> versions ) {
		this.id = id;
		this.name = name;
		this.description = description;
		this.versions = versions;
	}

	public String getId ( ) {
		return id;
	}

	public String getName ( ) {
		return name;
	}

	public String getDescription ( ) {
		return description;
	}

	public List<Version> getVersions ( ) {
		return versions;
	}

	static public Command parse ( String input ) {
		Pair<String, String> pair = splitStructAndList( input );
		String id = getValue( pair.getKey( ), "id" );
		String name = getValue( pair.getKey( ), "name" );
		String description = getValue( pair.getKey( ), "description" );
		List<Version> versions = split( pair.getValue( ) ).stream( ).map( Version::parse )
				.collect( Collectors.toList( ) );
		return new Command( id, name, description, versions );
	}

	@Override
	public Command clone( ) {
		return new Command( id, name, description, versions.stream( ).map( Version::clone )
				.collect( Collectors.toList( ) ) );
	}

	@Override
	public boolean equals( Object obj ) {
		if ( ! ( obj instanceof Command ) ) return false;

		Command cmd = ( Command ) obj;
		return id.equals( cmd.id ) && name.equals( cmd.name ) && description.equals( cmd.description )
				&& versions.equals( cmd.versions );
	}

	@Override
	public String toString( ) {
		return "Id: " + id + ", name: " + name + ", description: " + description + ", versions:" + versions.stream( )
				.map( p -> "\t" + p.getLeft( ) + "\n"
						+ "\t\t" + String.join( "", p.getMiddle( ) ) + "\n"
						+ "\t\t" + p.getRight( ) );
	}

	public static class Version extends Triple<String, List<String>, String> {

		private String id;
		private List<String> requirements;
		private String script;

		public Version( String id, List<String> requirements, String script ) {
			this.id = id;
			this.requirements = requirements;
			this.script = script;
		}

		public String getId ( ) {
			return id;
		}

		public List<String> getRequirements ( ) {
			return requirements;
		}

		public String getScript ( ) {
			return script;
		}

		static public Version parse ( String input ) {
			return new Version( getValue( input, "v_id" ),
					split( split( getValue( input ) ).stream( ).filter( lit -> lit.startsWith( "requirements" ) )
							.findFirst( ).map( LiteralParser::splitStructAndList ).get( ).getValue( ) ),
					getValue( input, "script" ) );
		}

		@Override
		public String getLeft ( ) {
			return getId( );
		}

		@Override
		public List<String> getMiddle ( ) {
			return getRequirements( );
		}

		@Override
		public String getRight ( ) {
			return getScript( );
		}

		@Override
		public Version clone( ) {
			return new Version( id, requirements.stream( ).map( String::new ).collect( Collectors.toList( ) ), script );
		}

		@Override
		public boolean equals( Object obj ) {
			if ( ! ( obj instanceof Version ) ) return false;

			Version vrs = ( Version ) obj;
			return id.equals( vrs.id ) && requirements.equals( vrs.requirements ) && script.equals( vrs.script );
		}
	}
}
