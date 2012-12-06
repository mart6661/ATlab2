package lab2;

import java.math.BigDecimal;
import java.util.Date;

public class InoviceRowGenerator {

    private InvoiceRowDao invoiceRowDao;
    private final BigDecimal minimumAmount = new BigDecimal(3);

    public void generateRowsFor(int total, Date start, Date end) {
    	BigDecimal totalSum = new BigDecimal(total);
    	
    	// skip rest if total sum is less than 6 EUR (can't divide into valid payments)
    	if (total <= 6) {
    		invoiceRowDao.save(new InvoiceRow(totalSum, start));
    		return;
    	}
    	
    	int numMonth = (findMonthDiff(start, end));  // Months
    	BigDecimal payRegular =  totalSum.divideToIntegralValue(new BigDecimal(numMonth)); // Regular payment.
        BigDecimal nextPayment = new BigDecimal(0);
        BigDecimal remainder = totalSum;
        
        Date date = (Date) start.clone();
        
        int monthsToSkip = 0;
        
        nextPayment = payRegular;
        while (nextPayment.compareTo(minimumAmount) < 0) {
        	nextPayment = nextPayment.add(payRegular);
        	monthsToSkip++;
        }
        
        //first payment
        invoiceRowDao.save(new InvoiceRow(nextPayment, start));
    	remainder = remainder.subtract(nextPayment);
    	for (int k=0;k < monthsToSkip;k++) {
        	date = newWithNextMonth(date);
        }
    	monthsToSkip = 0;
        
        date.setDate(1);
        for (int i = 0; i < numMonth - 1; i ++) {
        	date = newWithNextMonth(date);
        	if (nextPayment.compareTo(minimumAmount) < 0) {
        		nextPayment = nextPayment.add(payRegular);
        		monthsToSkip++;
        	} else {
        		remainder = remainder.subtract(nextPayment);
        		if (remainder.compareTo(minimumAmount) < 0) {
        			nextPayment = nextPayment.add(remainder);
        			invoiceRowDao.save(new InvoiceRow(nextPayment, date));
        			return;
        		} else {
        			invoiceRowDao.save(new InvoiceRow(nextPayment, date));
        			nextPayment = payRegular;
        			for (int k=0;k < monthsToSkip;k++) {
        	        	date = newWithNextMonth(date);
        	        }
        		}
        	}
        }
    }
    
    private int findMonthDiff(Date start, Date end) {
        return (end.getYear() - start.getYear()) * 12 + (end.getMonth() - start.getMonth()) + 1;
    }

    private Date newWithNextMonth(Date oldDate) {
        Date date = (Date) oldDate.clone();
        if (date.getMonth() == 11) {
            date.setYear(oldDate.getYear() + 1);
            date.setMonth(0);
        } else {
            date.setMonth(oldDate.getMonth() + 1);
        }
        return date;
    }

}
