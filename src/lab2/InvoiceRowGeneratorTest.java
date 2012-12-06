package lab2;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;
import static org.hamcrest.CoreMatchers.*;

import java.math.BigDecimal;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

import org.hamcrest.BaseMatcher;
import org.hamcrest.Description;
import org.hamcrest.Matcher;
import org.junit.Test;
import org.laughingpanda.beaninject.Inject;
import org.mockito.ArgumentCaptor;

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
        
        verify(invoiceRowDao).save(argThat(getMatcherForDate("2012-02-15")));
        verify(invoiceRowDao).save(argThat(getMatcherForDate("2012-03-01")));
        verify(invoiceRowDao).save(argThat(getMatcherForDate("2012-04-01")));
        verifyNoMoreInteractions(invoiceRowDao);
    }
    
    @Test // - osamake summa EI tuleks v�iksem kui 3 EUR-i.
    public void paymentAmountNotBelowMinimum() throws Exception {
        makeNewGenerator();

        generator.generateRowsFor(14, asDate("2011-12-15"), asDate("2012-04-02"));
        
        ArgumentCaptor<InvoiceRow> amountsCaptor = ArgumentCaptor.forClass(InvoiceRow.class);
		verify(invoiceRowDao, atLeast(1)).save(amountsCaptor.capture());

		List<InvoiceRow> capturedAmounts = amountsCaptor.getAllValues();
		for (InvoiceRow row : capturedAmounts){
			assertTrue(row.amount.compareTo(new BigDecimal(3)) >= 0);
		}
    }
    
    @Test // - kuup�evade kontroll osamaksete koondumisel
    public void paymentDatesAreCorrectWhenAmountsBelowMinimum() throws Exception {
        makeNewGenerator();

        generator.generateRowsFor(14, asDate("2011-12-15"), asDate("2012-04-02"));
        
        verify(invoiceRowDao).save(argThat(getMatcherForDate("2011-12-15")));
        verify(invoiceRowDao).save(argThat(getMatcherForDate("2012-02-01")));
        verify(invoiceRowDao).save(argThat(getMatcherForDate("2012-04-01")));
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
                description.appendText("Amounts do not match.");
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
                description.appendText("Dates do not match.");
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
