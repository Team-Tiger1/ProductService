package com.teamtiger.productservice.bundles.entities;
//Types of bundles(Collection of products)
import com.fasterxml.jackson.annotation.JsonFormat;

@JsonFormat(shape = JsonFormat.Shape.STRING)
public enum BundleCategory {
    BREAD_BAKED_GOODS,
    SWEET_TREATS_DESSERTS,
    MEAT_PROTEIN,
    FRUIT_VEGETABLES,
    DAIRY_EGGS,
    READY_MEALS_HOT_FOOD,
    SNACKS_SAVOURY_ITEMS,
    BREAKFAST_ITEMS,
    VEGAN_VEGETARIAN,
    DRINKS_BEVERAGES

}
