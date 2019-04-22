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
import static org.inspirenxe.skills.api.skill.builtin.applicator.EconomyApplicators.scaledMoney;
import static org.inspirenxe.skills.api.skill.builtin.applicator.XPApplicators.xp;
import static org.inspirenxe.skills.api.skill.builtin.block.FuzzyBlockState.state;
import static org.inspirenxe.skills.api.skill.builtin.block.TraitValue.trait;
import static org.inspirenxe.skills.api.skill.builtin.filter.MatchFilterResponseToResponseFilter.matchTo;
import static org.inspirenxe.skills.api.skill.builtin.filter.applicator.ApplicatorEntry.apply;
import static org.inspirenxe.skills.api.skill.builtin.filter.applicator.TriggerFilter.triggerIf;
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
                    .cancelEvent(
                        matchTo(
                            DENY,
                            value(Keys.GAME_MODE, GameModes.CREATIVE),
                            any(
                                matchTo(DENY, not(items(ItemTypes.STONE_PICKAXE)), level(20)),
                                matchTo(DENY, not(items(ItemTypes.IRON_PICKAXE)), level(30)),
                                matchTo(DENY, not(items(ItemTypes.GOLDEN_PICKAXE)), level(40)),
                                matchTo(DENY, not(items(ItemTypes.DIAMOND_PICKAXE)), level(50))
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
                .cancelTransaction(
                    matchTo(
                        DENY,
                        value(Keys.GAME_MODE, GameModes.CREATIVE),
                        any(
                            matchTo(DENY, not(blocks(BlockTypes.COAL_ORE)), level(10)),
                            matchTo(DENY, not(blocks(BlockTypes.IRON_ORE)), level(30)),
                            matchTo(DENY, not(blocks(BlockTypes.LAPIS_ORE)), level(35)),
                            matchTo(DENY, not(blocks(BlockTypes.GOLD_ORE)), level(40)),
                            matchTo(DENY, not(blocks(BlockTypes.DIAMOND_ORE)), level(50)),
                            matchTo(DENY, not(blocks(BlockTypes.OBSIDIAN)), level(60)),
                            matchTo(DENY, not(blocks(BlockTypes.NETHERRACK)), level(65)),
                            matchTo(DENY, not(blocks(BlockTypes.END_STONE)), level(85)),
                            matchTo(DENY, not(blocks(BlockTypes.EMERALD_ORE)), level(95)),
                            matchTo(DENY, not(blocks(BlockTypes.LIT_REDSTONE_ORE, BlockTypes.REDSTONE_ORE)), level(99))
                        )
                    )
                )
                .transactionTrigger(
                    triggerIf()
                    .all(not(value(Keys.GAME_MODE, GameModes.CREATIVE)), blocks(), natural())
                    .then(
                        apply(xp(10)).when(blocks(BlockTypes.STONE)),
                        apply(xp(10)).when(blocks(BlockTypes.END_STONE)),
                        apply(xp(10)).when(blocks(BlockTypes.SANDSTONE)),
                        apply(xp(15)).when(blocks(BlockTypes.NETHERRACK)),
                        apply(xp(15)).when(blocks(BlockTypes.LAPIS_ORE)),
                        apply(xp(30)).when(blocks(BlockTypes.COAL_ORE)),
                        apply(xp(60)).when(blocks(BlockTypes.IRON_ORE)),
                        apply(xp(90)).when(blocks(BlockTypes.GOLD_ORE)),
                        apply(xp(100)).when(blocks(BlockTypes.OBSIDIAN)),
                        apply(xp(120)).when(blocks(BlockTypes.REDSTONE_ORE)),
                        apply(xp(120)).when(blocks(BlockTypes.LIT_REDSTONE_ORE)),
                        apply(xp(150)).when(blocks(BlockTypes.DIAMOND_ORE)),
                        apply(xp(170)).when(blocks(BlockTypes.EMERALD_ORE))
                    )
                    .build()
                );

            if (es != null && ef != null) {
                final Currency c = es.getDefaultCurrency();

                breakBlock
                    .transactionTrigger(
                        triggerIf()
                        .all(not(value(Keys.GAME_MODE, GameModes.CREATIVE)), blocks(), natural())
                        .then(
                            apply(scaledMoney(es, ef, c, 0.9)).when(blocks(BlockTypes.NETHERRACK)),
                            apply(scaledMoney(es, ef, c, 1)).when(blocks(BlockTypes.STONE)),
                            apply(scaledMoney(es, ef, c, 1)).when(blocks(BlockTypes.END_STONE)),
                            apply(scaledMoney(es, ef, c, 2)).when(blocks(BlockTypes.SANDSTONE)),
                            apply(scaledMoney(es, ef, c, 3)).when(blocks(BlockTypes.COAL_ORE)),
                            apply(scaledMoney(es, ef, c, 4)).when(blocks(BlockTypes.IRON_ORE)),
                            apply(scaledMoney(es, ef, c, 4)).when(states(state(BlockTypes.STONEBRICK, trait(STONEBRICK_VARIANT, "stonebrick")))),
                            apply(scaledMoney(es, ef, c, 5)).when(states(state(BlockTypes.STONEBRICK, trait(STONEBRICK_VARIANT, "mossy_stonebrick")))),
                            apply(scaledMoney(es, ef, c, 5)).when(states(state(BlockTypes.STONEBRICK, trait(STONEBRICK_VARIANT, "cracked_stonebrick")))),
                            apply(scaledMoney(es, ef, c, 5)).when(blocks(BlockTypes.LAPIS_ORE)),
                            apply(scaledMoney(es, ef, c, 6)).when(blocks(BlockTypes.GOLD_ORE)),
                            apply(scaledMoney(es, ef, c, 7)).when(blocks(BlockTypes.REDSTONE_ORE)),
                            apply(scaledMoney(es, ef, c, 7)).when(blocks(BlockTypes.LIT_REDSTONE_ORE)),
                            apply(scaledMoney(es, ef, c, 8)).when(blocks(BlockTypes.DIAMOND_ORE)),
                            apply(scaledMoney(es, ef, c, 8)).when(blocks(BlockTypes.OBSIDIAN)),
                            apply(scaledMoney(es, ef, c, 10)).when(blocks(BlockTypes.EMERALD_ORE))
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
