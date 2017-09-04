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
package org.ms123.common.system.script;


import java.io.Reader;

import javax.script.Bindings;
import javax.script.ScriptContext;
import javax.script.ScriptEngine;
import javax.script.ScriptEngineFactory;
import javax.script.ScriptException;
import javax.script.Compilable;
import javax.script.Invocable;
import javax.script.CompiledScript;


public class OSGiScriptEngine implements ScriptEngine, Compilable, Invocable {
	private ScriptEngine engine;
	private OSGiScriptEngineFactory factory;
	public OSGiScriptEngine(ScriptEngine engine, OSGiScriptEngineFactory factory) {
		this.engine = engine;
		this.factory = factory;
	}

	public Bindings createBindings() {
		return engine.createBindings();
	}

	public Object eval(Reader reader, Bindings n) throws ScriptException {
		return engine.eval(reader, n);
	}

	public Object eval(Reader reader, ScriptContext context) throws ScriptException {
		return engine.eval(reader, context);
	}

	public Object eval(Reader reader) throws ScriptException {
		return engine.eval(reader);
	}

	public Object eval(String script, Bindings n) throws ScriptException {
		return engine.eval(script, n);
	}

	public Object eval(String script, ScriptContext context) throws ScriptException {
		return engine.eval(script, context);
	}

	public Object eval(String script) throws ScriptException {
		return engine.eval(script);
	}

	public Object get(String key) {
		return engine.get(key);
	}

	public Bindings getBindings(int scope) {
		return engine.getBindings(scope);
	}

	public ScriptContext getContext() {
		return engine.getContext();
	}

	public ScriptEngineFactory getFactory() {
		return factory;
	}

	public void put(String key, Object value) {
		engine.put(key, value);
	}

	public void setBindings(Bindings bindings, int scope) {
		engine.setBindings(bindings, scope);
	}

	public void setContext(ScriptContext context) {
		engine.setContext(context);
	}

	public CompiledScript	compile(Reader script) throws ScriptException{
		return ((Compilable)engine).compile(script);
	}
	public CompiledScript	compile(String script) throws ScriptException{
		return ((Compilable)engine).compile(script);
	}
	public <T> T	getInterface(Class<T> clasz){
		return ((Invocable)engine).getInterface(clasz);
	}
	public 	<T> T	getInterface(Object thiz, Class<T> clasz){
		return ((Invocable)engine).getInterface(thiz, clasz);
	}
	public 	Object	invokeFunction(String name, Object... args) throws ScriptException, NoSuchMethodException{
		return ((Invocable)engine).invokeFunction(name, args);
	}
	public 	Object	invokeMethod(Object thiz, String name, Object... args) throws ScriptException, NoSuchMethodException{
		return ((Invocable)engine).invokeMethod(thiz, name, args);
	}

}
