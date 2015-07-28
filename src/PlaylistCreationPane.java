import javafx.geometry.Insets;
import javafx.scene.control.Button;
import javafx.scene.control.RadioButton;
import javafx.scene.control.TextField;
import javafx.scene.control.ToggleGroup;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.text.Text;

public class PlaylistCreationPane extends VBox {
	private String name = "";

	private HBox textPane;
	private RadioButton rb1;
	private RadioButton rb2;
	private boolean isFinished = false;

	public PlaylistCreationPane(String name, boolean radioButtons) {
		this.name = name;

		setPadding(new Insets(4));

		// Text pane
		textPane = new HBox();
		textPane.setPadding(new Insets(4));
		Text label = new Text("Name: ");
		TextField tf = new TextField(name);
		tf.setMinWidth(200);

		// Radio Buttons
		final ToggleGroup group = new ToggleGroup();
		group.selectedToggleProperty().addListener((ov, toggle1, toggle2) -> {
			invertRadioButtons();
		});

		rb1 = new RadioButton("Create one playlist");
		rb1.setToggleGroup(group);
		rb1.setSelected(true);

		rb2 = new RadioButton("Create seperate playlist for each folder");
		rb2.setToggleGroup(group);

		if (!radioButtons) {
			rb1.setManaged(false);
			rb1.setVisible(false);
			rb2.setManaged(false);
			rb2.setVisible(false);
		}

		// Buttons
		HBox buttonPane = new HBox();
		buttonPane.setPadding(new Insets(4));
		Button createBtn = new Button("Create");
		Button cancelBtn = new Button("Cancel");

		createBtn.setOnAction(e -> {
			// TODO Try to create playlist
			if (FileUtils.isNameCorrect(tf.getText())) {
				this.name = tf.getText();
				isFinished = true;
				PlaylistPane.closePlaylistCreation();
			}
		});
		cancelBtn.setOnAction(e -> {
			PlaylistPane.closePlaylistCreation();
		});

		// Adding nodes to parent node
		textPane.getChildren().addAll(label, tf);
		buttonPane.getChildren().addAll(createBtn, cancelBtn);
		getChildren().addAll(textPane, rb1, rb2, buttonPane);
	}

	public String getResult() {
		return name;
	}

	public boolean getRadio() {
		return rb1.isSelected();
	}
	public boolean isFinished(){
		return isFinished;
	}

	private void invertRadioButtons() {
		if (rb2 != null) {
			if (rb2.isSelected()) {
				textPane.setDisable(true);
			} else {
				textPane.setDisable(false);
			}
		}
	}
}
