package com.musinsa.assignment.initdata;

import com.musinsa.assignment.domain.entity.Brand;
import com.musinsa.assignment.domain.entity.Category;

import java.util.HashMap;

public abstract class DataInitConstUtil {

    public static final Brand A = new Brand("A");
    public static final Brand B = new Brand("B");
    public static final Brand C = new Brand("C");
    public static final Brand D = new Brand("D");
    public static final Brand E = new Brand("E");
    public static final Brand F = new Brand("F");
    public static final Brand G = new Brand("G");
    public static final Brand H = new Brand("H");
    public static final Brand I = new Brand("I");
    
    public static final Category TOP = new Category("상의", 1);
    public static final Category OUTER = new Category("아우터", 2);
    public static final Category PANTS = new Category("바지", 3);
    public static final Category SNEAKERS = new Category("스니커즈", 4);
    public static final Category BAG = new Category("가방", 5);
    public static final Category HAT = new Category("모자", 6);
    public static final Category SOCKS = new Category("양말", 7);
    public static final Category ACCESSORIES = new Category("액세서리", 8);

    public static HashMap<Category, Integer> makeItemMapOfBrand(
            int topPrice, int outerPrice, int pantsPrice, int sneakersPrice,
            int bagPrice, int hatPrice, int socksPrice, int accessoriesPrice
    ) {
        HashMap<Category, Integer> result = new HashMap<>();
        result.put(TOP, topPrice);
        result.put(OUTER, outerPrice);
        result.put(PANTS, pantsPrice);
        result.put(SNEAKERS, sneakersPrice);
        result.put(BAG, bagPrice);
        result.put(HAT, hatPrice);
        result.put(SOCKS, socksPrice);
        result.put(ACCESSORIES, accessoriesPrice);
        return result;
    }
}
