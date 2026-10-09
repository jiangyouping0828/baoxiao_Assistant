package com.example.baoxiao;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;

/**
 * 报销助手（JYP）。
 *
 * <p>核心能力：
 * <ol>
 *   <li>员工自助查询「现行有效制度」；</li>
 *   <li>员工经授权查询本人单据；</li>
 *   <li>制度由行政专员（乔姐）维护；</li>
 *   <li>单据的业务事实由财务专员（周敏）核对。</li>
 * </ol>
 */
@SpringBootApplication
@ConfigurationPropertiesScan
public class BaoxiaoApplication {

    public static void main(String[] args) {
        SpringApplication.run(BaoxiaoApplication.class, args);
    }
}
