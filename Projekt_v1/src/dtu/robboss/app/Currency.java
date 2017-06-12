package dtu.robboss.app;

import java.sql.SQLException;
import java.text.DecimalFormat;

public enum Currency {

	DKK(1), EUR(0.13), USD(0.15), GBP(0.12), JPY(16.81);

	Currency(double value) {
		this.value = value;
	}

	public double value;

	public static String convert(double value, Customer customer) {
		double newValue = Math.round(value * customer.getCurrency().value * 100.0) / 100.0;

		// format from exponential notation to regular notation, e.g.
		// 1.2E3 to 1200
		DecimalFormat df = new DecimalFormat("#");
		df.setMaximumFractionDigits(32);
		String newValueString = df.format(newValue);

		return newValueString + " " + customer.getCurrency().name();
	}

	public static double revert(double value, Customer customer) {
		return Math.round((value / customer.getCurrency().value) * 1000.0) / 1000.0;
	}
	
	/**
	 * Converts a string representing a currency (e.g. "DKK") into the
	 * corresponding enum Valuta object.
	 * 
	 * @param currencyString
	 * @return If the string is not a valid currency, returns null.
	 * 
	 * @throws SQLException
	 */
	public static Currency currencyStringToEnum(String currencyString) {
		Currency currency = null;
		switch (currencyString) {
		case "EUR":
			currency = Currency.EUR;
			break;
		case "USD":
			currency = Currency.USD;
			break;
		case "GBP":
			currency = Currency.GBP;
			break;
		case "JPY":
			currency = Currency.JPY;
			break;
		default:
			currency = Currency.DKK;
		}
		return currency;
	}
}
