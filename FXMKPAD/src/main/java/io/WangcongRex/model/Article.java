package io.WangcongRex.model;

import java.io.File;
import java.math.BigInteger;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;


import com.vladsch.flexmark.html.HtmlRenderer;
import com.vladsch.flexmark.parser.Parser;
import com.vladsch.flexmark.profile.pegdown.Extensions;
import com.vladsch.flexmark.profile.pegdown.PegdownOptionsAdapter;
import com.vladsch.flexmark.util.ast.Node;
import com.vladsch.flexmark.util.data.DataHolder;


public class Article {

	private String saveText;
	private String markdownText;
	private File file;

	private DataHolder OPTIONS;
	private Parser PARSER;
	private HtmlRenderer RENDERER;

	private String currentMasrker; 

	public Article() {
		this("", new File("", "New File"));
		
	}


	public Article(String saveText, File file) {
		this.markdownText = saveText;
		this.saveText = saveText;
		this.file = file;
		
		this.OPTIONS = PegdownOptionsAdapter.flexmarkOptions(Extensions.ALL);
		this.PARSER = Parser.builder(OPTIONS).build();
		this.RENDERER = HtmlRenderer.builder(OPTIONS).build();
		MessageDigest md;
		try {
			md = MessageDigest.getInstance("MD5");
			md.update(("【【ＣＵＲＲＥＮＴspliter】】" + System.currentTimeMillis()).getBytes());
			currentMasrker = new BigInteger(1, md.digest()).toString(16);
		} catch (NoSuchAlgorithmException e) {
			e.printStackTrace();
		}
	}

	public void upData(String markdownText) {
		this.markdownText = markdownText;
	}

	public String getHTML() {
		Node document = PARSER.parse(markdownText);
		String html = RENDERER.render(document);
		html = "<!DOCTYPE html><html><head><meta charset=\"UTF-8\">\n" +
		         "\n" + 
		         "</head><body>" + html + "\n" +
		         "</body></html>";
		return String.join("\\`", html.split("`"));
	}

	public String getHTML4Preview(int pos) {

		if(isModified()) {

			int markpos = markdownText.substring(pos).indexOf("\n") + pos +1;
			StringBuffer sbmd = new StringBuffer(markdownText.substring(0, markpos));
			sbmd.append(currentMasrker);
			sbmd.append(markdownText.substring(markpos));
			Node document = PARSER.parse(sbmd.toString());
			String html = RENDERER.render(document);

			Document doc = Jsoup.parse(html);
			Elements el = doc.select(":contains(" + currentMasrker + ")");
			el.get(el.size()-1).addClass("current");


			return String.join("", String.join("", el.get(0).html().split(currentMasrker)).split("`"));

		} else {
			return getHTML();
		}
	}

	private String printNode(Node node, int level) {
		if(node==null) {
			return "\n\n";
		} else {
			StringBuffer sb = new StringBuffer();
			for(int i=0 ; i<level ; i++) {
				sb.append("\t");
			}
			sb.append(node.toAstString(false));
			sb.append(": ").append(node.getChars());
			sb.append("\n");

			Node child = node.getFirstChild();
			while(child != null) {
				sb.append(printNode(child, level + 1));
				sb.append(" ");
				child = child.getNext();
			}
			return  sb.toString();
		}
	}


	public String getATS() {
		Node document = PARSER.parse(markdownText);

		return printNode(document,0);
	}

	public Node getDocumaent() {
		return PARSER.parse(markdownText);
	}
	public boolean isModified() {
		return !markdownText.equals(saveText);
	}

	public File getFile() {
		return file;
	}


	public void setFile(File file) {
		this.file = file;
	}


	public String getMarkdownText() {
		return markdownText;
	}



}
