package lab2;

import java.math.BigDecimal;
import java.util.Date;

public class InoviceRowGenerator {

    private InvoiceRowDao invoiceRowDao;

    public void generateRowsFor(int total, Date start, Date end) {
        int numMonth = findMonthDiff(start, end);         // Number of months.
        int payRegular = total / numMonth;                // Regular payment.
        int payLast = (total % numMonth) + payRegular;    // Last payment.
        invoiceRowDao.save(new InvoiceRow(new BigDecimal(payRegular), start));
        Date date = (Date) start.clone();
        date.setDate(1);
        for (int i = 0; i < numMonth - 2; i ++) {
            date = newWithNextMonth(date);
            invoiceRowDao.save(new InvoiceRow(new BigDecimal(payRegular), date));
        }
        if (numMonth > 1) {
            date = newWithNextMonth(date);
            invoiceRowDao.save(new InvoiceRow(new BigDecimal(payLast), date));
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
