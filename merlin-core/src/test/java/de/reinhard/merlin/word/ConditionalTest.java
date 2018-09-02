package de.reinhard.merlin.word;

import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.regex.Matcher;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;

public class ConditionalTest {
    private Logger log = LoggerFactory.getLogger(ConditionalTest.class);

    @Test
    public void parseStringList() {
        assertArrayEquals(new String[0], Conditional.parseStringList(null));
        assertArrayEquals(new String[0], Conditional.parseStringList(""));
        assertArrayEquals(new String[]{"Berta"}, Conditional.parseStringList("Berta"));
        assertArrayEquals(new String[]{"Berta"}, Conditional.parseStringList("„Berta“"));
        assertArrayEquals(new String[]{"Berta", "Horst"}, Conditional.parseStringList("„Berta“, \"Horst\""));
        assertArrayEquals(new String[]{"Berta", "Horst"}, Conditional.parseStringList("Berta, Horst"));
        assertArrayEquals(new String[]{"Berta's", "Horst"}, Conditional.parseStringList("„Berta's“, „Horst"));
        assertArrayEquals(new String[]{"A", "B", "", "D"}, Conditional.parseStringList("„A“, „B\", '', D"));
        assertArrayEquals(new String[]{"A", "B", "", "D"}, Conditional.parseStringList("„A“, „B\"  '' D"));
        assertArrayEquals(new String[]{"A', „B", "", "D"}, Conditional.parseStringList("„A', „B\"  '' D"));
    }

    @Test
    public void regexpTest() {
        assertMatcher("{if Arbeitszeit = „Teilzeit“}", "Arbeitszeit", "=", "Teilzeit");
        assertMatcher("{if Arbeitszeit = ‚Teilzeit‘}", "Arbeitszeit", "=", "Teilzeit");
        assertMatcher("{if Arbeitszeit != ‚Vollzeit‘}", "Arbeitszeit", "!=", "Vollzeit");
        assertMatcher("{if Arbeitszeit in ‚Vollzeit‘}", "Arbeitszeit", "in", "Vollzeit");
        assertMatcher("{if Arbeitszeit !in ‚Vollzeit‘}", "Arbeitszeit", "!in", "Vollzeit");
        assertMatcher("{if Arbeitszeit !in ‚Vollzeit\"}", "Arbeitszeit", "!in", "Vollzeit\"");
        assertMatcher("{if name = „Horst's“}", "name", "=", "Horst's");
    }

    private void assertMatcher(String str, String... groups) {
        Matcher matcher = Conditional.beginIfPattern.matcher(str);
        assertEquals(groups.length > 0 ? true : false, matcher.find());
        if (groups.length == 0) {
            return;
        }
        assertEquals(3, matcher.groupCount(), "Number of regexp group count.");
        assertEquals(groups[0], matcher.group(1));
        assertEquals(groups[1], matcher.group(2));
        String[] params = Conditional.parseStringList(matcher.group(3));
        assertEquals(groups.length - 2, params.length, "Number of comma separated values.");
        for (int i = 2; i < groups.length; i++) {
            assertEquals(groups[i], params[i - 2]);
        }
    }

}
