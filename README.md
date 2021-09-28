# About
[![Language grade: Java](https://img.shields.io/lgtm/grade/java/g/Eduardo-Karpinski/hcf.svg?logo=lgtm&logoWidth=18)](https://lgtm.com/projects/g/Eduardo-Karpinski/hcf/context:java) 
[![Total alerts](https://img.shields.io/lgtm/alerts/g/Eduardo-Karpinski/hcf.svg?logo=lgtm&logoWidth=18)](https://lgtm.com/projects/g/Eduardo-Karpinski/hcf/alerts/)<br/>
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
# example hibernate.properties
# mysql database used for reference
# property hibernate.c3p0 need hibernate-c3p0 dependency to work

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
* [hibernate-orm](https://github.com/hibernate/hibernate-orm) - Framework used.
* [reflections8](https://github.com/aschoerk/reflections8) - Framework used.

## Authors
* **Eduardo W. K. Priester** - *developer* - [github](https://github.com/Eduardo-Karpinski)

## License
This project is licensed under the GNU General Public License v3.0 - see the [LICENSE](LICENSE) file for details
