package de.creditreform.crefoteam.cte.tesun.bic_import_ident;

import org.slf4j.Logger;

import java.sql.*;

public class BicDbUtil {
    private final String dbConnectionConfig;
    private final Logger logger;

    public BicDbUtil(String dbConnectionConfig, Logger logger) {
        this.dbConnectionConfig = dbConnectionConfig;
        this.logger = logger;
    }

    private void deleteRecordsFromDB() throws Exception {
        /*
         * 1. In der ENE-DB folgende SQL's absetzen:
         *      DELETE FROM &umg.admin.TRANSFER_ITEM TI WHERE TI.TRANSFER_DESIGNATOR LIKE '%bic%';
         *      DELETE FROM &umg.admin.TRANSFER_METADATEN TMD WHERE TMD.TRANSFER_DESIGNATOR LIKE 'bicexport.';
         *      COMMIT;
         */
        Connection dbConnection = getDbConnection();
        executeSQL(dbConnection, "DELETE FROM TRANSFER_METADATEN WHERE TRANSFER_DESIGNATOR = 'bicexport.'");
        executeSQL(dbConnection, "DELETE FROM TRANSFER_METADATEN WHERE TRANSFER_DESIGNATOR = 'bic-import'");
        executeSQL(dbConnection, "DELETE FROM STAGING_BIC_ANFRAGE WHERE ANFRAGE_LIEFERUNG_NAME LIKE '%CsvLoaderSample-KC-Test-%'");
        executeSQL(dbConnection, "DELETE FROM BESTAND_BIC_ANFRAGE WHERE ANFRAGE_LIEFERUNG_NAME LIKE '%CsvLoaderSample-KC-Test-%'");
    }

    private void executeSQL(Connection dbConnection, String sql) throws SQLException {
        PreparedStatement preparedStatement = dbConnection.prepareStatement(sql);
        boolean execute = preparedStatement.execute();
        if (!execute) {
            throw new SQLException("Fehler beim Ausführen des SQL's :\n\t" + sql);
        }
    }

    private int getNextAnfrageNummerFromBestand() throws Exception {
        Connection dbConnection = getDbConnection();
        PreparedStatement preparedStatement = dbConnection.prepareStatement("SELECT MAX(ANFRAGE_LIEFERUNG_ZEILE) FROM BESTAND_BIC_ANFRAGE");
        ResultSet resultSet = preparedStatement.executeQuery();
        boolean next = resultSet.next();
        if (next) {
            Object object = resultSet.getObject(1);
            return Integer.valueOf(object.toString()).intValue();
        }
        return 1;
    }

    private Connection getDbConnection() throws Exception {
        String[] split = dbConnectionConfig.split("\\|");
        if (split[0] == null || split[1] == null || split[2] == null) {
            throw new RuntimeException("\n   Fehler: DB-Connection ist nicht gesetzt!");
        }
        Class.forName("oracle.jdbc.driver.OracleDriver");
        Connection dbConnection = DriverManager.getConnection(split[0], split[1], split[2]);
        return dbConnection;
    }
}
