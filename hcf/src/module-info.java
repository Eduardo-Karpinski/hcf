@SuppressWarnings("module")
module hibernateConnectorFacilitator {
	exports br.com.hcf;
	exports br.com.hcf.enums;
	exports br.com.hcf.annotations;
	
	requires reflections8; // Name of automatic module 'reflections8' is unstable
	requires java.naming;
	requires java.persistence;
	
	requires transitive java.sql;
	requires transitive org.hibernate.orm.core;
}