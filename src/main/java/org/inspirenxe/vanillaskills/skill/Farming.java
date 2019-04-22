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
import static org.inspirenxe.skills.api.skill.builtin.EventProcessors.CHANGE_BLOCK_BREAK;
import static org.inspirenxe.skills.api.skill.builtin.EventProcessors.INTERACT_BLOCK_PRIMARY_MAIN_HAND;
import static org.inspirenxe.skills.api.skill.builtin.EventProcessors.INTERACT_BLOCK_PRIMARY_OFF_HAND;
import static org.inspirenxe.skills.api.skill.builtin.EventProcessors.INTERACT_BLOCK_SECONDARY_MAIN_HAND;
import static org.inspirenxe.skills.api.skill.builtin.EventProcessors.INTERACT_BLOCK_SECONDARY_OFF_HAND;
import static org.inspirenxe.skills.api.skill.builtin.EventProcessors.INTERACT_ITEM_PRIMARY_MAIN_HAND;
import static org.inspirenxe.skills.api.skill.builtin.EventProcessors.INTERACT_ITEM_PRIMARY_OFF_HAND;
import static org.inspirenxe.skills.api.skill.builtin.EventProcessors.INTERACT_ITEM_SECONDARY_MAIN_HAND;
import static org.inspirenxe.skills.api.skill.builtin.EventProcessors.INTERACT_ITEM_SECONDARY_OFF_HAND;
import static org.inspirenxe.skills.api.skill.builtin.EventProcessors.CHANGE_BLOCK_PLACE;
import static org.inspirenxe.skills.api.skill.builtin.FilterRegistrar.registrar;
import static org.inspirenxe.skills.api.skill.builtin.applicator.XPApplicators.xp;
import static org.inspirenxe.skills.api.skill.builtin.block.FuzzyBlockState.state;
import static org.inspirenxe.skills.api.skill.builtin.block.TraitValue.trait;
import static org.inspirenxe.skills.api.skill.builtin.filter.MatchFilterResponseToResponseFilter.matchTo;
import static org.inspirenxe.skills.api.skill.builtin.filter.applicator.ApplicatorEntry.apply;
import static org.inspirenxe.skills.api.skill.builtin.filter.applicator.TriggerFilter.triggerIf;
import static org.inspirenxe.skills.api.skill.builtin.filter.block.BlockFilters.blocks;
import static org.inspirenxe.skills.api.skill.builtin.filter.block.BlockFilters.states;
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
import org.spongepowered.api.block.BlockTypes;
import org.spongepowered.api.block.trait.IntegerTraits;
import org.spongepowered.api.data.key.Keys;
import org.spongepowered.api.entity.living.player.gamemode.GameModes;
import org.spongepowered.api.item.ItemTypes;
import org.spongepowered.api.plugin.PluginContainer;
import org.spongepowered.api.service.economy.EconomyService;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.text.format.TextColors;

import java.util.Collection;
import java.util.Optional;

public final class Farming extends BasicSkillType {

    private final FireworkEffectType levelUpFirework;

    public Farming(final PluginContainer container, final LevelFunctionType levelFunction, final int maxLevel) {
        super(container, "farming", "Farming", Text.of(TextColors.GREEN, "Farming"), levelFunction, maxLevel);

        this.levelUpFirework = Sponge.getRegistry().getType(FireworkEffectType.class, VanillaSkills.ID + ":farming-level-up").orElse(null);
    }

    @Override
    protected void onConfigure(final Collection<SkillHolderContainer> containers) {
        final EconomyService es = Sponge.getServiceManager().provide(EconomyService.class).orElse(null);
        final EconomyFunctionType ef = Sponge.getRegistry().getType(EconomyFunctionType.class, VanillaSkills.ID + ":standard").orElse(null);

        //@formatter:off
        containers.forEach(container -> this
            // Deny tools
            .register(container,
                registrar()
                .cancelEvent(
                    matchTo(
                        DENY,
                        value(Keys.GAME_MODE, GameModes.CREATIVE),
                        any(
                            matchTo(DENY, not(items(ItemTypes.STONE_HOE)), level(20)),
                            matchTo(DENY, not(items(ItemTypes.IRON_HOE)), level(30)),
                            matchTo(DENY, not(items(ItemTypes.GOLDEN_HOE)), level(40)),
                            matchTo(DENY, not(items(ItemTypes.DIAMOND_HOE)), level(50))
                        )
                    )
                )
                .build(),
                INTERACT_BLOCK_PRIMARY_MAIN_HAND,
                INTERACT_BLOCK_PRIMARY_OFF_HAND,
                INTERACT_BLOCK_SECONDARY_MAIN_HAND,
                INTERACT_BLOCK_SECONDARY_OFF_HAND,
                INTERACT_ITEM_PRIMARY_MAIN_HAND,
                INTERACT_ITEM_PRIMARY_OFF_HAND,
                INTERACT_ITEM_SECONDARY_MAIN_HAND,
                INTERACT_ITEM_SECONDARY_OFF_HAND
            )
            // Deny planting seeds
            .register(container,
                registrar()
                .cancelTransaction(
                    matchTo(
                        DENY,
                        value(Keys.GAME_MODE, GameModes.CREATIVE),
                        any(
                            matchTo(DENY, not(blocks(BlockTypes.CARROTS)), level(10))
                        )
                    )
                )
                // Reward turning soil into farmland
                .transactionTrigger(
                    triggerIf()
                    .all(not(value(Keys.GAME_MODE, GameModes.CREATIVE)), blocks(BlockTypes.FARMLAND))
                    .then(
                        apply(xp(0.25)).when(items(ItemTypes.WOODEN_HOE)),
                        apply(xp(0.50)).when(items(ItemTypes.STONE_HOE)),
                        apply(xp(0.75)).when(items(ItemTypes.GOLDEN_HOE)),
                        apply(xp(1)).when(items(ItemTypes.DIAMOND_HOE))
                    )
                    .build()
                )
                .build(),
                CHANGE_BLOCK_PLACE
            )
            // Deny breaking crops
            .register(container,
                registrar()
                .cancelTransaction(
                    matchTo(
                        DENY,
                        value(Keys.GAME_MODE, GameModes.CREATIVE),
                        any(
                            matchTo(DENY, not(states(state(BlockTypes.CARROTS, trait(IntegerTraits.CARROTS_AGE, 7)))), level(10))
                        )
                    )
                )
                .build(),
                CHANGE_BLOCK_BREAK
            )
            // TODO Xp per drop handling
        );
        //@formatter:on
    }

    @Override
    public Optional<FireworkEffectType> getFireworkEffectFor(final int level) {
        return Optional.ofNullable(this.levelUpFirework);
    }
}
