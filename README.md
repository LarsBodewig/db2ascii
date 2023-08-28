# db2ascii

A small utility to quickly dump your database query results as a formatted ascii table.

* Supports both classic `ResultSet` objects and JPA `TypedQuery` objects.
* Offers functions to print to `System.out`, a designated `PrintStream` or return a formatted `String`


## Example output

```
+----+-------+--------+--------+
| ID | PRICE | COLOR  | NAME   |
+----+-------+--------+--------+
|  1 |   1.5 | yellow | Banana |
+----+-------+--------+--------+
```


## Usage

```xml
<dependency>
    <groupId>dev.bodewig.db2ascii</groupId>
    <artifactId>db2ascii</artifactId>
    <version>1.0.0-SNAPSHOT</version>
    <scope>test</scope>
</dependency>
```

```java
import dev.bodewig.db2ascii.Db2Ascii;

/* usage with classic java.sql.ResultSet (and java.io.PrintStream) */
Db2Ascii.printResultSet(resultSet) // prints to System.out
Db2Ascii.printResultSet(resultSet, printStream)
String formattedResult = Db2Ascii.resultSetToString(resultSet)

/* usage with JPA jakarta.persistence.TypedQuery<X> (and java.io.PrintStream) */
Db2Ascii.queryResultToString(typedQuery) // prints to System.out
Db2Ascii.queryResultToString(typedQuery, printStream)
String formattedResult = Db2Ascii.queryResultToString(typedQuery)
```

You can check out the [unit tests](https://github.com/LarsBodewig/db2ascii/blob/main/src/test/java/dev/bodewig/db2ascii/Db2AsciiTest.java) for more complete examples.


### This dependency should only be used for testing purposes!

It is licensed under GPL-3.0 but can be used without any limitation as long as it is not modified or distributed - so it should be safe to use as a Maven dependency with the scope `test` (**but this is no legal advise**).


---

Run `git config --add include.path ../.gitconfig` to include the template config in your project config.
