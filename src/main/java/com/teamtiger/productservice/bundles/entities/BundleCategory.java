package com.teamtiger.productservice.bundles.entities;

import com.fasterxml.jackson.annotation.JsonFormat;

@JsonFormat(shape = JsonFormat.Shape.STRING)
public enum BundleCategory {

    Bread_Baked_Goods,
    Sweet_Treats_Desserts,
    Meat_Protein,
    Fruit_Vegetables,
    Dairy_Eggs,
    Ready_Meals_Hot_Food,
    Snacks_Savoury_Items,
    Breakfast_Items,
    Vegan_Vegetarian,
    Drinks_Beverages

}
