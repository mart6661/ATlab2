package lab2;

import static org.mockito.Mockito.*;

import java.math.BigDecimal;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

import org.hamcrest.BaseMatcher;
import org.hamcrest.Description;
import org.hamcrest.Matcher;
import org.junit.Test;
import org.laughingpanda.beaninject.Inject;

public class InvoiceRowGeneratorTest {

    private InoviceRowGenerator generator;
    private InvoiceRowDao invoiceRowDao;

    private void makeNewGenerator()
    {
        generator = new InoviceRowGenerator();
        invoiceRowDao = mock(InvoiceRowDao.class);
        Inject.bean(generator).with(invoiceRowDao);
    }

    @Test // - summa jagatakse õigesti;
    public void paymentAmountsAreCorrect() throws Exception {
        makeNewGenerator();

        generator.generateRowsFor(10, asDate("2012-02-15"), asDate("2012-04-02"));

        verify(invoiceRowDao, times(2)).save(argThat(getMatcherForSum(new BigDecimal(3))));
        verify(invoiceRowDao).save(argThat(getMatcherForSum(new BigDecimal(4))));
        verifyNoMoreInteractions(invoiceRowDao);
    }

    @Test // - kuupäevad on õiged; - ühele päevale ei tule kaks arvet;
    public void paymentDatesAreCorrect() throws Exception {
        makeNewGenerator();

        generator.generateRowsFor(10, asDate("2012-02-15"), asDate("2012-04-02"));

        verify(invoiceRowDao, atMost(1)).save(argThat(getMatcherForDate("2012-02-15")));
        verify(invoiceRowDao, atMost(1)).save(argThat(getMatcherForDate("2012-03-01")));
        verify(invoiceRowDao, atMost(1)).save(argThat(getMatcherForDate("2012-04-01")));
        verifyNoMoreInteractions(invoiceRowDao);
    }

    private Matcher<InvoiceRow> getMatcherForSum(final BigDecimal bigDecimal) {
        return new BaseMatcher<InvoiceRow>() {
            @Override
            public boolean matches(Object object) {
                InvoiceRow invoiceRow = (InvoiceRow) object;
                return bigDecimal.equals(invoiceRow.amount);
            }

            @Override
            public void describeTo(Description description) {
                description.appendText("Values do not match.");
            }
        };
    }

    private Matcher<InvoiceRow> getMatcherForDate(final String date) {
        return new BaseMatcher<InvoiceRow>() {
            @Override
            public boolean matches(Object object) {
                InvoiceRow invoiceRow = (InvoiceRow) object;
                return date.equals(asString(invoiceRow.date));
            }

            @Override
            public void describeTo(Description description) {
                description.appendText("Values do not match.");
            }
        };
    }

    private static Date asDate(String date) {
        try {
            return new SimpleDateFormat("yyyy-MM-dd").parse(date);
        } catch (ParseException e) {
            throw new RuntimeException(e);
        }
    }

    private String asString(Date date) {
        return new SimpleDateFormat("yyyy-MM-dd").format(date);
    }

}
