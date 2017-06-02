package dtu.robboss.app;

public enum Valuta {
	
	DKK(1),
	EUR(0.13),
	USD(0.15),
	GBP(0.12),
	JPY(16.81);
	
	Valuta(double value){
		this.value = value;
	}
	public double value;
	
	public static String convert(double value, Customer customer){
		double newValue = Math.round(value*customer.getCurrency().value*100.0)/100.0;
		return newValue + " " + customer.getCurrency().name();
	}
	
	public static double revert(double value, Customer customer){
		return Math.round((value/customer.getCurrency().value)*1000.0)/1000.0;
	}
}
