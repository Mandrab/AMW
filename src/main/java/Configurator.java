import com.google.common.io.Files;
import jason.util.Config;
import org.apache.commons.io.FileUtils;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;

public class Configurator {

	public static void main (String[] args) throws IOException {

		File antLibPath = new File(args[ 0 ].substring(0, args[ 0 ].lastIndexOf('/')));

		Config config = Config.get();

		// check ant launcher in path
		linkAntLauncher(antLibPath.getParentFile().getParentFile().getParentFile());
		linkAnt(antLibPath.getParentFile().getParentFile().getParentFile());

		System.out.println(antLibPath.getPath());
		config.setAntLib(antLibPath.getPath());
		config.put(Config.JADE_JAR, args[ 1 ]);

		File file = new File(args[ 2 ] + File.separator + "user.properties");
		Files.createParentDirs(file);
		Files.touch(file);
		file.createNewFile();

		config.store(file);

	}

	private static void linkAntLauncher(File antLibPath)throws IOException {
		// get path to ant launcher jar
		File antLauncherJar = findFile(antLibPath + File.separator + "ant-launcher" + File.separator
				, ".*", "ant-launcher.*\\.jar");

		// get path to ant jar
		File antJar = findFile(antLibPath + File.separator + "ant" + File.separator
				, "^((?!launcher).)*$", "ant.*\\.jar");

		// if a symlink do not exist, create it (we would have both jars into the same folder)
		try {
			findFile(antLibPath + File.separator + "ant" + File.separator
					, ".*", "ant-launcher.*\\.jar");
		} catch(FileNotFoundException e){
			createSymLink(antLauncherJar.getPath(), antJar.getParent());
		}
	}

	private static void linkAnt(File antLibPath)throws IOException {
		// get path to ant launcher jar
		File antLauncherJar = findFile(antLibPath + File.separator + "ant-launcher" + File.separator
				, ".*", "ant-launcher.*\\.jar");

		// get path to ant jar
		File antJar = findFile(antLibPath + File.separator + "ant" + File.separator
				, "^((?!launcher).)*$", "ant.*\\.jar");

		// if a symlink do not exist, create it (we would have both jars into the same folder)
		try {
			findFile(antLibPath + File.separator + "ant-launcher" + File.separator
					, "^((?!launcher).)*$", "ant.*\\.jar");
		} catch(FileNotFoundException e){
			createSymLink(antJar.getPath(), antLauncherJar.getParent());
		}
	}

	private static File findFile(String folderPath, String exclude, String match)throws FileNotFoundException {
		return FileUtils.listFiles(new File(folderPath), null, true)
				.stream()
				.filter(file -> file.getName().matches(exclude))
				.filter(file -> file.getName().matches(match)&& ! file.getName().contains("sources.jar"))
				.findFirst().orElseThrow(FileNotFoundException::new);
	}

	private static void createSymLink(String filePath, String folderPath)throws IOException {
		java.nio.file.Files.createSymbolicLink(
				new File(folderPath + File.separator + new File(filePath).getName()).toPath(),
				new File(filePath).toPath());
	}

}
