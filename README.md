# About
HCF is a generic DAO for database persistence.

## Dependency Management
```
<dependency>
  <groupId>br.com.hcf</groupId>
  <artifactId>hcf-data</artifactId>
  <version>3.4.0</version>
</dependency>
```
```
<repositories>
  <repository>
    <id>myMavenRepo.read</id>
    <url>https://mymavenrepo.com/repo/N6enl93StT9D1JnAX2qX/</url>
  </repository>
</repositories>
```

## hibernate.properties (optional/recommended)
```
# example
# equivalent to application.properties 

# hibernate
hibernate.connection.username=user
hibernate.connection.password=pass
hibernate.connection.driver_class=com.mysql.cj.jdbc.Driver
hibernate.connection.url=jdbc:mysql://localhost:3306/db?useTimezone=true&serverTimezone=America/Sao_Paulo
# c3p0
hibernate.c3p0.min_size=5
hibernate.c3p0.max_size=10
hibernate.c3p0.timeout=3000
hibernate.c3p0.max_statements=30
# others
hibernate.show_sql=false
hibernate.format_sql=false
hibernate.hbm2ddl.auto=update
hibernate.dialect=org.hibernate.dialect.MySQL5InnoDBDialect
hibernate.enable_lazy_load_no_trans=true
```

## How to test HCF 
```
// Product is a entity
List<Product> products = new HCFConnection<>(Product.class).all();
products.forEach(System.out::println);
```

## Built with
* Java - Language used (compiled in version 11).
* [hibernate-orm](https://github.com/hibernate/hibernate-orm) - Framework used.
* [reflections](https://github.com/ronmamo/reflections) - Framework used.

## Authors
* **Eduardo W. K. Priester** - *developer* - [github](https://github.com/Eduardo-Karpinski)

## License
This project is licensed under the GNU General Public License v3.0 - see the [LICENSE](LICENSE) file for details
