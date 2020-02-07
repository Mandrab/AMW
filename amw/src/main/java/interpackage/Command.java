package interpackage;

import org.apache.commons.lang3.tuple.Pair;
import org.apache.commons.lang3.tuple.Triple;

import java.util.List;
import java.util.stream.Collectors;

public class Command {

	private String id;
	private String name;
	private String description;
	private List<Triple<String, List<String>, String>> versions;

	public Command ( String id, String name, String description, List<Triple<String, List<String>, String>> versions ) {
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

	public List<Triple<String, List<String>, String>> getVersions ( ) {
		return versions;
	}

	@Override
	public String toString( ) {
		return "Id: " + id + ", name: " + name + ", description: " + description + ", versions:" + versions.stream( )
				.map( p -> "\t" + p.getLeft( ) + "\n"
						+ "\t\t" + p.getMiddle( ).stream( ).collect( Collectors.joining( ) ) + "\n"
						+ "\t\t" + p.getRight( ) );
	}
}
