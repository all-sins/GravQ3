import lv.all_sins.Main;
import org.junit.Test;

import java.math.RoundingMode;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.Locale;

import static org.junit.Assert.*;

public class ResultValidatorTest {

    @Test
    public void TestCastComputeDigitSum() {
        assertEquals(0, Main.castComputeDigitSum(0));
        assertEquals(1, Main.castComputeDigitSum(1));
        assertEquals(3, Main.castComputeDigitSum(12));
        assertEquals(6, Main.castComputeDigitSum(123));
        assertEquals(3, Main.castComputeDigitSum(300));
        assertEquals(5, Main.castComputeDigitSum(5000));
        assertEquals(18, Main.castComputeDigitSum(99));
        assertEquals(6, Main.castComputeDigitSum(111111));
    }

    @Test
    public void TestExtractDecimalPointSuffix() {
        double number = 3301.123456789;

        final DecimalFormatSymbols symbols = DecimalFormatSymbols.getInstance(Locale.US);
        final DecimalFormat decimalFormat = new DecimalFormat("#.########", symbols);
        decimalFormat.setRoundingMode(RoundingMode.DOWN); // THANK GOD I DECIDED TO WRITE THIS TEST.

        String formatedNumber = decimalFormat.format(number);
        assertEquals("3301.12345678", formatedNumber);

        String decimalPointSuffix = formatedNumber.substring(formatedNumber.indexOf(".") + 1);
        assertEquals(decimalPointSuffix, "12345678");
    }

}
