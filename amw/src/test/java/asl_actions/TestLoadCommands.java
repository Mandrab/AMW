package asl_actions;

import org.junit.Test;

import java.io.File;
import java.io.IOException;
import java.util.*;

import static model.utils.LiteralParser.*;
import static org.junit.Assert.*;

public class TestLoadCommands {

	private static final String SCRIPT_PATH = "src" + File.separator + "test" + File.separator + "asl" + File.separator;
	private static final String TEST_01_COMMAND_VARIANT = "cidScriptTest01-vidTestVersionID.asl";
	private static final String TEST_01_COMMAND_INFO = "cidScriptTest01-info.yaml";

	private static final String EXPECTED_DESCRIPTION = "test description";
	private static final String EXPECTED_NAME = "test name";
	private static final String[] EXPECTED_REQUIREMENTS = { "\"requirement0\"" };
	private static final String EXPECTED_REQUIREMENTS_STRING = "requirements[ \"requirement0\" ]";
	private static final String EXPECTED_V_ID = "\"vidTestVersionID\"";

	@Test
	public void testCommandLoad( ) throws IOException {
		String command = load_commands.getCommand( new LinkedList<>( Arrays.asList( getInfoFile( ), getScriptFile( ) ) ) ).toString( );

		assertTrue( command.startsWith( "command" ) );
	}

	@Test
	public void testDescriptionLoad( ) throws IOException {

		String description = load_commands.getDescription( getInfoFile( ) );

		assertEquals( EXPECTED_DESCRIPTION, description );
	}

	@Test
	public void testNameLoad( ) throws IOException {

		String name = load_commands.getName( getInfoFile( ) );

		assertEquals( EXPECTED_NAME, name );
	}

	@Test
	public void testRequirementsLoad( ) throws IOException {

		List<String> requirements = load_commands.getRequirements( getScriptFile( ) );

		System.out.println( requirements );
		assertArrayEquals( EXPECTED_REQUIREMENTS, requirements.toArray( ) );
	}

	@Test
	public void testScriptLoad( ) throws IOException {

		String script = load_commands.getScript( getScriptFile( ) );

		assertTrue( script.startsWith( "[" ) );
		assertTrue( script.endsWith( "]" ) );

		assertFalse( script.contains( "\"" ) );

		split( script.substring( 1, script.length( ) -1 ) ).forEach( plan -> {
			assertTrue( plan.startsWith( "{@" ) );
			assertTrue( plan.substring( plan.indexOf( " " ) +1 ).replaceAll( " ", "" ).startsWith( "+!" ) );
			assertTrue( plan.contains( "<-" ) );
			assertTrue( plan.endsWith( "}" ) );
		} );
	}

	@Test
	@SuppressWarnings("ConstantConditions")
	public void testVariantLoad( ) throws IOException {
		String variant = load_commands.getVariant( getScriptFile( ) ).toString( );

		assertEquals( EXPECTED_V_ID, getValue( variant, "v_id" ) );
		assertTrue( variant.contains( EXPECTED_REQUIREMENTS_STRING.replaceAll( " ", "" ) ) );
	}

	private File getInfoFile( ) throws IOException {
		// get test file data
		return new File( new File( "." ).getCanonicalPath( ) + File.separator + SCRIPT_PATH + TEST_01_COMMAND_INFO );
	}

	private File getScriptFile( ) throws IOException {
		// get test file data
		return new File( new File( "." ).getCanonicalPath( ) + File.separator + SCRIPT_PATH + TEST_01_COMMAND_VARIANT );
	}
}
