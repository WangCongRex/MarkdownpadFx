package io.WangcongRex.view;

import java.util.Collection;
import java.util.Collections;

import org.fxmisc.richtext.model.StyleSpans;
import org.fxmisc.richtext.model.StyleSpansBuilder;

import com.vladsch.flexmark.ast.Heading;
import com.vladsch.flexmark.util.ast.Node;

import io.WangcongRex.model.Article;

public class MarkdownHighlightingAST implements MarkdownHighlighting {

	@Override
	public StyleSpans<Collection<String>> highLightingFactory(Article article) {
		StyleSpansBuilder<Collection<String>> spansBuilder = new StyleSpansBuilder<>();
		spansBuilder.add(Collections.emptyList(), 0);
		int lastEndOffset = 0;
		Node documaent = article.getDocumaent();
		Node curPara = documaent.getFirstChild();
		while(curPara!=null) {
			String name = curPara.getNodeName();
			spansBuilder.add(Collections.emptyList(), curPara.getStartOffset() - lastEndOffset);
			if("Heading".equals(name)) {
				spansBuilder.add(Collections.singleton("h" + ((Heading)curPara).getLevel()), curPara.getEndOffset() - curPara.getStartOffset());
			}else if("BlockQuote".equals(name)) {
				spansBuilder.add(Collections.singleton("quote"), curPara.getEndOffset() - curPara.getStartOffset());
			}else if("FencedCodeBlock".equals(name)) {
				spansBuilder.add(Collections.singleton("code"), curPara.getEndOffset() - curPara.getStartOffset());
			}else if("HtmlBlock".equals(name)) {
				spansBuilder.add(Collections.singleton("code"), curPara.getEndOffset() - curPara.getStartOffset());
			}else if("OrderedList".equals(name)) {
				spansBuilder.add(Collections.singleton("ol"), curPara.getEndOffset() - curPara.getStartOffset());
			}else if("BulletList".equals(name)) {
				spansBuilder.add(Collections.singleton("ul"), curPara.getEndOffset() - curPara.getStartOffset());
			}else if("Reference".equals(name)) {
				spansBuilder.add(Collections.singleton("link"), curPara.getEndOffset() - curPara.getStartOffset());
			}else if("Paragraph".equals(name)) {
				int lastTextEndOffset = curPara.getStartOffset();
				Node curText = curPara.getFirstChild();
				while(curText!=null) {
					String textName = curText.getNodeName();
					spansBuilder.add(Collections.emptyList(), curText.getStartOffset() - lastTextEndOffset);
					if("LinkRef".equals(textName)) {
						spansBuilder.add(Collections.singleton("link"), curText.getEndOffset() - curText.getStartOffset());
					} else if("Emphasis".equals(textName)) {
						spansBuilder.add(Collections.singleton("italics"), curText.getEndOffset() - curText.getStartOffset());
					} else if("StrongEmphasis".equals(textName)) {
						spansBuilder.add(Collections.singleton("bolds"), curText.getEndOffset() - curText.getStartOffset());
					} else if("Code".equals(textName)) {
						spansBuilder.add(Collections.singleton("code"), curText.getEndOffset() - curText.getStartOffset());
					} else if("HtmlInline".equals(textName)) {
						spansBuilder.add(Collections.singleton("code"), curText.getEndOffset() - curText.getStartOffset());
					} else if("Image".equals(textName)) {
						spansBuilder.add(Collections.singleton("image"), curText.getEndOffset() - curText.getStartOffset());
					} else if("ImageRef".equals(textName)) {
						spansBuilder.add(Collections.singleton("image"), curText.getEndOffset() - curText.getStartOffset());
					} else if("Link".equals(textName)) {
						spansBuilder.add(Collections.singleton("link"), curText.getEndOffset() - curText.getStartOffset());
					} else if("AutoLink".equals(textName)) {
						spansBuilder.add(Collections.singleton("link"), curText.getEndOffset() - curText.getStartOffset());
					}else {
						spansBuilder.add(Collections.emptyList(), curText.getEndOffset() - curText.getStartOffset());
					}
					lastTextEndOffset = curText.getEndOffset();
					curText = curText.getNext();
				}
				spansBuilder.add(Collections.emptyList(), curPara.getEndOffset() - lastTextEndOffset);
			} else {
				spansBuilder.add(Collections.emptyList(), curPara.getEndOffset() - curPara.getStartOffset());
			}
			
			
			lastEndOffset = curPara.getEndOffset();
			curPara = curPara.getNext();
		}
		
		
		
		
		return spansBuilder.create();
	}

}
