/*
 * TransitCurrencyTest.java
 *
 * Copyright 2017-2019 Michael Farrell <micolous+git@gmail.com>
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package au.id.micolous.metrodroid.test;

import android.text.Spanned;

import org.hamcrest.Matchers;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import au.id.micolous.metrodroid.transit.TransitCurrency;

import static junit.framework.TestCase.assertEquals;
import static junit.framework.TestCase.assertFalse;
import static junit.framework.TestCase.assertTrue;

/**
 * Tests the currency handling.
 */
public class TransitCurrencyTest extends BaseInstrumentedTest {
    @Test
    public void testCurrencySame() {
        final TransitCurrency c1 = TransitCurrency.AUD(1234);
        final TransitCurrency c2 = TransitCurrency.AUD(138);
        final TransitCurrency expected = TransitCurrency.AUD(1372);

        // These all should be the same currency, and that works...
        assertTrue(c1.sameCurrency(c2));
        assertTrue(c1.sameCurrency(expected));
        assertTrue(c2.sameCurrency(c1));
        assertTrue(c2.sameCurrency(expected));
        assertTrue(expected.sameCurrency(c1));
        assertTrue(expected.sameCurrency(c2));

        // Do some arithmetic
        assertEquals(expected, c1.add(c2));
        assertEquals(expected, c2.add(c1));
    }

    @Rule
    public final ExpectedException exception = ExpectedException.none();

    @Test
    public void testCurrencyMixed() {
        final TransitCurrency c1 = TransitCurrency.AUD(1234);
        final TransitCurrency c2 = TransitCurrency.BRL(1234);

        // These don't trigger exception.
        assertFalse(c1.sameCurrency(c2));
        assertFalse(c2.sameCurrency(c1));

        // This does!
        exception.expect(IllegalArgumentException.class);
        c1.add(c2);
    }

    /**
     * In Australian English, AUD should come out as a bare "$", and USD should come out with some
     * different prefix.
     */
    @Test
    public void testEnglishAU() {
        // Note: en_AU data in Unicode CLDR currency data was broken in release
        // 28, Android 7.0+:
        // https://unicode.org/cldr/trac/changeset/11798/trunk/common/main/en_AU.xml
        // https://unicode.org/cldr/trac/ticket/10217
        // Only check to make sure AUD comes out correctly in en_AU.
        setLocale("en-AU");

        final Spanned aud = TransitCurrency.AUD(1234).formatCurrencyString(true);
        assertSpannedEquals("$12.34", aud);
        assertTtsMarkers("AUD", "12.34", aud);

        // May be "USD12.34", "U$12.34" or "US$12.34".
        final Spanned usd = TransitCurrency.USD(1234).formatCurrencyString(true);
        assertSpannedThat(usd, Matchers.startsWith("U"));
        assertSpannedThat(usd, Matchers.endsWith("12.34"));
        assertTtsMarkers("USD", "12.34", usd);
    }

    /**
     * In British English, everything should come out pretty similar.
     *
     * It might clarify USD (US$ vs. $). but that isn't very important.
     */
    @Test
    public void testEnglishGB() {
        setLocale("en-GB");

        // May be "$12.34", "U$12.34" or "US$12.34".
        final Spanned usd = TransitCurrency.USD(1234).formatCurrencyString(true);
        assertSpannedThat(usd, Matchers.endsWith("$12.34"));
        assertTtsMarkers("USD", "12.34", usd);

        // May be "A$12.34" or "AU$12.34".
        final Spanned aud = TransitCurrency.AUD(1234).formatCurrencyString(true);
        assertSpannedThat(aud, Matchers.startsWith("A"));
        assertSpannedThat(aud, Matchers.endsWith("$12.34"));
        assertTtsMarkers("AUD", "12.34", aud);

        final Spanned gbp = new TransitCurrency(1234, "GBP").formatCurrencyString(true);
        assertSpannedEquals("£12.34", gbp);
        assertTtsMarkers("GBP", "12.34", gbp);

        // May be "¥1,234" or "JP¥1,234".
        final Spanned jpy = TransitCurrency.JPY(1234).formatCurrencyString(true);
        assertSpannedThat(jpy, Matchers.endsWith("¥1,234"));
        assertTtsMarkers("JPY", "1234", jpy);
    }

    /**
     * In American English, USD should come out as a bare "$", and AUD should come out with some
     * different prefix.
     */
    @Test
    public void testEnglishUS() {
        setLocale("en-US");

        final Spanned usd = TransitCurrency.USD(1234).formatCurrencyString(true);
        assertSpannedEquals("$12.34", usd);
        assertTtsMarkers("USD", "12.34", usd);

        // May be "A$12.34" or "AU$12.34".
        final Spanned aud = TransitCurrency.AUD(1234).formatCurrencyString(true);
        assertSpannedThat(aud, Matchers.startsWith("A"));
        assertSpannedThat(aud, Matchers.endsWith("$12.34"));
        assertTtsMarkers("AUD", "12.34", aud);

        final Spanned gbp = new TransitCurrency(1234, "GBP").formatCurrencyString(true);
        assertSpannedEquals("£12.34", gbp);
        assertTtsMarkers("GBP", "12.34", gbp);

        // May be "¥1,234" or "JP¥1,234".
        final Spanned jpy = TransitCurrency.JPY(1234).formatCurrencyString(true);
        assertSpannedThat(jpy, Matchers.endsWith("¥1,234"));
        assertTtsMarkers("JPY", "1234", jpy);
    }

    /**
     * In Japanese, everything should come out pretty similar.  But the Yen character is probably
     * full-width.
     *
     * It might clarify USD (US$ vs. $). but that isn't very important.
     */
    @Test
    public void testJapanese() {
        setLocale("ja-JP");

        // May be "$12.34", "U$12.34" or "US$12.34".
        final Spanned usd = TransitCurrency.USD(1234).formatCurrencyString(true);
        assertSpannedThat(usd, Matchers.endsWith("$12.34"));
        assertTtsMarkers("USD", "12.34", usd);

        // May be "A$12.34" or "AU$12.34".
        final Spanned aud = TransitCurrency.AUD(1234).formatCurrencyString(true);
        assertSpannedThat(aud, Matchers.startsWith("A"));
        assertSpannedThat(aud, Matchers.endsWith("$12.34"));
        assertTtsMarkers("AUD", "12.34", aud);

        final Spanned gbp = new TransitCurrency(1234, "GBP").formatCurrencyString(true);
        assertSpannedEquals("£12.34", gbp);
        assertTtsMarkers("GBP", "12.34", gbp);


        // Note: this is the full-width yen character
        final Spanned jpy = TransitCurrency.JPY(1234).formatCurrencyString(true);
        assertSpannedEquals("￥1,234", jpy);
        assertTtsMarkers("JPY", "1234", jpy);
    }

    /**
     * In French, comma is used as a decimal separator, spaces are used for grouping, and currency
     * symbols are after the amount. TTS data must have an English formatting style.
     */
    @Test
    public void testFrench() {
        setLocale("fr-FR");

        final Spanned usd = TransitCurrency.USD(1234).formatCurrencyString(true);
        assertSpannedEquals("12,34 $US", usd);
        assertTtsMarkers("USD", "12.34", usd);

        final Spanned aud = TransitCurrency.AUD(1234).formatCurrencyString(true);
        assertSpannedEquals("12,34 $AU", aud);
        assertTtsMarkers("AUD", "12.34", aud);

        // Allow not qualifying the country code.
        final Spanned gbp = new TransitCurrency(1234, "GBP").formatCurrencyString(true);
        assertSpannedThat(gbp, Matchers.startsWith("12,34 £"));
        assertTtsMarkers("GBP", "12.34", gbp);

        // This may not have a proper symbol
        final Spanned jpy = TransitCurrency.JPY(1234).formatCurrencyString(true);
        assertSpannedThat(jpy, Matchers.startsWith("1 234"));

        final Spanned eur = new TransitCurrency(1234, "EUR").formatCurrencyString(true);
        assertSpannedEquals("12,34 €", eur);
        assertTtsMarkers("EUR", "12.34", eur);
    }

    @Test
    public void testNumericLookup() {
        setLocale("en-US");

        TransitCurrency c = new TransitCurrency(1234, 36, 100);
        assertEquals(TransitCurrency.AUD(1234), c);

        // Test with an invalid code
        c = new TransitCurrency(1234, 9999);
        assertEquals(TransitCurrency.XXX(1234), c);

    }

    @Test
    public void testDivisor() {
        setLocale("en-US");

        // Test with no divisor -- this should infer the divisor
        TransitCurrency c = new TransitCurrency(1234, 36);
        assertEquals(TransitCurrency.AUD(1234), c);

        // Test with no divisor -- a string currencyCode should NOT infer the divisor
        c = new TransitCurrency(1234, "JPY");
        assertSpannedThat(c.formatCurrencyString(true), Matchers.endsWith("¥12.34"));
        assertSpannedThat(c.formatCurrencyString(false), Matchers.endsWith("¥12.34"));

        // Test with different divisors for equality
        c = new TransitCurrency(12340, "AUD", 1000);
        assertEquals(TransitCurrency.AUD(1234), c);

        // Test overriding the divisor in a currency code.
        c = new TransitCurrency(12340, 36, 1000);
        assertEquals(TransitCurrency.AUD(1234), c);
    }
}
