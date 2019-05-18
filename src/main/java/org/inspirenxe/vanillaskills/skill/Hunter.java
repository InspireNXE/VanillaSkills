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
import static net.kyori.filter.Filters.not;
import static org.inspirenxe.skills.api.skill.builtin.EventProcessors.DESTRUCT_ENTITY_DEATH;
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
import static org.inspirenxe.skills.api.skill.builtin.SkillsEventContextKeys.PROCESSING_PLAYER;
import static org.inspirenxe.skills.api.skill.builtin.TriggerRegistrarTypes.ENTITY;
import static org.inspirenxe.skills.api.skill.builtin.applicator.XPApplicators.xp;
import static org.inspirenxe.skills.api.skill.builtin.filter.MatchFilterResponseToResponseFilter.matchTo;
import static org.inspirenxe.skills.api.skill.builtin.filter.trigger.TriggerEntry.apply;
import static org.inspirenxe.skills.api.skill.builtin.filter.trigger.TriggerFilter.triggerIf;
import static org.inspirenxe.skills.api.skill.builtin.filter.data.ValueFilters.value;
import static org.inspirenxe.skills.api.skill.builtin.filter.entity.EntityFilters.entities;
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
import org.spongepowered.api.entity.EntityTypes;
import org.spongepowered.api.entity.living.player.gamemode.GameModes;
import org.spongepowered.api.item.ItemTypes;
import org.spongepowered.api.plugin.PluginContainer;
import org.spongepowered.api.service.economy.EconomyService;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.text.format.TextColors;

import java.util.Collection;
import java.util.Optional;

public final class Hunter extends BasicSkillType {

    private final FireworkEffectType levelUpFirework;

    public Hunter(final PluginContainer container, final LevelFunctionType levelFunction, final int maxLevel) {
        super(container, "hunter", "Hunter", Text.of(TextColors.DARK_RED, "Hunter"), levelFunction, maxLevel);

        this.levelUpFirework = Sponge.getRegistry().getType(FireworkEffectType.class, VanillaSkills.ID + ":hunter-level-up").orElse(null);
    }

    @Override
    protected void onConfigure(Collection<SkillHolderContainer> containers) {
        final EconomyService es = Sponge.getServiceManager().provide(EconomyService.class).orElse(null);
        final EconomyFunctionType ef = Sponge.getRegistry().getType(EconomyFunctionType.class, VanillaSkills.ID + ":standard").orElse(null);

        //@formatter:off
        containers.forEach(container -> this
            .register(container,
                registrar()
                .addFilter(
                    CANCEL_EVENT,
                    matchTo(DENY, not(items(ItemTypes.STONE_SWORD)), level(20)),
                    matchTo(DENY, not(items(ItemTypes.IRON_SWORD)), level(30)),
                    matchTo(DENY, not(items(ItemTypes.GOLDEN_SWORD)), level(40)),
                    matchTo(DENY, not(items(ItemTypes.DIAMOND_SWORD)), level(50))
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
                .addTrigger(
                    ENTITY,
                    triggerIf()
                    .all(not(value(PROCESSING_PLAYER, Keys.GAME_MODE, GameModes.CREATIVE)))
                    .then(
                        apply(xp(20)).when(entities(EntityTypes.ZOMBIE)),
                        apply(xp(30)).when(entities(EntityTypes.SPIDER)),
                        apply(xp(40)).when(entities(EntityTypes.SKELETON)),
                        apply(xp(50)).when(entities(EntityTypes.CREEPER)),
                        apply(xp(60)).when(entities(EntityTypes.CAVE_SPIDER)),
                        apply(xp(70)).when(entities(EntityTypes.WITCH)),
                        apply(xp(80)).when(entities(EntityTypes.ENDERMAN))

                    )
                    .build()
                )
                .build(),
                DESTRUCT_ENTITY_DEATH
            ));
    }

    @Override
    public Optional<FireworkEffectType> getFireworkEffectFor(final int level) {
        return Optional.ofNullable(this.levelUpFirework);
    }
}
