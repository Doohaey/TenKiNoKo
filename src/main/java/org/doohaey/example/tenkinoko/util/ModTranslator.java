package org.doohaey.example.tenkinoko.util;

import com.imyvm.hoki.i18n.HokiLanguage;
import com.imyvm.hoki.i18n.HokiTranslator;
import net.minecraft.text.Text;
import org.doohaey.example.tenkinoko.TenKiNoKo;

import java.io.InputStream;

import static org.doohaey.example.tenkinoko.TenKiNoKo.TK_CONFIG;

public class ModTranslator extends HokiTranslator {
    private static HokiLanguage INS = createLanguage(TK_CONFIG.LANG.getValue());
    static {
        TK_CONFIG.LANG.changeEvents.register((option, oldValue, newValue) -> INS = createLanguage(option.getValue()));
    }
    public static Text tr(String key, Object... args) {
        return HokiTranslator.translate(getLanguageInstance(), key, args);
    }

    public static HokiLanguage getLanguageInstance() {
        return INS;
    }
    private static HokiLanguage createLanguage(String languageId) {
        String path = HokiLanguage.getResourcePath(TenKiNoKo.MOD_ID, languageId);
        InputStream inputStream = ModTranslator.class.getResourceAsStream(path);
        return HokiLanguage.create(inputStream);
    }

}
