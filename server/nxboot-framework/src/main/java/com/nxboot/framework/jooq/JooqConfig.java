package com.nxboot.framework.jooq;

import com.nxboot.common.util.SnowflakeIdGenerator;
import org.jooq.conf.RenderNameCase;
import org.jooq.conf.RenderQuotedNames;
import org.jooq.impl.DefaultConfiguration;
import org.jooq.impl.DefaultExecuteListenerProvider;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.jooq.DefaultConfigurationCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * jOOQ 配置：设置命名风格
 * <p>
 * 注：当前所有 Repository 使用 dsl.insertInto(table).set().execute() 而非 record.store()，
 * 因此 RecordListener 不会被触发。审计字段和 ID 生成均在 Repository 中手动处理。
 * 如果未来启用 codegen 并改用 record.store()，可以恢复 AuditRecordListener 和 IdGeneratorListener。
 */
@Configuration
public class JooqConfig {

    @Bean
    public DefaultConfigurationCustomizer jooqConfigurationCustomizer() {
        return (DefaultConfiguration config) -> {
            config.settings()
                    .withRenderNameCase(RenderNameCase.LOWER)
                    .withRenderQuotedNames(RenderQuotedNames.NEVER);
            // 注册慢查询监听器
            config.set(new DefaultExecuteListenerProvider(new SlowQueryListener()));
        };
    }

    /**
     * 雪花 ID 生成器，machineId 从配置读取
     */
    @Bean
    public SnowflakeIdGenerator snowflakeIdGenerator(
            @Value("${nxboot.snowflake.machine-id:1}") long machineId) {
        return new SnowflakeIdGenerator(machineId);
    }
}
