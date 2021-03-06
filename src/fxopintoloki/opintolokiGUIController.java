package fxopintoloki;


import java.awt.Desktop;
import java.io.IOException;
import java.io.PrintStream;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.List;
import java.util.ResourceBundle;
   
import fi.jyu.mit.fxgui.ComboBoxChooser;
import fi.jyu.mit.fxgui.Dialogs;
import fi.jyu.mit.fxgui.ListChooser;
import fi.jyu.mit.fxgui.ModalController;
import fi.jyu.mit.fxgui.TextAreaOutputStream;

import javaOpintoloki.Kurssi;
import javaOpintoloki.Opintoloki;
import javaOpintoloki.SailoException;
import javaOpintoloki.Suoritus;

import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.input.KeyCode;
import javafx.scene.layout.GridPane;
import static fxopintoloki.TietueDialogController.getFieldId; 

  
/**
 * Luokka käyttäjän käyttäliittymän kurssien hoitamiseksi.
 * @version 3.5.2019
 * @author Olli Mehtonen & Justus Uurtimo
 */
public class opintolokiGUIController implements Initializable {
	@FXML private TextField hakuehto;
	@FXML private ListChooser<Kurssi> chooserKurssi;
	@FXML private ListChooser<Suoritus> chooserSuoritus;
	@FXML private Label labelVirhe;
	@FXML private ScrollPane panelKurssi;
	@FXML private ComboBoxChooser<String> cbKentat;
	
	@FXML private TextArea areaKurssi = new TextArea();
	@FXML private TextField editOpintoPisteet;
	@FXML private TextField editSivuPaaAine;
	@FXML private TextField editLisaTiedot;
	@FXML private TextField editOpintotaso;
	@FXML private TextField editTunnus;
	
	@FXML private TextField lapi;
	@FXML private TextField hyl;
	@FXML private TextField kesken;
	@FXML private TextField kurssejaYht;
	
	@FXML private TextField opAnsaittu;
	@FXML private TextField opTavoite;
	
	@FXML private GridPane gridKurssi;
	@FXML private GridPane gridSuoritus;
	@FXML private GridPane gridOpintoKurssit;
	@FXML private GridPane gridOpintoPisteet;
	
	@Override
	public void initialize(URL url, ResourceBundle bundle) {
        alusta();
    }
	
	
	/**
	 * Tallentaa pääkäyttöliittymästä
	 */
	@FXML void tallenna() {
		tallennaTiedostoon();
	}
	
	/**
     * Jos muokkaus on ok
     */
	@FXML private void muokkausOk() {
        if ( kurssiKohdalla != null && kurssiKohdalla.getTunnus().trim().equals("") ) {
            naytaVirhe("Nimi ei saa olla tyhj�");
            return;
        }
        ModalController.closeStage(labelVirhe);
    }
	
	/**
     * Jos muokkaus ei ole ok
     */
	@FXML private void muokkausEiOk() {
		kurssiKohdalla = null;
		 ModalController.closeStage(labelVirhe);
	}
	
	
	/**
	 * Tulostaa tekstialueelle.
	 */
	@FXML void tulosta() {
		TulostusController tulostaCtrl = TulostusController.tulosta(null);
		tulostaValitut(tulostaCtrl.getTextArea());
	}
	
	
	/**
	 * tulostaa valitun kurssin v�liaikaisesti p��ikkunanan
	 */
	@FXML void tulostaPaaIkkunaan() {
		naytaKurssi();
	}
	
	
	/**
	 * Kirjautuu ulos
	 */
	@FXML void kirjauduUlos() {
		Dialogs.showMessageDialog("Ei osata vielä kirjautua ulos :(");
	}
	
	
	/**
	 * Kirjautuu sis��n p��ikkunaan
	 */
	@FXML void kirjautuminen() {
		Dialogs.showMessageDialog("Ei osata vielä kirjautua :(");
		//ModalController.showModal(opintolokiGUIController.class.getResource("opintolokiPaaIkkuna.fxml"), "Kirjaudu", null, "");
	}

	
	/**
	 * Sulkee ohjelman
	 */
	@FXML void sulje() {
		tallennaTiedostoon();
		Platform.exit();
	}
	
	
	/**
	 * Lis�� kurssin tietokantaan
	 * @throws SailoException 
	 */
	@FXML void lisaaKurssi() throws SailoException {
		uusiKurssi();
	}
	
	
	/**
	 * Muokataan kurssia
	 * @throws SailoException 
	 */
	@FXML void muokkaaKurssi() throws SailoException {
		muokkaaKurssia(1);
	}
	
	
	/**
	 * Lis�� kurssille suorituksen
	 * @throws SailoException 
	 */
	@FXML void lisaaSuoritus() throws SailoException {
		uusiSuoritus();
	}
	
	
	/**
	 * Poistaa Kurssin
	 */
	@FXML void poista() {
		poistaKurssi();
	}
	
	
	/**
	 * Antaa tietoja ohjelmasta
	 */
	@FXML void tiedot() {
		ModalController.showModal(opintolokiGUIController.class.getResource("opintolokiTiedot.fxml"), "Kurssin poistaminen", null, "");
	}
	
	@FXML void apua() {
		avustus();
	}
	
	
		//===============================================================================================================
		//Tästä eteenpäin ei käyttöliittymään suoraan liittyvää koodia
	
	
	private Opintoloki opintoloki = new Opintoloki();
	private String opintolokiNimi = "opintoloki";
	private Kurssi kurssiKohdalla;
	private Suoritus suoritusKohdalla;
	private TextField[] editKurssi;
	private TextField[] editSuoritus;
	private static Suoritus apuSuoritus = new Suoritus();
	private int kentta = 0;
	
	
	/**
     * Tekee tarvittavat muut alustukset, nyt vaihdetaan GridPanen tilalle
     * yksi iso tekstikenttä, johon voidaan tulostaa kurssin/suorituksen tiedot.
     * Alustetaan myös kurssilistan kuuntelija 
     */
	protected void alusta() {
	    chooserKurssi.clear();
	    chooserKurssi.addSelectionListener(e -> naytaKurssi());
	    
	    chooserSuoritus.clear();
	    chooserSuoritus.addSelectionListener(e -> naytaSuoritus());
	    
	    editKurssi = TietueDialogController.luoKentat(gridKurssi, new Kurssi());
	    editSuoritus = TietueDialogController.luoKentat(gridSuoritus, new Suoritus());
	        
	    for (TextField edit: editKurssi)  
	        if ( edit != null ) {  
	            edit.setEditable(false);  
	            edit.setOnMouseClicked(e -> { if ( e.getClickCount() > 1 ) muokkaaKurssia(getFieldId(e.getSource(),0)); });  
	            edit.focusedProperty().addListener((a,o,n) -> kentta = getFieldId(edit,kentta));
	            edit.setOnKeyPressed( e -> {if ( e.getCode() == KeyCode.F2 ) muokkaaKurssia(kentta);}); 
	        } 
	}
	
	 
	/**
	 * Näyttää kurssin väliaikaisesti tekstialueessa
	 */
	protected void naytaKurssi() {
        kurssiKohdalla = chooserKurssi.getSelectedObject();

        if (kurssiKohdalla == null) {
        	return;
        }
        TietueDialogController.naytaTietue(editKurssi, kurssiKohdalla);
        TietueDialogController.naytaTietue(editSuoritus, apuSuoritus);
    }
	
	
	/**
     * Näyttää virheen. 
     */
	private void naytaVirhe(String virhe) {
		if(virhe == null || virhe.isEmpty()) {
			labelVirhe.setText("");
			labelVirhe.getStyleClass().removeAll("virhe");
			return;
			
		}
		labelVirhe.setText(virhe);
		labelVirhe.getStyleClass().add("virhe");
	}
	
	
	/*private void setTitle(String title) {
		ModalController.getStage(hakuehto).setTitle(title);
	}*/
	
	
	/**
     * Hakee kurssin tiedot listaan
     * @param id kurssin numero, joka aktivoidaan haun jälkeen
     */
	protected void hae(int id) {
		chooserKurssi.clear();
		chooserSuoritus.clear();
        int index = 0;
        
        int laskuri = 0;
        for(Kurssi kurssi : opintoloki.getKunnonKurssit().values()) {
        	
        	if(id == kurssi.getId()) {
        		index = laskuri;
        		
        	}
        	
        	if((opintoloki.haeSuoritukset(kurssi)).isEmpty()) {
        		chooserKurssi.add(kurssi.getTunnus(), kurssi);
        	} else {
        		chooserSuoritus.add(kurssi.getTunnus(), opintoloki.haeSuoritukset(kurssi).get(index));
        	}
        	laskuri++;
        }
        //hakee oikeaan reunaan kurssien yhteismäärän
        kurssitYht();
        //hakee oikeaan reunaan hyv�ksytyt ja hylätyt kurssit
        arvostelut();
        //hakee oikeaan ansaitut opintopisteet ja op-tavoitteen
        opintopisteet();
        
        chooserKurssi.setSelectedIndex(index);
        chooserSuoritus.setSelectedIndex(index);
    }
	
	
	/**
     * Luo uuden kurssin(t�ll� hetkell� vakio) ja lisää sen opintolokiin
	 * @throws SailoException heitet��n SailoException
     */
	protected void uusiKurssi() throws SailoException {
		  try {
	            Kurssi uusi = new Kurssi();
	            uusi = TietueDialogController.kysyTietue(null, uusi, 1);  
	            if ( uusi == null ) return;
	            uusi.setOid();
	            opintoloki.lisaa(uusi);
	            hae(uusi.getId());
	        } catch (SailoException e) {
	            Dialogs.showMessageDialog("Ongelmia uuden luomisessa " + e.getMessage());
	            return;
	        }
		
	}
	
	
	/**
     * Muokataan kurssia. 
     */
	private void muokkaaKurssia(int k) { 
        if ( kurssiKohdalla == null ) return; 
        try { 
            Kurssi kurssi; 
            kurssi = TietueDialogController.kysyTietue(null, kurssiKohdalla.clone(), k);   
            if ( kurssi == null ) return; 
            opintoloki.korvaaTaiLisaa(kurssi); 
            hae(kurssi.getId()); 
        } catch (CloneNotSupportedException e) { 
            // 
        } catch (SailoException e) { 
            Dialogs.showMessageDialog(e.getMessage()); 
        } 
    } 
	
	
	/**
     * Poistetaan kurssi. 
     */
	private void poistaKurssi() {
		Kurssi kurssi = opintoloki.haeKurssi(suoritusKohdalla.getId());
		if ( kurssi == null ) return;
		if ( !Dialogs.showQuestionDialog("Poisto", "Poistetaanko kurssi: " + kurssi.getTunnus(), "Kyllä", "Ei") )
		     return;
		opintoloki.poista(kurssi);
		int index = chooserSuoritus.getSelectedIndex();
		hae(0);
		chooserSuoritus.setSelectedIndex(index);
	}
	
	
    /** 
     * Tekee uuden tyhjän suorituksen editointia varten 
     * Lisäksi lisää sen opintolokiin suoritukseksi id avulla
     * @throws SailoException 
     */ 
	private void uusiSuoritus() {
		Suoritus uusi = new Suoritus();
		uusi = TietueDialogController.kysyTietue(null, uusi, 1);  
		if ( uusi == null ) return;
		uusi.setOid(kurssiKohdalla.getId());
		opintoloki.lisaa(uusi);
		//chooserSuoritus.add(kurssiKohdalla.getTunnus(), uusi);
		//tähän chooserKurssista kurssin poisto
		hae(0);
	}
	
	
	/*private void naytaSuoritukset(Kurssi kurssi) {
	        if ( kurssi == null ) return;
	        
	        List<Suoritus> suoritukset = opintoloki.haeSuoritukset(kurssi);
			if ( suoritukset.size() == 0 ) return;
			for (Suoritus suor: suoritukset)
			    naytaSuoritus(suor); 
	}*/
	
	
	/**
     * Näyttää listasta valitun Suorituksen tiedot tekstikenttiin. 
     */
	private void naytaSuoritus() {
		suoritusKohdalla = chooserSuoritus.getSelectedObject();
	    if (suoritusKohdalla == null) {
	        return;
	    }
	    TietueDialogController.naytaTietue(editSuoritus, suoritusKohdalla);
	    TietueDialogController.naytaTietue(editKurssi, opintoloki.haeKurssi(suoritusKohdalla.getId()));	    
	}
	
	 
	
    /**
     * Setataan opintoloki, jota käytetään
     * @param opintoloki Opintoloki, jota käytetään tässä käyttöliittymässä
     */
	public void setOpintoloki(Opintoloki opintoloki) {
		this.opintoloki = opintoloki;
		naytaKurssi();
	}
	
	
	 /**
     * Kysytään tiedoston nimi ja luetaan se
     * @return true jos onnistui, false jos ei
	 * @throws SailoException heitetään sailo
     */
	public boolean avaa() throws SailoException {
		String uusinimi = opintolokiNimiController.kysyNimi(null, opintolokiNimi);
		if(uusinimi == null) {
			return false;
		}
		lueTiedosto(uusinimi);
		return true;
	}
	
	
	/**
	 * Tarkistetaan ollaanko tallennettu
	 * @return true, voidaan sulkea, false, jos ei voida
	 */
	public boolean voikoSulkea() {
		tallennaTiedostoon();
		return true;
	}
	
	
	/**
     * Tulostaa kurssin tiedot, lis�ksi muotoilee tulostuksen
     * @param os tietovirta johon tulostetaan
     * @param kurssi tulostettava kurssi
     */
	public void tulostus(PrintStream os, final Kurssi kurssi) {
	        os.println("----------------------------------------------");
	        kurssi.tulosta(os);
	        os.println("----------------------------------------------");
	        List<Suoritus> suoritukset = opintoloki.haeSuoritukset(kurssi);   
	        for (Suoritus suor: suoritukset)
	            suor.tulosta(os);
	}
	
	
	/**
	 * tulostaa alueelle listassa olevat kurssit tulostus metodin avulla
	 * @param teksti alue johonka tulostetaan
	 */
	public void tulostaValitut(TextArea teksti) {
		try (PrintStream os = TextAreaOutputStream.getTextPrintStream(teksti)) {
			os.println("Tulostetaan kaikki kurssit");
			for(int i = 0; i < opintoloki.KurssienMaara(); i++) {
				Kurssi kurssi = opintoloki.haeKurssi(i);
				tulostus(os, kurssi);
				os.println("\n\n");
			}
		}
	}
	
	
	/**
     * Tietojen tallennus
     * @return null jos onnistuu, muuten virhe tekstinä
     */
	private String tallennaTiedostoon() {
		try {
			opintoloki.tallenna();
			return null;
		} catch (SailoException e) {
			Dialogs.showMessageDialog("Tallennkuksessa robleema" + e.getMessage());
			return e.getMessage();
		}
	}
	
	
	/**
	 * @param nimi joka luetaan
	 * @return virhe
	 * @throws SailoException heitet��n SailoException
	 */
	protected String lueTiedosto(String nimi) throws SailoException {
		opintolokiNimi = nimi;
		//setTitle("Opintoloki - " + opintolokiNimi);
		try {
			opintoloki.lueTiedostosta(nimi);
			hae(0);
			return null;
		} catch (SailoException e) {
			hae(0);
			String virhe = e.getMessage();
			if(virhe != null) {
				Dialogs.showMessageDialog(virhe);
			}
			return virhe;
		}
	}
	
	
	private void avustus() {
		Desktop desktop = Desktop.getDesktop();
		try {
			URI uri = new URI("https://tim.jyu.fi/view/kurssit/tie/ohj2/2019k/ht/opintoloki#ohjelman-k%C3%A4ytt%C3%B6");
			desktop.browse(uri);
		} catch (URISyntaxException e) {
			return;
	    } catch (IOException e) {
				return;
		}
	}
	
	
	//===============================================================================================================
	//Tästä eteenpäin opintosuorituksiin (ohjelman oikea reuna) liittyvät asiat
	
	
	/**
	 * asettaa oikeaan reunaan kurssien kokonaism��r�n.
	 * asettaa oikeaan reunaan keskener�isten kurssien m��r�n
	 */
	public void kurssitYht() {
		int x = opintoloki.KurssienMaara();
		String y = Integer.toString(x);
		this.kurssejaYht.setText(y);
		//kesken = kaikki - hyv�ksytyt - hyl�tyt
		int kesken2 = 0;
		if(x - opintoloki.getHylatyt() - opintoloki.getHyvaksytyt() >= 0) {
			kesken2 = x - opintoloki.getHylatyt() - opintoloki.getHyvaksytyt();
		}
		
		this.kesken.setText(Integer.toString(kesken2));
	}
	
	
	/**
	 * asettaa oikeassa reunassa oleviin kenttiin hyv�ksyttyjen ja hyl�ttyjen arvon
	 */
	public void arvostelut() {
		String hylatyt = Integer.toString(opintoloki.getHylatyt());
		String hyvaksytyt = Integer.toString(opintoloki.getHyvaksytyt());
		
		this.hyl.setText(hylatyt);
		this.lapi.setText(hyvaksytyt);
	}
	
	
	/**
	 * asetetaan opintopiste tavoite ja ansaitut opintopisteet
	 */
	public void opintopisteet() {
		int tavoite = 60 - opintoloki.getOpintopisteet();
		int ansaittu = opintoloki.getOpintopisteet();
		
		this.opAnsaittu.setText(Integer.toString(ansaittu));
		this.opTavoite.setText(Integer.toString(tavoite));
	}
}