# HCF [![Maven Central](https://maven-badges.herokuapp.com/maven-central/io.github.eduardo-karpinski/hcf-core/badge.svg)](https://maven-badges.herokuapp.com/maven-central/io.github.eduardo-karpinski/hcf-core)
HCF is a generic DAO for database persistence.

## Dependency Management
```
<dependencies>
    ...
    <dependency>
        <groupId>io.github.eduardo-karpinski</groupId>
        <artifactId>hcf-core</artifactId>
        <version>1.0.0</version>
    </dependency>
    ...
</dependencies>
```

## hibernate.properties
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
hibernate.enable_lazy_load_no_trans=false
hibernate.dialect=org.hibernate.dialect.MySQL5InnoDBDialect
hibernate.session.events.log.LOG_QUERIES_SLOWER_THAN_MS=500
hibernate.jdbc.batch_size=20
```

## HCF Wiki
https://github.com/Eduardo-Karpinski/hcf/wiki

## Built with
* [hibernate-orm](https://github.com/hibernate/hibernate-orm) - Framework used.
* [reflections](https://github.com/ronmamo/reflections) - Framework used.

## Authors
* **Eduardo W. K. Priester** - *developer* - [github](https://github.com/Eduardo-Karpinski)

## License
This project is licensed under the GNU General Public License v3.0 - see the [LICENSE](LICENSE) file for details
