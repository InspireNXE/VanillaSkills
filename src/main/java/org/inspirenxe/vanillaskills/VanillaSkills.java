package org.inspirenxe.vanillaskills;

import com.google.inject.Inject;
import org.inspirenxe.skills.api.function.level.LevelFunctionType;
import org.inspirenxe.skills.api.skill.SkillType;
import org.inspirenxe.vanillaskills.type.Mining;
import org.spongepowered.api.GameRegistry;
import org.spongepowered.api.event.game.GameRegistryEvent;
import org.spongepowered.api.event.Listener;
import org.spongepowered.api.plugin.Plugin;
import org.spongepowered.api.plugin.PluginContainer;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.text.format.TextColors;

@Plugin(
    id = VanillaSkills.ID,
    name = VanillaSkills.NAME
)
public final class VanillaSkills {

    public static final String ID = "vanilla_skills";
    public static final String NAME = "Vanilla Skills";

    private final PluginContainer container;
    private final GameRegistry registry;

    @Inject
    public VanillaSkills(final PluginContainer container, final GameRegistry registry) {
        this.container = container;
        this.registry = registry;
    }

    @Listener
    public void onRegisterSkills(final GameRegistryEvent.Register<SkillType> event) {
        this.registry.getType(LevelFunctionType.class, "skills:rs-normal").ifPresent(levelFunction -> {
            event.register(new Mining(this.container, "mining", "Mining", Text.of(TextColors.AQUA, "Mining"), levelFunction, 99));
        });
    }
}
