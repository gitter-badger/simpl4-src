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
package org.ms123.common.system.compile;
import java.util.List;
import java.io.File;
import java.io.IOException;
import java.util.Map;
import org.osgi.framework.Bundle;
import org.ms123.common.rpc.RpcException;

public interface CompileService {
	public  void	compileGroovy(String namespace,String path,String code);
	public void compileGroovyAll();
	public List<Map> compileGroovyNamespace(String namespace);

	public  void	compileJava(String namespace,String path,String code);
	public  void	compileJava(String namespace,String path,String code,Bundle bundle);
	public void compileJavaAll();
	public List<Map> compileJavaNamespace(String namespace);
	public List<Map> compileJavaNamespace(String namespace, Bundle bundle);
	public void compileJava(Bundle bundle, File destinationDirectory, File compileDirectory, List<File> classPath) throws IOException;
}
