package com.bijlipay.util;

import org.hibernate.dialect.MySQL5Dialect;
import org.hibernate.dialect.function.SQLFunctionTemplate;
import org.hibernate.type.DateType;

public class CustomMySQL5Dialect extends MySQL5Dialect {
    public CustomMySQL5Dialect() {
        super();
        registerFunction("sub_date", new SQLFunctionTemplate(DateType.INSTANCE, "SUBDATE(?1, INTERVAL ?2 ?3)"));
    }
}

