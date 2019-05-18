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
import static org.inspirenxe.skills.api.skill.builtin.FilterRegistrar.registrar;
import static org.inspirenxe.skills.api.skill.builtin.RegistrarTypes.CANCEL_EVENT;
import static org.inspirenxe.skills.api.skill.builtin.RegistrarTypes.CANCEL_TRANSACTION;
import static org.inspirenxe.skills.api.skill.builtin.SkillsEventContextKeys.PROCESSING_PLAYER;
import static org.inspirenxe.skills.api.skill.builtin.TriggerRegistrarTypes.TRANSACTION;
import static org.inspirenxe.skills.api.skill.builtin.applicator.XPApplicators.xp;
import static org.inspirenxe.skills.api.skill.builtin.block.FuzzyBlockState.state;
import static org.inspirenxe.skills.api.skill.builtin.block.TraitValue.trait;
import static org.inspirenxe.skills.api.skill.builtin.filter.MatchFilterResponseToResponseFilter.matchTo;
import static org.inspirenxe.skills.api.skill.builtin.filter.trigger.TriggerEntry.apply;
import static org.inspirenxe.skills.api.skill.builtin.filter.trigger.TriggerFilter.triggerIf;
import static org.inspirenxe.skills.api.skill.builtin.filter.block.BlockCreatorFilters.creatorTracked;
import static org.inspirenxe.skills.api.skill.builtin.filter.block.BlockCreatorFilters.natural;
import static org.inspirenxe.skills.api.skill.builtin.filter.block.BlockFilters.states;
import static org.inspirenxe.skills.api.skill.builtin.filter.data.ValueFilters.value;
import static org.inspirenxe.skills.api.skill.builtin.filter.item.ItemFilters.items;
import static org.inspirenxe.skills.api.skill.builtin.filter.level.LevelFilters.level;
import static org.spongepowered.api.block.trait.EnumTraits.LOG2_VARIANT;
import static org.spongepowered.api.block.trait.EnumTraits.LOG_VARIANT;

import org.inspirenxe.skills.api.effect.firework.FireworkEffectType;
import org.inspirenxe.skills.api.function.economy.EconomyFunctionType;
import org.inspirenxe.skills.api.function.level.LevelFunctionType;
import org.inspirenxe.skills.api.skill.builtin.BasicSkillType;
import org.inspirenxe.skills.api.skill.holder.SkillHolderContainer;
import org.inspirenxe.vanillaskills.VanillaSkills;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.block.BlockTypes;
import org.spongepowered.api.data.key.Keys;
import org.spongepowered.api.entity.living.player.gamemode.GameModes;
import org.spongepowered.api.item.ItemTypes;
import org.spongepowered.api.plugin.PluginContainer;
import org.spongepowered.api.service.economy.EconomyService;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.text.format.TextColors;

import java.util.Collection;
import java.util.Optional;

public final class Woodcutting extends BasicSkillType {

    private final FireworkEffectType levelUpFirework;

    public Woodcutting(final PluginContainer container, final LevelFunctionType levelFunction, final int maxLevel) {
        super(container, "woodcutting", "Woodcutting", Text.of(TextColors.DARK_GREEN, "Woodcutting"), levelFunction, maxLevel);

        this.levelUpFirework = Sponge.getRegistry().getType(FireworkEffectType.class, VanillaSkills.ID + ":woodcutting-level-up").orElse(null);
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
                            matchTo(DENY, level(20), not(items(ItemTypes.STONE_AXE))),
                            matchTo(DENY, level(30), not(items(ItemTypes.IRON_AXE))),
                            matchTo(DENY, level(40), not(items(ItemTypes.GOLDEN_AXE))),
                            matchTo(DENY, level(50), not(items(ItemTypes.DIAMOND_AXE)))
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
            .register(container,
                registrar()
                .addFilter(
                    CANCEL_TRANSACTION,
                    matchTo(
                        DENY,
                        value(PROCESSING_PLAYER, Keys.GAME_MODE, GameModes.CREATIVE),
                        any(
                            matchTo(DENY, level(10), not(states(state(BlockTypes.LOG2, trait(LOG2_VARIANT, "dark_oak"))))),
                            matchTo(DENY, level(20), not(states(state(BlockTypes.LOG, trait(LOG_VARIANT, "spruce"))))),
                            matchTo(DENY, level(30), not(states(state(BlockTypes.LOG, trait(LOG_VARIANT, "birch"))))),
                            matchTo(DENY, level(40), not(states(state(BlockTypes.LOG, trait(LOG_VARIANT, "jungle"))))),
                            matchTo(DENY, level(50), not(states(state(BlockTypes.LOG2, trait(LOG2_VARIANT, "acacia")))))
                        )
                    )
                )
                .addTrigger(
                    TRANSACTION,
                    triggerIf()
                    .all(not(value(PROCESSING_PLAYER, Keys.GAME_MODE, GameModes.CREATIVE)), any(creatorTracked(), natural()))
                    .then(
                        apply(xp(5)).when(states(state(BlockTypes.LOG, trait(LOG_VARIANT, "oak")))),
                        apply(xp(7)).when(states(state(BlockTypes.LOG2, trait(LOG2_VARIANT, "dark_oak")))),
                        apply(xp(9)).when(states(state(BlockTypes.LOG, trait(LOG_VARIANT, "spruce")))),
                        apply(xp(11)).when(states(state(BlockTypes.LOG, trait(LOG_VARIANT, "birch")))),
                        apply(xp(13)).when(states(state(BlockTypes.LOG, trait(LOG_VARIANT, "jungle")))),
                        apply(xp(15)).when(states(state(BlockTypes.LOG2, trait(LOG2_VARIANT, "acacia"))))
                    )
                    .build()
                )
                .build(),
                CHANGE_BLOCK_BREAK
            ));

        //@formatter:on
    }

    @Override
    public Optional<FireworkEffectType> getFireworkEffectFor(final int level) {
        return Optional.ofNullable(this.levelUpFirework);
    }
}
