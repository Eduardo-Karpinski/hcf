# HCF
DAO genérica para persistência de banco de dados.

## Começando
Para começar a desenvolver usando o HCF você precisara importar a dependencia do mesmo
via jar ou maven, tambem precisara adicionar em seu projeto o arquivo hibernate.properties que
contem as informações que o hibernate ira seguir para realizar a conexão com o banco de dados.

## Pré-requisitos
**1º passo** deve-se importar o HCF
```
<dependency>
  <groupId>br.com.hcf</groupId>
  <artifactId>hcf-data</artifactId>
  <version>3.1.4</version>
</dependency>
```
**2º passo** deve-se criar a sessão **repositories** e adicinar o repositório do HCF
```
<repositories>
  <repository>
    <id>myMavenRepo.read</id>
    <url>https://mymavenrepo.com/repo/N6enl93StT9D1JnAX2qX/</url>
  </repository>
</repositories>
```
**3º passo (opcional/recomendado)** deve-se criar o arquivo **hibernate.properties** no projeto
```
# Arquivo exemplo usando banco de dados mysql
# Caso queira que as propriedades do c3p0 funcione deve-se importar sua respectiva dependencia

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
# outros
hibernate.show_sql=false
hibernate.format_sql=false
hibernate.hbm2ddl.auto=update
hibernate.dialect=org.hibernate.dialect.MySQL5InnoDBDialect
hibernate.enable_lazy_load_no_trans=true
```

## Executando os testes
Para testar a api basta executar uma chamada simples do HCF e se tudo estiver
configurado de forma correta o teste sera um sucesso, lembrando que a class deve estar
com a anotação Entity implementada.
```
List<Product> products = new HCFConnection<>(Product.class).all();
products.forEach(System.out::println);
```
## Construído com
* [Java] - Linguagem usada.
* [hibernate-orm](https://github.com/hibernate/hibernate-orm) - Framework usado.

## Authors
* **Eduardo W. K. Priester** - *desenvolvedor* - [github](https://github.com/Eduardo-Karpinski)

## License
This project is licensed under the GNU General Public License v3.0 - see the [LICENSE](LICENSE) file for details
