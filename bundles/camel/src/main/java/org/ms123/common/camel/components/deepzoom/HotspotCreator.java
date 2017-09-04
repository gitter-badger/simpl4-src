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
package org.ms123.common.camel.components.deepzoom;

import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStreamWriter;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import org.dom4j.DocumentHelper;
import org.dom4j.Element;
import org.dom4j.io.OutputFormat;
import org.dom4j.io.XMLWriter;
import static com.jcabi.log.Logger.error;
import static com.jcabi.log.Logger.info;
import static org.apache.commons.io.FilenameUtils.getBaseName;

@SuppressWarnings({ "unchecked", "deprecation" })
public class HotspotCreator {

	public HotspotCreator() {
	}

	public boolean process(String pdfFile, String outdir, String regex, double factor) {
		info(this, "pdfFile:" + pdfFile);
		info(this, "Outdir:" + outdir);
		info(this, "Regex:" + regex);
		info(this, "Factor:" + factor);
		try {
			List<PDFExtractor.StringObject> soList = PDFExtractor.getStringObjects(pdfFile, regex, 1);
			String baseName = getBaseName(pdfFile);
			generateMap(soList, Paths.get(outdir, baseName + ".xml").toString(), baseName, factor);
		} catch (Exception e) {
			error(this, "process.error:%[exception]s", e);
			throw new RuntimeException("HotspotCreator", e);
		}
		return true;
	}

	private void generateMap(List so_list, String mapfile, String name, double factor) throws Exception {
		org.dom4j.Document document = DocumentHelper.createDocument();
		Element map = document.addElement("MAP").addAttribute("name", name);
		if (so_list != null) {
			Iterator it = so_list.iterator();
			while (it.hasNext()) {
				PDFExtractor.StringObject so = (PDFExtractor.StringObject) it.next();
				Element area = map.addElement("AREA");
				area.addAttribute("SHAPE", "RECT");
				java.awt.geom.Rectangle2D.Float r = so.getLocation();
				int x1 = (int) (r.getX() * factor);
				int y1 = (int) (r.getY() * factor);
				int x2 = (int) Math.round((r.getX() + r.getWidth()) * factor);
				int y2 = (int) Math.round((r.getY() + r.getHeight()) * factor);
				if (so.isRotated()) {
					area.addAttribute("COORDS", y1 + "," + x1 + "," + y2 + "," + x2);
				} else {
					area.addAttribute("COORDS", x1 + "," + y1 + "," + x2 + "," + y2);
				}
				area.addAttribute("HREF", so.getText());
			}
		}
		writeXML(document, mapfile);
	}

	private void writeXML(org.dom4j.Document document, String ofile) throws Exception {
		OutputFormat format = OutputFormat.createPrettyPrint();
		format.setEncoding("UTF-8");
		OutputStreamWriter osw = new OutputStreamWriter(new FileOutputStream(ofile), "UTF-8");

		XMLWriter writer = new XMLWriter(osw, format);
		writer.write(document);
		writer.close();
	}

}

