package org.purejava.portal;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

class UpdatePortalTest {

    private static final Logger LOG = LoggerFactory.getLogger(UpdatePortalTest.class);
    private Context context;
    private UpdatePortal portal;

    @BeforeEach
    void setUp() {
        context = new Context();
        context.ensureService();
    }

    @AfterEach
    void tearDown() {
        context.after();
        portal.close();
    }

    @Test
    void getVersion() {
        portal = new UpdatePortal();
        LOG.info(String.valueOf(portal.getVersion()));
    }

    @Test
    void getSupports() {
        portal = new UpdatePortal();
        LOG.info(String.valueOf(portal.getSupports()));
    }
}