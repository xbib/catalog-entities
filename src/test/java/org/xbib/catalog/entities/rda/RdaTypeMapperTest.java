package org.xbib.catalog.entities.rda;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.junit.Test;

/**
 */
public class RdaTypeMapperTest {

    private static final Logger logger = LogManager.getLogger(RdaTypeMapperTest.class);

    @Test
    public void testRdaTyepMapper() throws Exception {
        RdaTypeMapper rdaTypeMapper = new RdaTypeMapper();
        logger.info(rdaTypeMapper.getGer2Eng().toString());
    }
}
