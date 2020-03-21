package common;

public enum Environment {

	PORT("4567"),
	DATABASE_HOST("localhost"),
	DATABASE_PORT("3306"),
	DATABASE_NAME(""),
	DATABASE_USER(""),
	DATABASE_PASS("");

	private String defaultValue; 

	Environment(String defaultValue) {
		this.defaultValue = defaultValue;
	}

	public String getValue() {
		final String value = System.getenv(this.name());
		return (this.exists()) ? value : this.defaultValue;
	}

	public Boolean exists() {
		final String value = System.getenv(this.name());
		return !(value == null || value.isEmpty());
	}

}
