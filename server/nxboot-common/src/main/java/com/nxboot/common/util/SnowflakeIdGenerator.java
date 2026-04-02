package com.nxboot.common.util;

/**
 * 雪花算法 ID 生成器（简化单机版）
 * <p>
 * 结构：1位符号 + 41位时间戳 + 10位机器ID + 12位序列号
 */
public final class SnowflakeIdGenerator {

    /** 起始时间戳：2024-01-01 00:00:00 UTC */
    private static final long EPOCH = 1704067200000L;

    private static final long SEQUENCE_BITS = 12L;
    private static final long MACHINE_ID_BITS = 10L;

    private static final long MAX_SEQUENCE = ~(-1L << SEQUENCE_BITS);

    private static final long MACHINE_ID_SHIFT = SEQUENCE_BITS;
    private static final long TIMESTAMP_SHIFT = SEQUENCE_BITS + MACHINE_ID_BITS;

    private final long machineId;
    private long sequence = 0L;
    private long lastTimestamp = -1L;

    private static final SnowflakeIdGenerator INSTANCE = new SnowflakeIdGenerator(1);

    public SnowflakeIdGenerator(long machineId) {
        long maxMachineId = ~(-1L << MACHINE_ID_BITS);
        if (machineId < 0 || machineId > maxMachineId) {
            throw new IllegalArgumentException("机器ID超出范围: " + machineId);
        }
        this.machineId = machineId;
    }

    /**
     * 获取默认实例
     */
    public static SnowflakeIdGenerator getInstance() {
        return INSTANCE;
    }

    /**
     * 生成下一个 ID
     */
    public synchronized long nextId() {
        long currentTimestamp = System.currentTimeMillis();

        if (currentTimestamp < lastTimestamp) {
            throw new RuntimeException("时钟回拨，拒绝生成ID");
        }

        if (currentTimestamp == lastTimestamp) {
            sequence = (sequence + 1) & MAX_SEQUENCE;
            if (sequence == 0) {
                currentTimestamp = waitNextMillis(lastTimestamp);
            }
        } else {
            sequence = 0L;
        }

        lastTimestamp = currentTimestamp;

        return ((currentTimestamp - EPOCH) << TIMESTAMP_SHIFT)
                | (machineId << MACHINE_ID_SHIFT)
                | sequence;
    }

    private long waitNextMillis(long lastTimestamp) {
        long timestamp = System.currentTimeMillis();
        while (timestamp <= lastTimestamp) {
            timestamp = System.currentTimeMillis();
        }
        return timestamp;
    }
}
