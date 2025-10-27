# HCF [![Maven Central](https://maven-badges.herokuapp.com/maven-central/io.github.eduardo-karpinski/hcf-core/badge.svg)](https://maven-badges.herokuapp.com/maven-central/io.github.eduardo-karpinski/hcf-core)
HCF is a generic DAO for database persistence.

## Dependency Management
```
<dependencies>
    ...
    <dependency>
        <groupId>io.github.eduardo-karpinski</groupId>
        <artifactId>hcf-core</artifactId>
        <version>1.0.1</version>
    </dependency>
    ...
</dependencies>
```

## hibernate.properties
```
# example hibernate.properties
# mysql database used for reference

# hibernate
hibernate.connection.username=user
hibernate.connection.password=pass
hibernate.connection.driver_class=com.mysql.cj.jdbc.Driver
hibernate.connection.url=jdbc:mysql://localhost:3306/db?useTimezone=true&serverTimezone=America/Sao_Paulo

# others
hibernate.hbm2ddl.auto=update
hibernate.session.events.log.LOG_QUERIES_SLOWER_THAN_MS=500
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
