package com.example.practicaandroid.util;
import android.content.Context;
import android.content.res.Resources;

public class TextResolver {

    private static final String RESOURCE_PREFIX = "res:";

    //Los ejercicios de la BD que empiezan por res: son los predefinidos, y por lo tanto deben ser traducidos
    public static String resolve(Context context, String textFromDb) {
        if (textFromDb == null || !textFromDb.startsWith(RESOURCE_PREFIX)) {
            return textFromDb;
        }

        try {
            String resourceName = textFromDb.substring(RESOURCE_PREFIX.length());

            int resourceId = context.getResources().getIdentifier(
                    resourceName,
                    "string",
                    context.getPackageName()
            );

            // ID no encontrado
            if (resourceId == 0) {
                return resourceName;
            }

            return context.getString(resourceId);

        } catch (Resources.NotFoundException e) {
            return textFromDb;
        }
    }
}
