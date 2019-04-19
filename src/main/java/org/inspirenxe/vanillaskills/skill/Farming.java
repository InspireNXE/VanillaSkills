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

import static net.kyori.filter.FilterResponse.ALLOW;
import static net.kyori.filter.FilterResponse.DENY;
import static net.kyori.filter.Filters.any;
import static net.kyori.filter.Filters.not;
import static org.inspirenxe.skills.api.skill.builtin.EventProcessors.USER_CHANGE_BLOCK_BREAK;
import static org.inspirenxe.skills.api.skill.builtin.EventProcessors.USER_CHANGE_BLOCK_PLACE;
import static org.inspirenxe.skills.api.skill.builtin.EventProcessors.USER_INTERACT_BLOCK_PRIMARY_MAIN_HAND;
import static org.inspirenxe.skills.api.skill.builtin.EventProcessors.USER_INTERACT_BLOCK_PRIMARY_OFF_HAND;
import static org.inspirenxe.skills.api.skill.builtin.EventProcessors.USER_INTERACT_BLOCK_SECONDARY_MAIN_HAND;
import static org.inspirenxe.skills.api.skill.builtin.EventProcessors.USER_INTERACT_BLOCK_SECONDARY_OFF_HAND;
import static org.inspirenxe.skills.api.skill.builtin.EventProcessors.USER_INTERACT_ITEM_PRIMARY_MAIN_HAND;
import static org.inspirenxe.skills.api.skill.builtin.EventProcessors.USER_INTERACT_ITEM_PRIMARY_OFF_HAND;
import static org.inspirenxe.skills.api.skill.builtin.EventProcessors.USER_INTERACT_ITEM_SECONDARY_MAIN_HAND;
import static org.inspirenxe.skills.api.skill.builtin.EventProcessors.USER_INTERACT_ITEM_SECONDARY_OFF_HAND;
import static org.inspirenxe.skills.api.skill.builtin.FilterRegistrar.registrar;
import static org.inspirenxe.skills.api.skill.builtin.applicator.EconomyApplicators.scaledMoney;
import static org.inspirenxe.skills.api.skill.builtin.applicator.XPApplicators.xp;
import static org.inspirenxe.skills.api.skill.builtin.block.FuzzyBlockState.state;
import static org.inspirenxe.skills.api.skill.builtin.block.TraitValue.trait;
import static org.inspirenxe.skills.api.skill.builtin.filter.Filters.filters;
import static org.inspirenxe.skills.api.skill.builtin.filter.MatchResponseToFilter.matchTo;
import static org.inspirenxe.skills.api.skill.builtin.filter.RedirectResponseFilter.whenThen;
import static org.inspirenxe.skills.api.skill.builtin.filter.applicator.ApplicatorEntry.applyIf;
import static org.inspirenxe.skills.api.skill.builtin.filter.applicator.TriggerFilter.triggerIf;
import static org.inspirenxe.skills.api.skill.builtin.filter.block.BlockCreatorFilters.creator;
import static org.inspirenxe.skills.api.skill.builtin.filter.block.BlockCreatorFilters.natural;
import static org.inspirenxe.skills.api.skill.builtin.filter.block.BlockFilters.blocks;
import static org.inspirenxe.skills.api.skill.builtin.filter.block.BlockFilters.states;
import static org.inspirenxe.skills.api.skill.builtin.filter.data.ValueFilters.value;
import static org.inspirenxe.skills.api.skill.builtin.filter.item.ItemFilters.item;
import static org.inspirenxe.skills.api.skill.builtin.filter.level.LevelFilters.level;
import static org.spongepowered.api.block.trait.IntegerTraits.CARROTS_AGE;
import static org.spongepowered.api.block.trait.IntegerTraits.WHEAT_AGE;

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

public final class Farming extends BasicSkillType {

    private final FireworkEffectType levelUpFirework;

    public Farming(final PluginContainer container, final LevelFunctionType levelFunction, final int maxLevel) {
        super(container, "farming", "Farming", Text.of(TextColors.GREEN, "Farming"), levelFunction, maxLevel);

        this.levelUpFirework = Sponge.getRegistry().getType(FireworkEffectType.class, VanillaSkills.ID + ":farming-level-up").orElse(null);
    }

    @Override
    protected void onConfigure(final Collection<SkillHolderContainer> containers) {
        final EconomyService es = Sponge.getServiceManager().provide(EconomyService.class).orElse(null);
        final EconomyFunctionType ef = Sponge.getRegistry().getType(EconomyFunctionType.class, "vanilla_skills:standard").orElse(null);

        //@formatter:off
        containers.forEach(container -> {
            this
                .register(container,
                    registrar()
                        .cancelEvent(
                            matchTo(
                                filters(
                                    value(Keys.GAME_MODE, GameModes.CREATIVE),
                                    any(
                                        matchTo(filters(whenThen(filters(item(ItemTypes.STONE_HOE)), ALLOW, DENY), level(15)), DENY),
                                        matchTo(filters(whenThen(filters(item(ItemTypes.IRON_HOE)), ALLOW, DENY), level(30)), DENY),
                                        matchTo(filters(whenThen(filters(item(ItemTypes.GOLDEN_HOE)), ALLOW, DENY), level(45)), DENY),
                                        matchTo(filters(whenThen(filters(item(ItemTypes.DIAMOND_HOE)), ALLOW, DENY), level(60)), DENY)
                                    )
                                ),
                                DENY
                            )
                        )
                        .build(),
                    USER_INTERACT_BLOCK_PRIMARY_MAIN_HAND,
                    USER_INTERACT_BLOCK_PRIMARY_OFF_HAND,
                    USER_INTERACT_BLOCK_SECONDARY_MAIN_HAND,
                    USER_INTERACT_BLOCK_SECONDARY_OFF_HAND,
                    USER_INTERACT_ITEM_PRIMARY_MAIN_HAND,
                    USER_INTERACT_ITEM_PRIMARY_OFF_HAND,
                    USER_INTERACT_ITEM_SECONDARY_MAIN_HAND,
                    USER_INTERACT_ITEM_SECONDARY_OFF_HAND
                )
            ;
            this
                .register(container, registrar()
                    .eventTrigger(
                        triggerIf()
                            .all(not(value(Keys.GAME_MODE, GameModes.CREATIVE)))
                            .then(
                                applyIf().any(blocks(BlockTypes.DIRT)).then(xp(0.10)).build()
                            )
                        .build()
                    )
                    .build(),
                    USER_INTERACT_BLOCK_SECONDARY_MAIN_HAND,
                    USER_INTERACT_BLOCK_SECONDARY_OFF_HAND
                );

            final FilterRegistrar.Builder placeBlock =
                registrar()
                    .cancelTransaction(
                        matchTo(
                            filters(
                                value(Keys.GAME_MODE, GameModes.CREATIVE),
                                any(
                                    matchTo(filters(whenThen(filters(states(state(BlockTypes.CARROTS))), ALLOW, DENY), level(10)), DENY)
                                )
                            ),
                            DENY
                        )
                    )
                    .transactionTrigger(
                        triggerIf()
                            .all(not(value(Keys.GAME_MODE, GameModes.CREATIVE)), blocks())
                            .then(
                                applyIf().any(states(state(BlockTypes.WHEAT))).then(xp(0.25)).build(),
                                applyIf().any(states(state(BlockTypes.CARROTS))).then(xp(0.50)).build()
                            )
                            .build()
                    )
                ;

            this.register(container, placeBlock.build(), USER_CHANGE_BLOCK_PLACE);

            final FilterRegistrar.Builder breakBlock =
                registrar()
                    .cancelTransaction(
                        matchTo(
                            filters(
                                value(Keys.GAME_MODE, GameModes.CREATIVE),
                                any(
                                    matchTo(filters(whenThen(filters(states(state(BlockTypes.CARROTS, trait(CARROTS_AGE, 7)))), ALLOW, DENY), level(10)), DENY)
                                )
                            ),
                            DENY
                        )
                    )
                    .transactionTrigger(
                        triggerIf()
                            .all(not(value(Keys.GAME_MODE, GameModes.CREATIVE)), blocks(), creator())
                            .then(
                                applyIf().any(states(state(BlockTypes.WHEAT, trait(WHEAT_AGE, 7)))).then(xp(10)).build(),
                                applyIf().any(states(state(BlockTypes.CARROTS, trait(CARROTS_AGE, 7)))).then(xp(10)).build()
                            )
                            .build()
                    );


            if (es != null && ef != null) {
                final Currency c = es.getDefaultCurrency();

                breakBlock
                    .transactionTrigger(
                        triggerIf()
                            .all(not(value(Keys.GAME_MODE, GameModes.CREATIVE)), blocks(), any(creator(), natural()))
                            .then(
                                applyIf().any(states(state(BlockTypes.WHEAT, trait(WHEAT_AGE, 5)))).then(scaledMoney(es, ef, c, 4)).build(),
                                applyIf().any(states(state(BlockTypes.CARROTS, trait(CARROTS_AGE, 10)))).then(scaledMoney(es, ef, c, 4)).build()
                            )
                            .elseApply(scaledMoney(es, ef, c, 0.1))
                            .build()
                    )
                ;
            }

            this.register(container, breakBlock.build(), USER_CHANGE_BLOCK_BREAK);
        });
        //@formatter:on
    }

    @Override
    public Optional<FireworkEffectType> getFireworkEffectFor(final int level) {
        return Optional.ofNullable(this.levelUpFirework);
    }
}
