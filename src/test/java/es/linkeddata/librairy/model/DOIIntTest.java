package es.linkeddata.librairy.model;

import org.junit.Assert;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

/**
 * @author Badenes Olmedo, Carlos <cbadenes@fi.upm.es>
 */

public class DOIIntTest {

    private static final Logger LOG = LoggerFactory.getLogger(DOIIntTest.class);


    @Test
    public void getPDFs(){
        DOI doi = new DOI("10.1038/nature12373");

        List<String> pdfs = doi.getPDFs();

        Assert.assertEquals(3, pdfs.size());

        LOG.info("PDFs: " + pdfs);

    }

}
