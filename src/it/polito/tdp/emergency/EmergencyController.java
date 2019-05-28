package it.polito.tdp.emergency;

import java.net.URL;
import java.time.Duration;
import java.util.ResourceBundle;

import it.polito.tdp.emergency.model.Simulatore;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;

public class EmergencyController {
	
	private Simulatore sim;

    @FXML
    private ResourceBundle resources;

    @FXML
    private URL location;

    @FXML
    private TextField txtNumStudi;

    @FXML
    private TextField txtNumPazienti;

    @FXML
    private TextField txtIntervalloPazienti;

    @FXML
    private TextArea txtResult;

    @FXML
    void doSimulazione(ActionEvent event) {
    	
    	int numPazienti = Integer.parseInt(txtNumPazienti.getText());
    	int numStudi = Integer.parseInt(txtNumStudi.getText());
    	int intervallo = Integer.parseInt(txtIntervalloPazienti.getText());
    	Duration intPazienti = Duration.ofMinutes(intervallo);
    	Simulatore sim = new Simulatore();
    	sim.setNP(numPazienti);
    	sim.setNS(numStudi);
    	sim.setT_ARRIVAL(intPazienti);
    	sim.init();
    	sim.run();
    	txtResult.appendText("Pazienti curati: "+sim.getNumDimessi()+"\n");
    	txtResult.appendText("Pazienti rinunciatari: "+sim.getNumAbbandoni()+"\n");
    	txtResult.appendText("Pazienti deceduti: "+sim.getNumMorti()+"\n");

    }

    @FXML
    void initialize() {
        assert txtNumStudi != null : "fx:id=\"txtNumStudi\" was not injected: check your FXML file 'Emergency.fxml'.";
        assert txtNumPazienti != null : "fx:id=\"txtNumPazienti\" was not injected: check your FXML file 'Emergency.fxml'.";
        assert txtIntervalloPazienti != null : "fx:id=\"txtIntervalloPazienti\" was not injected: check your FXML file 'Emergency.fxml'.";
        assert txtResult != null : "fx:id=\"txtResult\" was not injected: check your FXML file 'Emergency.fxml'.";
        
        

    }
    
    public void setSimulatore(Simulatore sim) {
    	this.sim = sim;
    	txtIntervalloPazienti.setText(sim.getT_ARRIVAL().toMinutes()+"");
        txtNumPazienti.setText(sim.getNP()+"");
        txtNumStudi.setText(sim.getNS() +"");
    }
}