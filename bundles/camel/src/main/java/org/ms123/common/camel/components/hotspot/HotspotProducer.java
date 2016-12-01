/**
 * This file is part of SIMPL4(http://simpl4.org).
 *
 * 	Copyright [2014] [Manfred Sattler] <manfred@ms123.org>
 *
 * SIMPL4 is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * SIMPL4 is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with SIMPL4.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.ms123.common.camel.components.hotspot;

import org.apache.camel.AsyncCallback;
import org.apache.camel.Exchange;
import org.apache.camel.InvalidPayloadRuntimeException;
import org.apache.camel.impl.DefaultAsyncProducer;
import org.apache.camel.util.ExchangeHelper;
import org.apache.camel.util.MessageHelper;
import org.apache.camel.Message;
import org.ms123.common.camel.api.ExchangeUtils;
import static com.jcabi.log.Logger.info;
import static com.jcabi.log.Logger.error;
import java.util.HashMap;
import java.util.Map;
import java.util.List;
import java.util.Iterator;
import java.io.File;
import java.io.OutputStreamWriter;
import java.io.FileOutputStream;
import org.dom4j.DocumentHelper;
import org.dom4j.Element;
import org.dom4j.io.OutputFormat;
import org.dom4j.io.XMLWriter;
import static org.apache.commons.io.FilenameUtils.getBaseName;

@SuppressWarnings({ "unchecked", "deprecation" })
public class HotspotProducer extends DefaultAsyncProducer {

	public HotspotProducer(HotspotEndpoint endpoint) {
		super(endpoint);
	}

	@Override
	public HotspotEndpoint getEndpoint() {
		return (HotspotEndpoint) super.getEndpoint();
	}

	@Override
	public boolean process(Exchange exchange, AsyncCallback callback) {
		String input = getEndpoint().getInputpath();
		String outfile = getEndpoint().getOutputfile();
		String regex = getEndpoint().getRegex();
		info(this, "Input:" + input);
		info(this, "Outdir:" + outfile);
		info(this, "Regex:" + regex);
		input = ExchangeUtils.getParameter(input, exchange, String.class, "inputpath");
		outfile = ExchangeUtils.getParameter(outfile, exchange, String.class, "outputfile");
		regex = ExchangeUtils.getParameter(regex, exchange, String.class, "regex");
		info(this, "Input2:" + input);
		info(this, "Outfile2:" + outfile);
		info(this, "Regex2:" + regex);
		try {
			List<PDFExtractor.StringObject> soList = PDFExtractor.getStringObjects(input, regex, 1);
			generateMap(soList, outfile, getBaseName(input), 0.5);
		} catch (Exception e) {
			error(this, "process.error:%[exception]s", e);
			throw new RuntimeException("HotspotProducer",e);
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

