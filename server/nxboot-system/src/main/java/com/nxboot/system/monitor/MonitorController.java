package com.nxboot.system.monitor;

import com.nxboot.common.result.R;
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
 */
@RestController
@RequestMapping("/api/v1/system/monitor")
public class MonitorController {

    @GetMapping("/server")
    @PreAuthorize("@perm.has('system:monitor:list')")
    public R<Map<String, Object>> serverInfo() {
        SystemInfo si = new SystemInfo();
        CentralProcessor processor = si.getHardware().getProcessor();
        GlobalMemory memory = si.getHardware().getMemory();
        MemoryMXBean memoryMXBean = ManagementFactory.getMemoryMXBean();
        RuntimeMXBean runtimeMXBean = ManagementFactory.getRuntimeMXBean();

        Map<String, Object> data = new LinkedHashMap<>();

        // CPU 信息
        Map<String, Object> cpu = new LinkedHashMap<>();
        cpu.put("name", processor.getProcessorIdentifier().getName());
        cpu.put("cores", processor.getLogicalProcessorCount());
        long[] prevTicks = processor.getSystemCpuLoadTicks();
        try { Thread.sleep(500); } catch (InterruptedException ignored) {}
        double cpuLoad = processor.getSystemCpuLoadBetweenTicks(prevTicks) * 100;
        cpu.put("usage", Math.round(cpuLoad * 10) / 10.0);
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
