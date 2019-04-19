package org.inspirenxe.vanillaskills.skill;

import static net.kyori.filter.FilterResponse.ALLOW;
import static net.kyori.filter.FilterResponse.DENY;
import static net.kyori.filter.Filters.any;
import static net.kyori.filter.Filters.not;
import static org.inspirenxe.skills.api.skill.builtin.EventProcessors.USER_CHANGE_BLOCK_BREAK;
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
import static org.inspirenxe.skills.api.skill.builtin.filter.block.BlockCreatorFilters.creatorTracked;
import static org.inspirenxe.skills.api.skill.builtin.filter.block.BlockCreatorFilters.natural;
import static org.inspirenxe.skills.api.skill.builtin.filter.block.BlockFilters.blocks;
import static org.inspirenxe.skills.api.skill.builtin.filter.block.BlockFilters.states;
import static org.inspirenxe.skills.api.skill.builtin.filter.data.ValueFilters.value;
import static org.inspirenxe.skills.api.skill.builtin.filter.item.ItemFilters.item;
import static org.inspirenxe.skills.api.skill.builtin.filter.level.LevelFilters.level;
import static org.spongepowered.api.block.trait.EnumTraits.LOG2_VARIANT;
import static org.spongepowered.api.block.trait.EnumTraits.LOG_VARIANT;

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

public final class Woodcutting extends BasicSkillType {

    private final FireworkEffectType levelUpFirework;

    public Woodcutting(final PluginContainer container, final LevelFunctionType levelFunction, final int maxLevel) {
        super(container, "woodcutting", "Woodcutting", Text.of(TextColors.DARK_GREEN, "Woodcutting"), levelFunction, maxLevel);

        this.levelUpFirework = Sponge.getRegistry().getType(FireworkEffectType.class, VanillaSkills.ID + ":woodcutting-level-up").orElse(null);
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
                                        matchTo(filters(whenThen(filters(item(ItemTypes.WOODEN_AXE)), ALLOW, DENY), level(10)), DENY),
                                        matchTo(filters(whenThen(filters(item(ItemTypes.STONE_AXE)), ALLOW, DENY), level(20)), DENY),
                                        matchTo(filters(whenThen(filters(item(ItemTypes.IRON_AXE)), ALLOW, DENY), level(30)), DENY),
                                        matchTo(filters(whenThen(filters(item(ItemTypes.GOLDEN_AXE)), ALLOW, DENY), level(40)), DENY),
                                        matchTo(filters(whenThen(filters(item(ItemTypes.DIAMOND_PICKAXE)), ALLOW, DENY), level(50)), DENY)
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
                );

            final FilterRegistrar.Builder breakBlock =
                registrar()
                    .cancelTransaction(
                        matchTo(
                            filters(
                                value(Keys.GAME_MODE, GameModes.CREATIVE),
                                any(
                                    matchTo(filters(whenThen(filters(states(state(BlockTypes.LOG2, trait(LOG2_VARIANT, "dark_oak")))), ALLOW, DENY), level(10)), DENY),
                                    matchTo(filters(whenThen(filters(states(state(BlockTypes.LOG, trait(LOG_VARIANT, "spruce")))), ALLOW, DENY), level(20)), DENY),
                                    matchTo(filters(whenThen(filters(states(state(BlockTypes.LOG, trait(LOG_VARIANT, "birch")))), ALLOW, DENY), level(30)), DENY),
                                    matchTo(filters(whenThen(filters(states(state(BlockTypes.LOG, trait(LOG_VARIANT, "jungle")))), ALLOW, DENY), level(40)), DENY),
                                    matchTo(filters(whenThen(filters(states(state(BlockTypes.LOG2, trait(LOG2_VARIANT, "acacia")))), ALLOW, DENY), level(50)), DENY)
                                )
                            ),
                            DENY
                        )
                    )
                    .transactionTrigger(
                        triggerIf()
                            .all(not(value(Keys.GAME_MODE, GameModes.CREATIVE)), blocks(), any(creatorTracked(), natural()))
                            .then(
                                applyIf().any(states(state(BlockTypes.LOG, trait(LOG_VARIANT, "oak")))).then(xp(5)).build(),
                                applyIf().any(states(state(BlockTypes.LOG2, trait(LOG2_VARIANT, "dark_oak")))).then(xp(7)).build(),
                                applyIf().any(states(state(BlockTypes.LOG, trait(LOG_VARIANT, "spruce")))).then(xp(9)).build(),
                                applyIf().any(states(state(BlockTypes.LOG, trait(LOG_VARIANT, "birch")))).then(xp(11)).build(),
                                applyIf().any(states(state(BlockTypes.LOG, trait(LOG_VARIANT, "jungle")))).then(xp(13)).build(),
                                applyIf().any(states(state(BlockTypes.LOG2, trait(LOG2_VARIANT, "acacia")))).then(xp(15)).build()
                            )
                            .build()
                    );


            if (es != null && ef != null) {
                final Currency c = es.getDefaultCurrency();

                breakBlock
                    .transactionTrigger(
                        triggerIf()
                            .all(not(value(Keys.GAME_MODE, GameModes.CREATIVE)), blocks(), any(creatorTracked(), natural()))
                            .then(
                                applyIf().any(states(state(BlockTypes.LOG, trait(LOG_VARIANT, "oak")))).then(scaledMoney(es, ef, c, 0.1)).build(),
                                applyIf().any(states(state(BlockTypes.LOG2, trait(LOG2_VARIANT, "dark_oak")))).then(scaledMoney(es, ef, c, 0.3)).build(),
                                applyIf().any(states(state(BlockTypes.LOG, trait(LOG_VARIANT, "spruce")))).then(scaledMoney(es, ef, c, 0.7)).build(),
                                applyIf().any(states(state(BlockTypes.LOG, trait(LOG_VARIANT, "birch")))).then(scaledMoney(es, ef, c, 0.9)).build(),
                                applyIf().any(states(state(BlockTypes.LOG, trait(LOG_VARIANT, "jungle")))).then(scaledMoney(es, ef, c, 1.1)).build(),
                                applyIf().any(states(state(BlockTypes.LOG2, trait(LOG2_VARIANT, "acacia")))).then(scaledMoney(es, ef, c, 1.3)).build()
                            )
                            .elseApply(scaledMoney(es, ef, c, 0.1))
                            .build()
                    );
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
