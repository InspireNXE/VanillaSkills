package org.inspirenxe.vanillaskills.type;

import static java.util.Collections.singleton;
import static net.kyori.filter.FilterResponse.ABSTAIN;
import static net.kyori.filter.FilterResponse.DENY;
import static net.kyori.filter.Filters.all;
import static net.kyori.filter.Filters.any;
import static net.kyori.filter.Filters.not;
import static org.inspirenxe.skills.api.skill.builtin.FilterRegistrar.registrar;
import static org.inspirenxe.skills.api.skill.builtin.applicator.EconomyApplicators.scaledMoney;
import static org.inspirenxe.skills.api.skill.builtin.applicator.XPApplicators.xp;
import static org.inspirenxe.skills.api.skill.builtin.block.FuzzyBlockState.state;
import static org.inspirenxe.skills.api.skill.builtin.block.TraitValue.trait;
import static org.inspirenxe.skills.api.skill.builtin.filter.RedirectResponseFilter.whenThen;
import static org.inspirenxe.skills.api.skill.builtin.filter.applicator.ApplicatorEntry.applyIf;
import static org.inspirenxe.skills.api.skill.builtin.filter.applicator.TriggerFilter.triggerIf;
import static org.inspirenxe.skills.api.skill.builtin.filter.block.BlockCreatorFilters.natural;
import static org.inspirenxe.skills.api.skill.builtin.filter.block.BlockFilters.blocks;
import static org.inspirenxe.skills.api.skill.builtin.filter.block.BlockFilters.states;
import static org.inspirenxe.skills.api.skill.builtin.filter.item.ItemFilters.item;
import static org.inspirenxe.skills.api.skill.builtin.filter.level.LevelFilters.level;
import static org.spongepowered.api.block.trait.EnumTraits.STONEBRICK_VARIANT;

import org.inspirenxe.skills.api.effect.firework.FireworkEffectType;
import org.inspirenxe.skills.api.function.economy.EconomyFunctionType;
import org.inspirenxe.skills.api.function.level.LevelFunctionType;
import org.inspirenxe.skills.api.skill.Skill;
import org.inspirenxe.skills.api.skill.builtin.BasicSkillType;
import org.inspirenxe.skills.api.skill.builtin.EventProcessors;
import org.inspirenxe.skills.api.skill.builtin.FilterRegistrar;
import org.inspirenxe.skills.api.skill.holder.SkillHolderContainer;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.block.BlockTypes;
import org.spongepowered.api.event.cause.Cause;
import org.spongepowered.api.item.ItemTypes;
import org.spongepowered.api.plugin.PluginContainer;
import org.spongepowered.api.service.economy.EconomyService;
import org.spongepowered.api.text.Text;

import java.util.Collection;
import java.util.Optional;

public final class Mining extends BasicSkillType {

    public Mining(final PluginContainer container, final String id, final String name, final Text formattedName,
        final LevelFunctionType levelFunction, final int maxLevel) {
        super(container, id, name, formattedName, levelFunction, maxLevel);
    }

    @Override
    public void onConfigure(final Collection<SkillHolderContainer> containers) {
        final EconomyService es = Sponge.getServiceManager().provide(EconomyService.class).orElse(null);
        final EconomyFunctionType ef = Sponge.getRegistry().getType(EconomyFunctionType.class, "skills:standard").orElse(null);

        //@formatter:off
        containers.forEach(container -> {
            this
                .register(container, EventProcessors.USER_INTERACT_ITEM,
                    registrar()
                        .cancelEvent(
                            any(
                                all(whenThen(singleton(item(ItemTypes.STONE_PICKAXE)), DENY, ABSTAIN), not(level(15))),
                                all(whenThen(singleton(item(ItemTypes.IRON_PICKAXE)), DENY, ABSTAIN), not(level(30))),
                                all(whenThen(singleton(item(ItemTypes.GOLDEN_PICKAXE)), DENY, ABSTAIN), not(level(45))),
                                all(whenThen(singleton(item(ItemTypes.DIAMOND_PICKAXE)), DENY, ABSTAIN), not(level(60)))
                            )
                        )
                        .build()
                );

            final FilterRegistrar.Builder breakBlock =
                registrar()
                    .cancelTransaction(
                        any(
                            all(whenThen(singleton(blocks(BlockTypes.SANDSTONE)), DENY, ABSTAIN), not(level(10))),
                            all(whenThen(singleton(blocks(BlockTypes.COAL_ORE)), DENY, ABSTAIN), not(level(15))),
                            all(whenThen(singleton(blocks(BlockTypes.STONEBRICK)), DENY, ABSTAIN), not(level(15))),
                            all(whenThen(singleton(blocks(BlockTypes.IRON_ORE)), DENY, ABSTAIN), not(level(25))),
                            all(whenThen(singleton(blocks(BlockTypes.LAPIS_ORE)), DENY, ABSTAIN), not(level(35))),
                            all(whenThen(singleton(blocks(BlockTypes.GOLD_ORE)), DENY, ABSTAIN), not(level(45))),
                            all(whenThen(singleton(blocks(BlockTypes.DIAMOND_ORE)), DENY, ABSTAIN), not(level(55))),
                            all(whenThen(singleton(blocks(BlockTypes.OBSIDIAN)), DENY, ABSTAIN), not(level(65))),
                            all(whenThen(singleton(blocks(BlockTypes.NETHERRACK)), DENY, ABSTAIN), not(level(75))),
                            all(whenThen(singleton(blocks(BlockTypes.END_STONE)), DENY, ABSTAIN), not(level(85))),
                            all(whenThen(singleton(blocks(BlockTypes.EMERALD_ORE)), DENY, ABSTAIN), not(level(95))),
                            all(whenThen(singleton(blocks(BlockTypes.REDSTONE_ORE)), DENY, ABSTAIN), not(level(99))),
                            all(whenThen(singleton(blocks(BlockTypes.LIT_REDSTONE_ORE)), DENY, ABSTAIN), not(level(99)))
                        )
                    )
                    .transactionTrigger(
                        triggerIf()
                            .all(blocks(), natural())
                            .then(
                                applyIf().any(blocks(BlockTypes.STONE)).then(xp(5)).build(),
                                applyIf().any(blocks(BlockTypes.NETHERRACK)).then(xp(2.5)).build(),
                                applyIf().any(blocks(BlockTypes.SANDSTONE)).then(xp(4)).build(),
                                applyIf().any(blocks(BlockTypes.COAL_ORE)).then(xp(8)).build(),
                                applyIf().any(blocks(BlockTypes.IRON_ORE)).then(xp(15)).build(),
                                applyIf().any(blocks(BlockTypes.LAPIS_ORE)).then(xp(15)).build(),
                                applyIf().any(blocks(BlockTypes.GOLD_ORE)).then(xp(25)).build(),
                                applyIf().any(blocks(BlockTypes.REDSTONE_ORE)).then(xp(30)).build(),
                                applyIf().any(blocks(BlockTypes.LIT_REDSTONE_ORE)).then(xp(35)).build(),
                                applyIf().any(blocks(BlockTypes.DIAMOND_ORE)).then(xp(35)).build(),
                                applyIf().any(blocks(BlockTypes.END_STONE)).then(xp(35)).build(),
                                applyIf().any(blocks(BlockTypes.OBSIDIAN)).then(xp(40)).build(),
                                applyIf().any(blocks(BlockTypes.EMERALD_ORE)).then(xp(50)).build(),
                                applyIf().any(states(state(BlockTypes.STONEBRICK, trait(STONEBRICK_VARIANT, "stonebrick")))).then(xp(4)).build(),
                                applyIf().any(states(state(BlockTypes.STONEBRICK, trait(STONEBRICK_VARIANT, "mossy_stonebrick")))).then(xp(7)).build(),
                                applyIf().any(states(state(BlockTypes.STONEBRICK, trait(STONEBRICK_VARIANT, "cracked_stonebrick")))).then(xp(15)).build()
                            )
                            .elseApply(xp(0.1))
                            .build()
                    );


            if (es != null && ef != null) {
                breakBlock
                    .transactionTrigger(
                        triggerIf()
                            .all(blocks(), natural())
                            .then(
                                applyIf().any(blocks(BlockTypes.STONE)).then(scaledMoney(es, ef, es.getDefaultCurrency(), 1)).build(),
                                applyIf().any(blocks(BlockTypes.NETHERRACK)).then(scaledMoney(es, ef, es.getDefaultCurrency(), 0.9)).build(),
                                applyIf().any(blocks(BlockTypes.SANDSTONE)).then(scaledMoney(es, ef, es.getDefaultCurrency(), 2)).build(),
                                applyIf().any(blocks(BlockTypes.COAL_ORE)).then(scaledMoney(es, ef, es.getDefaultCurrency(), 3)).build(),
                                applyIf().any(blocks(BlockTypes.IRON_ORE)).then(scaledMoney(es, ef, es.getDefaultCurrency(), 4)).build(),
                                applyIf().any(blocks(BlockTypes.LAPIS_ORE)).then(scaledMoney(es, ef, es.getDefaultCurrency(), 5)).build(),
                                applyIf().any(blocks(BlockTypes.GOLD_ORE)).then(scaledMoney(es, ef, es.getDefaultCurrency(), 6)).build(),
                                applyIf().any(blocks(BlockTypes.REDSTONE_ORE)).then(scaledMoney(es, ef, es.getDefaultCurrency(), 7)).build(),
                                applyIf().any(blocks(BlockTypes.LIT_REDSTONE_ORE)).then(scaledMoney(es, ef, es.getDefaultCurrency(), 7)).build(),
                                applyIf().any(blocks(BlockTypes.DIAMOND_ORE)).then(scaledMoney(es, ef, es.getDefaultCurrency(), 8)).build(),
                                applyIf().any(blocks(BlockTypes.END_STONE)).then(scaledMoney(es, ef, es.getDefaultCurrency(), 1)).build(),
                                applyIf().any(blocks(BlockTypes.OBSIDIAN)).then(scaledMoney(es, ef, es.getDefaultCurrency(), 8)).build(),
                                applyIf().any(blocks(BlockTypes.EMERALD_ORE)).then(scaledMoney(es, ef, es.getDefaultCurrency(), 10)).build(),
                                applyIf().any(states(state(BlockTypes.STONEBRICK, trait(STONEBRICK_VARIANT, "stonebrick")))).then(scaledMoney(es, ef, es.getDefaultCurrency(), 4)).build(),
                                applyIf().any(states(state(BlockTypes.STONEBRICK, trait(STONEBRICK_VARIANT, "mossy_stonebrick")))).then(scaledMoney(es, ef, es.getDefaultCurrency(), 5)).build(),
                                applyIf().any(states(state(BlockTypes.STONEBRICK, trait(STONEBRICK_VARIANT, "cracked_stonebrick")))).then(scaledMoney(es, ef, es.getDefaultCurrency(), 5)).build()
                            )
                            .elseApply(scaledMoney(es, ef, es.getDefaultCurrency(), 0.1))
                            .build()
                    );
            }

            this.register(container, EventProcessors.USER_CHANGE_BLOCK_BREAK, breakBlock.build());
        });
        //@formatter:on
    }

    @Override
    public void onXPChanged(final Cause cause, final Skill skill, final double amount) {
    }

    @Override
    public void onLevelChanged(final Cause cause, final Skill skill, final int newLevel) {
    }

    @Override
    public Optional<FireworkEffectType> getFireworkEffectFor(final int level) {
        return super.getFireworkEffectFor(level);
    }
}
