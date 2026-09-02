package dev.bodewig.db2ascii;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.fail;

import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;
import jakarta.persistence.EntityTransaction;
import jakarta.persistence.Persistence;
import jakarta.persistence.TypedQuery;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import org.hibernate.Session;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.parallel.ResourceAccessMode;
import org.junit.jupiter.api.parallel.ResourceLock;

class Db2AsciiTest {

    protected static EntityManagerFactory emf;
    protected static EntityManager em;

    @BeforeAll
    public static void initDb() {
        emf = Persistence.createEntityManagerFactory("h2");
        em = emf.createEntityManager();

        EntityTransaction transaction = em.getTransaction();
        transaction.begin();
        Fruit banana = new Fruit();
        banana.id = 1;
        banana.name = "Banana";
        banana.color = "yellow";
        banana.price = 1.5f;
        em.persist(banana);
        transaction.commit();
    }

    @AfterAll
    public static void closeDb() {
        try {
            em.close();
        } finally {
            emf.close();
        }
    }

    @Test
    public void resultSetToString() {
        em.unwrap(Session.class).doWork(con -> {
            try (PreparedStatement stmt = con.prepareStatement("SELECT * FROM Fruit");
                 ResultSet rs = stmt.executeQuery()) {
                String result = Db2Ascii.resultSetToString(rs);
                String expected = """
                        +----+-------+--------+--------+
                        | ID | PRICE | COLOR  | NAME   |
                        +----+-------+--------+--------+
                        |  1 |   1.5 | yellow | Banana |
                        +----+-------+--------+--------+""".replaceAll("\n", System.lineSeparator());
                assertEquals(expected, result);
            } catch (SQLException e) {
                fail(e);
            }
        });
    }

    @Test
    public void resultSetToString_empty() {
        em.unwrap(Session.class).doWork(con -> {
            try (PreparedStatement stmt = con.prepareStatement("SELECT * FROM dual WHERE 1=0");
                 ResultSet rs = stmt.executeQuery()) {
                String result = Db2Ascii.resultSetToString(rs);
                String expected = """
						++
						++""".replaceAll("\n", System.lineSeparator());
                assertEquals(expected, result);
            } catch (SQLException e) {
                fail(e);
            }
        });
    }

    @Test
    public void printResultSet() {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        PrintStream ps = new PrintStream(baos);
        em.unwrap(Session.class).doWork(con -> {
            try (PreparedStatement stmt = con.prepareStatement("SELECT * FROM Fruit");
                 ResultSet rs = stmt.executeQuery()) {
                Db2Ascii.printResultSet(rs, ps);
            } catch (SQLException e) {
                fail(e);
            }
        });
        String result = baos.toString();
        String expected = """
                +----+-------+--------+--------+
                | ID | PRICE | COLOR  | NAME   |
                +----+-------+--------+--------+
                |  1 |   1.5 | yellow | Banana |
                +----+-------+--------+--------+
                """.replaceAll("\n", System.lineSeparator());
        assertEquals(expected, result);
    }

    @Test
    public void queryResultToString() {
        try {
            CriteriaBuilder cb = em.getCriteriaBuilder();
            CriteriaQuery<Fruit> cq = cb.createQuery(Fruit.class);
            cq.select(cq.from(Fruit.class));
            TypedQuery<Fruit> query = em.createQuery(cq);
            String result = Db2Ascii.queryResultToString(query);
            String expected = """
                    +---------------+----------+----+--------+--------+-------+
                    | DISCRIMINATOR | pricePer | id | name   | color  | price |
                    +---------------+----------+----+--------+--------+-------+
                    |         Fruit |     unit |  1 | Banana | yellow |   1.5 |
                    +---------------+----------+----+--------+--------+-------+""".replaceAll("\n", System.lineSeparator());
            assertEquals(expected, result);
        } catch (IllegalAccessException e) {
            fail(e);
        }
    }

    @Test
    public void queryResultToString_empty() {
        try {
            CriteriaBuilder cb = em.getCriteriaBuilder();
            CriteriaQuery<Fruit> cq = cb.createQuery(Fruit.class).where(cb.equal(cb.literal(1), cb.literal(0)));
            cq.select(cq.from(Fruit.class));
            TypedQuery<Fruit> query = em.createQuery(cq);
            String result = Db2Ascii.queryResultToString(query);
            String expected = """
						++
						++""".replaceAll("\n", System.lineSeparator());
            assertEquals(expected, result);
        } catch (IllegalAccessException e) {
            fail(e);
        }
    }

    @Test
    public void printQueryResult() {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        PrintStream ps = new PrintStream(baos);
        try {
            CriteriaBuilder cb = em.getCriteriaBuilder();
            CriteriaQuery<Fruit> cq = cb.createQuery(Fruit.class);
            cq.select(cq.from(Fruit.class));
            TypedQuery<Fruit> query = em.createQuery(cq);
            Db2Ascii.printQueryResult(query, ps);
        } catch (IllegalAccessException e) {
            fail(e);
        }
        String result = baos.toString();
        String expected = """
                +---------------+----------+----+--------+--------+-------+
                | DISCRIMINATOR | pricePer | id | name   | color  | price |
                +---------------+----------+----+--------+--------+-------+
                |         Fruit |     unit |  1 | Banana | yellow |   1.5 |
                +---------------+----------+----+--------+--------+-------+
                """.replaceAll("\n", System.lineSeparator());
        assertEquals(expected, result);
    }

    @Nested
    class Print {

        private static final PrintStream out = System.out;

        @AfterEach
        public void resetSystemOut() {
            System.setOut(out);
        }

        @Test
        @ResourceLock(value = "System.out", mode = ResourceAccessMode.READ_WRITE)
        public void printResultSet() {

            em.unwrap(Session.class).doWork(con -> {
                try (ByteArrayOutputStream baos = new ByteArrayOutputStream();
                     PrintStream ps = new PrintStream(baos);
                     PreparedStatement stmt = con.prepareStatement("SELECT * FROM Fruit");
                     ResultSet rs = stmt.executeQuery()) {
                    System.setOut(ps);
                    Db2Ascii.printResultSet(rs);
                    String result = baos.toString();
                    String expected = """
                            +----+-------+--------+--------+
                            | ID | PRICE | COLOR  | NAME   |
                            +----+-------+--------+--------+
                            |  1 |   1.5 | yellow | Banana |
                            +----+-------+--------+--------+
                            """.replaceAll("\n", System.lineSeparator());
                    assertEquals(expected, result);
                } catch (SQLException | IOException e) {
                    fail(e);
                }
            });
        }

        @Test
        @ResourceLock(value = "System.out", mode = ResourceAccessMode.READ_WRITE)
        public void printQueryResult() {
            try (ByteArrayOutputStream baos = new ByteArrayOutputStream();
                 PrintStream ps = new PrintStream(baos)) {
                System.setOut(ps);
                CriteriaBuilder cb = em.getCriteriaBuilder();
                CriteriaQuery<Fruit> cq = cb.createQuery(Fruit.class);
                cq.select(cq.from(Fruit.class));
                TypedQuery<Fruit> query = em.createQuery(cq);
                Db2Ascii.printQueryResult(query);
                String result = baos.toString();
                String expected = """
                        +---------------+----------+----+--------+--------+-------+
                        | DISCRIMINATOR | pricePer | id | name   | color  | price |
                        +---------------+----------+----+--------+--------+-------+
                        |         Fruit |     unit |  1 | Banana | yellow |   1.5 |
                        +---------------+----------+----+--------+--------+-------+
                        """.replaceAll("\n", System.lineSeparator());
                assertEquals(expected, result);
            } catch (IllegalAccessException | IOException e) {
                fail(e);
            }
        }
    }
}
