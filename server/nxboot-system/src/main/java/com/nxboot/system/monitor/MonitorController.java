package com.nxboot.system.monitor;

import com.nxboot.common.result.R;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import oshi.SystemInfo;
import oshi.hardware.CentralProcessor;
import oshi.hardware.GlobalMemory;

import java.io.File;
import java.lang.management.ManagementFactory;
import java.lang.management.MemoryMXBean;
import java.lang.management.RuntimeMXBean;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * 服务器监控接口
 * <p>
 * CPU 使用率通过 @Scheduled 每 5 秒采样并缓存，避免请求线程阻塞。
 */
@RestController
@RequestMapping("/api/v1/system/monitor")
public class MonitorController {

    private final SystemInfo systemInfo = new SystemInfo();
    private final CentralProcessor processor = systemInfo.getHardware().getProcessor();

    /** 缓存的 CPU 使用率（百分比），由定时任务更新 */
    private volatile double cachedCpuUsage = 0.0;
    private long[] prevTicks;

    public MonitorController() {
        // 初始化首次 tick 快照
        this.prevTicks = processor.getSystemCpuLoadTicks();
    }

    /**
     * 每 5 秒采样一次 CPU 使用率，存入 cachedCpuUsage
     */
    @Scheduled(fixedRate = 5000)
    public void sampleCpuUsage() {
        double load = processor.getSystemCpuLoadBetweenTicks(prevTicks) * 100;
        this.cachedCpuUsage = Math.round(load * 10) / 10.0;
        this.prevTicks = processor.getSystemCpuLoadTicks();
    }

    @GetMapping("/server")
    @PreAuthorize("@perm.has('system:monitor:list')")
    public R<Map<String, Object>> serverInfo() {
        GlobalMemory memory = systemInfo.getHardware().getMemory();
        MemoryMXBean memoryMXBean = ManagementFactory.getMemoryMXBean();
        RuntimeMXBean runtimeMXBean = ManagementFactory.getRuntimeMXBean();

        Map<String, Object> data = new LinkedHashMap<>();

        // CPU 信息（使用缓存值，无阻塞）
        Map<String, Object> cpu = new LinkedHashMap<>();
        cpu.put("name", processor.getProcessorIdentifier().getName());
        cpu.put("cores", processor.getLogicalProcessorCount());
        cpu.put("usage", cachedCpuUsage);
        data.put("cpu", cpu);

        // 内存信息
        Map<String, Object> mem = new LinkedHashMap<>();
        long totalMem = memory.getTotal();
        long usedMem = totalMem - memory.getAvailable();
        mem.put("total", formatBytes(totalMem));
        mem.put("used", formatBytes(usedMem));
        mem.put("free", formatBytes(memory.getAvailable()));
        mem.put("usage", Math.round(usedMem * 1000.0 / totalMem) / 10.0);
        data.put("memory", mem);

        // JVM 信息
        Map<String, Object> jvm = new LinkedHashMap<>();
        long heapUsed = memoryMXBean.getHeapMemoryUsage().getUsed();
        long heapMax = memoryMXBean.getHeapMemoryUsage().getMax();
        jvm.put("heapUsed", formatBytes(heapUsed));
        jvm.put("heapMax", formatBytes(heapMax));
        jvm.put("heapUsage", Math.round(heapUsed * 1000.0 / heapMax) / 10.0);
        jvm.put("javaVersion", System.getProperty("java.version"));
        jvm.put("uptime", formatDuration(runtimeMXBean.getUptime()));
        data.put("jvm", jvm);

        // 磁盘信息
        File root = new File("/");
        Map<String, Object> disk = new LinkedHashMap<>();
        disk.put("total", formatBytes(root.getTotalSpace()));
        disk.put("used", formatBytes(root.getTotalSpace() - root.getFreeSpace()));
        disk.put("free", formatBytes(root.getFreeSpace()));
        disk.put("usage", Math.round((root.getTotalSpace() - root.getFreeSpace()) * 1000.0 / root.getTotalSpace()) / 10.0);
        data.put("disk", disk);

        return R.ok(data);
    }

    private String formatBytes(long bytes) {
        if (bytes < 1024) return bytes + " B";
        if (bytes < 1024 * 1024) return String.format("%.1f KB", bytes / 1024.0);
        if (bytes < 1024L * 1024 * 1024) return String.format("%.1f MB", bytes / (1024.0 * 1024));
        return String.format("%.1f GB", bytes / (1024.0 * 1024 * 1024));
    }

    private String formatDuration(long millis) {
        long seconds = millis / 1000;
        long hours = seconds / 3600;
        long minutes = (seconds % 3600) / 60;
        return hours + "h " + minutes + "m";
    }
}
