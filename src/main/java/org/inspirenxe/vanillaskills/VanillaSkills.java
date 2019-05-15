/*
 * This file is part of Vanilla Skills, licensed under the MIT License (MIT).
 *
 * Copyright (c) InspireNXE
 * Copyright (c) contributors
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */
package org.inspirenxe.vanillaskills;

import com.google.inject.Inject;
import org.inspirenxe.skills.api.event.DiscoverContentEvent;
import org.inspirenxe.skills.api.function.level.LevelFunctionType;
import org.inspirenxe.skills.api.plugin.SkillsPlugin;
import org.inspirenxe.skills.api.skill.SkillType;
import org.inspirenxe.vanillaskills.skill.Crafting;
import org.inspirenxe.vanillaskills.skill.Farming;
import org.inspirenxe.vanillaskills.skill.Hunter;
import org.inspirenxe.vanillaskills.skill.Mining;
import org.inspirenxe.vanillaskills.skill.Woodcutting;
import org.slf4j.Logger;
import org.spongepowered.api.GameRegistry;
import org.spongepowered.api.config.ConfigDir;
import org.spongepowered.api.event.Order;
import org.spongepowered.api.event.game.GameRegistryEvent;
import org.spongepowered.api.event.Listener;
import org.spongepowered.api.plugin.Plugin;
import org.spongepowered.api.plugin.PluginContainer;

import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.file.Path;

@Plugin(id = VanillaSkills.ID)
public final class VanillaSkills extends SkillsPlugin {

    public static final String ID = "vanilla_skills";

    private final PluginContainer container;
    private final GameRegistry registry;
    private final Path configDir;

    @Inject
    public VanillaSkills(final PluginContainer container, final Logger logger, final GameRegistry registry, @ConfigDir(sharedRoot = false) final Path configDir) throws IOException, URISyntaxException {
        super(VanillaSkills.ID, logger, configDir);
        this.container = container;
        this.registry = registry;
        this.configDir = configDir;

        this.writeDefaultAssets();
    }

    @Listener
    public void onDiscoverContent(final DiscoverContentEvent event) {
        event.addSearchPath(this.configDir);
    }

    @Listener(order = Order.LAST)
    public void onRegisterSkills(final GameRegistryEvent.Register<SkillType> event) {
        this.registry.getType(LevelFunctionType.class, VanillaSkills.ID + ":rs-normal").ifPresent(levelFunction -> {
            event.register(new Mining(this.container, levelFunction, 99));
            event.register(new Farming(this.container, levelFunction, 99));
            event.register(new Woodcutting(this.container, levelFunction, 99));
            event.register(new Crafting(this.container, levelFunction, 99));
            event.register(new Hunter(this.container, levelFunction, 99));
        });
    }
}
