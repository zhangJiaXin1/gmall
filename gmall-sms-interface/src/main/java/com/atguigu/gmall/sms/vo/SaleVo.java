package com.atguigu.gmall.sms.vo;



import lombok.Data;

import java.math.BigDecimal;
import java.util.List;
@Data
public class SaleVo {
    private Long skuId;
    private Long price;

    private BigDecimal growBounds;
    private BigDecimal buyBounds;
    private List<String> work;

    private BigDecimal fullPrice;
    private BigDecimal reducePrice;
    private Integer fullAddOther;

    private Integer fullCount;
    private BigDecimal discount;
    private Integer ladderAddOther;

}
