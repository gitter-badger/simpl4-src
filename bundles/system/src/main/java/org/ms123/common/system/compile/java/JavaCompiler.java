package org.ms123.common.system.compile.java;

import javax.tools.JavaFileObject;
import javax.tools.ToolProvider;
import javax.tools.DiagnosticCollector;
import javax.tools.Diagnostic;
import java.util.Arrays;
import java.util.List;
import java.util.ArrayList;
import java.io.File;
import static org.apache.commons.io.FileUtils.writeByteArrayToFile;
import static org.apache.commons.io.FileUtils.readFileToString;
import org.phidias.compile.BundleJavaManager;
import org.osgi.framework.Bundle;
import org.osgi.framework.wiring.BundleWiring;
import javax.tools.StandardJavaFileManager;
import javax.tools.StandardLocation;
import org.phidias.compile.StringJavaFileObject;

/**
 * Created by trung on 5/3/15.
 */
@SuppressWarnings({"unchecked","deprecation"})
public class JavaCompiler {
	static javax.tools.JavaCompiler javac = ToolProvider.getSystemJavaCompiler();


	private static String workspace   = System.getProperty("workspace");
	private static String gitRepos   = System.getProperty("git.repos");
	public static Class<?> compile(String namespace, Bundle bundle, String className, String sourceCodeInText) throws Exception {
		File[] locations = new File[2];
		locations[0] = new File(workspace, "jooq/build");
		locations[1] = new File(gitRepos, namespace+ "/.etc/jooq/build");
		SourceCode sourceCode = new SourceCode(className, sourceCodeInText);
		CompiledCode compiledCode = new CompiledCode(className);
		Iterable<? extends JavaFileObject> compilationUnits = Arrays.asList(sourceCode);

		ClassLoader loader = bundle.adapt(BundleWiring.class).getClassLoader();
		DynamicClassLoader cl = new DynamicClassLoader(loader);
		DiagnosticCollector<JavaFileObject> diagnostics = new DiagnosticCollector<JavaFileObject>();
		ExtendedStandardJavaFileManager fileManager = new ExtendedStandardJavaFileManager(javac.getStandardFileManager(diagnostics, null, null), locations, compiledCode, cl);

 // the OSGi aware file manager
    BundleJavaManager bundleFileManager = new BundleJavaManager( bundle, fileManager, null);

		javax.tools.JavaCompiler.CompilationTask task = javac.getTask(null, bundleFileManager, diagnostics, null, null, compilationUnits);
		fileManager.close();
		if (task.call()) {
			return cl.loadClass(className);
		} else {
			StringBuffer error = new StringBuffer();
			for (Diagnostic diagnostic : diagnostics.getDiagnostics()) {
				error.append(diagnostic.toString());
			}
			throw new Exception(error.toString());
		}
	}

	public static void compile(String namespace, Bundle bundle, String className, File sourceFile, File destinationDirectory) throws Exception {
		compile(namespace, bundle, className, readFileToString(sourceFile), destinationDirectory);
	}

	public static void compile(String namespace, Bundle bundle, String className, String sourceCodeInText, File destinationDirectory) throws Exception {
		List<String> options = new ArrayList<String>();
		options.add("-verbose"); 

		DiagnosticCollector<JavaFileObject> diagnostics = new DiagnosticCollector<JavaFileObject>();
		StandardJavaFileManager standardFileManager = javac.getStandardFileManager(diagnostics, null, null);
		JavaFileObject[] sourceFiles = { new StringJavaFileObject(className, sourceCodeInText)};
		standardFileManager.setLocation(StandardLocation.CLASS_OUTPUT, Arrays.asList(destinationDirectory)); 

		List<File> classPath = new ArrayList<File>(); 
		classPath.add( new File(workspace, "jooq/build"));
		classPath.add( new File(gitRepos, namespace+ "/.etc/jooq/build"));

		standardFileManager.setLocation( StandardLocation.CLASS_PATH, classPath);

		BundleJavaManager bundleFileManager = new BundleJavaManager( bundle, standardFileManager, options);
		javax.tools.JavaCompiler.CompilationTask compilationTask = javac.getTask( null, bundleFileManager, diagnostics, options, null, Arrays.asList(sourceFiles));
		bundleFileManager.close();

		// perform the actual compilation
		if (compilationTask.call()) {
			// Success!
			return;
		}

		StringBuffer error = new StringBuffer();
		for (Diagnostic<?> dm: diagnostics.getDiagnostics()) {
			error.append(dm.toString());
		}
		throw new Exception(error.toString());
	}
}

