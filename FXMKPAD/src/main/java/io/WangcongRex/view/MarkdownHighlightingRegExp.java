package io.WangcongRex.view;

import java.util.Collection;
import java.util.Collections;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.fxmisc.richtext.model.StyleSpans;
import org.fxmisc.richtext.model.StyleSpansBuilder;

import io.WangcongRex.model.Article;

public class MarkdownHighlightingRegExp implements MarkdownHighlighting {

	private static final String LINK_PATTERN = "(\\[.*?\\](\\(.*\\)|:.*|))";
	private static final String IMAGE_PATTERN = "(!\\[.*?\\](\\(.*\\)|))";
	private static final String UL_PATTERN = "^(?:\\s{0,7}|\\t)(\\*|\\+|\\-)\\s+(.*?)$";
	private static final String OL_PATTERN = "^(?:\\s{0,7}|\\t)\\d+\\.\\s+.*$";
	private static final String QUOTE_PATTERN = "^>[\\s\\t].*$";
	private static final String ITALICS_PATTERN = "\\s[_\\*][^_\\*].*?[_\\*]";
	private static final String BOLDS_PATTERN = "\\s[_\\*]{2}[^_\\*].*?[_\\*]{2}";
	private static final String ITALICSBOLDS_PATTERN = "\\s[_\\*]{3}[^_\\*].*?[_\\*]{3}";
	private static final String INLINECODE_PATTERN = "`.+?`";
//	private static final String CODEBLOCK_PATTERN = "^```(.|\\n)*?```|(\\s{4,}|\\t+).*";
	private static final String H1_PATTERN = "^\\s{0,3}([^\\s].*\\n={2,}|#\\s+.*)$";
	private static final String H2_PATTERN = "^\\s{0,3}([^\\s].*\\n-{2,}|##\\s+.*)$";
	private static final String H3_PATTERN = "^\\s{0,3}###\\s+.*$";
	private static final String H4_PATTERN = "^\\s{0,3}####\\s+.*$";
	private static final String H5_PATTERN = "^\\s{0,3}#####\\s+.*$";
	private static final String H6_PATTERN = "^\\s{0,3}######\\s+.*$";


	private static final Pattern PATTERN = Pattern.compile(
			"(?<LINK>" + LINK_PATTERN + ")"
					+ "|(?<IMAGE>" + IMAGE_PATTERN + ")"
					+ "|(?<UL>" + UL_PATTERN + ")"
					+ "|(?<OL>" + OL_PATTERN + ")"
					+ "|(?<QUOTE>" + QUOTE_PATTERN + ")"
					+ "|(?<ITALICS>" + ITALICS_PATTERN + ")"
					+ "|(?<BOLDS>" + BOLDS_PATTERN + ")"
					+ "|(?<ITALICSBOLDS>" + ITALICSBOLDS_PATTERN + ")"
					+ "|(?<INLINECODE>" + INLINECODE_PATTERN + ")"
//					+ "|(?<CODEBLOCK>" + CODEBLOCK_PATTERN + ")"
					+ "|(?<H1>" + H1_PATTERN + ")"
					+ "|(?<H2>" + H2_PATTERN + ")"
					+ "|(?<H3>" + H3_PATTERN + ")"
					+ "|(?<H4>" + H4_PATTERN + ")"
					+ "|(?<H5>" + H5_PATTERN + ")"
					+ "|(?<H6>" + H6_PATTERN + ")"
					, Pattern.MULTILINE);

	public StyleSpans<Collection<String>> highLightingFactory(Article article){
		String markdownCode = article.getMarkdownText();
		Matcher matcher = PATTERN.matcher(markdownCode);
		int lastKwEnd = 0;
		StyleSpansBuilder<Collection<String>> spansBuilder = new StyleSpansBuilder<>();
		while(matcher.find()) {
			String styleClass =
					matcher.group("LINK") != null ? "link" :
					matcher.group("IMAGE") != null ? "image" :
					matcher.group("UL") != null ? "ul" :
					matcher.group("OL") != null ? "ol" :
					matcher.group("QUOTE") != null ? "quote" :
					matcher.group("ITALICS") != null ? "italics" :
					matcher.group("BOLDS") != null ? "bolds" :
					matcher.group("ITALICSBOLDS") != null ? "italicsbolds" :
					matcher.group("INLINECODE") != null ? "code" :
//					matcher.group("CODEBLOCK") != null ? "code" :
						
					matcher.group("H1") != null ? "h1" :
					matcher.group("H2") != null ? "h2" :
					matcher.group("H3") != null ? "h3" :
					matcher.group("H4") != null ? "h4" :
					matcher.group("H5") != null ? "h5" :
					matcher.group("H6") != null ? "h6" :
					null; /* never happens */ 
			assert styleClass != null;
			spansBuilder.add(Collections.emptyList(), matcher.start() - lastKwEnd);
			spansBuilder.add(Collections.singleton(styleClass), matcher.end() - matcher.start());
			lastKwEnd = matcher.end();
		}
		spansBuilder.add(Collections.emptyList(), markdownCode.length() - lastKwEnd);
		return spansBuilder.create();

	}
}
