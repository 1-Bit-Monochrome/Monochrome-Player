package com.monochrome.monochrome_player;

import android.content.Context;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.os.Build;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import com.google.android.material.card.MaterialCardView;
import androidx.core.graphics.ColorUtils;

public class ThemeColors {
    public int accentColor;
    public int backgroundColor;
    public int surfaceColor;
    public int listSurfaceColor;
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
                colors.listSurfaceColor = colors.surfaceColor;
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
                    colors.listSurfaceColor = colors.surfaceColor;
                    colors.onSurfaceColor = context.getColor(android.R.color.system_neutral1_50);
                    colors.onSurfaceVariantColor = context.getColor(android.R.color.system_accent1_100);
                    colors.primaryColor = context.getColor(android.R.color.system_accent1_200);
                    colors.secondaryColor = context.getColor(android.R.color.system_accent2_200);
                } else {
                    colors.accentColor = 0xFF7CB8FF; // softer blue
                    colors.backgroundColor = 0xFF11141A;
                    colors.surfaceColor = 0xFF1A1F27;
                    colors.listSurfaceColor = colors.surfaceColor;
                    colors.onSurfaceColor = 0xFFE8ECF3;
                    colors.onSurfaceVariantColor = 0xFFB7C5D9;
                    colors.primaryColor = 0xFF9CC9FF;
                    colors.secondaryColor = 0xFF8ED6C5;
                }
                break;

                case SettingsManager.THEME_BOZKURT:
                // Bozkurt: red-white palette
                colors.accentColor = 0xFFEF233C; // vivid crimson
                colors.backgroundColor = 0xFF0C0506; // deep near-black red
                colors.surfaceColor = 0xFF14080A; // richer crimson surface
                colors.listSurfaceColor = 0xCC14080A; // slight translucency for lists
                colors.onSurfaceColor = 0xFFFFF7F5; // warm white text
                colors.onSurfaceVariantColor = 0xFFFFB3B3; // soft blush for secondary
                colors.primaryColor = 0xFFFFE5E5; // light rose highlight
                colors.secondaryColor = 0xFFFF6F61; // coral secondary
                break;
                
            case SettingsManager.THEME_MECHA:
                // Mecha: dark blue background with violet/red accents
                colors.accentColor = 0xFFE91E63; // accent red/pink
                colors.backgroundColor = 0xFF071024; // very dark blue
                colors.surfaceColor = 0xFF0B1624; // slightly lighter dark surface
                colors.listSurfaceColor = ColorUtils.setAlphaComponent(colors.surfaceColor, 170); // translucent lists
                colors.onSurfaceColor = Color.WHITE;
                colors.onSurfaceVariantColor = 0xFF9C27B0; // violet variant
                colors.primaryColor = 0xFF7C4DFF; // violet
                colors.secondaryColor = 0xFFE53935; // red
                break;
                
            case SettingsManager.THEME_ANDROID:
            default:
                colors.accentColor = 0xFF53E28C; // calmer green
                colors.backgroundColor = 0xFF0C1F16; // deep android dark
                colors.surfaceColor = 0xFF122A1D;
                colors.listSurfaceColor = colors.surfaceColor;
                colors.onSurfaceColor = 0xFFE5FFF1;
                colors.onSurfaceVariantColor = 0xFF9AD9BD;
                colors.primaryColor = 0xFF6FF2A6;
                colors.secondaryColor = 0xFF2FB07F;
                break;
        }
        if (colors.listSurfaceColor == 0) {
            colors.listSurfaceColor = colors.surfaceColor;
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
