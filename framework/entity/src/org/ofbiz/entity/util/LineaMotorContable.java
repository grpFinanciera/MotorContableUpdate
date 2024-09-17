package org.ofbiz.entity.util;

import java.math.BigDecimal;
import java.sql.Timestamp;
import java.util.Map;

import org.ofbiz.entity.GenericValue;

public class LineaMotorContable {
	
	private String clavePresupuestal;
	private BigDecimal montoPresupuestal;
	private Map<String, GenericValue> lineasPresupuestales;
	private Map<String, GenericValue> lineasContables;
	private Timestamp fecha;
	
	//Campo  auxiliar , no es necesario para el motor
	private String indice;
	
	public String getClavePresupuestal() {
		return clavePresupuestal;
	}
	public void setClavePresupuestal(String clavePresupuestal) {
		this.clavePresupuestal = clavePresupuestal;
	}
	public BigDecimal getMontoPresupuestal() {
		return montoPresupuestal;
	}
	public void setMontoPresupuestal(BigDecimal montoPresupuestal) {
		this.montoPresupuestal = montoPresupuestal;
	}
	public Map<String, GenericValue> getLineasPresupuestales() {
		return lineasPresupuestales;
	}
	public void setLineasPresupuestales(
			Map<String, GenericValue> lineasPresupuestales) {
		this.lineasPresupuestales = lineasPresupuestales;
	}
	public Map<String, GenericValue> getLineasContables() {
		return lineasContables;
	}
	public void setLineasContables(Map<String, GenericValue> lineasContables) {
		this.lineasContables = lineasContables;
	}
	public Timestamp getFecha() {
		return fecha;
	}
	public void setFecha(Timestamp fecha) {
		this.fecha = fecha;
	}
	public String getIndice() {
		return indice;
	}
	public void setIndice(String indice) {
		this.indice = indice;
	}
	@Override
	public String toString() {
		return this.clavePresupuestal+" $"+this.montoPresupuestal+" "+
				(this.lineasPresupuestales == null ? "" : this.lineasPresupuestales.values().toString())+" "+this.fecha+" ||"+
				(this.lineasContables == null ? "" : this.lineasContables.values().toString())+"||";
	}
	
	public LineaMotorContable clone(){  
	    LineaMotorContable clon = new LineaMotorContable();
	    clon.setClavePresupuestal(this.clavePresupuestal);
	    clon.setFecha(this.fecha);
	    clon.setIndice(this.indice);
	    clon.setLineasContables(this.lineasContables);
	    clon.setLineasPresupuestales(this.lineasPresupuestales);
	    clon.setMontoPresupuestal(this.montoPresupuestal);
	    return clon;
	}
	
}
