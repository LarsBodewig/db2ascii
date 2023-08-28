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
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import org.hibernate.Session;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

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
				// @formatter:off
				String expected = "+----+-------+--------+--------+\r\n"
						+ "| ID | PRICE | COLOR  | NAME   |\r\n"
						+ "+----+-------+--------+--------+\r\n"
						+ "|  1 |   1.5 | yellow | Banana |\r\n"
						+ "+----+-------+--------+--------+";
				// @formatter:on
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
				String expected = "++\r\n++";
				assertEquals(expected, result);
			} catch (SQLException e) {
				fail(e);
			}
		});
	}

	@Test
	public void queryResultToString() {
		try {
			CriteriaBuilder cb = em.getCriteriaBuilder();
			CriteriaQuery<Fruit> cq = cb.createQuery(Fruit.class);
			cq.select(cq.from(Fruit.class));
			TypedQuery<Fruit> query = em.createQuery(cq);
			String result = Db2Ascii.queryResultToString(query);
			// @formatter:off
			String expected = "+----+--------+--------+-------+\r\n"
					+ "| id | name   | color  | price |\r\n"
					+ "+----+--------+--------+-------+\r\n"
					+ "|  1 | Banana | yellow |   1.5 |\r\n"
					+ "+----+--------+--------+-------+";
			// @formatter:on
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
			String expected = "++\r\n++";
			assertEquals(expected, result);
		} catch (IllegalAccessException e) {
			fail(e);
		}
	}
}
