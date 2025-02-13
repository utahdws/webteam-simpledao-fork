package org.simpledao;

import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.dbcp2.BasicDataSource;

import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Properties;

@Slf4j
@Getter
public class SimpleDBConnection
{
    private static final String DEFAULT_PROP_FILE = "database.properties";

    @Setter
    private String databaseDriver;
    @Setter
    private String databaseURL;
    @Setter
    private String databaseUser;
    @Setter
    private String databasePassword;
    @Setter
    private boolean usingDatabasePool = false;
    private String propFile;
    @Setter
    private String jndiDSName;
    @Setter
    private Type type;

    public enum Type { PROP_FILE , JNDI_NAME }

    public SimpleDBConnection()
    {
        type = Type.PROP_FILE;
        propFile = DEFAULT_PROP_FILE;
        readPropFile();
    }

    public SimpleDBConnection( Type type,String namespace )
    {
        log.debug("new SimpleDBConnection with type '{}' and namespace '{}'", type, namespace);

        this.type = type;
        switch ( type )
        {
            case PROP_FILE:
                this.propFile = namespace;
                readPropFile();
                break;
            case JNDI_NAME:
                this.jndiDSName = namespace;
        }
    }

    public void setPropFile(String propFile)
    {
        this.propFile = propFile;
        readPropFile();
    }

    public Connection getDBConnection() throws SQLException
    {
        log.debug("getDBConnection()");

        if ( jndiDSName == null || jndiDSName.isEmpty())
        {
            if ( usingDatabasePool )
            {
                return getPooledDBConnection();
            }
            else
            {
                return getSingleDBConnection();
            }
        }
        else
        {
            return getJNDIDBConnection();
        }
    }

    private Connection getJNDIDBConnection() throws SQLException
    {
        if ( "".equals(jndiDSName ) || jndiDSName == null)
        {
            throw new RuntimeException("A JNDI Datasource name must be specified to use the JNDI Connection");
        }

        log.debug("getJNDIDBConnection() jndiDSName: {}", jndiDSName);

        DataSource ds;
        try
        {

            Context ctx = new InitialContext();
            ds = (DataSource) ctx.lookup( jndiDSName );
        }
        catch (NamingException ne)
        {
            log.error("The JNDI Datasource named '" + jndiDSName + "' was not found", ne);
            throw new RuntimeException("The JNDI Datasource name specified '" + jndiDSName + "' was not found");
        }

        return ds.getConnection();
    }

    private void readPropFile()
    {
        Properties props = new Properties();
        try
        {
            props.load( Thread.currentThread().getContextClassLoader().getResourceAsStream( propFile ) );
        }
        catch (Exception e)
        {
            if ( propFile.equals( DEFAULT_PROP_FILE ) )
            {
                // no default prop file found, return
                return;
            }
            else
            {
                log.error("Unable to load the properties file '" + propFile + "'", e);
                throw new RuntimeException("Unable to load the properties file '" + propFile + "'");
            }
        }
        databaseURL = props.getProperty( "url" );
        databaseUser = props.getProperty( "user" );
        databasePassword = props.getProperty( "password" );
        databaseDriver = props.getProperty( "driver" );
        jndiDSName = props.getProperty("jndiDSName");
        String useDatabasePool = props.getProperty("usePool");
        usingDatabasePool = "true".equalsIgnoreCase(useDatabasePool) || "yes".equalsIgnoreCase(useDatabasePool);
    }

    private Connection getPooledDBConnection() throws SQLException
    {
        log.debug("get a pooled database connection");
        BasicDataSource ds = new BasicDataSource();
        ds.setUrl(databaseURL);
        ds.setUsername(databaseUser);
        ds.setPassword(databasePassword);
        ds.setInitialSize( 10 );
        ds.setMaxIdle( 5 );
        ds.setDriverClassName( databaseDriver );
        return ds.getConnection();
    }

    private Connection getSingleDBConnection() throws SQLException
    {
        log.debug("get a non-pooled database connection");
        try
        {
            Class.forName( databaseDriver);
        }
        catch (ClassNotFoundException e)
        {
            log.error("Unable to load teh database driver '{}'", databaseDriver, e);
            throw new RuntimeException("Unable to load the database driver specified '" + databaseDriver + "'");
        }

        return DriverManager.getConnection( databaseURL, databaseUser, databasePassword);
    }

    public void closeDBConnection(Connection con)
    {
        try
        {
            con.close();
        }
        catch (Exception e) {/**/}
    }

}
