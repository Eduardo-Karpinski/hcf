module hibernateConnectorFacilitator {
	exports br.com.hcf;
	exports br.com.hcf.enums;
	
	requires java.naming;
	requires java.persistence;
	
	requires transitive java.sql;
	requires transitive org.hibernate.orm.core;
}