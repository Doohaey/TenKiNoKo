package org.doohaey.example.tenkinoko.util;

import com.imyvm.hoki.config.ConfigOption;
import com.imyvm.hoki.config.HokiConfig;
import com.imyvm.hoki.config.Option;
import com.typesafe.config.Config;

public class ModConfig extends HokiConfig {
    public static final String CONF = "tk.conf";

    public ModConfig() {
        super(CONF);
    }

    @ConfigOption
    public final Option<String> LANG = new Option<>(
            "core.language",
            "en_us",
            "Displayed in English.",
            Config::getString);

    @ConfigOption
    public static final Option<Double> TAX_RESTOCK = new Option<>(
            "core.tax.restock",
            0.01,
            "The tax rate players should pay when restocking.",
            Config::getDouble
    );
}
