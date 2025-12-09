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
                colors.accentColor = 0xFFE0E0E0;
                colors.backgroundColor = 0xFF000000;
                colors.surfaceColor = 0xFF0A0A0A;
                colors.onSurfaceColor = 0xFFF5F5F5;
                colors.onSurfaceVariantColor = 0xFFCCCCCC;
                colors.primaryColor = 0xFFFFFFFF;
                colors.secondaryColor = 0xFF7A7A7A;
                break;
                
            case SettingsManager.THEME_MATERIAL_YOU:
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                    colors.accentColor = context.getColor(android.R.color.system_accent1_400);
                    colors.backgroundColor = context.getColor(android.R.color.system_neutral1_900);
                    colors.surfaceColor = context.getColor(android.R.color.system_neutral1_700);
                    colors.onSurfaceColor = context.getColor(android.R.color.system_neutral1_50);
                    colors.onSurfaceVariantColor = context.getColor(android.R.color.system_accent1_200);
                    colors.primaryColor = context.getColor(android.R.color.system_accent1_300);
                    colors.secondaryColor = context.getColor(android.R.color.system_accent2_300);
                } else {
                    colors.accentColor = 0xFF6EACDA;
                    colors.backgroundColor = 0xFF1C1B1F;
                    colors.surfaceColor = 0xFF2C2B2F;
                    colors.onSurfaceColor = 0xFFE6E1E5;
                    colors.onSurfaceVariantColor = 0xFFCAC4D0;
                    colors.primaryColor = 0xFF8AB4F8;
                    colors.secondaryColor = 0xFFD7BCFF;
                }
                break;

                case SettingsManager.THEME_BOZKURT:
                // Bozkurt: red + white accents
                colors.accentColor = 0xFFE53935; // vivid red
                colors.backgroundColor = 0xFF0F172A;
                colors.surfaceColor = 0xFF111827;
                colors.onSurfaceColor = Color.WHITE;
                colors.onSurfaceVariantColor = 0xFFFFCDD2; // light red/pink variant
                colors.primaryColor = Color.WHITE;
                colors.secondaryColor = 0xFFE57373; // softer red
                break;
                
            case SettingsManager.THEME_MECHA:
                // Mecha: dark blue background with violet/red accents
                colors.accentColor = 0xFFE91E63; // accent red/pink
                colors.backgroundColor = 0xFF071024; // very dark blue
                colors.surfaceColor = 0xFF0B1624; // slightly lighter dark surface
                colors.onSurfaceColor = Color.WHITE;
                colors.onSurfaceVariantColor = 0xFF9C27B0; // violet variant
                colors.primaryColor = 0xFF7C4DFF; // violet
                colors.secondaryColor = 0xFFE53935; // red
                break;
                
            case SettingsManager.THEME_ANDROID:
            default:
                colors.accentColor = 0xFF3DDC84;
                colors.backgroundColor = 0xFF073042;
                colors.surfaceColor = 0xFF0A4759;
                colors.onSurfaceColor = 0xFFFFFFFF;
                colors.onSurfaceVariantColor = 0xFFA8E6CF;
                colors.primaryColor = 0xFF3DDC84;
                colors.secondaryColor = 0xFF00D9A5;
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
