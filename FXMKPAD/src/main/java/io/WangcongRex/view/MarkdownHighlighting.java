package io.WangcongRex.view;

import java.util.Collection;

import org.fxmisc.richtext.model.StyleSpans;

import io.WangcongRex.model.Article;

public interface MarkdownHighlighting {
	public StyleSpans<Collection<String>> highLightingFactory(Article article);
}
