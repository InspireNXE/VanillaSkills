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
