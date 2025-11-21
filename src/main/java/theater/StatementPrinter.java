package theater;

import java.text.NumberFormat;
import java.util.Locale;
import java.util.Map;

/**
 * This class generates a statement for a given invoice of performances.
 */
public class StatementPrinter {
    public Invoice invoice;
    public Map<String, Play> plays;

    public StatementPrinter(Invoice invoice, Map<String, Play> plays) {
        this.invoice = invoice;
        this.plays = plays;
    }

    /**
     * Returns a formatted statement of the invoice associated with this printer.
     * @return the formatted statement
     * @throws RuntimeException if one of the play types is not known
     */
    public String statement() {
        int totalAmount = 0;
        int volumeCredits = 0;
        StringBuilder result = new StringBuilder(
                "Statement for " + invoice.getCustomer() + System.lineSeparator()
        );
        final NumberFormat frmt =
                NumberFormat.getCurrencyInstance(Locale.US);

        for (Performance performance : invoice.getPerformances()) {
            int amount = getAmount(performance);

            // Task 2.2: delegate volume-credit calc
            volumeCredits += getVolumeCredits(performance);

            result.append(String.format(
                    "  %s: %s (%d seats)%n",
                    getPlay(performance).getName(),
                    frmt.format(amount / Constants.PERCENT_FACTOR),
                    performance.getAudience()
            ));
            totalAmount += amount;
        }

        result.append(String.format(
                "Amount owed is %s%n",
                frmt.format(totalAmount / Constants.PERCENT_FACTOR)
        ));
        result.append(String.format("You earned %d credits%n", volumeCredits));
        return result.toString();
    }

    private Play getPlay(Performance performance) {
        return plays.get(performance.getPlayID());
    }

    private int getAmount(Performance performance) {
        int amount = 0;
        switch (getPlay(performance).getType()) {
            case "tragedy":
                amount = Constants.TRAGEDY_BASE_AMOUNT;
                if (performance.getAudience() > Constants.TRAGEDY_AUDIENCE_THRESHOLD) {
                    amount += Constants.TRAGEDY_OVER_BASE_CAPACITY_PER_PERSON
                            * (performance.getAudience()
                            - Constants.TRAGEDY_AUDIENCE_THRESHOLD);
                }
                break;
            case "comedy":
                amount = Constants.COMEDY_BASE_AMOUNT;
                if (performance.getAudience() > Constants.COMEDY_AUDIENCE_THRESHOLD) {
                    amount += Constants.COMEDY_OVER_BASE_CAPACITY_AMOUNT
                            + (Constants.COMEDY_OVER_BASE_CAPACITY_PER_PERSON
                            * (performance.getAudience()
                            - Constants.COMEDY_AUDIENCE_THRESHOLD));
                }
                amount += Constants.COMEDY_AMOUNT_PER_AUDIENCE
                        * performance.getAudience();
                break;
            default:
                throw new RuntimeException(
                        String.format("unknown type: %s",
                                getPlay(performance).getType())
                );
        }
        return amount;
    }

    // Task 2.2: extracted from statement() loop
    private int getVolumeCredits(Performance performance) {
        int result = 0;
        result += Math.max(
                performance.getAudience() - Constants.BASE_VOLUME_CREDIT_THRESHOLD,
                0
        );
        if ("comedy".equals(getPlay(performance).getType())) {
            result += performance.getAudience()
                    / Constants.COMEDY_EXTRA_VOLUME_FACTOR;
        }
        return result;
    }
}
