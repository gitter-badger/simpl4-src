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
package org.ms123.common.git;

import java.util.List;
import java.io.FileInputStream;
import java.io.File;
import java.io.BufferedReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Dictionary;
import java.util.Hashtable;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Date;
import java.util.Locale;
import java.util.Calendar;
import java.util.Iterator;
import java.util.TimeZone;
import java.text.SimpleDateFormat;
import static org.apache.commons.io.FileUtils.writeStringToFile;
import static org.apache.commons.io.FileUtils.readFileToString;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.nio.*;
import java.io.*;
import java.nio.file.*;
import java.nio.charset.*;
import java.nio.file.attribute.*;

@SuppressWarnings({"unchecked","deprecation"})
public class FileObject {

	private Path m_path = null;

	public FileObject(File file) throws IOException{
		this(file,null);
	}
	public FileObject(File file, String type) throws IOException{
		m_path = file.toPath();
		if( file.exists() && getType() == null){
			convertFile();
		}
		if( type != null){
			setType(type);
		}
	}

	private final File getFile() {
		return m_path.toFile();
	}

	private final Path getPath() {
		return m_path;
	}

	public String getType(){
		try{
			return getString("sw.type");
		}catch(Exception e){
			return null;
		}
	}
	public void setType(String type)  throws IOException{
		setString("sw.type",type);
	}

	public void setString(String key,String value) throws IOException{
		System.out.println("setString:"+key+"="+value);
		Path path = getPath();
		UserDefinedFileAttributeView view = Files.getFileAttributeView(path, UserDefinedFileAttributeView.class);
		view.write(key, Charset.defaultCharset().encode(value));
	}

	public String getString(String key) throws IOException{
		Path path = getPath();
		UserDefinedFileAttributeView view = Files.getFileAttributeView(path, UserDefinedFileAttributeView.class);
		ByteBuffer bb = ByteBuffer.allocateDirect(64);
		int num = view.read(key,bb);
		bb.flip();
		return Charset.defaultCharset().decode(bb).toString();
	}

	public String getContent() throws IOException {
		return readFileToString(getFile());
	}

	public void putContent(String content) throws IOException {
		writeStringToFile(getFile(),content);
	}

	public void putContent(String type, String content) throws IOException {
		writeStringToFile(getFile(),content);
		setType(type);
	}

	@Override
	public String toString() {
		return "[" + getFile().getPath()+"/"+getType() + "]";
	}


	private void convertFile(){
		FileHolder fr = new FileHolder(getFile());
		try{
			String type = fr.getType();
			String content = fr.getContent();
			putContent(type,content);
		}catch(Exception e){
			throw new RuntimeException("cannot convert '"+getFile().toString()+"':"+e.getMessage());
		}
	}
}
