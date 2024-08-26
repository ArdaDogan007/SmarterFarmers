package net.mehvahdjukaar.smarterfarmers.fabric;

import com.terraformersmc.modmenu.api.ConfigScreenFactory;
import com.terraformersmc.modmenu.api.ModMenuApi;
import net.mehvahdjukaar.smarterfarmers.SmarterFarmers;

public class ModMenuCompat implements ModMenuApi {

    @Override
    public ConfigScreenFactory<?> getModConfigScreenFactory() {
        return SmarterFarmers.CONFIG::makeScreen;
    }
}