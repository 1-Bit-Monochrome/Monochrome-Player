package com.monochrome.monochrome_player;

import android.content.Context;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.os.Build;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import com.google.android.material.card.MaterialCardView;

public class ThemeColors {
    public int accentColor;
    public int backgroundColor;
    public int surfaceColor;
    public int onSurfaceColor;
    public int onSurfaceVariantColor;
    public int primaryColor;
    public int secondaryColor;
    
    public static ThemeColors getThemeColors(Context context, String theme) {
        ThemeColors colors = new ThemeColors();
        
        switch (theme) {
            case SettingsManager.THEME_MONOCHROME:
                colors.accentColor = Color.WHITE;
                colors.backgroundColor = Color.BLACK;
                colors.surfaceColor = 0xFF1A1A1A;
                colors.onSurfaceColor = Color.WHITE;
                colors.onSurfaceVariantColor = 0xFFB0B0B0;
                colors.primaryColor = Color.WHITE;
                colors.secondaryColor = 0xFF808080;
                break;
                
            case SettingsManager.THEME_MATERIAL_YOU:
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                    colors.accentColor = context.getColor(android.R.color.system_accent1_500);
                    colors.backgroundColor = context.getColor(android.R.color.system_neutral1_900);
                    colors.surfaceColor = context.getColor(android.R.color.system_neutral1_800);
                    colors.onSurfaceColor = context.getColor(android.R.color.system_neutral1_10);
                    colors.onSurfaceVariantColor = context.getColor(android.R.color.system_neutral2_200);
                    colors.primaryColor = context.getColor(android.R.color.system_accent1_400);
                    colors.secondaryColor = context.getColor(android.R.color.system_accent2_400);
                } else {
                    // Fallback for older versions
                    colors.accentColor = 0xFF2196F3;
                    colors.backgroundColor = 0xFF0D47A1;
                    colors.surfaceColor = 0xFF1565C0;
                    colors.onSurfaceColor = Color.WHITE;
                    colors.onSurfaceVariantColor = 0xFFBBDEFB;
                    colors.primaryColor = 0xFF42A5F5;
                    colors.secondaryColor = 0xFF64B5F6;
                }
                break;
                
            case SettingsManager.THEME_ANDROID:
            default:
                colors.accentColor = 0xFF4ADE80;
                colors.backgroundColor = 0xFF0F172A;
                colors.surfaceColor = 0xFF111827;
                colors.onSurfaceColor = 0xFFE5E7EB;
                colors.onSurfaceVariantColor = 0xFF9CA3AF;
                colors.primaryColor = 0xFF4ADE80;
                colors.secondaryColor = 0xFF34D399;
                break;
        }
        
        return colors;
    }
    
    public void applyToButton(Button button) {
        if (button == null) return;
        button.setTextColor(accentColor);
        button.setBackgroundTintList(ColorStateList.valueOf(surfaceColor));
    }
    
    public void applyToTextView(TextView textView, boolean isAccent) {
        if (textView == null) return;
        textView.setTextColor(isAccent ? accentColor : onSurfaceColor);
    }
    
    public void applyToCard(MaterialCardView card) {
        if (card == null) return;
        card.setCardBackgroundColor(surfaceColor);
    }
}
