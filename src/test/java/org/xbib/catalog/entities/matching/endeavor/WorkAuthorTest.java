package org.xbib.catalog.entities.matching.endeavor;

import org.junit.Assert;
import org.junit.Test;

public class WorkAuthorTest extends Assert {

    @Test
    public void test() throws Exception {
        assertFalse(new WorkAuthor().blacklist().isEmpty());
    }

    @Test
    public void testAuthor() throws Exception {
        String workAuthor = new WorkAuthor().authorName("Jörg Prante").workName("Hello World").createIdentifier();
        assertEquals(workAuthor, "wHeoWrD.aPranteJ");
    }

    @Test
    public void testAuthorForeName() throws Exception {
        String workAuthor = new WorkAuthor().authorNameWithForeNames("Prante", "Jörg").workName("Hello World").createIdentifier();
        assertEquals(workAuthor, "wHeoWrD.aPranteJ");
    }

    @Test
    public void testAuthorInitials() throws Exception {
        String workAuthor = new WorkAuthor().authorNameWithInitials("Prante", "J").workName("Hello World").createIdentifier();
        assertEquals(workAuthor, "wHeoWrD.aPranteJ");
    }

    @Test
    public void testMedline() throws Exception {
        String workAuthor = new WorkAuthor()
                .workName("Critical involvement of macrophage infiltration in the development of sjögren's syndrome-associated dry eye")
                .authorNameWithForeNames("Zhou", "Delou")
                .authorNameWithForeNames("Chen", "Ying-Ting")
                .authorNameWithForeNames("Chen", "Feeling")
                .authorNameWithForeNames("Gallup", "Marianne")
                .authorNameWithForeNames("Vijmasi", "Trinka")
                .authorNameWithForeNames("Bahrami", "Ahmad F")
                .authorNameWithForeNames("Noble", "Lisa B")
                .authorNameWithForeNames("van Rooijen", "Nico")
                .authorNameWithForeNames("McNamara", "Nancy A")
                .chronology("2012")
                .createIdentifier();
        assertEquals(workAuthor, "wCOMphgTDSjdE.aZuDYFgpMvjsTrAfLRN.2012");
    }

    @Test
    public void testMedline2() throws Exception {
        String workAuthor = new WorkAuthor()
                .workName("Ethics, Professionalism, and Rheumatology")
                .authorNameWithForeNames("Romain", "Paul L")
                .authorNameWithForeNames("Dorff", "Elliot N")
                .authorNameWithForeNames("Rajbhandary", "Rosy")
                .authorNameWithForeNames("Panush", "Richard S")
                .chronology("2013")
                .createIdentifier();
        assertEquals(workAuthor, "wEthcPrfenalmAdRugY.aomiPldfEjbhypusS.2013");

        workAuthor = new WorkAuthor()
                .workName("Ethics, Professionalism, and Rheumatology")
                .authorNameWithForeNames("Paul L. Romain", null)
                .authorNameWithForeNames("Elliot Dorff", null)
                .authorNameWithForeNames("Rosy Rajbhandary", null)
                .authorNameWithForeNames("Richard S. Panush", null)
                .chronology("2013")
                .createIdentifier();
        assertEquals(workAuthor, "wEthcPrfenalmAdRugY.aomiPldfEjbhypusS.2013");

        workAuthor = new WorkAuthor()
                .workName("Ethics, Professionalism, and Rheumatology")
                .authorName("Paul L. Romain")
                .authorName("Elliot Dorff")
                .authorName("Rosy Rajbhandary")
                .authorName("Richard S. Panush")
                .chronology("2013")
                .createIdentifier();
        assertEquals(workAuthor, "wEthcPrfenalmAdRugY.aomiPldfEjbhypusS.2013");

        workAuthor = new WorkAuthor()
                .workName("Ethics, Professionalism, and Rheumatology")
                .authorName("Romain, Paul L.")
                .authorName("Dorff, Elliot")
                .authorName("Rajbhandary, Rosy")
                .authorName("Panush, Richard S.")
                .chronology("2013")
                .createIdentifier();
        assertEquals(workAuthor, "wEthcPrfenalmAdRugY.aomiPldfEjbhypusS.2013");

        workAuthor = new WorkAuthor()
                .workName("Ethics, Professionalism, and Rheumatology")
                .authorName("Romain, P L")
                .authorName("Dorff, E")
                .authorName("Rajbhandary, R")
                .authorName("Panush, R S")
                .chronology("2013")
                .createIdentifier();
        assertEquals(workAuthor, "wEthcPrfenalmAdRugY.aomiPldfEjbhypusS.2013");
    }
}
