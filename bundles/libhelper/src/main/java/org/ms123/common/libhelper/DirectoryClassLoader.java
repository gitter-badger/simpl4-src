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
package org.ms123.common.libhelper;

import java.io.File;
import java.io.*;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.URL;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;
import java.util.jar.JarEntry;
import java.util.jar.JarInputStream;
import java.util.jar.Manifest;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import static com.jcabi.log.Logger.error;
import static com.jcabi.log.Logger.debug;
import static org.apache.commons.io.FileUtils.readFileToByteArray;

public class DirectoryClassLoader extends ClassLoader {

	private Map<String, byte[]> resourceStore = new HashMap<String,byte[]>();
	private File directory;


	public DirectoryClassLoader(ClassLoader parent, File directory) {
		super(parent);
		this.directory=directory;
		init();
	}

	private void init() {
		try{
			File[] files = this.directory.listFiles();
			if( files == null){
				return;
			}
			resourceStore = new HashMap<>();
			for (File file : files) {
				if (file.isDirectory()){
					continue;
				}

				byte[] b = readFileToByteArray(file);
				InputStream is = new ByteArrayInputStream(b);
				initJarFile(is);
			}

			debug(this, "DirectoryClassLoader was initalized."+resourceStore );
		}catch(Exception e){
			error(this,"init:%[exception]s", e);
			e.printStackTrace();
		}

	}

	@Override
	public URL getResource(String name) {
		return super.getResource(name);
	}

	@Override
	public Enumeration<URL> getResources(String name) throws IOException {
		return super.getResources(name); //To change body of generated methods, choose Tools | Templates.
	}

	@Override
	public InputStream getResourceAsStream(String name) {
		debug(this, "DirectoryClassLoader.getResourceAsStream:'"+ name);
		if (resourceStore.get(name) == null){
			return super.getResourceAsStream(name);
		}
		byte[] b = resourceStore.get(name);
		return new ByteArrayInputStream(b);
	}

	@Override
	protected Class<? extends Object> findClass(String name) throws ClassNotFoundException {
		debug(this, "DirectoryClassLoader.findClass:"+ name);

		String resourceName = name.replace('.', '/') + ".class";

		byte[] b = resourceStore.get(resourceName);
		if (b == null){
			init();
			b = resourceStore.get(resourceName);
		}
		if (b == null){
			throw new ClassNotFoundException(name);
		}

		Class<? extends Object> clazz = defineClass(name, b, 0, b.length);
		return clazz;
	}

	private byte[] getByteArrayFromZip(ZipInputStream jis) throws IOException {
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		byte[] b = new byte[8192];
		for (int len = 0; len != -1;) {
			len = jis.read(b);
			if (len != -1)
				baos.write(b, 0, len);
		}
		baos.flush();
		baos.close();
		//FileOutputStream fos = new FileOutputStream("D:\\testjar\\" + javaName.substring(javaName.lastIndexOf(".")+1) + ".class");
		//fos.write(baos.toByteArray());
		//fos.flush();
		//fos.close();
		return baos.toByteArray();
	}
	private void initJarFile(InputStream is) throws IOException {
		JarInputStream jis = new JarInputStream(is);

		JarEntry je;
		while ((je = jis.getNextJarEntry()) != null) {
			if (!je.isDirectory()) {
				String resourceName = je.getName();

				if (resourceStore.containsKey(resourceName)){
					continue;
				}
				byte[] b = getByteArrayFromZip(jis);
				resourceStore.put(resourceName, b);
			}
		}
	}
}

