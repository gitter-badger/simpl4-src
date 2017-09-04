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
package org.ms123.admin;


import com.beust.jcommander.*;
import java.util.*;
 

public class Main {

	private static String[] commands = { "ExecuteScript", "GenerateClasses", "CreateWorkspace" };

	public static void main(String args[]) {
		if (args.length < 2) {
			Main.glocalHelp();
			System.exit(1);
		}
		String[] argsNew = new String[args.length - 1];
		Main.copyArgs(args, argsNew);
		AbstractCommand esc = null;
		for( String command : Main.commands ){
			if (args[0].toLowerCase().equals(command.toLowerCase())) {
				try{
					Class c = Class.forName( "org.ms123.admin."+command+"Command" );
					esc = (AbstractCommand)c.newInstance();	
				}catch( Exception e){
					e.printStackTrace();
					System.exit(2);
				}
				break;
			}
		}
		if (esc != null) {
			JCommander jc = new JCommander(esc, argsNew);
			if (esc.help) {
				jc.usage();
			} else {
				esc.execute();
			}
		} else {
			Main.glocalHelp();
			System.exit(1);
		}
	}

	private static void glocalHelp(){
			StringBuffer sb = new StringBuffer();
			sb.append("[");
			String sep="";
			for( String command : Main.commands ){
				sb.append( sep + command.toLowerCase() );
				sep = "|";
			}
			sb.append("]");
			System.out.println("Usage:sw "+sb.toString()+"  options");
	}
	private static void copyArgs(String[] args, String[] argsNew) {
		for (int i = 0; i < argsNew.length; ++i) {
			argsNew[i] = args[i + 1];
		}
	}
}
