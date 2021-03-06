package fxopintoloki;
	
import javafx.application.Application;
import javafx.application.Platform;
import javafx.stage.Stage;
import javaOpintoloki.Opintoloki;
import javafx.scene.Scene;
import javafx.scene.layout.Pane;
import javafx.fxml.FXMLLoader;

/**
 * @author Olli Mehtonen ja Justus Uurtimo
 * @version 3.5.2019
 * 
 * Pääohjelma Opintoloki-ohjelman käynnistämiseksi
 */
public class OpintolokiMain extends Application {
	@Override
    public void start(Stage primaryStage) {
        try {
            final FXMLLoader ldr = new FXMLLoader(getClass().getResource("opintolokiPaaIkkuna.fxml"));
            final Pane root = (Pane)ldr.load();
            final opintolokiGUIController OpintolokiCtrl = (opintolokiGUIController)ldr.getController();

            final Scene scene = new Scene(root);
            scene.getStylesheets().add(getClass().getResource("fxopintoloki.css").toExternalForm());
            primaryStage.setScene(scene);
            primaryStage.setTitle("Opintoloki");

            primaryStage.setOnCloseRequest((event) -> {
                    if ( !OpintolokiCtrl.voikoSulkea() ) event.consume();
                });
            
            Opintoloki Opintoloki = new Opintoloki();  
            OpintolokiCtrl.setOpintoloki(Opintoloki); 
            
            primaryStage.show();
            
            
            Application.Parameters params = getParameters();
            if ( params.getRaw().size() > 0 ) 
                OpintolokiCtrl.lueTiedosto(params.getRaw().get(0));  
            else
                if ( !OpintolokiCtrl.avaa() ) Platform.exit();
            
        } catch(Exception e) {
            e.printStackTrace();
        }
    }
	
	
    /**
     * Käynnistetään käyttöliittymä 
     * @param args komentorivin parametrit
     */
	public static void main(String[] args) {
		launch(args);
	}
}
