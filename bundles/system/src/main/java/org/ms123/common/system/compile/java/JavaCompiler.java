/**
 * This file is part of SIMPL4(http://simpl4.org).
 *
 * 	Copyright [2017] [Manfred Sattler] <manfred@ms123.org>
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *  http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

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
import org.osgi.framework.Bundle;
import org.osgi.framework.wiring.BundleWiring;
import javax.tools.StandardJavaFileManager;
import javax.tools.StandardLocation;
import org.ms123.common.libhelper.FileSystemClassLoader;
import org.osgi.framework.wiring.BundleWiring;


/**
 * Created by trung on 5/3/15.
 */
@SuppressWarnings({"unchecked","deprecation"})
public class JavaCompiler {
	static javax.tools.JavaCompiler javac = ToolProvider.getSystemJavaCompiler();

	private static String workspace   = System.getProperty("workspace");
	private static String gitRepos   = System.getProperty("git.repos");
	public static Class<?> compile(String namespace, Bundle bundle, String className, String sourceCodeInText) throws Exception {
		File[] locations = new File[3];
		locations[0] = new File(workspace, "jooq/build");
		locations[1] = new File(gitRepos, namespace+ "/.etc/jooq/build");
		locations[2] = new File(System.getProperty("workspace") + "/" + "java" + "/" + namespace);
		SourceCode sourceCode = new SourceCode(className, sourceCodeInText);
		CompiledCode compiledCode = new CompiledCode(className);
		Iterable<? extends JavaFileObject> compilationUnits = Arrays.asList(sourceCode);

		ClassLoader loader = bundle.adapt(BundleWiring.class).getClassLoader();
		loader = new FileSystemClassLoader(loader, locations);
		DynamicClassLoader cl = new DynamicClassLoader(loader);
		DiagnosticCollector<JavaFileObject> diagnostics = new DiagnosticCollector<JavaFileObject>();
		ExtendedStandardJavaFileManager fileManager = new ExtendedStandardJavaFileManager(javac.getStandardFileManager(diagnostics, null, null), locations, compiledCode, cl);

    BundleJavaManager bundleFileManager = new BundleJavaManager( bundle, fileManager);
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

		DiagnosticCollector<JavaFileObject> diagnostics = new DiagnosticCollector<JavaFileObject>();
		StandardJavaFileManager standardFileManager = javac.getStandardFileManager(diagnostics, null, null);
		JavaFileObject[] sourceFiles = { new StringJavaFileObject(className, sourceCodeInText)};
		standardFileManager.setLocation(StandardLocation.CLASS_OUTPUT, Arrays.asList(destinationDirectory)); 

		List<File> classPath = new ArrayList<File>(); 
		classPath.add( new File(workspace, "jooq/build"));
		classPath.add( new File(gitRepos, namespace+ "/.etc/jooq/build"));
		classPath.add( new File(System.getProperty("workspace") + "/" + "java" + "/" + namespace));

		standardFileManager.setLocation( StandardLocation.CLASS_PATH, classPath);

		BundleJavaManager	bundleJavaManager = new BundleJavaManager( bundle, standardFileManager);

		javax.tools.JavaCompiler.CompilationTask compilationTask = javac.getTask( null, bundleJavaManager, diagnostics, options, null, Arrays.asList(sourceFiles));
		standardFileManager.close();

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

