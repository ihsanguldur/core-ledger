package com.ihsanguldur.coreledger.domain.valueobject;

import com.ihsanguldur.coreledger.domain.exception.CurrencyMismatchException;
import lombok.*;
import lombok.extern.slf4j.Slf4j;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Currency;
import java.util.Objects;

@Getter
@Slf4j
@EqualsAndHashCode
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public final class Money {

    private final BigDecimal amount;
    private final Currency currency;

    public static Money of(BigDecimal amount, Currency currency) {
        Objects.requireNonNull(amount, "amount cannot be null");
        Objects.requireNonNull(currency, "currency cannot be null");

        if (amount.signum() < 0) {
            throw new IllegalArgumentException("amount cannot be negative: " + amount);
        }

        BigDecimal scaled;
        try {
            scaled = amount.setScale(currency.getDefaultFractionDigits(), RoundingMode.UNNECESSARY);
        } catch (ArithmeticException e) {
            throw new IllegalArgumentException(
                    "amount " + amount + " has more precision than " + currency.getCurrencyCode()
                    + " allows (" + currency.getDefaultFractionDigits() + " decimal places)", e);
        }

        return new Money(scaled, currency);
    }

    public static Money zero(Currency currency) {
        return of(BigDecimal.ZERO, currency);
    }

    public Money add(Money other) {
        requireSameCurrency(other);

        return new Money(this.amount.add(other.amount), this.currency);
    }

    public Money subtract(Money other) {
        requireSameCurrency(other);

        BigDecimal result = this.amount.subtract(other.amount);
        if (result.signum() < 0) {
            throw new IllegalArgumentException("result would be negative: " + result);
        }

        return new Money(result, this.currency);
    }

    public boolean isGreaterThanOrEqualTo(Money other) {
        requireSameCurrency(other);

        return this.amount.compareTo(other.amount) >= 0;
    }

    public void requireSameCurrency(Money other) {
        Objects.requireNonNull(other, "other cannot be null");
        if (!this.currency.equals(other.currency)) {
            throw new CurrencyMismatchException(this.currency, other.currency);
        }
    }

    @Override
    public String toString() {
        return amount + " " + currency.getCurrencyCode();
    }
}
