package io.WangcongRex.view;

import java.util.ArrayList;
import java.util.HashMap;

import de.jensd.fx.glyphs.fontawesome.FontAwesomeIcon;
import javafx.scene.Node;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Dialog;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.GridPane;

public class inputDialog {

	public static Dialog<HashMap<String,String>> dialogFactory(String title, Node icon, String... contants ){
		Dialog<HashMap<String,String>> dialog = new Dialog<HashMap<String,String>>();
		GridPane dp = new GridPane();
		dp.setHgap(10);
		dp.setVgap(10);
		ArrayList<TextField> tfs = new ArrayList<TextField>();
		
		for(int i=0 ; i<contants.length ; i++) {
			dp.add(new Label(contants[i]+": "), 0, i);
			tfs.add(new TextField());
			dp.add(tfs.get(i),1,i);
		}

		dialog.setTitle(title);
		dialog.setGraphic(icon);
		dialog.getDialogPane().setContent(dp);
		dialog.getDialogPane().getButtonTypes().add(ButtonType.OK);
		dialog.getDialogPane().getButtonTypes().add(ButtonType.CANCEL);
		
		dialog.setResultConverter((button) -> {
			if(ButtonType.OK.equals(button)) {
				HashMap<String, String> rst = new HashMap<String, String>();
				for(int i=0 ; i<contants.length ; i++) {
					rst.put(contants[i], tfs.get(i).getText());
				}
				return rst;
			}
			return null;
		});
			
		return dialog;
		
	}
	
	public static Dialog<HashMap<String,String>> dialogFactory(String title, String iconName, String... contants ){
		FontAwesomeIcon icon = new FontAwesomeIcon();
		icon.setIconName(iconName);
		icon.setSize("3em");
		
		return dialogFactory(title, icon, contants);
	}
}
