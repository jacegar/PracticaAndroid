package com.example.practicaandroid.util;
import android.content.Context;
import android.content.res.Resources;

public class TextResolver {

    private static final String RESOURCE_PREFIX = "res:";

    //Los ejercicios de la BD que empiezan por res: son los predefinidos, y por lo tanto deben ser traducidos
    public static String resolveTextFromDB(Context context, String textFromDb) {
        if (textFromDb == null || !textFromDb.startsWith(RESOURCE_PREFIX)) {
            return textFromDb;
        }

        String resourceName = textFromDb.substring(RESOURCE_PREFIX.length());
        return TextResolver.resolve (context, resourceName);
    }

    public static String resolve(Context context, String typeKey) {
        if (context == null || typeKey == null || typeKey.isEmpty()) {
            return typeKey;
        }

        try {
            int resourceId = context.getResources().getIdentifier(
                    typeKey,
                    "string",
                    context.getPackageName()
            );

            if (resourceId == 0) {
                return typeKey;
            }

            return context.getString(resourceId);

        } catch (Resources.NotFoundException e) {
            return typeKey;
        }
    }
}
