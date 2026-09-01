package com.ihsanguldur.coreledger.domain.valueobject;

import com.ihsanguldur.coreledger.domain.exception.CurrencyMismatchException;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.Currency;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class MoneyTest {

    private static final Currency USD = Currency.getInstance("USD");
    private static final Currency JPY = Currency.getInstance("JPY");

    @Test
    void rejectsNegativeAmount() {
        assertThrows(IllegalArgumentException.class,
                () -> Money.of(new BigDecimal("-1.00"), USD));
    }

    @Test
    void rejectsTooMuchPrecisionForCurrency() {
        assertThrows(IllegalArgumentException.class,
                () -> Money.of(new BigDecimal("1.005"), USD));
    }

    @Test
    void jpyHasZeroDecimalPlaces() {
        Money money = Money.of(new BigDecimal("100"), JPY);
        assertEquals(0, money.getAmount().scale());
        assertThrows(IllegalArgumentException.class, () -> Money.of(new BigDecimal("100.5"), JPY));
    }

    @Test
    void addingDifferentCurrenciesThrows() {
        Money usd =  Money.of(new BigDecimal("10.00"), USD);
        Money jpy =  Money.of(new BigDecimal("10"), JPY);
        assertThrows(CurrencyMismatchException.class, () -> usd.add(jpy));
    }

    @Test
    void equalAmountsAndCurrenciesAreEqual() {
        Money a =  Money.of(new BigDecimal("50.00"), USD);
        Money b =  Money.of(new BigDecimal("50.00"), USD);
        assertEquals(a, b);
        assertEquals(a.hashCode(), b.hashCode());
    }
}
