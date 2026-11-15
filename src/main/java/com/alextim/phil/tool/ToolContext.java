package com.alextim.phil.tool;

import com.alextim.phil.permission.OrdersKeeper;
import com.alextim.phil.permission.ToolPermissionContext;
import org.springframework.stereotype.Component;

@Component
public record ToolContext(FileStateCache readFileState, OrdersKeeper ordersKeeper) {
    public ToolPermissionContext permissions() {
        return ordersKeeper.snapshot();
    }
}
