package org.simpledao;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class SimpleDBConnectionTest {
    @Test
    void testDefaultConstructor() {
        org.simpledao.SimpleDBConnection conn = new org.simpledao.SimpleDBConnection();
        assertNotNull(conn);
        assertEquals(org.simpledao.SimpleDBConnection.Type.PROP_FILE, conn.getType());
    }
}

