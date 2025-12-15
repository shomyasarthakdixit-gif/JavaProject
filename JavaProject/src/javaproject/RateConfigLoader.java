package javaproject;

import java.io.FileInputStream;
import java.io.IOException;
import java.util.EnumMap;
import java.util.Map;
import java.util.Properties;

public class RateConfigLoader {

    public static Map<Ward, Double> loadRates(String fileName) {
        Map<Ward, Double> rates = new EnumMap<>(Ward.class);
        Properties props = new Properties();

        try (FileInputStream fis = new FileInputStream(fileName)) {
            props.load(fis);

            for (Ward ward : Ward.values()) {
                String value = props.getProperty(ward.name());
                if (value != null) {
                    rates.put(ward, Double.parseDouble(value));
                }
            }

            System.out.println("✅ Rates loaded from rates.cfg");

        } catch (IOException e) {
            System.out.println("❌ Could not read rates.cfg. Using defaults.");
        }

        return rates;
    }
}