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

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.io.StringWriter;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.math.BigDecimal;
import java.util.Properties;
import java.util.Vector;
import java.awt.Rectangle;
import java.awt.geom.*;

import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.text.PDFTextStripper;
import org.apache.pdfbox.text.TextPosition;
import org.apache.pdfbox.text.TextPositionComparator;

/**
 */
public class PDFExtractor extends PDFTextStripper {

	private static final float ENDOFLASTTEXTX_RESET_VALUE = -1;
	private static final float MAXYFORLINE_RESET_VALUE = -Float.MAX_VALUE;
	private static final float EXPECTEDSTARTOFNEXTWORDX_RESET_VALUE = -Float.MAX_VALUE;
	private static final float MAXHEIGHTFORLINE_RESET_VALUE = -1;
	private static final float MINYTOPFORLINE_RESET_VALUE = Float.MAX_VALUE;
	private static final float LASTWORDSPACING_RESET_VALUE = -1;
	protected List stringObjects;
	protected Pattern pattern;
	protected boolean prevMethod = false;

	/**
	 * Default constructor.
	 *
	 * @throws IOException If there is an error constructing this class.
	 */
	public PDFExtractor() throws IOException {
		super();
		this.setSortByPosition(true);
		this.setShouldSeparateByBeads(false);
		this.stringObjects = new ArrayList();
	}

	public static void main(String[] args) throws Exception {
		List ret = PDFExtractor.getStringObjects("P3.pdf", "[0-9]{1,4}", 1);
		System.out.println("ret:" + ret);

	}

	public static List<StringObject> getStringObjects(String file, String regex, int startpage) throws Exception {
		return getStringObjects(file, regex, startpage, false);
	}

	public static List<StringObject> getStringObjects(String file, String regex, int startpage, boolean prev) throws Exception {
		PDDocument document = null;
		PDFExtractor ms = new PDFExtractor();
		try {
			ms.prevMethod = prev;
			ms.setStartPage(startpage);
			ms.setEndPage(startpage);
			document = PDDocument.load(new java.io.File(file));
			StringWriter sw = new StringWriter();
			ms.pattern = Pattern.compile(regex);
			ms.writeText(document, sw);
		} finally {
			if (document != null) {
				document.close();
			}
		}
		return ms.stringObjects;
	}

	/**
	 * This will print the text of the processed page to "output".
	 * It will estimate, based on the coordinates of the text, where
	 * newlines and word spacings should be placed. The text will be
	 * sorted only if that feature was enabled. 
	 *
	 * @throws IOException If there is an error writing the text.
	 */
	protected void writePage() throws IOException {
		float maxYForLine = MAXYFORLINE_RESET_VALUE;
		float minYTopForLine = MINYTOPFORLINE_RESET_VALUE;
		float endOfLastTextX = ENDOFLASTTEXTX_RESET_VALUE;
		float lastWordSpacing = LASTWORDSPACING_RESET_VALUE;
		float maxHeightForLine = MAXHEIGHTFORLINE_RESET_VALUE;
		PositionWrapper lastPosition = null;
		PositionWrapper lastLineStartPosition = null;
		System.err.println("writePage");

		boolean startOfPage = true; // flag to indicate start of page
		boolean startOfArticle = true;

		if (charactersByArticle.size() > 0) {
			writePageStart();
		}

		for (int i = 0; i < charactersByArticle.size(); i++) {
			List<TextPosition> textList = charactersByArticle.get(i);

			if (getSortByPosition()) {
				TextPositionComparator comparator = new TextPositionComparator();

				Collections.sort(textList, comparator);
			}

			Iterator<TextPosition> textIter = textList.iterator();

			/* Before we can display the text, we need to do some normalizing.
			 * Arabic and Hebrew text is right to left and is typically stored
			 * in its logical format, which means that the rightmost character is
			 * stored first, followed by the second character from the right etc.
			 * However, PDF stores the text in presentation form, which is left to
			 * right.  We need to do some normalization to convert the PDF data to
			 * the proper logical output format.
			 *
			 * Note that if we did not sort the text, then the output of reversing the
			 * text is undefined and can sometimes produce worse output then not trying
			 * to reverse the order.  Sorting should be done for these languages.
			 * */

			/* First step is to determine if we have any right to left text, and
			 * if so, is it dominant. */
			int ltrCnt = 0;
			int rtlCnt = 0;

			while (textIter.hasNext()) {
				TextPosition position = (TextPosition) textIter.next();
				String stringValue = position.getUnicode();

				for (int a = 0; a < stringValue.length(); a++) {
					byte dir = Character.getDirectionality(stringValue.charAt(a));

					if ((dir == Character.DIRECTIONALITY_LEFT_TO_RIGHT) || (dir == Character.DIRECTIONALITY_LEFT_TO_RIGHT_EMBEDDING) || (dir == Character.DIRECTIONALITY_LEFT_TO_RIGHT_OVERRIDE)) {
						ltrCnt++;
					} else if ((dir == Character.DIRECTIONALITY_RIGHT_TO_LEFT) || (dir == Character.DIRECTIONALITY_RIGHT_TO_LEFT_ARABIC) || (dir == Character.DIRECTIONALITY_RIGHT_TO_LEFT_EMBEDDING) || (dir == Character.DIRECTIONALITY_RIGHT_TO_LEFT_OVERRIDE)) {
						rtlCnt++;
					}
				}
			}

			// choose the dominant direction
			boolean isRtlDominant = rtlCnt > ltrCnt;

			startArticle(!isRtlDominant);
			startOfArticle = true;
			// we will later use this to skip reordering
			boolean hasRtl = rtlCnt > 0;

			/* Now cycle through to print the text.
			 * We queue up a line at a time before we print so that we can convert
			 * the line from presentation form to logical form (if needed). 
			 */
			List<TextPosition> line = new ArrayList<TextPosition>();

			textIter = textList.iterator(); // start from the beginning again

			/* PDF files don't always store spaces. We will need to guess where we should add
			 * spaces based on the distances between TextPositions. Historically, this was done
			 * based on the size of the space character provided by the font. In general, this worked
			 * but there were cases where it did not work. Calculating the average character width
			 * and using that as a metric works better in some cases but fails in some cases where the
			 * spacing worked. So we use both. NOTE: Adobe reader also fails on some of these examples.
			 */
			// Keeps track of the previous average character width
			float previousAveCharWidth = -1;

			while (textIter.hasNext()) {
				TextPosition position = (TextPosition) textIter.next();
				PositionWrapper current = new PositionWrapper(position);
				String characterValue = position.getUnicode();

				// Resets the average character width when we see a change in font
				// or a change in the font size
				if (lastPosition != null && ((position.getFont() != lastPosition.getTextPosition().getFont()) || (position.getFontSize() != lastPosition.getTextPosition().getFontSize()))) {
					previousAveCharWidth = -1;
				}

				float positionX;
				float positionY;
				float positionWidth;
				float positionHeight;

				/* If we are sorting, then we need to use the text direction
				 * adjusted coordinates, because they were used in the sorting. */
				if (getSortByPosition()) {
					positionX = position.getXDirAdj();
					positionY = position.getYDirAdj();
					positionWidth = position.getWidthDirAdj();
					positionHeight = position.getHeightDir();
				} else {
					positionX = position.getX();
					positionY = position.getY();
					positionWidth = position.getWidth();
					positionHeight = position.getHeight();
				}

				// The current amount of characters in a word
				int wordCharCount = position.getIndividualWidths().length;

				/* Estimate the expected width of the space based on the
				 * space character with some margin. */
				float wordSpacing = position.getWidthOfSpace();
				float deltaSpace = 0;

				if ((wordSpacing == 0) || (wordSpacing == Float.NaN)) {
					deltaSpace = Float.MAX_VALUE;
				} else {
					if (lastWordSpacing < 0) {
						deltaSpace = (wordSpacing * getSpacingTolerance());
					} else {
						deltaSpace = (((wordSpacing + lastWordSpacing) / 2f) * getSpacingTolerance());
					}
				}

				/* Estimate the expected width of the space based on the
				 * average character width with some margin. This calculation does not
				 * make a true average (average of averages) but we found that it gave the
				 * best results after numerous experiments. Based on experiments we also found that
				 * .3 worked well. */
				float averageCharWidth = -1;

				if (previousAveCharWidth < 0) {
					averageCharWidth = (positionWidth / wordCharCount);
				} else {
					averageCharWidth = (previousAveCharWidth + (positionWidth / wordCharCount)) / 2f;
				}
				float deltaCharWidth = (averageCharWidth * getAverageCharTolerance());

				// Compares the values obtained by the average method and the wordSpacing method and picks
				// the smaller number.
				float expectedStartOfNextWordX = EXPECTEDSTARTOFNEXTWORDX_RESET_VALUE;

				if (endOfLastTextX != ENDOFLASTTEXTX_RESET_VALUE) {
					if (deltaCharWidth > deltaSpace) {
						expectedStartOfNextWordX = endOfLastTextX + deltaSpace;
					} else {
						expectedStartOfNextWordX = endOfLastTextX + deltaCharWidth;
					}
				}

				if (lastPosition != null) {
					//System.out.println("wordSpacing:"+wordSpacing+"/deltaSpace:"+deltaSpace+"/expectedStartOfNextWordX:"+expectedStartOfNextWordX+"/positionX:"+positionX+"/"+lastPosition.getTextPosition().getUnicode());
					if (startOfArticle) {
						lastPosition.setArticleStart();
						startOfArticle = false;
					}
					// RDD - Here we determine whether this text object is on the current
					// line.  We use the lastBaselineFontSize to handle the superscript
					// case, and the size of the current font to handle the subscript case.
					// Text must overlap with the last rendered baseline text by at least
					// a small amount in order to be considered as being on the same line.

					/* XXX BC: In theory, this check should really check if the next char is in full range
					 * seen in this line. This is what I tried to do with minYTopForLine, but this caused a lot
					 * of regression test failures.  So, I'm leaving it be for now. */
					// System.out.println("positionY:"+round(positionY)+",positionHeight:"+round(positionHeight)+",maxYForLine:"+round(maxYForLine)+",maxHeightForLine:"+round(maxHeightForLine)+",minYTopForLine:"+round(minYTopForLine)+",line:"+line);
					if (!overlap(positionY, positionHeight, maxYForLine, maxHeightForLine)) {
						writeLine(normalize(line, isRtlDominant, hasRtl), isRtlDominant);
						line.clear();

						lastLineStartPosition = handleLineSeparation(current, lastPosition, lastLineStartPosition, maxHeightForLine);

						endOfLastTextX = ENDOFLASTTEXTX_RESET_VALUE;
						expectedStartOfNextWordX = EXPECTEDSTARTOFNEXTWORDX_RESET_VALUE;
						maxYForLine = MAXYFORLINE_RESET_VALUE;
						maxHeightForLine = MAXHEIGHTFORLINE_RESET_VALUE;
						minYTopForLine = MINYTOPFORLINE_RESET_VALUE;
					}

					// Test if our TextPosition starts after a new word would be expected to start.
					if (expectedStartOfNextWordX != EXPECTEDSTARTOFNEXTWORDX_RESET_VALUE && expectedStartOfNextWordX < positionX && // only bother adding a space if the last character was not a space
							lastPosition.getTextPosition().getUnicode() != null && !lastPosition.getTextPosition().getUnicode().endsWith(" ")) {
						// line.add(WordSeparator.getSeparator());
						writeLine(normalize(line, isRtlDominant, hasRtl), isRtlDominant);
						line.clear();

						lastLineStartPosition = handleLineSeparation(current, lastPosition, lastLineStartPosition, maxHeightForLine);

						endOfLastTextX = ENDOFLASTTEXTX_RESET_VALUE;
						expectedStartOfNextWordX = EXPECTEDSTARTOFNEXTWORDX_RESET_VALUE;
						maxYForLine = MAXYFORLINE_RESET_VALUE;
						maxHeightForLine = MAXHEIGHTFORLINE_RESET_VALUE;
						minYTopForLine = MINYTOPFORLINE_RESET_VALUE;
					}
				}

				// if (positionY >= maxYForLine)
				// {
				maxYForLine = positionY;
				// }

				// RDD - endX is what PDF considers to be the x coordinate of the
				// end position of the text.  We use it in computing our metrics below.
				endOfLastTextX = positionX + positionWidth;

				// add it to the list
				if (characterValue != null) {
					if (startOfPage && lastPosition == null) {
						writeParagraphStart(); // not sure this is correct for RTL?
					}
					line.add(position);
				}
				maxHeightForLine = Math.max(maxHeightForLine, positionHeight);
				minYTopForLine = Math.min(minYTopForLine, positionY - positionHeight);
				lastPosition = current;
				if (startOfPage) {
					lastPosition.setParagraphStart();
					lastPosition.setLineStart();
					lastLineStartPosition = lastPosition;
					startOfPage = false;
				}
				lastWordSpacing = wordSpacing;
				previousAveCharWidth = averageCharWidth;
			}

			// print the final line

			if (line.size() > 0) {
				writeLine(normalize(line, isRtlDominant, hasRtl), isRtlDominant);
				writeParagraphEnd();
			}

			endArticle();
		}
		writePageEnd();
	}

	/**
	 * Normalize the given list of TextPositions.
	 * @param line list of TextPositions
	 * @param isRtlDominant determines if rtl or ltl is dominant 
	 * @param hasRtl determines if lines contains rtl formatted text(parts)
	 * @return a list of strings, one string for every word
	 */
	private List<String> normalize(List<TextPosition> line, boolean isRtlDominant, boolean hasRtl) {
		if (prevMethod) {
			return _normalize(line, isRtlDominant, hasRtl);
		}
		LinkedList<String> normalized = new LinkedList<String>();
		StringBuilder lineBuilder = new StringBuilder();
		TextPosition last = line.get(0);

		for (TextPosition text : line) {
			last = text;
			lineBuilder.append(text.getUnicode());
		}
		if (lineBuilder.length() > 0) {
			String lineStr = lineBuilder.toString();
			normalized.add(lineStr);
		}

		System.err.println("normalized:" + normalized + "/" + line.size());
		Matcher matcher = pattern.matcher(normalized.get(0));
		while (matcher.find()) {
			System.err.printf("%s an Position [%d,%d]%n", matcher.group(), matcher.start(), matcher.end());
			int start = matcher.start();
			int end = matcher.end();
			String text = matcher.group();
			float x1 = line.get(start).getX();
			float y1 = line.get(start).getY();
			float x2 = line.get(end - 1).getX();
			float y2 = line.get(end - 1).getY();
			float h = line.get(end - 1).getHeight();
			float w = line.get(end - 1).getWidth();
			float h1 = line.get(start).getHeight();
			float w1 = line.get(start).getWidth();
			float ht = 0;
			for (int i = start; i < end; i++) {
				ht = Math.max(ht, line.get(i).getHeight());
			}

			StringObject so = new StringObject(text, x1, y1 - ht, w + (x2 - x1), ht, line.get(start).isRotated());
			stringObjects.add(so);
		}
		return normalized;
	}

	private List<String> _normalize(List<TextPosition> line, boolean isRtlDominant, boolean hasRtl) {
		LinkedList<String> normalized = new LinkedList<String>();
		StringBuilder lineBuilder = new StringBuilder();
		TextPosition last = line.get(0);

		for (TextPosition text : line) {
			last = text;
			lineBuilder.append(text.getUnicode());
		}
		if (lineBuilder.length() > 0) {
			String lineStr = lineBuilder.toString();
			normalized.add(lineStr);
		}
		System.err.println("normalized:" + normalized + "/" + line.size());
		if (pattern.matcher(normalized.get(0)).matches()) {
			float x1 = line.get(0).getX();
			float y1 = line.get(0).getY();
			float x2 = last.getX();
			float y2 = last.getY();
			float h = last.getHeight();
			float w = last.getWidth();
			float h1 = line.get(0).getHeight();
			float w1 = line.get(0).getWidth();
			float ht = 0;
			for (int i = 0; i < line.size(); i++) {
				ht = Math.max(ht, line.get(i).getHeight());
			}
			StringObject so = new StringObject(normalized.get(0), x1, y1 - ht, w + (x2 - x1), ht, last.isRotated());
			stringObjects.add(so);
		} else if (normalized.get(0).indexOf(" ") != -1) {
			tryWithDelimeter(line, " ");
		} else if (normalized.get(0).indexOf("/") != -1) {
			tryWithDelimeter(line, "/");
		} else if (normalized.get(0).indexOf(",") != -1) {
			tryWithDelimeter(line, ",");
		}
		return normalized;
	}

	private void tryWithDelimeter(List<TextPosition> line, String delim) {
		StringBuilder lineBuilder = new StringBuilder();
		TextPosition start = line.get(0);
		TextPosition last = start;
		for (TextPosition text : line) {
			if (lineBuilder.length() == 0) {
				start = text;
			}
			String ch = text.getUnicode();
			if (ch.equals(delim)) {
				if (pattern.matcher(lineBuilder.toString()).matches()) {
					StringObject so = getSO(start, last, lineBuilder.toString());
					stringObjects.add(so);
				}
				lineBuilder = new StringBuilder();
			} else {
				lineBuilder.append(ch);
			}
			last = text;
		}
		if (pattern.matcher(lineBuilder.toString()).matches()) {
			StringObject so = getSO(start, last, lineBuilder.toString());
			stringObjects.add(so);
		}
	}

	private boolean overlap(float y1, float height1, float y2, float height2) {
		return within(y1, y2, .1f) /*|| (y2 <= y1 && y2 >= y1 - height1) || (y1 <= y2 && y1 >= y2 - height2)*/;
	}

	StringObject getSO(TextPosition start, TextPosition end, String text) {
		float x1 = start.getX();
		float y1 = start.getY();
		float x2 = end.getX();
		float y2 = end.getY();
		float h = end.getHeight();
		float w = end.getWidth();
		float h1 = start.getHeight();
		float w1 = start.getWidth();
		float ht = Math.max(start.getHeight(), end.getHeight());
		StringObject so = new StringObject(text, x1, y1 - ht, w + (x2 - x1), ht, end.isRotated());
		return so;
	}

	/**
	 * This will determine of two floating point numbers are within a specified variance.
	 *
	 * @param first The first number to compare to.
	 * @param second The second number to compare to.
	 * @param variance The allowed variance.
	 */
	private boolean within(float first, float second, float variance) {
		return second < first + variance && second > first - variance;
	}

	/**
	 * Write a list of string containing a whole line of a document.
	 * @param line a list with the words of the given line
	 * @param isRtlDominant determines if rtl or ltl is dominant
	 * @throws IOException if something went wrong
	 */
	private void writeLine(List<String> line, boolean isRtlDominant) throws IOException {
		int numberOfStrings = line.size();

		if (isRtlDominant) {
			for (int i = numberOfStrings - 1; i >= 0; i--) {
				if (i > 1) {
					writeWordSeparator();
				}
				writeString(line.get(i));
			}
		} else {
			for (int i = 0; i < numberOfStrings; i++) {
				writeString(line.get(i));
				if (!isRtlDominant && i < numberOfStrings - 1) {
					writeWordSeparator();
				}
			}
		}
	}

	private float round(float x) {
		int decimalPlaces = 1;
		BigDecimal bd = new BigDecimal(x);

		bd = bd.setScale(decimalPlaces, BigDecimal.ROUND_HALF_UP);
		return bd.floatValue();
	}

	/**
	 * internal marker class.  Used as a place holder in
	 * a line of TextPositions.
	 * @author ME21969
	 *
	 */
	/*	private static final class WordSeparator extends TextPosition {
	 private static final WordSeparator separator = new WordSeparator();

	 private WordSeparator() {}

	 public static final WordSeparator getSeparator() {
	 return separator;
	 }

	 }*/

	public static final class StringObject {
		private String m_text;
		private float m_x;
		private float m_y;
		private float m_h;
		private float m_w;
		private boolean m_isRot;
		public Rectangle2D.Float m_bounds2D;

		public StringObject() {
		}

		public StringObject(String s, float x, float y, float w, float h, boolean isRotated) {
			m_text = s;
			m_x = x;
			m_y = y;
			m_w = w;
			m_h = h;
			m_isRot = isRotated;
			m_bounds2D = new Rectangle2D.Float(x, y, w, h);
		}

		public StringObject(String s, float x, float y, float w, float h) {
			m_text = s;
			m_x = x;
			m_y = y;
			m_w = w;
			m_h = h;
			m_isRot = false;
			m_bounds2D = new Rectangle2D.Float(x, y, w, h);
		}

		public StringObject(String s, Rectangle2D.Float bounds) {
			this.m_text = s;
			this.m_bounds2D = bounds;
		}

		public boolean contains(float x, float y) {
			return m_bounds2D.contains(x, y);
		}

		public Rectangle2D.Float getLocation() {
			return m_bounds2D;
		}

		public float getX() {
			return m_x;
		}

		public float getY() {
			return m_y;
		}

		public float getHeight() {
			return m_h;
		}

		public float getWidth() {
			return m_w;
		}

		public String getText() {
			return m_text;
		}

		public boolean isRotated() {
			return m_isRot;
		}

		public String toString() {
			return "[" + m_text + ":" + m_bounds2D.getBounds2D() + "]";
		}
	}

}

