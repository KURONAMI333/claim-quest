package com.kuronami.claimquest.gametest;

/**
 * Hex ids of the quests in {@code claimquest_test.snbt}, as longs for
 * {@code BaseQuestFile.get(long)}. Id-based lookup is required: the quest file
 * collects objects in hash order, so positional access to
 * {@code collect(ClaimTask.class)} is not stable once the pack has several
 * claim tasks.
 */
final class TestQuestIds {

    /** Single claim task, value 3. */
    static final long SINGLE = 0x0A0B0C0D0E0F0002L;

    /** Prerequisite quest, one claim task of value 3. */
    static final long DEP_A = 0x0A0B0C0D0E0F0010L;

    /** Depends on {@link #DEP_A}, one claim task of value 4. */
    static final long DEP_B = 0x0A0B0C0D0E0F0012L;

    /** {@code require_sequential_tasks}: two claim tasks of value 3 then 4. */
    static final long SEQUENTIAL = 0x0A0B0C0D0E0F0014L;

    private TestQuestIds() {}
}
