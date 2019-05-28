package it.polito.tdp.emergency.model;

import java.util.Comparator;

import it.polito.tdp.emergency.model.Paziente.StatoPaziente;

public class Priorit�Paziente implements Comparator<Paziente> {

	@Override
	public int compare(Paziente p1, Paziente p2) {
		
		if(p1.getStato() == StatoPaziente.WAITING_RED && p2.getStato() != StatoPaziente.WAITING_RED)
			return -1;
		if(p1.getStato() != StatoPaziente.WAITING_RED && p2.getStato() == StatoPaziente.WAITING_RED)
			return +1;
		
		if(p1.getStato() == StatoPaziente.WAITING_YELLOW && p2.getStato() != StatoPaziente.WAITING_YELLOW)
			return -1;
		if(p1.getStato() != StatoPaziente.WAITING_YELLOW && p2.getStato() == StatoPaziente.WAITING_YELLOW)
			return +1;
		
		return p1.getOraArrivo().compareTo(p2.getOraArrivo());
		
		
	}

}
