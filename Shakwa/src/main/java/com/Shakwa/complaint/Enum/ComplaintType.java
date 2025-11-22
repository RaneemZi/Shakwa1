package com.Shakwa.complaint.Enum;

import com.fasterxml.jackson.annotation.JsonValue;

public enum ComplaintType {
    
    تأخر_في_إنجاز_معاملة,
    تعامل_الموظف_مقدم_الخدمة,
    تعطل_النظام_التقني,
    تعقيد_في_الإجراءات,
    رسوم_الخدمة,
    ضعف_جودة_الخدمة,
    طول_مدة_الانتظار,
    عدم_الموافقة_على_الخدمة;

    @JsonValue
    public String getLabel() {
        return this.name().replace("_", " ");
    }
}

