package com.apollo.recyclerviewitemspringanimation;

import android.view.animation.Interpolator;

/**
 * Created by lei.xiao on 2018/4/28.
 */

public class SpringInterpolator implements Interpolator {
    @Override
    public float getInterpolation(float input) {
        double d2 = Math.asin(1.0D);
        double d1 = Math.pow(2.0D, -10.0F * input);
//        float factor = 0.2f;
//        return (float)(Math.pow(2, -10 * input) * Math.sin((input - factor / 4) * (2 * Math.PI) / factor) + 1);

        return (float)(Math.sin((input - 0.0477464829275686D * d2) * 6.283185307179586D / 0.3D) * (d1 * 1.0D) + 1.0D);
    }
}
