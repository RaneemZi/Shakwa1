package com.Shakwa.complaint.Enum;

import com.fasterxml.jackson.annotation.JsonValue;

public enum Governorate {
    
    دمشق,
    ريف_دمشق,
    حلب,
    حمص,
    اللاذقية,
    حماة,
    طرطوس,
    دير_الزور,
    الحسكة,
    الرقة,
    إدلب,
    السويداء,
    درعا,
    القنيطرة;

    @JsonValue
    public String getLabel() {
        return this.name().replace("_", " ");
    }
}

