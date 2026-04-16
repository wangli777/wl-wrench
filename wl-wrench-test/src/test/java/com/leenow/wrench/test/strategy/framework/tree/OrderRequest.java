package com.leenow.wrench.test.strategy.framework.tree;

import lombok.*;

/**
 * @author: WangLi
 * @date: 2026/4/16 13:53
 * @description:
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@ToString
public class OrderRequest {

    private String name;
    private String address;
    private String productId;
    private Integer quantity;


}
