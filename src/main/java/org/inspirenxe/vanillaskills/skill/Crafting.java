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
package org.inspirenxe.vanillaskills.skill;

import static net.kyori.filter.FilterResponse.DENY;
import static net.kyori.filter.Filters.any;
import static net.kyori.filter.Filters.not;
import static org.inspirenxe.skills.api.skill.builtin.EventProcessors.CRAFT_ITEM_CRAFT;
import static org.inspirenxe.skills.api.skill.builtin.FilterRegistrar.registrar;
import static org.inspirenxe.skills.api.skill.builtin.RegistrarTypes.CANCEL_EVENT;
import static org.inspirenxe.skills.api.skill.builtin.SkillsEventContextKeys.PROCESSING_PLAYER;
import static org.inspirenxe.skills.api.skill.builtin.TriggerRegistrarTypes.EVENT;
import static org.inspirenxe.skills.api.skill.builtin.applicator.XPApplicators.xp;
import static org.inspirenxe.skills.api.skill.builtin.filter.MatchFilterResponseToResponseFilter.matchTo;
import static org.inspirenxe.skills.api.skill.builtin.filter.trigger.TriggerEntry.apply;
import static org.inspirenxe.skills.api.skill.builtin.filter.trigger.TriggerFilter.triggerIf;
import static org.inspirenxe.skills.api.skill.builtin.filter.data.ValueFilters.value;
import static org.inspirenxe.skills.api.skill.builtin.filter.item.ItemFilters.items;
import static org.inspirenxe.skills.api.skill.builtin.filter.level.LevelFilters.level;

import org.inspirenxe.skills.api.effect.firework.FireworkEffectType;
import org.inspirenxe.skills.api.function.economy.EconomyFunctionType;
import org.inspirenxe.skills.api.function.level.LevelFunctionType;
import org.inspirenxe.skills.api.skill.builtin.BasicSkillType;
import org.inspirenxe.skills.api.skill.holder.SkillHolderContainer;
import org.inspirenxe.vanillaskills.VanillaSkills;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.data.key.Keys;
import org.spongepowered.api.entity.living.player.gamemode.GameModes;
import org.spongepowered.api.item.ItemTypes;
import org.spongepowered.api.plugin.PluginContainer;
import org.spongepowered.api.service.economy.EconomyService;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.text.format.TextColors;

import java.util.Collection;
import java.util.Optional;

public final class Crafting extends BasicSkillType {

    private final FireworkEffectType levelUpFirework;

    public Crafting(final PluginContainer container, final LevelFunctionType levelFunction, final int maxLevel) {
        super(container, "crafting", "Crafting", Text.of(TextColors.LIGHT_PURPLE, "Crafting"), levelFunction, maxLevel);

        this.levelUpFirework = Sponge.getRegistry().getType(FireworkEffectType.class, VanillaSkills.ID + ":crafting-level-up").orElse(null);
    }

    @Override
    protected void onConfigure(final Collection<SkillHolderContainer> containers) {
        final EconomyService es = Sponge.getServiceManager().provide(EconomyService.class).orElse(null);
        final EconomyFunctionType ef = Sponge.getRegistry().getType(EconomyFunctionType.class, VanillaSkills.ID + ":standard").orElse(null);

        //@formatter:off
        containers.forEach(container -> this
            .register(container,
                registrar()
                .addFilter(
                    CANCEL_EVENT,
                    matchTo(
                        DENY,
                        value(PROCESSING_PLAYER, Keys.GAME_MODE, GameModes.CREATIVE),
                        any(
                            matchTo(DENY, not(items(ItemTypes.TORCH)), level(5)),
                            matchTo(DENY, not(items(ItemTypes.STONE_PICKAXE, ItemTypes.STONE_AXE, ItemTypes.STONE_HOE, ItemTypes.STONE_SHOVEL)), level(20)),
                            matchTo(DENY, not(items(ItemTypes.IRON_PICKAXE, ItemTypes.IRON_AXE, ItemTypes.IRON_HOE, ItemTypes.IRON_SHOVEL)), level(30)),
                            matchTo(DENY, not(items(ItemTypes.GOLDEN_PICKAXE, ItemTypes.GOLDEN_AXE, ItemTypes.GOLDEN_HOE, ItemTypes.GOLDEN_SHOVEL)), level(40)),
                            matchTo(DENY, not(items(ItemTypes.DIAMOND_PICKAXE, ItemTypes.DIAMOND_AXE, ItemTypes.DIAMOND_HOE, ItemTypes.DIAMOND_SHOVEL)), level(50))
                        )
                    )
                )
                .addTrigger(
                    EVENT,
                    triggerIf()
                    .all(not(value(PROCESSING_PLAYER, Keys.GAME_MODE, GameModes.CREATIVE)))
                    .then(
                        apply(xp(5)).when(items(ItemTypes.WOODEN_PICKAXE, ItemTypes.WOODEN_AXE)),
                        apply(xp(10)).when(items(ItemTypes.STONE_PICKAXE, ItemTypes.STONE_AXE)),
                        apply(xp(15)).when(items(ItemTypes.IRON_PICKAXE, ItemTypes.IRON_AXE)),
                        apply(xp(20)).when(items(ItemTypes.GOLDEN_PICKAXE, ItemTypes.GOLDEN_AXE)),
                        apply(xp(25)).when(items(ItemTypes.DIAMOND_PICKAXE, ItemTypes.DIAMOND_AXE))
                    )
                    .build()
                )
                .build(),
                CRAFT_ITEM_CRAFT
            ));
    }

    @Override
    public Optional<FireworkEffectType> getFireworkEffectFor(final int level) {
        return Optional.ofNullable(this.levelUpFirework);
    }
}
