package it.polito.tdp.emergency.model;

import java.time.Duration;
import java.time.LocalTime;
import java.util.PriorityQueue;

public class Simulatore {

	// Coda degli eventi
	private PriorityQueue<Evento> queue = new PriorityQueue<>() ;
	
	// Modello del Mondo
	
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
	
	// Variabili interne
	
}
