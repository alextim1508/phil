package com.alextim.phil.tool;

import com.alextim.phil.permission.OrdersKeeper;
import com.alextim.phil.permission.ToolPermissionContext;
import org.springframework.stereotype.Component;

@Component
public record ToolContext(FileStateCache readFileState, OrdersKeeper ordersKeeper, TodoStore todos) {
    public ToolPermissionContext permissions() {
        return ordersKeeper.snapshot();
    }
}
