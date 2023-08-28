package dev.bodewig.db2ascii;

import com.github.freva.asciitable.AsciiTable;
import jakarta.persistence.TypedQuery;
import java.io.PrintStream;
import java.lang.reflect.Field;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * Collection of utility functions to dump database query results as a formatted
 * ascii table.
 */
public class Db2Ascii {

	private Db2Ascii() {
	}

	/**
	 * Prints the contents of the {@code ResultSet} as a formatted ascii table to
	 * {@code System.out}
	 * <p>
	 * An empty result prints an ascii table with no cells
	 * 
	 * @param rs
	 *            an open {@code ResultSet} to read contents from
	 * @throws SQLException
	 *             if reading or navigating the {@code ResultSet} fails
	 */
	public static void printResultSet(ResultSet rs) throws SQLException {
		printResultSet(rs, System.out);
	}

	/**
	 * Prints the contents of the {@code ResultSet} as a formatted ascii table to
	 * the {@code PrintStream}
	 * <p>
	 * An empty result prints an ascii table with no cells
	 * 
	 * @param rs
	 *            an open {@code ResultSet} to read contents from
	 * @param out
	 *            an open {@code PrintStream} to write to
	 * @throws SQLException
	 *             if reading or navigating the {@code ResultSet} fails
	 */
	public static void printResultSet(ResultSet rs, PrintStream out) throws SQLException {
		String ascii = resultSetToString(rs);
		out.println(ascii);
	}

	/**
	 * Returns the contents of the {@code ResultSet} as a formatted ascii table
	 * {@code String}
	 * <p>
	 * An empty result returns an ascii table with no cells
	 * 
	 * @param rs
	 *            an open {@code ResultSet} to read contents from
	 * @return the formatted ascii table
	 * @throws SQLException
	 *             if reading or navigating the {@code ResultSet} fails
	 */
	public static String resultSetToString(ResultSet rs) throws SQLException {
		ResultSetMetaData rsmd = rs.getMetaData();
		int columnCount = rsmd.getColumnCount();

		String[] headers = new String[columnCount];
		for (int i = 0; i < columnCount; i++) {
			headers[i] = rsmd.getColumnLabel(i + 1);
		}

		ArrayList<Object[]> rows = new ArrayList<>();
		while (rs.next()) {
			Object[] values = new Object[columnCount];
			for (int i = 0; i < columnCount; i++) {
				values[i] = rs.getObject(i + 1);
			}
			rows.add(values);
		}
		Object[][] data = rows.toArray(new Object[rows.size()][columnCount]);

		return AsciiTable.getTable(headers, data);
	}

	/**
	 * Prints the result of the SELECT query as a formatted ascii table to
	 * {@code System.out}
	 * <p>
	 * An empty result prints an ascii table with no cells
	 * 
	 * @param query
	 *            a {@code TypedQuery} to execute and read results from
	 * @throws IllegalArgumentException
	 *             if the reflective access on the result list fails
	 * @throws IllegalAccessException
	 *             if the reflective access on the result list fails
	 */
	public static void printQueryResult(TypedQuery<?> query) throws IllegalArgumentException, IllegalAccessException {
		printQueryResult(query, System.out);
	}

	/**
	 * Prints the results of the SELECT query as a formatted ascii table to the
	 * {@code PrintStream}
	 * <p>
	 * An empty result prints an ascii table with no cells
	 * 
	 * @param query
	 *            a {@code TypedQuery} to execute and read results from
	 * @param out
	 *            an open {@code PrintStream} to write to
	 * @throws IllegalArgumentException
	 *             if the reflective access on the result list fails
	 * @throws IllegalAccessException
	 *             if the reflective access on the result list fails
	 */
	public static void printQueryResult(TypedQuery<?> query, PrintStream out)
			throws IllegalArgumentException, IllegalAccessException {
		String ascii = queryResultToString(query);
		out.println(ascii);
	}

	/**
	 * Returns the results of the SELECT query as a formatted ascii table
	 * {@code String}
	 * <p>
	 * An empty result returns an ascii table with no cells
	 * 
	 * @param query
	 *            a {@code TypedQuery} to execute and read results from
	 * @return the formatted ascii table
	 * @throws IllegalArgumentException
	 *             if the reflective access on the result list fails
	 * @throws IllegalAccessException
	 *             if the reflective access on the result list fails
	 */
	public static String queryResultToString(TypedQuery<?> query)
			throws IllegalArgumentException, IllegalAccessException {
		List<?> results = query.getResultList();
		if (results.isEmpty()) {
			return AsciiTable.getTable(new Object[][]{});
		}

		int resultCount = results.size();
		Object testResult = results.get(0);
		Class<?> clazz = testResult.getClass();
		int clazzFieldCount = clazz.getFields().length;
		ArrayList<String> fieldNames = new ArrayList<>(clazzFieldCount);
		ArrayList<Field> fields = new ArrayList<>(clazzFieldCount);
		for (int i = 0; i < clazzFieldCount; i++) {
			Field field = clazz.getFields()[i];
			if (field.canAccess(testResult)) { // also filters static fields
				fieldNames.add(field.getName());
				fields.add(field);
			}
		}
		int fieldCount = fields.size();
		String[] headers = fieldNames.toArray(new String[fieldCount]);

		Object[][] data = new Object[resultCount][fieldCount];
		Iterator<?> itr = results.iterator();
		for (int i = 0; i < resultCount; i++) {
			Object result = itr.next();
			for (int j = 0; j < fieldCount; j++) {
				data[i][j] = fields.get(j).get(result);
			}
		}

		return AsciiTable.getTable(headers, data);
	}
}
