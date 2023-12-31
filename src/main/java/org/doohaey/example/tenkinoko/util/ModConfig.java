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
            "zh_cn",
            "默认语言为中文",
            Config::getString);

    @ConfigOption
    public static final Option<Double> TAX_RESTOCK = new Option<>(
            "core.tax.restock",
            0.01,
            "The tax rate players should pay when restocking.",
            Config::getDouble
    );

    @ConfigOption
    public static final Option<Boolean> CHANGEABLE = new Option<>(
            "core.changeable",
            true,
            "The weather is allowed to change.",
            Config::getBoolean
    );
}
