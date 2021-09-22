@SuppressWarnings("module")
module hibernateConnectorFacilitator {
	opens br.com.hcf to org.hibernate.orm.core;
	
	exports br.com.hcf;
	exports br.com.hcf.enums;
	exports br.com.hcf.annotations;
	
	requires reflections; // Name of automatic module 'reflections' is unstable
	requires java.naming;
	requires java.persistence;
	
	requires transitive java.sql;
	requires transitive org.hibernate.orm.core;
}
