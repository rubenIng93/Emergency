package it.polito.tdp.emergency.model;

import java.time.Duration;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;
import java.util.PriorityQueue;

import it.polito.tdp.emergency.model.Evento.TipoEvento;
import it.polito.tdp.emergency.model.Paziente.StatoPaziente;

public class Simulatore {

	// Coda degli eventi
	private PriorityQueue<Evento> queue = new PriorityQueue<>() ;
	
	// Modello del Mondo
	private List<Paziente> pazienti;
	private PriorityQueue<Paziente> salaAttesa;
	private int studiLiberi;
	
	
	
	// Parametri di simulazione
	private int NS = 3; // numero di studi medici
	private int NP = 50; // numero di pazienti in arrivo
	private Duration T_ARRIVAL = Duration.ofMinutes(15); // intervallo di tempo tra i pazienti

	private LocalTime T_inizio = LocalTime.of(8, 0);
	private LocalTime T_fine = LocalTime.of(20, 0);

	private int DURATION_TRIAGE = 5;
	private int DURATION_WHITE = 10;
	private int DURATION_YELLOW = 15;
	private int DURATION_RED = 30;
	private int TIMEOUT_WHITE = 120;
	private int TIMEOUT_YELLOW = 60;
	private int TIMEOUT_RED = 90;

	
	// Statistiche da calcolare
	
	private int numDimessi;
	private int numAbbandoni;
	private int numMorti;
		
	// Variabili interne
	private StatoPaziente nuovoStatoPaziente;
	private Duration intervalloPolling = Duration.ofMinutes(5);
	
	public Simulatore() {
		this.pazienti = new ArrayList<>();
	}
	
	public void init() {
		//creare i pazienti
		LocalTime oraArrivo = T_inizio;
		pazienti.clear();
		for(int i = 0; i < NP; i++) {
			Paziente p = new Paziente(i + 1, oraArrivo);
			pazienti.add(p);
			oraArrivo = oraArrivo.plus(T_ARRIVAL);
		}
		
		//inizializzo la sala d'attesa vuota
		this.salaAttesa = new PriorityQueue<>(new PrioritàPaziente());
		
		//creare gli studi
		studiLiberi = NS;
		
		nuovoStatoPaziente = nuovoStatoPaziente.WAITING_WHITE;
		
		//creare gli eventi iniziali
		queue.clear();
		for(Paziente p : pazienti) {
			queue.add(new Evento(p.getOraArrivo(), TipoEvento.ARRIVO, p));
		}
		
		//lancia l'osservatore in polling
		queue.add(new Evento(T_inizio.plus(intervalloPolling), TipoEvento.POLLING, null));
		
		//resettare le statistiche
		numAbbandoni = 0;
		numDimessi = 0;
		numMorti = 0;
	}
	
	public void run() {
		
		while(!queue.isEmpty()) {
			Evento ev = queue.poll();
			//System.out.println(ev);
			/*
			if(ev.getOra().isAfter(T_fine)) se la simulazione dovesse terminare alle 20:00
				break;
			*/
			Paziente p = ev.getPaziente();
			switch(ev.getTipo()) {
			
			case ARRIVO:
				//tra 5 minuti verrà assegnato iun codice colore
				queue.add(new Evento(ev.getOra().plusMinutes(DURATION_TRIAGE), TipoEvento.TRIAGE, ev.getPaziente()));
				break;
			
			case TRIAGE:
				//Paziente p = ev.getPaziente();
				p.setStato(nuovoStatoPaziente);
				
				if(p.getStato() == StatoPaziente.WAITING_WHITE)
					queue.add(new Evento(ev.getOra().plusMinutes(TIMEOUT_WHITE), TipoEvento.TIMEOUT, p));
				else if(p.getStato() == StatoPaziente.WAITING_YELLOW)
					queue.add(new Evento(ev.getOra().plusMinutes(TIMEOUT_YELLOW), TipoEvento.TIMEOUT, p));
				else if(p.getStato() == StatoPaziente.WAITING_RED)
					queue.add(new Evento(ev.getOra().plusMinutes(TIMEOUT_RED), TipoEvento.TIMEOUT, p));
				
				salaAttesa.add(p);
				
				ruotaNuovoStatoPaziente();
				
				break;
				
			case VISITA:
				//determina il paziente con massima priorità
				Paziente pazChiamato = salaAttesa.poll();//con .poll() lo cancella
				if(pazChiamato == null)
					break;
				
				//paziente entra in studio, che diventa occupato, 
				StatoPaziente vecchio = pazChiamato.getStato();
				pazChiamato.setStato(StatoPaziente.TREATING);
				studiLiberi --;
				
				//e schedula l'uscita del paziente CURATO
				if(vecchio == StatoPaziente.WAITING_RED) {
					queue.add(new Evento(ev.getOra().plusMinutes(DURATION_RED), TipoEvento.CURATO, pazChiamato));
				}else if(vecchio == StatoPaziente.WAITING_YELLOW) {
					queue.add(new Evento(ev.getOra().plusMinutes(DURATION_YELLOW), TipoEvento.CURATO, pazChiamato));
				}else if(vecchio == StatoPaziente.WAITING_WHITE) {
					queue.add(new Evento(ev.getOra().plusMinutes(DURATION_WHITE), TipoEvento.CURATO, pazChiamato));
				}
				
				break;
				
			case CURATO:
				//paziente è fuori
				p.setStato(StatoPaziente.OUT);
				
				//aggiorna numDimessi
				numDimessi ++;
				//schedula evento VISITA "adesso"
				studiLiberi ++;
				queue.add(new Evento(ev.getOra(), TipoEvento.VISITA, null));
				
				break;			
				
			case TIMEOUT:
				//rimuovi dalla lista d'attesa
				salaAttesa.remove(p);
				
				
				if(p.getStato() == StatoPaziente.WAITING_WHITE) {
					
					p.setStato(StatoPaziente.OUT);
					numAbbandoni ++;
					
				}else if(p.getStato() == StatoPaziente.WAITING_YELLOW) {
					
					p.setStato(StatoPaziente.WAITING_RED);
					queue.add(new Evento(ev.getOra().plusMinutes(TIMEOUT_RED), TipoEvento.TIMEOUT, p));
					salaAttesa.add(p);
					
				}else if(p.getStato() == StatoPaziente.WAITING_RED) {
					
					p.setStato(StatoPaziente.BLACK);
					numMorti ++;
					
				}else {
					//System.out.println("Timeout anomalo nello stato "+p.getStato());
				}
				
				break;
				
			case POLLING:
				//verifica se ci sono pazienti in attesa con studi liberi
				if(!salaAttesa.isEmpty() && studiLiberi > 0) {
					queue.add(new Evento(ev.getOra(), TipoEvento.VISITA, null));
				}
				//rischedula se stesso
				if(ev.getOra().isBefore(T_fine)) {
					queue.add(new Evento(ev.getOra().plus(intervalloPolling), TipoEvento.POLLING, null));
				}
				
			
			}
			
		}
		
	}

	private void ruotaNuovoStatoPaziente() {
		
		if(nuovoStatoPaziente == StatoPaziente.WAITING_WHITE)
			nuovoStatoPaziente = StatoPaziente.WAITING_YELLOW;
		else if(nuovoStatoPaziente == StatoPaziente.WAITING_YELLOW)
			nuovoStatoPaziente = StatoPaziente.WAITING_RED;
		else if(nuovoStatoPaziente == StatoPaziente.WAITING_RED)
			nuovoStatoPaziente = StatoPaziente.WAITING_WHITE;
		
	}

	public int getNS() {
		return NS;
	}

	public void setNS(int nS) {
		NS = nS;
	}

	public int getNP() {
		return NP;
	}

	public void setNP(int nP) {
		NP = nP;
	}

	public Duration getT_ARRIVAL() {
		return T_ARRIVAL;
	}

	public void setT_ARRIVAL(Duration t_ARRIVAL) {
		T_ARRIVAL = t_ARRIVAL;
	}

	public int getNumDimessi() {
		return numDimessi;
	}

	public void setNumDimessi(int numDimessi) {
		this.numDimessi = numDimessi;
	}

	public int getNumAbbandoni() {
		return numAbbandoni;
	}

	public int getNumMorti() {
		return numMorti;
	}
	
	
	
}
