package com.Shakwa.user.Enum;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum Permission {
    USER_MANAGE("user:manage"),
    EMPLOYEE_MANAGE("employee:manage"),
    PRODUCT_MANAGE("product:manage"),
    SALE_MANAGE("sale:manage"),
    PURCHASE_MANAGE("purchase:manage"),
    INVENTORY_VIEW("inventory:view"),
    INVENTORY_MANAGE("inventory:manage"),
    REPORT_VIEW("report:view"),
    PERMISSION_MANAGE("permission:manage");

    private final String permission;
}
