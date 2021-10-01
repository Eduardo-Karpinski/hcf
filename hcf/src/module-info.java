@SuppressWarnings("module")
module br.com.hcf {
	exports br.com.hcf;
	exports br.com.hcf.enums;
	exports br.com.hcf.annotations;

	requires java.naming;
	requires reflections8; // Name of automatic module 'reflections8' is unstable
	requires java.persistence;
	
	requires transitive java.sql;
	requires transitive org.hibernate.orm.core;
}