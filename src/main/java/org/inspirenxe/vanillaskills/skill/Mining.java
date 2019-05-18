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
import static org.inspirenxe.skills.api.skill.builtin.EventProcessors.INTERACT_ITEM_SECONDARY_MAIN_HAND;
import static org.inspirenxe.skills.api.skill.builtin.EventProcessors.INTERACT_ITEM_PRIMARY_OFF_HAND;
import static org.inspirenxe.skills.api.skill.builtin.EventProcessors.INTERACT_ITEM_SECONDARY_OFF_HAND;
import static org.inspirenxe.skills.api.skill.builtin.FilterRegistrar.registrar;
import static org.inspirenxe.skills.api.skill.builtin.RegistrarTypes.CANCEL_EVENT;
import static org.inspirenxe.skills.api.skill.builtin.RegistrarTypes.CANCEL_TRANSACTION;
import static org.inspirenxe.skills.api.skill.builtin.SkillsEventContextKeys.PROCESSING_PLAYER;
import static org.inspirenxe.skills.api.skill.builtin.TriggerRegistrarTypes.TRANSACTION;
import static org.inspirenxe.skills.api.skill.builtin.applicator.EconomyApplicators.scaledMoney;
import static org.inspirenxe.skills.api.skill.builtin.applicator.XPApplicators.xp;
import static org.inspirenxe.skills.api.skill.builtin.block.FuzzyBlockState.state;
import static org.inspirenxe.skills.api.skill.builtin.block.TraitValue.trait;
import static org.inspirenxe.skills.api.skill.builtin.filter.MatchFilterResponseToResponseFilter.matchTo;
import static org.inspirenxe.skills.api.skill.builtin.filter.trigger.TriggerEntry.apply;
import static org.inspirenxe.skills.api.skill.builtin.filter.trigger.TriggerFilter.triggerIf;
import static org.inspirenxe.skills.api.skill.builtin.filter.block.BlockCreatorFilters.natural;
import static org.inspirenxe.skills.api.skill.builtin.filter.block.BlockFilters.blocks;
import static org.inspirenxe.skills.api.skill.builtin.filter.block.BlockFilters.states;
import static org.inspirenxe.skills.api.skill.builtin.filter.data.ValueFilters.value;
import static org.inspirenxe.skills.api.skill.builtin.filter.item.ItemFilters.items;
import static org.inspirenxe.skills.api.skill.builtin.filter.level.LevelFilters.level;
import static org.spongepowered.api.block.trait.EnumTraits.STONEBRICK_VARIANT;

import org.inspirenxe.skills.api.effect.firework.FireworkEffectType;
import org.inspirenxe.skills.api.function.economy.EconomyFunctionType;
import org.inspirenxe.skills.api.function.level.LevelFunctionType;
import org.inspirenxe.skills.api.skill.builtin.BasicSkillType;
import org.inspirenxe.skills.api.skill.builtin.FilterRegistrar;
import org.inspirenxe.skills.api.skill.holder.SkillHolderContainer;
import org.inspirenxe.vanillaskills.VanillaSkills;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.block.BlockTypes;
import org.spongepowered.api.data.key.Keys;
import org.spongepowered.api.entity.living.player.gamemode.GameModes;
import org.spongepowered.api.item.ItemTypes;
import org.spongepowered.api.plugin.PluginContainer;
import org.spongepowered.api.service.economy.Currency;
import org.spongepowered.api.service.economy.EconomyService;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.text.format.TextColors;

import java.util.Collection;
import java.util.Optional;

public final class Mining extends BasicSkillType {

    private final FireworkEffectType levelUpFirework;

    public Mining(final PluginContainer container, final LevelFunctionType levelFunction, final int maxLevel) {
        super(container, "mining", "Mining", Text.of(TextColors.AQUA, "Mining"), levelFunction, maxLevel);

        this.levelUpFirework = Sponge.getRegistry().getType(FireworkEffectType.class, VanillaSkills.ID + ":mining-level-up").orElse(null);
    }

    @Override
    public void onConfigure(final Collection<SkillHolderContainer> containers) {
        final EconomyService es = Sponge.getServiceManager().provide(EconomyService.class).orElse(null);
        final EconomyFunctionType ef = Sponge.getRegistry().getType(EconomyFunctionType.class, VanillaSkills.ID + ":standard").orElse(null);

        //@formatter:off
        containers.forEach(container -> {
            this
                .register(container,
                    registrar()
                    .addFilter(
                        CANCEL_EVENT,
                        matchTo(
                            DENY,
                            value(PROCESSING_PLAYER, Keys.GAME_MODE, GameModes.CREATIVE),
                            any(
                                matchTo(DENY, level(20), not(items(ItemTypes.STONE_PICKAXE))),
                                matchTo(DENY, level(30), not(items(ItemTypes.IRON_PICKAXE))),
                                matchTo(DENY, level(40), not(items(ItemTypes.GOLDEN_PICKAXE))),
                                matchTo(DENY, level(50), not(items(ItemTypes.DIAMOND_PICKAXE)))
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
                );

            final FilterRegistrar.Builder breakBlock =
                registrar()
                .addFilter(
                    CANCEL_TRANSACTION,
                    matchTo(
                        DENY,
                        value(PROCESSING_PLAYER, Keys.GAME_MODE, GameModes.CREATIVE),
                        any(
                            matchTo(DENY, level(10), not(blocks(BlockTypes.COAL_ORE))),
                            matchTo(DENY, level(30), not(blocks(BlockTypes.IRON_ORE))),
                            matchTo(DENY, level(35), not(blocks(BlockTypes.LAPIS_ORE))),
                            matchTo(DENY, level(40), not(blocks(BlockTypes.GOLD_ORE))),
                            matchTo(DENY, level(50), not(blocks(BlockTypes.DIAMOND_ORE))),
                            matchTo(DENY, level(60), not(blocks(BlockTypes.OBSIDIAN))),
                            matchTo(DENY, level(65), not(blocks(BlockTypes.NETHERRACK))),
                            matchTo(DENY, level(85), not(blocks(BlockTypes.END_STONE))),
                            matchTo(DENY, level(95), not(blocks(BlockTypes.EMERALD_ORE))),
                            matchTo(DENY, level(99), not(blocks(BlockTypes.LIT_REDSTONE_ORE, BlockTypes.REDSTONE_ORE)))
                        )
                    )
                )
                .addTrigger(
                    TRANSACTION,
                    triggerIf()
                    .all(not(value(PROCESSING_PLAYER, Keys.GAME_MODE, GameModes.CREATIVE)), blocks(), natural())
                    .then(
                        apply(xp(10)).when(blocks(BlockTypes.STONE, BlockTypes.END_STONE, BlockTypes.SANDSTONE)),
                        apply(xp(15)).when(blocks(BlockTypes.LAPIS_ORE, BlockTypes.NETHERRACK)),
                        apply(xp(30)).when(blocks(BlockTypes.COAL_ORE)),
                        apply(xp(60)).when(blocks(BlockTypes.IRON_ORE)),
                        apply(xp(90)).when(blocks(BlockTypes.GOLD_ORE)),
                        apply(xp(100)).when(blocks(BlockTypes.OBSIDIAN)),
                        apply(xp(120)).when(blocks(BlockTypes.REDSTONE_ORE, BlockTypes.LIT_REDSTONE_ORE)),
                        apply(xp(150)).when(blocks(BlockTypes.DIAMOND_ORE)),
                        apply(xp(170)).when(blocks(BlockTypes.EMERALD_ORE))
                    )
                    .build()
                );

            if (es != null && ef != null) {
                final Currency c = es.getDefaultCurrency();

                breakBlock
                    .addTrigger(
                        TRANSACTION,
                        triggerIf()
                        .all(not(value(PROCESSING_PLAYER, Keys.GAME_MODE, GameModes.CREATIVE)), blocks(), natural())
                        .then(
                            apply(scaledMoney(0.9, es, ef, c)).when(blocks(BlockTypes.NETHERRACK)),
                            apply(scaledMoney(1, es, ef, c)).when(blocks(BlockTypes.STONE, BlockTypes.END_STONE)),
                            apply(scaledMoney(2, es, ef, c)).when(blocks(BlockTypes.SANDSTONE)),
                            apply(scaledMoney(3, es, ef, c)).when(blocks(BlockTypes.COAL_ORE)),
                            apply(scaledMoney(4, es, ef, c)).when(blocks(BlockTypes.IRON_ORE), states(state(BlockTypes.STONEBRICK, trait(STONEBRICK_VARIANT, "stonebrick")))),
                            apply(scaledMoney(5, es, ef, c)).when(states(state(BlockTypes.STONEBRICK, trait(STONEBRICK_VARIANT, "mossy_stonebrick")), state(BlockTypes.STONEBRICK, trait(STONEBRICK_VARIANT, "cracked_stonebrick")), state(BlockTypes.STONEBRICK, trait(STONEBRICK_VARIANT, "cracked_stonebrick"))), blocks(BlockTypes.LAPIS_ORE)),
                            apply(scaledMoney(6, es, ef, c)).when(blocks(BlockTypes.GOLD_ORE)),
                            apply(scaledMoney(7, es, ef, c)).when(blocks(BlockTypes.REDSTONE_ORE, BlockTypes.LIT_REDSTONE_ORE)),
                            apply(scaledMoney(8, es, ef, c)).when(blocks(BlockTypes.DIAMOND_ORE, BlockTypes.OBSIDIAN)),
                            apply(scaledMoney(10, es, ef, c)).when(blocks(BlockTypes.EMERALD_ORE))
                        )
                        .build()
                    );
            }

            this.register(container, breakBlock.build(), CHANGE_BLOCK_BREAK);
        });
        //@formatter:on
    }

    @Override
    public Optional<FireworkEffectType> getFireworkEffectFor(final int level) {
        return Optional.ofNullable(this.levelUpFirework);
    }
}
