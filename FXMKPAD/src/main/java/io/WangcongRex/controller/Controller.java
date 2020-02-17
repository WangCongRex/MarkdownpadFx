/**
 * Sample Skeleton for 'EditorViewScene.fxml' Controller Class
 */

package io.WangcongRex.controller;

import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.geometry.Side;
import javafx.scene.control.*;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.input.KeyCode;
import javafx.scene.input.ScrollEvent;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.HBox;
import javafx.scene.web.WebView;
import javafx.stage.FileChooser;
import javafx.stage.Stage;

import java.io.*;
import java.time.Duration;
import java.util.HashMap;
import java.util.Optional;
import java.util.ResourceBundle;

import org.controlsfx.control.HiddenSidesPane;
import org.fxmisc.flowless.VirtualizedScrollPane;
import org.fxmisc.richtext.CodeArea;
import org.fxmisc.richtext.LineNumberFactory;
import org.fxmisc.richtext.model.TwoDimensional.Bias;

import com.vladsch.flexmark.pdf.converter.PdfConverterExtension;
import com.vladsch.flexmark.profile.pegdown.Extensions;
import com.vladsch.flexmark.profile.pegdown.PegdownOptionsAdapter;
import com.vladsch.flexmark.util.data.DataHolder;

import io.WangcongRex.Main;
import io.WangcongRex.model.Article;
import io.WangcongRex.view.MarkdownHighlighting;
import io.WangcongRex.view.MarkdownHighlightingAST;
import io.WangcongRex.view.inputDialog;

public class Controller {

	public Controller() {
		this.article = new Article();
		System.out.println("Controller: Controller构建成功");
	}


	private void syncScroll() {
		double maxValue = TextMDCode.totalHeightEstimateProperty().getOrElse(0.).doubleValue() - TextMDCode.getHeight();
		double scrollValue  = TextMDCode.estimatedScrollYProperty().getValue().doubleValue();
		WebPreview.getEngine().executeScript("window.scrollTo(0, (document.body.clientHeight - document.documentElement.clientHeight) * " + 
				(scrollValue / maxValue) + 
				");");
	}

	public void init() throws IOException {
		TextMDCode = new CodeArea();
		TextMDCode.setWrapText(true);
		TextMDCode.setParagraphGraphicFactory(LineNumberFactory.get(TextMDCode));
		TextMDCode.setContextMenu(contextMenu);

		scrollPane = new VirtualizedScrollPane<CodeArea>(TextMDCode);

		codeAreaPane.setContent(scrollPane);
		codeAreaPane.setTriggerDistance(0);
		replaceBox.setVisible(false);

		WebPreview.setContextMenuEnabled(false);
		WebPreview.setDisable(false);
		
		webHTMLCode.setContextMenuEnabled(false);
		
		Platform.runLater(() -> {
			
			WebPreview.getEngine().documentProperty().addListener((ob,olddoc,newdoc)->{
				if(olddoc!=null && !olddoc.equals(newdoc)) {
					newdoc=olddoc;
				}
			});

			TextMDCode.estimatedScrollYProperty().addListener((observable, oldValue, newValue) -> {
				syncScroll();
			});


			rootNode.getScene().getStylesheets().add(Main.class.getResource("css/markdown-keywords.css").toExternalForm());


			TextMDCode.multiPlainChanges().successionEnds(Duration.ofMillis(500)).subscribe((e) -> {

				MarkdownHighlighting highlighting = new MarkdownHighlightingAST();
				TextMDCode.setStyleSpans(0, highlighting.highLightingFactory(article));
				doUpData();
				syncScroll();
			});

			TextMDCode.setOnMouseReleased((e) -> {
				doUpData();
				syncScroll();
			});

			TextMDCode.setOnKeyReleased((e) -> {
				if(e.getCode() == KeyCode.PAGE_DOWN ||
						e.getCode() == KeyCode.PAGE_UP ||
						e.getCode() == KeyCode.HOME ||
						e.getCode() == KeyCode.END ||
						e.getCode() == KeyCode.UP ||
						e.getCode() == KeyCode.DOWN ||
						e.getCode() == KeyCode.LEFT ||
						e.getCode() == KeyCode.RIGHT 
						) {
					syncScroll();
				}
			});


			WebPreview.getEngine().loadContent(""
					+ "<!DOCTYPE html>\r\n" + 
					"<html lang=\"en\">\r\n" + 
					"<head>\r\n" + 
					"    <meta charset=\"UTF-8\">\r\n" + 
					"    <meta name=\"viewport\" content=\"width=device-width, initial-scale=1.0\">\r\n" + 
					"    <meta http-equiv=\"X-UA-Compatible\" content=\"ie=edge\">\r\n" + 
					"    <link rel=\"stylesheet\" href=\"" + Main.class.getResource("css/prism.css") + "\">\r\n" + 
					"    <link rel=\"stylesheet\" href=\"" + Main.class.getResource("css/bootstrap.min.css") + "\">\r\n" + 
					"    <link rel=\"stylesheet\" href=\"" + Main.class.getResource("css/preview.css") + "\">\r\n" + 
					"    <script src=\"" + Main.class.getResource("vue.js") + "\"></script>\r\n" + 
					"    <script src=\"" + Main.class.getResource("prism.js") + "\"></script>\r\n" + 
					"    <title>vue test</title>\r\n" + 
					"</head>\r\n" + 
					"<body >\r\n" + //style='font-family: 'Source Han Serif CN';'
					"   <div class=\"container\">\r\n" + 
					"        <div class=\"row\">\r\n" + 
					"            <div class=\"col-lg-10 col-lg-offset-1\" >    " +
					"				<div id=\"APP\" v-html=\"htmlCode\">\r\n" + 
					"  				</div>\r\n" + 
					"    		 </div>\r\n" + 
					"    	 </div>\r\n" + 
					"   </div>\r\n" + 
					"    <script>\r\n" + 
					"        var vue = new Vue({\r\n" + 
					"            el: \"#APP\",\r\n" + 
					"            data: {\r\n" + 
					"                markdownCode:``,\r\n" + 
					"            },\r\n" + 
					"            computed: {\r\n" + 
					"                htmlCode: function () {\r\n" + 
					"                    return this.markdownCode.replace(new RegExp(\"<table\", \"g\"),`<table class=\"table table-striped\"`);\r\n" + 
					"                }\r\n" + 
					"            },\r\n" + 
					"            updated: function(){\r\n" + 
					"                Prism.highlightAll();\r\n" + 
					"            }\r\n" + 
					"        });\r\n" + 
					"    </script>\r\n" + 
					"</body>\r\n" + 
					"</html>"
					+ "");

			webHTMLCode.getEngine().loadContent(""
					+ "<!DOCTYPE html>\r\n" + 
					"<html lang=\"en\">\r\n" + 
					"<head>\r\n" + 
					"    <meta charset=\"UTF-8\">\r\n" + 
					"    <meta name=\"viewport\" content=\"width=device-width, initial-scale=1.0\">\r\n" + 
					"    <meta http-equiv=\"X-UA-Compatible\" content=\"ie=edge\">\r\n" + 
					"    <link rel=\"stylesheet\" href=\"" + Main.class.getResource("css/prism.css") + "\">\r\n" + 
					"    <link rel=\"stylesheet\" href=\"" + Main.class.getResource("css/bootstrap.min.css") + "\">\r\n" + 
					"    <script src=\"" + Main.class.getResource("vue.js") + "\"></script>\r\n" + 
					"    <script src=\"" + Main.class.getResource("marked.min.js") + "\"></script>\r\n" + 
					"    <script src=\"" + Main.class.getResource("prism.js") + "\"></script>\r\n" + 
					"    <title>vue test</title>\r\n" + 
					"</head>\r\n" + 
					"<body >\r\n" + 
					"   <div class=\"container\">\r\n" + 
					"        <div class=\"row\">\r\n" + 
					"            <div class=\"col-lg-10 col-lg-offset-1\" >    " +
					"				<div id=\"APP\" v-html=\"htmlCode\">\r\n" + 
					"  				</div>\r\n" + 
					"    		 </div>\r\n" + 
					"    	 </div>\r\n" + 
					"   </div>\r\n" + 
					"    <script>\r\n" + 
					"        var vue = new Vue({\r\n" + 
					"            el: \"#APP\",\r\n" + 
					"            data: {\r\n" + 
					"                markdownCode:``,\r\n" + 
					"            },\r\n" + 
					"            computed: {\r\n" + 
					"                htmlCode: function () {\r\n" + 
					"                    return marked(`\\`\\`\\`html\r\n" + 
					"`+this.markdownCode+`\r\n" + 
					"                    \\`\\`\\``);" + 
					"                }\r\n" + 
					"            },\r\n" + 
					"            updated: function(){\r\n" + 
					"                Prism.highlightAll();\r\n" + 
					"            }\r\n" + 
					"        });\r\n" + 
					"    </script>\r\n" + 
					"</body>\r\n" + 
					"</html>"
					+ "");



		});
	}

	private CodeArea TextMDCode;

	private VirtualizedScrollPane<CodeArea> scrollPane;

	private Article article;

	@FXML // fx:id="rootNode"
	private AnchorPane rootNode; // Value injected by FXMLLoader

	@FXML // fx:id="MenuNew"
	private MenuItem MenuNew; // Value injected by FXMLLoader

	@FXML // fx:id="MenuOpen"
	private MenuItem MenuOpen; // Value injected by FXMLLoader

	@FXML // fx:id="MenuOpenRecent"
	private Menu MenuOpenRecent; // Value injected by FXMLLoader

	@FXML // fx:id="MenuSave"
	private MenuItem MenuSave; // Value injected by FXMLLoader

	@FXML // fx:id="MenuSaveAs"
	private MenuItem MenuSaveAs; // Value injected by FXMLLoader

	@FXML // fx:id="MenuExport"
	private MenuItem MenuExport; // Value injected by FXMLLoader

	@FXML // fx:id="MenuSetting"
	private MenuItem MenuSetting; // Value injected by FXMLLoader

	@FXML // fx:id="MenuQuit"
	private MenuItem MenuQuit; // Value injected by FXMLLoader

	@FXML // fx:id="MenuUndo"
	private MenuItem MenuUndo; // Value injected by FXMLLoader

	@FXML // fx:id="MenuCopy"
	private MenuItem MenuCopy; // Value injected by FXMLLoader

	@FXML // fx:id="MenuCut"
	private MenuItem MenuCut; // Value injected by FXMLLoader

	@FXML // fx:id="MenuPaste"
	private MenuItem MenuPaste; // Value injected by FXMLLoader

	@FXML // fx:id="MenuDelete"
	private MenuItem MenuDelete; // Value injected by FXMLLoader

	@FXML // fx:id="MenuSelectAll"
	private MenuItem MenuSelectAll; // Value injected by FXMLLoader

	@FXML // fx:id="MentSelectNone"
	private MenuItem MentSelectNone; // Value injected by FXMLLoader

	@FXML // fx:id="MenuShowFind"
	private MenuItem MenuShowFind; // Value injected by FXMLLoader

	@FXML // fx:id="MenuShowReplace"
	private MenuItem MenuShowReplace; // Value injected by FXMLLoader

	@FXML // fx:id="MenuBold"
	private MenuItem MenuBold; // Value injected by FXMLLoader

	@FXML // fx:id="MenuItalic"
	private MenuItem MenuItalic; // Value injected by FXMLLoader

	@FXML // fx:id="MenuDeleteLine"
	private MenuItem MenuDeleteLine; // Value injected by FXMLLoader

	@FXML // fx:id="MenuH1"
	private MenuItem MenuH1; // Value injected by FXMLLoader

	@FXML // fx:id="MenuH2"
	private MenuItem MenuH2; // Value injected by FXMLLoader

	@FXML // fx:id="MenuH3"
	private MenuItem MenuH3; // Value injected by FXMLLoader

	@FXML // fx:id="MenuH4"
	private MenuItem MenuH4; // Value injected by FXMLLoader

	@FXML // fx:id="MenuH5"
	private MenuItem MenuH5; // Value injected by FXMLLoader

	@FXML // fx:id="MenuH6"
	private MenuItem MenuH6; // Value injected by FXMLLoader

	@FXML // fx:id="MenuQuote"
	private MenuItem MenuQuote; // Value injected by FXMLLoader

	@FXML // fx:id="MenuInlineCode"
	private MenuItem MenuInlineCode; // Value injected by FXMLLoader

	@FXML // fx:id="MenuCodeBlock"
	private MenuItem MenuCodeBlock; // Value injected by FXMLLoader

	@FXML // fx:id="MenuOL"
	private MenuItem MenuOL; // Value injected by FXMLLoader

	@FXML // fx:id="MenuUL"
	private MenuItem MenuUL; // Value injected by FXMLLoader

	@FXML // fx:id="MenuSep"
	private MenuItem MenuSep; // Value injected by FXMLLoader

	@FXML // fx:id="MenuImage"
	private MenuItem MenuImage; // Value injected by FXMLLoader

	@FXML // fx:id="MenuLink"
	private MenuItem MenuLink; // Value injected by FXMLLoader

	@FXML // fx:id="MenuTable"
	private MenuItem MenuTable; // Value injected by FXMLLoader

	@FXML // fx:id="MenuHelp"
	private MenuItem MenuHelp; // Value injected by FXMLLoader

	@FXML // fx:id="MenuAbout"
	private MenuItem MenuAbout; // Value injected by FXMLLoader

	@FXML // fx:id="BNew"
	private Button BNew; // Value injected by FXMLLoader

	@FXML // fx:id="BOpen"
	private Button BOpen; // Value injected by FXMLLoader

	@FXML // fx:id="BSave"
	private Button BSave; // Value injected by FXMLLoader

	@FXML // fx:id="BCopy"
	private Button BCopy; // Value injected by FXMLLoader

	@FXML // fx:id="BCut"
	private Button BCut; // Value injected by FXMLLoader

	@FXML // fx:id="BPaste"
	private Button BPaste; // Value injected by FXMLLoader

	@FXML // fx:id="BUndo"
	private Button BUndo; // Value injected by FXMLLoader

	@FXML // fx:id="BRedo"
	private Button BRedo; // Value injected by FXMLLoader

	@FXML // fx:id="BBlod"
	private Button BBlod; // Value injected by FXMLLoader

	@FXML // fx:id="BItalic"
	private Button BItalic; // Value injected by FXMLLoader

	@FXML // fx:id="BH1"
	private MenuItem BH1; // Value injected by FXMLLoader

	@FXML // fx:id="BH2"
	private MenuItem BH2; // Value injected by FXMLLoader

	@FXML // fx:id="BH3"
	private MenuItem BH3; // Value injected by FXMLLoader

	@FXML // fx:id="BH4"
	private MenuItem BH4; // Value injected by FXMLLoader

	@FXML // fx:id="BH5"
	private MenuItem BH5; // Value injected by FXMLLoader

	@FXML // fx:id="BH6"
	private MenuItem BH6; // Value injected by FXMLLoader

	@FXML // fx:id="BQuote"
	private Button BQuote; // Value injected by FXMLLoader

	@FXML // fx:id="BUl"
	private Button BUl; // Value injected by FXMLLoader

	@FXML // fx:id="BOl"
	private Button BOl; // Value injected by FXMLLoader

	@FXML // fx:id="BCode"
	private Button BCode; // Value injected by FXMLLoader

	@FXML // fx:id="BCodeBlock"
	private Button BCodeBlock; // Value injected by FXMLLoader

	@FXML // fx:id="BSeparator"
	private Button BSeparator; // Value injected by FXMLLoader

	@FXML // fx:id="BImage"
	private Button BImage; // Value injected by FXMLLoader

	@FXML // fx:id="BLink"
	private Button BLink; // Value injected by FXMLLoader

	@FXML // fx:id="Btable"
	private Button Btable; // Value injected by FXMLLoader

	@FXML // fx:id="LStatus"
	private Label LStatus; // Value injected by FXMLLoader

	@FXML // fx:id="contextMenu"
	private ContextMenu contextMenu; // Value injected by FXMLLoader

	@FXML // fx:id="MenuUndo1"
	private MenuItem MenuUndo1; // Value injected by FXMLLoader

	@FXML // fx:id="MenuCopy1"
	private MenuItem MenuCopy1; // Value injected by FXMLLoader

	@FXML // fx:id="MenuCut1"
	private MenuItem MenuCut1; // Value injected by FXMLLoader

	@FXML // fx:id="MenuPaste1"
	private MenuItem MenuPaste1; // Value injected by FXMLLoader

	@FXML // fx:id="MenuDelete1"
	private MenuItem MenuDelete1; // Value injected by FXMLLoader

	@FXML // fx:id="MenuSelectAll1"
	private MenuItem MenuSelectAll1; // Value injected by FXMLLoader

	@FXML // fx:id="MentSelectNone1"
	private MenuItem MentSelectNone1; // Value injected by FXMLLoader

	@FXML // fx:id="codeAreaPane"
	private HiddenSidesPane codeAreaPane; // Value injected by FXMLLoader

	@FXML // fx:id="FNRPane"
	private AnchorPane FNRPane; // Value injected by FXMLLoader

	@FXML // fx:id="findText"
	private TextField findText; // Value injected by FXMLLoader

	@FXML // fx:id="findLastButton"
	private Button findLastButton; // Value injected by FXMLLoader

	@FXML // fx:id="findNextButton"
	private Button findNextButton; // Value injected by FXMLLoader

	@FXML // fx:id="capCaseSwitch"
	private ToggleButton capCaseSwitch; // Value injected by FXMLLoader

	@FXML // fx:id="regxSwitch"
	private ToggleButton regxSwitch; // Value injected by FXMLLoader

	@FXML // fx:id="replaceBox"
	private HBox replaceBox; // Value injected by FXMLLoader

	@FXML // fx:id="replaceText"
	private TextField replaceText; // Value injected by FXMLLoader

	@FXML // fx:id="replaceButton"
	private Button replaceButton; // Value injected by FXMLLoader

	@FXML // fx:id="replaceAllButton"
	private Button replaceAllButton; // Value injected by FXMLLoader

	@FXML // fx:id="closeButton"
	private Button closeButton; // Value injected by FXMLLoader

	@FXML // fx:id="TABPreview"
	private Tab TABPreview; // Value injected by FXMLLoader

	@FXML // fx:id="WebPreview"
	private WebView WebPreview; // Value injected by FXMLLoader

	@FXML // fx:id="TABHTMLCode"
	private Tab TABHTMLCode; // Value injected by FXMLLoader

	@FXML // fx:id="webHTMLCode"
	private WebView webHTMLCode; // Value injected by FXMLLoader

	@FXML // fx:id="ASTArea"
	private TextArea ASTArea; // Value injected by FXMLLoader


	@FXML
	void doFindLast(ActionEvent event) {
		String findstring = findText.getText();
		int spiltPos = TextMDCode.getSelection().getStart();
		int finPos = TextMDCode.getText().substring(0, spiltPos).indexOf(findstring);
		if(finPos == -1) {
			finPos = TextMDCode.getText().lastIndexOf(findstring);
			if(finPos == -1) {
				Alert alert = new Alert(AlertType.ERROR);
				alert.setTitle("Can not find this text");
				alert.setContentText("Can not find this text in the article.");
				alert.showAndWait();
				return ;
			}
		}
		TextMDCode.selectRange(finPos, finPos + findstring.length() );

		double line = TextMDCode.offsetToPosition(finPos, Bias.Backward).getMajor();
		double maxValue = TextMDCode.totalHeightEstimateProperty().getOrElse(0.).doubleValue() - TextMDCode.getHeight();
		double scrollValue = (line / TextMDCode.getParagraphs().size()) * maxValue;
		TextMDCode.estimatedScrollYProperty().setValue(new Double(scrollValue));
	}

	@FXML
	void doFindNext(ActionEvent event) {
		String findstring = findText.getText();
		int spiltPos = TextMDCode.getSelection().getEnd();
		int finPos = TextMDCode.getText().substring(spiltPos).indexOf(findstring);
		if(finPos == -1) {
			finPos = TextMDCode.getText().indexOf(findstring);
			if(finPos == -1) {
				Alert alert = new Alert(AlertType.ERROR);
				alert.setTitle("Can not find this text");
				alert.setContentText("Can not find this text in the article.");
				alert.showAndWait();
				return ;
			} 
		} else {
			finPos = finPos + spiltPos;
		}
		TextMDCode.selectRange(finPos, finPos + findstring.length() );

		double line = TextMDCode.offsetToPosition(finPos, Bias.Backward).getMajor();
		double maxValue = TextMDCode.totalHeightEstimateProperty().getOrElse(0.).doubleValue() - TextMDCode.getHeight();
		double scrollValue = (line / TextMDCode.getParagraphs().size()) * maxValue;
		TextMDCode.estimatedScrollYProperty().setValue(new Double(scrollValue));
	}

	@FXML
	void doReplace(ActionEvent event) {

	}

	@FXML
	void doReplaceAll(ActionEvent event) {

	}

	@FXML
	void doCloseFNRPane(ActionEvent event) {
		codeAreaPane.setPinnedSide(null);
		replaceBox.setVisible(false);
	}


	private void insertToken(String token, String AltText){
		int start = TextMDCode.getSelection().getStart();
		int end = TextMDCode.getSelection().getEnd();
		TextMDCode.insertText(end, token);
		if(start == end) {
			TextMDCode.insertText(end, AltText);
		}
		TextMDCode.insertText(start, token);
		if(start == end) {
			TextMDCode.selectRange(start+token.length(), start+token.length() + AltText.length());
		} else {
			TextMDCode.selectRange(start+token.length(), end+2*token.length());
		}
	}


	private void insertLineToken(String token){
		int start = TextMDCode.getSelection().getStart();
		int insertpos = TextMDCode.getText().substring(0, start).lastIndexOf("\n")+1;

		if(TextMDCode.getText().substring(insertpos).trim().indexOf(token + " ") == 0) {
			TextMDCode.replaceText(insertpos, TextMDCode.getText().length(), TextMDCode.getText().substring(insertpos).substring(token.length()).trim());
		} else {
			TextMDCode.insertText(insertpos, token+" ");
		}
	}

	private void upDataTitle() {
		StringBuffer sb = new StringBuffer();
		if(article.isModified()) {
			sb.append("* ");
		}
		sb .append(article.getFile().getPath())
		.append(" - FX Markdown Pad");

		((Stage)rootNode.getScene().getWindow()).setTitle(sb.toString());

	}


	@FXML
	void doOl(ActionEvent event) {
		insertLineToken("1.");
	}

	@FXML
	void doUl(ActionEvent event) {
		insertLineToken("-");
	}

	@FXML
	void doSeparate(ActionEvent event) {
		insertLineToken("\n-----\n");
	}

	@FXML
	void doQuote(ActionEvent event) {
		insertLineToken(">");
	}

	@FXML
	void doCode(ActionEvent event) {
		insertToken("`", "code");
	}

	@FXML
	void doCodeBlock(ActionEvent event) {
		
		ResourceBundle resource = ResourceBundle.getBundle("io.WangcongRex.language.ApplicationResources");
		
		Dialog<HashMap<String,String>> codeDialog = inputDialog.dialogFactory(resource.getString("Dialog.codeblock"), "FILE_CODE_ALT", resource.getString("Dialog.codeblock.language"));
		Optional<HashMap<String, String>> codeInfo = codeDialog.showAndWait();
		if(codeInfo.isPresent()) {
			TextMDCode.insertText(TextMDCode.getCaretPosition(), "``` " + codeInfo.get().get("Coding language") + "\n\n```");
		}
	}




	@FXML
	void doSaveAs(ActionEvent event) {
		
		ResourceBundle resource = ResourceBundle.getBundle("io.WangcongRex.language.ApplicationResources");
		
		FileChooser fc = new FileChooser();
		fc.setTitle(resource.getString("saveas"));
		fc.getExtensionFilters().addAll(
				new FileChooser.ExtensionFilter("MarkDown Files", "*.md"),
				new FileChooser.ExtensionFilter("All Files", "*.*")
				);
		try {
			article.setFile(fc.showSaveDialog(rootNode.getScene().getWindow()));
			if(article.getFile() != null) {
				FileWriter fw = new FileWriter(article.getFile());
				fw.write(article.getMarkdownText());
				fw.close();
				article = new Article(article.getMarkdownText(), article.getFile());
			} else {
				article.setFile(new File("", "New File"));
			}
			upDataTitle();
		}catch (IOException e) {
			e.printStackTrace();
		}

	}

	@FXML
	void doExport(ActionEvent event) throws InterruptedException {

		ResourceBundle resource = ResourceBundle.getBundle("io.WangcongRex.language.ApplicationResources");
		FileChooser fc = new FileChooser();
		fc.setTitle(resource.getString("export"));
		fc.getExtensionFilters().addAll(
				new FileChooser.ExtensionFilter("PDF Files", "*.pdf")
				);

		String filePath = fc.showSaveDialog(rootNode.getScene().getWindow()).getPath();
		DataHolder OPTIONS = PegdownOptionsAdapter.flexmarkOptions(
				Extensions.ALL & ~(Extensions.ANCHORLINKS | Extensions.EXTANCHORLINKS_WRAP)
				).toImmutable();

//		Thread th = new Thread(() ->{
//			PdfConverterExtension.exportToPdf(filePath, this.article.getHTML(),"", OPTIONS);
//		});
//		th.setDaemon(true);
//        th.start();
        
//        Dialog<Boolean> dialog = new Dialog<Boolean>();
//        dialog.initStyle(StageStyle.UNDECORATED);
//        dialog.getDialogPane().setContent(new Label("Plase wait..."));
//        dialog.show();
//		th.join();
		PdfConverterExtension.exportToPdf(filePath, this.article.getHTML(),"", OPTIONS);
//		dialog.close();
		System.out.println("导出完成！");



	}

	@FXML
	void doQuit(ActionEvent event) {
		System.exit(0);
	}


	@FXML
	void doDelete(ActionEvent event) {
		TextMDCode.deleteText(TextMDCode.getSelection());
	}

	@FXML
	void doSelectAll(ActionEvent event) {
		TextMDCode.selectAll();
	}

	@FXML
	void doSelectNone(ActionEvent event) {
		TextMDCode.deselect();
	}



	@FXML
	void doBlod(ActionEvent event) {
		insertToken("**", "bold text");
	}

	@FXML
	void doItalic(ActionEvent event) {
		insertToken("_", "italic text");
	}

	@FXML
	void doDeleteLine(ActionEvent event) {
		insertToken("~~", "deleted text");
	}


	@FXML
	void doH1(ActionEvent event) {
		insertLineToken("#");
	}

	@FXML
	void doH2(ActionEvent event) {
		insertLineToken("##");
	}

	@FXML
	void doH3(ActionEvent event) {
		insertLineToken("###");
	}

	@FXML
	void doH4(ActionEvent event) {
		insertLineToken("####");
	}

	@FXML
	void doH5(ActionEvent event) {
		insertLineToken("#####");
	}

	@FXML
	void doH6(ActionEvent event) {
		insertLineToken("######");
	}

	@FXML
	void doImage(ActionEvent event) {
		ResourceBundle resource = ResourceBundle.getBundle("io.WangcongRex.language.ApplicationResources");
		
		Dialog<HashMap<String,String>> imgDialog = inputDialog.dialogFactory(
				resource.getString("Dialog.image"), 
				"PICTURE_ALT", 
				resource.getString("Dialog.image.ImageUrl"), 
				resource.getString("Dialog.image.altematetext"), 
				resource.getString("Dialog.image.title")
				);
		Optional<HashMap<String, String>> imgInfo = imgDialog.showAndWait();
		if(imgInfo.isPresent()) {
			TextMDCode.insertText(TextMDCode.getCaretPosition(), "\n![" + imgInfo.get().get("Altemate Text") + "](" + imgInfo.get().get("Image Url") +" \""+ imgInfo.get().get("Title(Tooltip)") +"\")\n");
		}
	}

	@FXML
	void doLink(ActionEvent event) {

		ResourceBundle resource = ResourceBundle.getBundle("io.WangcongRex.language.ApplicationResources");
		
		Dialog<HashMap<String,String>> linkDialog = inputDialog.dialogFactory(
				resource.getString("Dialog.link"), 
				"LINK", 
				resource.getString("Dialog.link.linkUrl"), 
				resource.getString("Dialog.link.linktext"), 
				resource.getString("Dialog.link.title")
				);


		Optional<HashMap<String, String>> linkInfo = linkDialog.showAndWait();
		if(linkInfo.isPresent()) {
			TextMDCode.insertText(TextMDCode.getCaretPosition(), "\n[" + linkInfo.get().get("Link Text") + "](" + linkInfo.get().get("Link Url") +" \"" + linkInfo.get().get("Title(Tooltip)") + "\")\n");
		}
	}

	@FXML
	void doTable(ActionEvent event) {

		ResourceBundle resource = ResourceBundle.getBundle("io.WangcongRex.language.ApplicationResources");
		
		Dialog<HashMap<String,String>> tableDialog = inputDialog.dialogFactory(
				resource.getString("Dialog.table"), 
				"TABLE", 
				resource.getString("Dialog.table.rownumber"), 
				resource.getString("Dialog.table.columnnumber")
				);


		Optional<HashMap<String, String>> tableInfo = tableDialog.showAndWait();
		if(tableInfo.isPresent()) {
			try {
				int row = Integer.valueOf(tableInfo.get().get("Row Number")).intValue();
				int col = Integer.valueOf(tableInfo.get().get("Column Number")).intValue();


				StringBuffer tableCode = new StringBuffer();

				for (int c=0 ; c<col ; c++) {
					tableCode.append("|  ");
				}
				tableCode.append("|\n");
				for (int c=0 ; c<col ; c++) {
					tableCode.append("|---");
				}
				tableCode.append("|\n");


				for(int r=1 ; r<row ; r++) {
					for (int c=0 ; c<col ; c++) {
						tableCode.append("|  ");
					}
					tableCode.append("|\n");
				}

				TextMDCode.insertText(TextMDCode.getCaretPosition(), "\n" + tableCode.toString() + "\n");
			} catch (NumberFormatException e) {
				Alert a = new Alert(Alert.AlertType.ERROR);
				a.setTitle("Wrong numbers");
				a.setHeaderText("The row or/and column numbers are incorrect.");
				a.setContentText("Pleast input correct row and column numbers.");
				a.show();
			}
		}
	}

	@FXML
	void doHelp(ActionEvent event) {

	}

	@FXML
	void doAbout(ActionEvent event) {

	}

	@FXML
	void doNew(ActionEvent event) throws Exception {
		Stage s = new Stage();
		Main m = new Main();
		m.start(s);
	}

	@FXML
	void doOpen(ActionEvent event) {
		if(article.isModified()) {
			Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
			alert.setTitle("Save Document");
			alert.setHeaderText("Save changes in this Document?");
			alert.getButtonTypes().clear();
			alert.getButtonTypes().add(ButtonType.YES);
			alert.getButtonTypes().add(ButtonType.NO);
			alert.getButtonTypes().add(ButtonType.CANCEL);
			Optional<ButtonType> rst = alert.showAndWait();
			if(!rst.isPresent() || rst.get().equals(ButtonType.CANCEL)) {
				return;
			} else if(rst.get().equals(ButtonType.YES)) {
				doSave(event);
			}
		}

		FileChooser fc = new FileChooser();
		fc.setTitle("Open ...");
		fc.getExtensionFilters().addAll(
				new FileChooser.ExtensionFilter("MarkDown Files", "*.md", "*.markdown"),
				new FileChooser.ExtensionFilter("All Files", "*.*")
				);
		try {
			File file = fc.showOpenDialog(rootNode.getScene().getWindow());
			if(file != null) {
				FileReader fr = new FileReader(file);
				StringBuffer sb = new StringBuffer();
				int ch = 0;  

				while((ch = fr.read()) != -1){  
					sb.append((char)ch);  
				}

				article = new Article(sb.toString(), file);
				TextMDCode.replaceText(article.getMarkdownText());
				fr.close();
			}
		} catch (IOException e) {
			e.printStackTrace();
		}  
	}

	@FXML
	void doSave(ActionEvent event) {
		File file = article.getFile();
		if(file == null || file.getPath()==null || file.getPath().equals("") || file.getPath().equals("\\New File")) {
			doSaveAs(null);
			return;
		} else {
			try {
				FileWriter fw = new FileWriter(file);
				fw.write(article.getMarkdownText());
				fw.close();
				article = new Article(article.getMarkdownText(), file);
			}catch (IOException e) {
				e.printStackTrace();
			}
		}
		upDataTitle();
	}

	@FXML
	void doCopy(ActionEvent event) {
		TextMDCode.copy();
	}

	@FXML
	void doCut(ActionEvent event) {
		TextMDCode.cut();
	}

	@FXML
	void doPaste(ActionEvent event) {
		TextMDCode.paste();
	}

	@FXML
	void doUndo(ActionEvent event) {
		TextMDCode.undo();
	}

	@FXML
	void doRedo(ActionEvent event) {
		TextMDCode.redo();
	}

	@FXML
	void doShowFind(ActionEvent event) {
		codeAreaPane.setPinnedSide(Side.BOTTOM);
	}

	@FXML
	void doShowReplace(ActionEvent event) {
		replaceBox.setVisible(true);
		codeAreaPane.setPinnedSide(Side.BOTTOM);
	}

	@FXML
	void doSetting(ActionEvent event) {

	}

	private void doUpData() {

		Platform.runLater(() -> {

			article.upData(TextMDCode.getText());

			WebPreview.getEngine().executeScript("this.vue.$data.markdownCode = `" + article.getHTML4Preview(TextMDCode.getCaretPosition()) + "`");
			webHTMLCode.getEngine().executeScript("this.vue.$data.markdownCode = `" + article.getHTML() + "`");
			ASTArea.setText(article.getATS());
			upDataTitle();
		});

	}


	@FXML
	void doAutoScroll2MD(ScrollEvent event) {

	}



}
